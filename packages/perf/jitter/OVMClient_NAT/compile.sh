cp ../OVMClient_INT/methods .
cp ../OVMClient_INT/classes .

$OVM/OpenVM_bin/bin/gen-ovm -engine=j2c -immortal-size=100m -threads=RealtimeJVM -model=MostlyCopyingRegions-B_M_F_H  -ud-reflective-method=@methods -ud-reflective-class=@classes -classpath=$RTZEN_OVM/lib/RTZen.jar:$RTZEN_OVM/lib/perf_world.jar -main=perf.jitter.OVMClient

