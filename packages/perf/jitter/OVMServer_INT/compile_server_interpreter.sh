#!/bin/bash

export OVMHOME=/groups/pces/uav_oep/exp/RTZEN/tools/OpenVM
export RTZENHOME=/users/kraman/RTZen

$OVMHOME/OpenVM_bin/bin/gen-ovm -engine=interpreter -threads=RealtimeJVM -model=MostlyCopyingRegions-B_M_F_H  -app-methods-live -classpath=$RTZENHOME/lib/RTZen.jar:$RTZENHOME/lib/perf_world.jar -main=perf.jitter.Server
#
#. ./bin/prepare.sh
#./bin/build.sh -e j2c -C RealtimeJVMConfigurator -M MostlyCopyingRegions-B_M_F_H -J app-methods-live -c /home/kraman/RTZen/lib/rteverything.jar:/home/yuez/RTZen/lib/hello_world.jar -m demo.hello.Client

