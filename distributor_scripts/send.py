import logging

from utils import buildjavafiles, distributefiles, startservers, startclients
from droplets import droplets, getclients, getservers, getdatabase

logging.basicConfig(level=logging.INFO)

CLIENTS = getclients()
SERVERS = getservers()
DATABASE = getdatabase()

for droplet in droplets:
    logging.info('{name}: ({globalip}, {localip})'.format(name=droplet.name, globalip=droplet.ip_address, localip=droplet.private_ip_address))
buildjavafiles()
distributefiles(clients=CLIENTS, servers=SERVERS)
startservers(servers=SERVERS, database=DATABASE, logginglevel='none',
             db_connections=15, worker_threads=20)
startclients(clients=CLIENTS, server_ip=SERVERS[0][1], test_name="writetest",
             client_threads=20, msgs_per_client=30000)
