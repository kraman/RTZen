export TAO_id=2
export RTZen_home=/home/yuez/RTZen
./client -l 4
mv timeRecords.txt timeRecords.$TAO_id.4.txt
java -classpath $RTZen_home/classes perf.jitter.DataProcTao timeRecords.$TAO_id.4.txt
./client -l 16 
mv timeRecords.txt timeRecords.$TAO_id.16.txt
java -classpath $RTZen_home/classes perf.jitter.DataProcTao timeRecords.$TAO_id.16.txt
./client -l 64
mv timeRecords.txt timeRecords.$TAO_id.64.txt
java -classpath $RTZen_home/classes perf.jitter.DataProcTao timeRecords.$TAO_id.64.txt
./client -l 128
mv timeRecords.txt timeRecords.$TAO_id.128.txt
java -classpath $RTZen_home/classes perf.jitter.DataProcTao timeRecords.$TAO_id.128.txt
./client -l 256
mv timeRecords.txt timeRecords.$TAO_id.256.txt
java -classpath $RTZen_home/classes perf.jitter.DataProcTao timeRecords.$TAO_id.256.txt
./client -l 1024
mv timeRecords.txt timeRecords.$TAO_id.1024.txt
java -classpath $RTZen_home/classes perf.jitter.DataProcTao timeRecords.$TAO_id.1024.txt
./client -l 4096
mv timeRecords.txt timeRecords.$TAO_id.4096.txt
java -classpath $RTZen_home/classes perf.jitter.DataProcTao timeRecords.$TAO_id.4096.txt




