import logging

from utils import buildjavafiles, distributefiles, startservers, startclients
from droplets import getdroplets, getclients, getservers, getdatabase, createclient, createserver, destroyalldroplets

logging.basicConfig(level=logging.INFO, format="%(levelname)s:%(message)s")

while(True):
    print "Choose an option"
    print "1) create client"
    print "2) create server"
    print "3) build jars"
    print "4) upload jars"
    print "5) start test <testname>"
    print "6) list available tests"
    print "7) list running droplets"

    choice = raw_input()

    if choice == "1":
        createclient()
    elif choice == "2":
        createserver()
    elif choice == "3":
        buildjavafiles()
    elif choice == "4":
        distributefiles(clients=getclients(), servers=getservers())
    elif choice == "5":
        print "not implemented :("
    elif choice == "6":
        print "not implemented :("
    elif choice == "7":
        for droplet in getdroplets():
            logging.info('{name}: ({globalip}, {localip})'.format(name=droplet.name, globalip=droplet.ip_address, localip=droplet.private_ip_address))


    print ""
    print "----------------"
    print ""
    # startservers(servers=SERVERS, database=DATABASE, logginglevel='none',
    #              db_connections=15, worker_threads=20)
    # startclients(clients=CLIENTS, server_ip=SERVERS[0][1], test_name="writetest",
    #              client_threads=20, msgs_per_client=30000)
