package edu.uci.ece.zen.orb.transport.serial;

import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

public class Connector extends edu.uci.ece.zen.orb.transport.Connector {
    public Connector() {
    }

    protected edu.uci.ece.zen.orb.transport.Transport internalConnect(
            String host, int port, edu.uci.ece.zen.orb.ORB orb,
            edu.uci.ece.zen.orb.ORBImpl orbImpl) {
        System.err.println( "Serial transport: internalConnect() " );
	try{
		if( NativeSerialPort.instance().lock.attempt(0) ){
		    return NativeSerialPort.instance().myTransport = new Transport(orb, orbImpl);
		}else
		    return NativeSerialPort.instance().myTransport;
	}catch( Exception e ){
	    return NativeSerialPort.instance().myTransport;
	}
    }

    private static Connector _instance;

    public static Connector instance() {
        if (_instance == null) {
            try {
                _instance = (Connector) javax.realtime.ImmortalMemory
                        .instance()
                        .newInstance(
                                edu.uci.ece.zen.orb.transport.serial.Connector.class);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, Connector.class, "instance", e);
            }
        }
        return _instance;
    }
}
