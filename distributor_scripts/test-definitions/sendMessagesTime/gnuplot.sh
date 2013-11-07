#!/bin/bash
gnuplot << EOF
testfile = "$1";
set terminal png
set output '$2.png'
set xrange [0:]
set yrange [0:]
set datafile separator ",";
set xlabel 'Time since test started (milliseconds)';
set ylabel 'Number of messages';
plot testfile using 1:2 with lines title "SendMessage";
EOF
