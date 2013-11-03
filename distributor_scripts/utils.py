import re
import functools
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
    out = ""
    for line in proc.stdout:
        out += line.strip()
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
    cmd = SSH_COMMAND.format(ip=ip).split(' ')
    cmd += "killall screen".split(' ')
    logger.info("Destroying all screens on {ip}".format(ip=ip))
    call(cmd)


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
    testdesc = {}
    with open(testfile, 'r') as f:
        for line in f:
            line = line.strip()
            splitline = line.split('=')
            typ = splitline[0]
            if typ == "testtime" or typ.startswith('num'):
                testdesc[typ] = int(splitline[1])
            elif typ == "clientargs":
                testdesc['clientargs'] = splitline[1].split(',')
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
    logger.info("Starting {numclients} clients in test '{testname}' ".format(numclients=len(clients),
                                                                             testname=testname))
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
        for machine in machines:
            killallscreens(machine[0])


def updateserverconfigfile(configfile, databaseip):
    res = ""
    with open(configfile, 'r') as f:
        res = f.read()
    res = re.sub("DB_SERVER_NAME\t.*?\n", "DB_SERVER_NAME\t{}\n".format(databaseip), res)
    logger.debug("new settings file: {}".format(res))
    with open(configfile, 'w') as f:
        f.write(res)


def fetchlogs(clients, servers, testdir):
    logdir = path.join(testdir, 'logs')
    for i, client in enumerate(clients):
        fetchlog(client, "client_{}".format(i), logdir)

    for i, server in enumerate(servers):
        fetchlog(server, "server_{}".format(i), logdir)


def fetchlog(machine, machinetype, logdir):
    if not path.isdir(logdir):
        mkdir(logdir)
    timestamp = int(time())
    logname = '{logtype}_{machinetype}__{timestamp}.txt'.format(logtype='test',
                                                                machinetype=machinetype,
                                                                timestamp=timestamp)
    logfile = path.join(logdir, logname)
    scpdownloadfile(machine[0], REMOTE_TEST_LOG_FILE_PATH, logfile)
    logname = '{logtype}_{machinetype}__{timestamp}.txt'.format(logtype='application',
                                                                machinetype=machinetype,
                                                                timestamp=timestamp)
    logfile = path.join(logdir, logname)
    scpdownloadfile(machine[0], REMOTE_APPLICATION_LOG_FILE_PATH, logfile)


def starttest(testname):
    testdir = path.join(ROOT, 'test-definitions', testname)
    if not path.isdir(testdir):
        logger.critical("No such test directory: {dir}".format(dir=testdir))
        return

    buildjavafiles()

    testfile = path.join(testdir, 'test.txt')
    testdesc = parsetestfile(testfile)

    startmissingmachines(numclients=testdesc.get('numclients'),
                         numservers=testdesc.get('numservers'))
    clients = getclients()
    servers = getservers()

    distributejavafiles(clients=clients, servers=servers)

    serverconfigfile = path.join(testdir, SERVER_CONFIG_FILE_NAME)
    updateserverconfigfile(serverconfigfile, getdatabase()[0][1])
    scpuploadfile(servers, serverconfigfile, path.join(REMOTE_SERVER_DIR, SERVER_CONFIG_FILE_NAME))

    serversstarttest(servers=servers)
    clientsstarttest(clients=clients,
                     servers=servers,
                     testname=testname,
                     args=testdesc.get('clientargs'))

    # wait until we stop the test
    waittime = int(testdesc.get('testtime'))
    logger.info("Waiting for {secs} seconds".format(secs=waittime))
    for i in range(0, waittime, WAIT_INTERVAL):
        logger.info("Waited {} out of {}".format(i, waittime))
        sleep(WAIT_INTERVAL)

    stoptest(clients, servers)

    fetchlogs(clients=clients, servers=servers, testdir=testdir)
    logger.info("Test done!")
