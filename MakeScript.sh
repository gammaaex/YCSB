DB=bigchaindb

#for RECORD in 1000 10000 100000
for RECORD in 100000
do
	for COUNT in `seq 1 10`
	do
		echo --------------------------------------------------
		echo Start: Record number is $RECORD, count is $COUNT.
		echo --------------------------------------------------
		cd /Users/admin/Desktop/bigchaindb
		docker-compose down
		echo 
		docker-compose up -d $DB
		sleep 10
		cd /Users/admin/Documents/Sourcetree/YCSB
		make run db=$DB record=$RECORD count=$COUNT
		echo --------------------------------------------------
		echo Finish: Record number is $RECORD, count is $COUNT.
		echo --------------------------------------------------
	done
done
