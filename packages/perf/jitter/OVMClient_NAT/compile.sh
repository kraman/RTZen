cp ../OVMClient_INT/methods .
cp ../OVMClient_INT/classes .

$OVM/OpenVM_bin/bin/gen-ovm -engine=j2c -immortal-size=100m -threads=RealtimeJVM -model=MostlyCopyingRegions-B_M_F_H  -opt=run -io=SIGIOSockets_PollingOther -ud-reflective-methods=@methods -ud-reflective-classes=@classes -classpath=$RTZEN_OVM/lib/RTZen.jar:$RTZEN_OVM/lib/perf_world.jar -main=perf.jitter.Client

