import re
import functools
from collections import defaultdict
from sys import exit
from os import path, chdir, mkdir
from subprocess import call, PIPE, Popen
from time import sleep, time
import logging

logger = logging.getLogger('distributor')

from droplets import (createclient, createserver, createdatabase, getclients,
                      getservers, getdatabase, DEFAULT_DROPLET_SIZE)

ROOT = path.dirname(path.realpath(__file__))

CLIENT_FILE = 'asl_client.jar'
SERVER_FILE = 'asl_server.jar'

LOCAL_CLIENT_FILE = path.join(ROOT, 'client', CLIENT_FILE)
LOCAL_SERVER_FILE = path.join(ROOT, 'server', SERVER_FILE)

REMOTE_CLIENT_FILE = path.join('/root/', CLIENT_FILE)
REMOTE_SERVER_DIR = path.join('/root/')
REMOTE_SERVER_FILE = path.join(REMOTE_SERVER_DIR, SERVER_FILE)

SERVER_CONFIG_FILE_NAME = "conf.txt"

REMOTE_TEST_LOG_FILE_PATH = "/root/test_log.txt"
REMOTE_APPLICATION_LOG_FILE_PATH = "/root/application_log.txt"


SSH_COMMAND = "ssh root@{ip}"
SCREEN_COMMAND = "screen -d -m -S {name}"

DROPLET_CREATION_TIME = 180  # seconds it takes for a droplet to be ready after creation

WAIT_INTERVAL = 5  # seconds to wait between printing status when waiting testtime


def buildjavafiles():
    # compile client file
    _antmake(LOCAL_CLIENT_FILE)
    # compile server file
    _antmake(LOCAL_SERVER_FILE)


def _antmake(f):
    chdir(path.dirname(f))
    proc = Popen(['ant'], stdout=PIPE)
    out = " ".join(line for line in proc.stdout)
    logger.debug("_antmake: {}".format(out))
    if not "build successful" in out.lower():
        logger.info("Building {file} FAILED!".format(file=path.basename(f)))
        exit(-1)
    logger.info("Building {file} SUCCEEDED!".format(file=path.basename(f)))
    chdir(ROOT)
    return True


def distributejavafiles(clients, servers):
    # distribute client files
    scpuploadfile(clients, LOCAL_CLIENT_FILE, REMOTE_CLIENT_FILE)
    # distribute server files
    scpuploadfile(servers, LOCAL_SERVER_FILE, REMOTE_SERVER_FILE)


def scpuploadfile(machines, localfile, remotefile):
    scpcmd = "scp {} root@{}:{}"
    for globalip, localip in machines:
        logger.info('Uploading {localfile} to {ip}:{serverfile}'.format(localfile=path.basename(localfile),
                                                                        ip=globalip,
                                                                        serverfile=remotefile))
        call(scpcmd.format(localfile, globalip, remotefile).split(' '))


def killallscreens(ip):
    cmd = "{ssh} killall screen".format(ssh=SSH_COMMAND.format(ip=ip))
    logger.info("Destroying all screens on {ip}".format(ip=ip))
    call(cmd.split(' '))


def scpdownloadfile(ip, remotefile, localfile):
    scpcmd = "scp root@{ip}:{remotefile} {localfile}".format(remotefile=remotefile, ip=ip,
                                                             localfile=localfile)
    call(scpcmd.split(' '), stdout=PIPE)


def startmissingmachines(numclients, numservers, numdatabases=1, size=DEFAULT_DROPLET_SIZE):
    clientsstarted = startmissingmachine(machines=getclients(), nummachines=numclients,
                                         createfun=functools.partial(createclient, size))
    logger.info("Started {} client machines.".format(clientsstarted))
    serversstarted = startmissingmachine(machines=getservers(), nummachines=numservers,
                                         createfun=functools.partial(createserver, size))
    logger.info("Started {} server machines.".format(serversstarted))
    databasestarted = startmissingmachine(machines=getdatabase(), nummachines=1,
                                          createfun=functools.partial(createdatabase, size))
    logger.info("Started {} database machines.".format(databasestarted))

    started = clientsstarted + serversstarted + databasestarted
    if started > 0:
        logger.info("Waiting {secs} seconds for {num} machines to boot..".format(secs=DROPLET_CREATION_TIME,
                                                                                 num=started))
        sleep(DROPLET_CREATION_TIME)


def startmissingmachine(machines, nummachines, createfun):
    if len(machines) < nummachines:
        for __ in xrange(nummachines - len(machines)):
            createfun()
        return nummachines - len(machines)
    return 0


def parsetestfile(testfile):
    testdesc = defaultdict(list)
    with open(testfile, 'r') as f:
        for line in f:
            line = line.strip()
            splitline = line.split('=')
            typ, args = splitline
            if typ == "testtime" or typ.startswith('num'):
                testdesc[typ] = int(args)
            elif typ == "clientargs":
                testdesc['clientargs'].append(args.split(','))
            elif typ == "serverargs":
                args = args.split(',')
                res = {}
                for arg in args:
                    arg, value = arg.split(':')
                    res[arg] = value
                testdesc['serverargs'].append(res)
    return testdesc


