export OpenVM_root=~/OpenVM
export RTZen_root=~/RTZen

$OpenVM_root/OpenVM_bin/bin/gen-ovm -engine=interpreter -immortal-size=100m -threads=RealtimeJVM -model=MostlyCopyingRegions-B_M_F_H -app-methods-live -reflective-method-trace=methods -reflective-class-trace=classes -classpath=$RTZen_root/lib/RTZen.jar:$RTZen_root/lib/hello_world.jar -main=demo.hello.Client

