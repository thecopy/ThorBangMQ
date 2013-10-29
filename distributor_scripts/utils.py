from sys import exit
from os import path, chdir
from subprocess import call, PIPE, Popen
import logging

ROOT = path.dirname(path.realpath(__file__))

CLIENT_FILE = 'asl_client.jar'
SERVER_FILE = 'asl_server.jar'

LOCAL_CLIENT_FILE = path.join(ROOT, 'client', CLIENT_FILE)
LOCAL_SERVER_FILE = path.join(ROOT, 'server', SERVER_FILE)

REMOTE_CLIENT_FILE = path.join(ROOT, '/root/', CLIENT_FILE)
REMOTE_SERVER_FILE = path.join(ROOT, '/root/', SERVER_FILE)


SSH_COMMAND = "ssh root@{ip}"
SCREEN_COMMAND = "screen -d -m -S {name}"


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
        logging.info("Building {file} FAILED!".format(file=path.basename(f)))
        exit(-1)
    logging.info("Building {file} SUCCEEDED!".format(file=path.basename(f)))
    chdir(ROOT)
    return True


def distributefiles(clients, servers):
    # distribute client files
    _distributefile(clients, LOCAL_CLIENT_FILE, REMOTE_CLIENT_FILE)
    # distribute server files
    _distributefile(servers, LOCAL_SERVER_FILE, REMOTE_SERVER_FILE)


def _distributefile(machines, localfile, remotefile):
    scpcmd = "scp {} root@{}:{}"
    for globalip, localip in machines:
        logging.info('Uploading {localfile} to {ip}:{serverfile}'.format(localfile=path.basename(localfile), ip=globalip, serverfile=remotefile))
        call(scpcmd.format(localfile, globalip, remotefile).split(' '))


def killallscreens(ip):
    cmd = SSH_COMMAND.format(ip=ip).split(' ')
    cmd += "killall screen".split(' ')
    logging.info("Destroying all screens on {ip}".format(ip=ip))
    call(cmd)


def startservers(servers, database, db_connections=5, worker_threads=10, logginglevel='NONE'):
    for globalip, localip in servers:
        killallscreens(globalip)
        cmd = SSH_COMMAND.format(ip=globalip).split(' ')
        cmd += ["{screen} java -jar {file} {db_ip} {db_threads} "
                "{worker_threads} {logginglevel}".format(screen=SCREEN_COMMAND.format(name="server"),
                                                         file=REMOTE_SERVER_FILE,
                                                         db_ip=database[1],
                                                         db_threads=db_connections,
                                                         worker_threads=worker_threads,
                                                         logginglevel=logginglevel.upper())]
        logging.info(cmd)
        call(cmd)


def startclients(clients, server_ip, test_name, client_threads=5, msgs_per_client=10000):
    for globalip, localip in clients:
        killallscreens(globalip)
        cmd = SSH_COMMAND.format(ip=globalip).split(' ')
        cmd += ["{screen} java -jar {file} {server_ip} {client_threads} "
                "{msgs_per_client} {test_name}".format(screen=SCREEN_COMMAND.format(name="client"),
                                                       file=REMOTE_CLIENT_FILE,
                                                       server_ip=server_ip,
                                                       client_threads=client_threads,
                                                       msgs_per_client=msgs_per_client,
                                                       test_name=test_name)]
        call(cmd)