def serversstarttest(servers):
    logger.info("Starting test on {numservers} servers".format(numservers=len(servers)))
    for globalip, localip in servers:
        killallscreens(globalip)
        cmd = SSH_COMMAND.format(ip=globalip).split(' ')
        cmd += ["{screen} java -jar {file}".format(screen=SCREEN_COMMAND.format(name="server"),
                                                   file=REMOTE_SERVER_FILE)]
        call(cmd)


def clientsstarttest(clients, servers, testname, args):
    logger.info("Starting {numclients} clients in test '{testname}' "
                "with args '{args}'".format(numclients=len(clients), testname=testname,
                                            args=args))
    for i, client in enumerate(clients):
        # divide clients evenly across servers
        servernum = (i + 1) % len(servers)
        serverip = servers[servernum][1]
        globalip, __ = client
        killallscreens(globalip)
        cmd = SSH_COMMAND.format(ip=globalip).split(' ')
        cmd += ["{screen} java -jar {file} {serverip} "
                "{testname} {args}".format(screen=SCREEN_COMMAND.format(name="client"),
                                           file=REMOTE_CLIENT_FILE,
                                           serverip=serverip,
                                           testname=testname, args=" ".join(args))]
        logger.debug("Start clients call: {}".format(cmd))
        call(cmd)


def stoptest(*args):
    for machines in args:
        for remoteip, localip in machines:
            killallscreens(remoteip)


def updateserverconfigfile(configfile, databaseip, databasecons, workerthreads):
    res = ""
    with open(configfile, 'r') as f:
        res = f.read()
    res = re.sub("DB_SERVER_NAME\t.*?\n",
                 "DB_SERVER_NAME\t{}\n".format(databaseip), res)
    res = re.sub("DB_MAX_CONNECTIONS\t.*?\n",
                 "DB_MAX_CONNECTIONS\t{}\n".format(databasecons), res)
    res = re.sub("NUM_CLIENTREQUESTWORKER_THREADS\t.*?\n",
                 "NUM_CLIENTREQUESTWORKER_THREADS\t{}\n".format(workerthreads), res)
    logger.debug("new settings file: {}".format(res))
    with open(configfile, 'w') as f:
        f.write(res)


def fetchlogs(clients, servers, testdir, testnum=0):
    logdir = path.join(testdir, 'logs')
    for i, client in enumerate(clients):
        fetchlog(client, "client{}".format(i), testnum, logdir)

    for i, server in enumerate(servers):
        fetchlog(server, "server{}".format(i), testnum, logdir)


def fetchlog((remoteip, localip), machinetype, testnum, logdir):
    logstr = "{timestamp}_test{testnum}_{machinetype}_{logtype}.txt"
    if not path.isdir(logdir):
        mkdir(logdir)
    timestamp = int(time())
    logname = logstr.format(logtype='test', machinetype=machinetype,
                            timestamp=timestamp, testnum=testnum)
    logfile = path.join(logdir, logname)
    scpdownloadfile(remoteip, REMOTE_TEST_LOG_FILE_PATH, logfile)
    logname = logstr.format(logtype='application', machinetype=machinetype,
                            timestamp=timestamp, testnum=testnum)
    logfile = path.join(logdir, logname)
    scpdownloadfile(remoteip, REMOTE_APPLICATION_LOG_FILE_PATH, logfile)


def starttest(testname):
    testdir = path.join(ROOT, 'test-definitions', testname)
    if not path.isdir(testdir):
        logger.critical("No such test directory: {dir}".format(dir=testdir))
        return

    buildjavafiles()

    testfile = path.join(testdir, 'test.txt')
    testdesc = parsetestfile(testfile)

    numclients, numservers = testdesc.get('numclients'), testdesc.get('numservers')
    startmissingmachines(numclients=numclients,
                         numservers=numservers)
    clients = getclients()[0:numclients]
    servers = getservers()[0:numservers]

    distributejavafiles(clients=clients, servers=servers)

    serverconfigfile = path.join(testdir, SERVER_CONFIG_FILE_NAME)

    __, databaseip = getdatabase()[0]

    for i, serverarg in enumerate(testdesc.get('serverargs')):
        i += 1
        # prepare server for next test
        updateserverconfigfile(serverconfigfile, databaseip=databaseip,
                               databasecons=serverarg['databaseconnections'],
                               workerthreads=serverarg['workerthreads'])
        scpuploadfile(servers, serverconfigfile, path.join(REMOTE_SERVER_DIR, SERVER_CONFIG_FILE_NAME))
        serversstarttest(servers=servers)

        for u, clientarg in enumerate(testdesc.get('clientargs')):
            u += 1
            clientsstarttest(clients=clients,
                             servers=servers,
                             testname=testname,
                             args=clientarg)
            waittime = int(testdesc.get('testtime'))
            wait(waittime)
            stoptest(clients)
            fetchlogs(clients=clients, servers=[], testdir=testdir, testnum=i + u * 0.1)
            # wait so that we can identify test switching on the server
            wait(10)
        stoptest(servers)
        fetchlogs(clients=[], servers=servers, testdir=testdir, testnum=i)
    logger.info("Test done!")


def wait(waittime):
    logger.info("Waiting for {secs} seconds".format(secs=waittime))
    for i in range(0, waittime, WAIT_INTERVAL):
        logger.debug("Waited {} out of {}".format(i, waittime))
        sleep(WAIT_INTERVAL)
