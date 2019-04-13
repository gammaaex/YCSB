set xrange [800:130000]
set logscale x

plot 'mongodb_mbp.dat' using 1:2:3:4 with errorbars linetype -1 pointtype 6 linecolor rgbcolor "#ff0000",\
    'mongodb_mbp.dat' using 1:2 with lines notitle linetype -1 linecolor rgbcolor "#ff0000",\
    'bigchaindb_mbp.dat' using 1:2:3:4 with errorbars pointtype 6 linecolor rgbcolor "#0000ff",\
    'bigchaindb_mbp.dat' using 1:2 with lines notitle linecolor rgbcolor "#0000ff"

set terminal postscript eps
set output 'plot_mbp.eps'
replot

pause -1 "hit Enter key"
