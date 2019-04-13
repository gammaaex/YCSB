set key right top
set yrange[0:]
set ytics 500
set grid ytics

set style data histogram
set style histogram clustered gap 1
set style fill solid

set ylabel 'Latency [{/Symbol m}s]'
set xlabel 'Number of Inserted'

plot 'mongodb_mbp.dat' using 2:xtic(1) every 3 title 'MongoDB' linetype 1 linecolor rgb '#ff0000', \
    'bigchaindb_mbp.dat' using 2 every 3 title 'BigchainDB' linetype 1 linecolor rgb '#0000ff'

set terminal postscript eps enhanced color
set output 'plot_box_mbp.eps'
replot

pause -1 'hit Enter key'
