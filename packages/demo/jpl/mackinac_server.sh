export RTZEN_HOME=/home/yuez/RTZen_test
export PATH=$MACKINAC_HOME/bin:$PATH
#java -Xbootclasspath/p:/$RTZEN_HOME/lib/RTZen.jar:/opt/mackinac/binaries/jre/lib/rt2.jar -Dorg.omg.CORBA.ORB=edu.uci.ece.zen.ORB -Dorg.omg.CORBA.ORBSingleton=edu.uci.ece.zen.ORBSingleton -XX:ImmortalSize=100M -XX:ScopedSize=100M -XX:+VMStackTraceAtRTSJException -XX:+RTSJTraceAccessChecks -XX:+RTSJTraceAssignmentChecks -XX:+RTSJAccessChecksDebug -XX:+RTSJAssignmentChecksDebug -XX:+RTSJIgnoreThrowBoundaryError -classpath $RTZEN_HOME/classes demo.poa2.Server

java -Xbootclasspath/p:/$RTZEN_HOME/lib/RTZen.jar:/opt/mackinac/binaries/jre/lib/rt2.jar -Dorg.omg.CORBA.ORB=edu.uci.ece.zen.ORB -Dorg.omg.CORBA.ORBSingleton=edu.uci.ece.zen.ORBSingleton -XX:ImmortalSize=200M -XX:ScopedSize=100M -classpath $RTZEN_HOME/classes demo.jpl.Server

