export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/opt/timesys/rtsj-ri/pthreadrt
export LD_ASSUME_KERNEL=2.4.1;
RTSJ_DIR=/opt/timesys/rtsj-ri
HOME=/home/hojjat/SVN-REPO/RTZen/trunk

$RTSJ_DIR/bin/tjvm -Xverify:all -Ximmortal100M -Xms200M -Djava.class.path=$HOME/classes/ -Xbootclasspath=/opt/timesys/rtsj-ri/lib/foundation.jar -Djava.library.path=$LD_LIBRARY_PATH demo.params.Client "$@"
