export RTZEN_HOME=/home/yuez/RTZen_newrepo

java -Xint -Xbootclasspath/p:/$RTZEN_HOME/lib/RTZen.jar:/opt/mackinac/binaries/jre/lib/rt2.jar -Dorg.omg.CORBA.ORB=edu.uci.ece.zen.ORB -Dorg.omg.CORBA.ORBSingleton=edu.uci.ece.zen.ORBSingleton -XX:ImmortalSize=200M -XX:ScopedSize=100M -classpath $RTZEN_HOME/classes demo.jpl.Server

