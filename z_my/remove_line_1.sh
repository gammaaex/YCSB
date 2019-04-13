#!bin/sh

for RECORD in 1000 10000 100000; do
  for FILE in $(ls results | grep mongodb_${RECORD}_); do
    sed -i.bak '1d' results/$FILE
  done
done
