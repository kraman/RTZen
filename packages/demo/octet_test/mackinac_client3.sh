export RTZEN_HOME=/home/yuez/RTZen

psradm -i 0
psrset -d 1
psrset -c
psrset -a 1 0

psrset -e 1 java -Xint -Xbootclasspath/p:/$RTZEN_HOME/classes:/opt/mackinac/binaries/jre/lib/rt2.jar -Dorg.omg.CORBA.ORB=edu.uci.ece.zen.ORB -Dorg.omg.CORBA.ORBSingleton=edu.uci.ece.zen.ORBSingleton -XX:ImmortalSize=200M -XX:ScopedSize=200M -classpath $RTZEN_HOME/classes -XX:+RTSJIgnoreThrowBoundaryError demo.jpl.Client3 $@ 
#java -verbose -Xint -Xbootclasspath/p:/$RTZEN_HOME/classes:/opt/mackinac/binaries/jre/lib/rt2.jar -Dorg.omg.CORBA.ORB=edu.uci.ece.zen.ORB -Dorg.omg.CORBA.ORBSingleton=edu.uci.ece.zen.ORBSingleton -XX:ImmortalSize=100M -XX:ScopedSize=100M -classpath $RTZEN_HOME/classes demo.jpl.Client3

