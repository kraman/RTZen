export RTZEN_HOME=/home/yuez/RTZen_test
java -Xbootclasspath/p:/$RTZEN_HOME/lib/RTZen.jar:/opt/mackinac/binaries/jre/lib/rt2.jar -Dorg.omg.CORBA.ORB=edu.uci.ece.zen.ORB -Dorg.omg.CORBA.ORBSingleton=edu.uci.ece.zen.ORBSingleton -XX:ImmortalSize=100M -XX:ScopedSize=100M -classpath $RTZEN_HOME/classes demo.jpl.Client2 ior2.txt 2
