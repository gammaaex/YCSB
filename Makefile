run:
    ifdef db
		./bin/ycsb run ${db} -P z_my/workloads/workload_${record}.ini -P z_my/properties/workload_properties_${db}.dat -s > z_my/results/${db}_${record}_${count}.csv
    else
		echo "Usage: make run db=DB_NAME record=NUMBER_OF_RECORD count=NUMBER_OF_COUNT"
    endif
