export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/opt/timesys/rtsj_ri_1.1/pthreadrt/
export LD_ASSUME_KERNEL=2.4.1;
RTSJ_DIR=/opt/timesys/rtsj-ri
HOME=/home/juancol/SVN-REPO/RTZen/trunk

$RTSJ_DIR/bin/tjvm -Xverify:all -Ximmortal100M -Xms200M -Djava.class.path=$HOME/classes/ -Xbootclasspath=/opt/timesys/rtsj-ri/lib/foundation.jar -Djava.library.path=$LD_LIBRARY_PATH demo.octet_test.Server "$@"
