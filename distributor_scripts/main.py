import logging
from os import listdir, path

from infrastructure import buildjavafiles, distributejavafiles, starttest, killallscreens, fetchlogs
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
        print "4) upload jars <testid>"
        print "5) start test <testname> <testid> [<testRunName>]"
        print "6) list available tests"
        print "7) list running droplets"
        print "8) kill screens <testid>"
        print "9) fetch logs <testid>"
        print "999) destroy all droplets <testid>"

        choice = raw_input()

        if choice == "1":
            createclient()
        elif choice == "2":
            createserver()
        elif choice == "3":
            buildjavafiles()
        elif choice.startswith("4"):
            testid = choice.split(' ')[1]
            distributejavafiles(clients=getclients(testid), servers=getservers(testid))
        elif choice.startswith("5"):
            args = choice.split(' ')
            testname, testid = None, None
            if len(args) >= 2:
                testname = args[1]
            if len(args) >= 3:
                testid = args[2]
            if len(args) >= 4:
                testRunName = args[3]

            if testname not in gettests():
                print "Invalid test name '{}'".format(testname)
                print "Please choose one of the following:"
                for i, testname in enumerate(gettests()):
                    print "{}: {}".format(i + 1, testname)
                continue
            starttest(testname, testid, testRunName)
        elif choice == "6":
            for testname in gettests():
                print testname
        elif choice == "7":
            for i, droplet in enumerate(getdroplets()):
                logger.info('{number}: {name}: ({globalip}, {localip})'.format(number=i, name=droplet.name, globalip=droplet.ip_address, localip=droplet.private_ip_address))
        elif choice.startswith("8"):
            __, testid = choice.split(' ')
            for client in getclients(testid):
                print "killing screns on {}".format(client[0])
                killallscreens(client[0])
            for server in getservers(testid):
                print "killing screns on {}".format(client[0])
                killallscreens(server[0])
        elif choice.startswith("9") and not choice.startswith("999"):
            __, testid = choice.split(' ')
            servers, clients = getservers(testid), getclients(testid)
            logdir = path.dirname(path.realpath(__file__))
            fetchlogs(clients=clients, servers=servers, logdir=logdir)
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
