export RTZEN_HOME=/home/yuez/RTZen

psradm -i 0
psrset -d 1
psrset -c
psrset -a 1 0

#java -verbose -Xint -Xbootclasspath/p:/$RTZEN_HOME/classes:/opt/mackinac/binaries/jre/lib/rt2.jar -Dorg.omg.CORBA.ORB=edu.uci.ece.zen.ORB -Dorg.omg.CORBA.ORBSingleton=edu.uci.ece.zen.ORBSingleton -XX:ImmortalSize=200M -XX:ScopedSize=100M -classpath $RTZEN_HOME/classes demo.jpl.Server

psrset -e 1 java -Xint -Xbootclasspath/p:/$RTZEN_HOME/classes:/opt/mackinac/binaries/jre/lib/rt2.jar -Dorg.omg.CORBA.ORB=edu.uci.ece.zen.ORB -Dorg.omg.CORBA.ORBSingleton=edu.uci.ece.zen.ORBSingleton -XX:ImmortalSize=300M -XX:ScopedSize=200M -XX:+RTSJIgnoreThrowBoundaryError -classpath $RTZEN_HOME/classes demo.jpl.Server -pm cp -lp 10000 -st 5 

