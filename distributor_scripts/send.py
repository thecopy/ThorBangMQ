import logging
from os import listdir

from utils import buildjavafiles, distributejavafiles, starttest
from droplets import getdroplets, getclients, getservers, createclient, createserver, destroyalldroplets

logger = logging.getLogger('distributor')
logger.addHandler(logging.StreamHandler())
logger.setLevel(logging.DEBUG)


def main():
    while(True):
        print "Choose an option"
        print "1) create client"
        print "2) create server"
        print "3) build jars"
        print "4) upload jars"
        print "5) start test <testname> [<testid>]"
        print "6) list available tests"
        print "7) list running droplets"
        print "999) destroy all droplets <testid>"

        choice = raw_input()

        if choice == "1":
            createclient()
        elif choice == "2":
            createserver()
        elif choice == "3":
            buildjavafiles()
        elif choice == "4":
            distributejavafiles(clients=getclients(), servers=getservers())
        elif choice.startswith("5"):
            args = choice.split(' ')
            testname, testid = None, None
            if len(args) >= 2:
                testname = args[1]
            if len(args) >= 3:
                testid = args[2]

            if testname not in gettests():
                print "Invalid test name '{}'".format(testname)
                print "Please choose one of the following:"
                for i, testname in enumerate(gettests()):
                    print "{}: {}".format(i + 1, testname)
                continue
            starttest(testname, testid)
        elif choice == "6":
            for i, testname in gettests():
                print "{}: {}".format(i + 1, testname)
        elif choice == "7":
            for droplet in getdroplets():
                logger.info('{name}: ({globalip}, {localip})'.format(name=droplet.name, globalip=droplet.ip_address, localip=droplet.private_ip_address))
        elif choice.startswith("999"):
            __, testid = choice.split(' ')
            destroyalldroplets(testid)

        print ""
        print "----------------"
        print ""


def gettests():
    return listdir("test-definitions/.")

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print "Program stopped.."
