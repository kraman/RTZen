export TIMESYS=/opt/timesys
export RTZEN=/home/yuez/RTZen

export LD_LIBRARY_PATH=/opt/timesys/rtsj-ri/lib/:/opt/timesys/rtsj-ri/pthreadrt:$LD_LIBRARY_PATH
export LD_ASSUME_KERNEL=2.4.1


$TIMESYS/rtsj_ri_1.1/bin/tjvm -Xverify:all -Ximmortal100M -Xms200M -Djava.class.path=$RTZEN/classes/ -Xbootclasspath=$TIMESYS/rtsj-ri/lib/foundation.jar -Djava.library.path=$LD_LIBRARY_PATH unit.test.cdr.CDRTestClient
