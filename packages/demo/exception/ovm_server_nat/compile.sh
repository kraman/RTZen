export OpenVM_root=~/OpenVM
export RTZen_root=~/RTZen

$OpenVM_root/OpenVM_bin/bin/gen-ovm -engine=j2c -immortal-size=100m -threads=RealtimeJVM -model=MostlyCopyingRegions-B_M_F_H -ud-reflective-methods=@methods -ud-reflective-classes=@classes -classpath=$RTZen_root/lib/RTZen.jar:$RTZen_root/lib/hello_world.jar -main=demo.hello.Server

