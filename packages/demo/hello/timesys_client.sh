export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/opt/timesys/rtsj_ri_1.1/pthreadrt/
/opt/timesys/rtsj_ri_1.1/bin/tjvm -Xverify:all -Ximmortal100M -Xms200M -Djava.class.path=$HOME/RTZen/classes/ -Xbootclasspath=/opt/timesys/rtsj-ri/lib/foundation.jar -Djava.library.path=$LD_LIBRARY_PATH demo.hello.Client
