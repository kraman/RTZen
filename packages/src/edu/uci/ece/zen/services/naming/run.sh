export RTZEN=/home/yuez/RTZen

java -Xbootclasspath/p:$RTZen/lib/RTZen.jar:$RTZen/lib/omg.jar -Dorg.omg.CORBA.ORBClass=edu.uci.ece.zen.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=edu.uci.ece.zen.orb.ORBSingleton edu.uci.ece.zen.services.naming.NamingService
