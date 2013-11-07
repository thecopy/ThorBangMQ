import re
from os import path, listdir
from subprocess import call

GNUPLOT = "gnuplot.sh"
TEST_FILE_REGEX = re.compile(".*?server\d+_test.txt$")


def doplots(logdir):
    testdir = path.dirname(path.dirname(path.realpath(logdir)))
    gnuplotfile = path.join(testdir, GNUPLOT)
    print "gnuplot", gnuplotfile
    if not path.exists(gnuplotfile):
        print "No gnuplot.sh found in {}!".format(testdir)
        return False
    for f in listdir(logdir + "/."):
        fpath = path.realpath(path.join(logdir, f))
        print f
        if TEST_FILE_REGEX.match(f):
            call([gnuplotfile, fpath, path.join(logdir, "graph_" + f)])

if __name__ == '__main__':
    doplots('test-definitions/sendAndPopSameClient/logs/2013-11-07__21_34_58')
