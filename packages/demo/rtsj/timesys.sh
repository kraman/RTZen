export PATH=$PATH:/opt/timesys/rtsj-ri/bin/
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/opt/timesys/rtsj-ri/lib/:/opt/timesys/rtsj-ri/pthreadrt
export LD_ASSUME_KERNEL=2.4.1
tjvm -Xverify:all -Ximmortal100M -Xms200M -Djava.class.path=$HOME/RTZen/classes/ -Xbootclasspath=/opt/timesys/rtsj-ri/lib/foundation.jar demo.rtsj.Test
