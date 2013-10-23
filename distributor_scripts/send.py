from os import path, chdir
from subprocess import call
ROOT = path.dirname(path.realpath(__file__))

SERVER_FILE = 'asl_server.jar'
CLIENT_FILE = 'asl_client.jar'

LOCAL_SERVER_FILE = path.join(ROOT, 'server', SERVER_FILE)
LOCAL_CLIENT_FILE = path.join(ROOT, 'client', CLIENT_FILE)

REMOTE_SERVER_FILE = path.join(ROOT, '/root/', SERVER_FILE)
REMOTE_CLIENT_FILE = path.join(ROOT, '/root/', CLIENT_FILE)

SCP_COMMAND = "scp {} root@{}:{}"


CLIENTS = [('162.243.49.79', '10.128.19.42')]
SERVERS = [('162.243.49.78', '10.128.18.8')]
DATABASE = ('162.243.49.79', '10.128.19.42')


def build():
    # compile server file
    chdir(path.dirname(LOCAL_SERVER_FILE))
    call(['ant'])

    # compile client file
    chdir(path.dirname(LOCAL_CLIENT_FILE))
    call(['ant'])


def distribute(clients, servers):
    for cglobal, clocal in clients:
        scpcommand = SCP_COMMAND.format(LOCAL_CLIENT_FILE, cglobal, REMOTE_CLIENT_FILE)
        call(scpcommand.split(' '))

    for sglobal, slocal in servers:
        scpcommand = SCP_COMMAND.format(LOCAL_SERVER_FILE, sglobal, REMOTE_SERVER_FILE)
        call(scpcommand.split(' '))


def startservers(servers, database):
    for sglobal, slocal in servers:
        # java -jar REMOTE_SERVER_FILE database[0] DB_CONNECTIONS WORKER_THREADS
        pass


def startclients(clients, servers):
    for cglobal, clocal in servers:
        # java -jar REMOTE_CLIENT_FILE args
        pass


if __name__ == '__main__':
    build()
    distribute(clients=CLIENTS, servers=SERVERS)
    startservers(servers=SERVERS, database=DATABASE)
    startclients(clients=CLIENTS, servers=SERVERS)
