 
export OVMHOME=/groups/pces/uav_oep/exp/RTZEN/tools/OpenVM
export RTZENHOME=/users/kraman/RTZen

$OVMHOME/OpenVM_bin/bin/gen-ovm -engine=j2c -immortal-size=100m -threads=RealtimeJVM -model=MostlyCopyingRegions-B_M_F_H  -reflective-method-trace=methods -reflective-class-trace=classes -classpath=$RTZENHOME/lib/RTZen.jar:$RTZENHOME/lib/perf_world.jar -main=perf.jitter.OVMClient

