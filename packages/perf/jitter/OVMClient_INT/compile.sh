
$OVM/OpenVM_bin/bin/gen-ovm -engine=interpreter -immortal-size=100m -threads=RealtimeJVM -model=MostlyCopyingRegions-B_M_F_H  -opt=run -io=SIGIOSockets_PollingOther -app-methods-live -reflective-method-trace=methods -reflective-class-trace=classes -classpath=$RTZEN_OVM/lib/RTZen.jar:$RTZEN_OVM/lib/perf_world.jar -main=perf.jitter.OVMClient

