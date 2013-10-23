from os import path, chdir
from subprocess import call

from droplets import getclients, getservers, getdatabase
ROOT = path.dirname(path.realpath(__file__))

SERVER_FILE = 'asl_server.jar'
CLIENT_FILE = 'asl_client.jar'

LOCAL_SERVER_FILE = path.join(ROOT, 'server', SERVER_FILE)
LOCAL_CLIENT_FILE = path.join(ROOT, 'client', CLIENT_FILE)

REMOTE_SERVER_FILE = path.join(ROOT, '/root/', SERVER_FILE)
REMOTE_CLIENT_FILE = path.join(ROOT, '/root/', CLIENT_FILE)

CLIENTS = getclients()  # [('162.243.49.79', '10.128.19.42')]
SERVERS = getservers()  # [('162.243.49.78', '10.128.18.8')]
DATABASE = getdatabase()  # ('162.243.49.79', '10.128.19.42')


def build():
    # compile server file
    chdir(path.dirname(LOCAL_SERVER_FILE))
    call(['ant'])

    # compile client file
    chdir(path.dirname(LOCAL_CLIENT_FILE))
    call(['ant'])

    chdir(ROOT)


def distribute(clients, servers):
    scp_command = "scp {} root@{}:{}"
    for cglobal, clocal in clients:
        scpclient = scp_command.format(LOCAL_CLIENT_FILE, cglobal, REMOTE_CLIENT_FILE)
        call(scpclient.split(' '))

    for sglobal, slocal in servers:
        scpserver = scp_command.format(LOCAL_SERVER_FILE, sglobal, REMOTE_SERVER_FILE)
        call(scpserver.split(' '))


def startservers(servers, database, db_connections=5, worker_threads=10):
    for sglobal, slocal in servers:
        cmd = ("screen -d -m -S server java -jar {file} "
               "{db_ip} {db_threads} {worker_threads}".format(file=REMOTE_SERVER_FILE,
                                                              db_ip=database[1],
                                                              db_threads=db_connections,
                                                              worker_threads=worker_threads))
        print cmd


def startclients(clients, server_ip, worker_threads=5, msgs_per_client=10000):
    for cglobal, clocal in clients:
        cmd = ("screen -d -m -S client java -jar {file} {server_ip} "
               "{worker_threads} {msgs_per_client}".format(file=REMOTE_CLIENT_FILE,
                                                           server_ip=server_ip,
                                                           worker_threads=worker_threads,
                                                           msgs_per_client=msgs_per_client))
        print cmd


if __name__ == '__main__':
    build()
    distribute(clients=CLIENTS, servers=SERVERS)
    startservers(servers=SERVERS, database=DATABASE)
    startclients(clients=CLIENTS, server_ip=SERVERS[0][1])