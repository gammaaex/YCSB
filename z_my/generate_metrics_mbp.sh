#!bin/sh

for DB in bigchaindb mongodb; do
  OUTPUT_FILE=${DB}_mbp.dat
  printf "" > $OUTPUT_FILE

  for RECORD in 1000 10000 100000; do
    INDEX=0
    SUM_AVERAGE_LATENCY=0
    MIN_AVERAGE_LATENCY=2147483647
    MAX_AVERAGE_LATENCY=0

    for FILE in $(ls results_mbp | grep ${DB}_${RECORD}_); do
      INDEX=$(echo "$INDEX + 1" | bc)
      # head -n 行番号 対象ファイル | tail -n 1 | cut -d 区切り文字 -f 表示する列
      AVERAGE_LATENCY=$(head -n 20 results_mbp/$FILE | tail -n 1 | cut -d ' ' -f 3)

      SUM_AVERAGE_LATENCY=$(echo "$SUM_AVERAGE_LATENCY + $AVERAGE_LATENCY" | bc)

      if [ `echo "$AVERAGE_LATENCY < $MIN_AVERAGE_LATENCY" | bc` == 1 ] ; then
        MIN_AVERAGE_LATENCY=$AVERAGE_LATENCY
      fi

      if [ `echo "$AVERAGE_LATENCY > $MAX_AVERAGE_LATENCY" | bc` == 1 ] ; then
        MAX_AVERAGE_LATENCY=$AVERAGE_LATENCY
      fi
    done
    AVERAGE_AVERAGE_LATENCY=$(echo "$SUM_AVERAGE_LATENCY / $INDEX" | bc)
 
    echo "$RECORD $AVERAGE_AVERAGE_LATENCY $MIN_AVERAGE_LATENCY $MAX_AVERAGE_LATENCY" >> $OUTPUT_FILE
  done
done

