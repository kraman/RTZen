package edu.uci.ece.zen.orb.transport.serial;

import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

public class Connector extends edu.uci.ece.zen.orb.transport.Connector {

    byte[] magic = new byte[89];

    public Connector() {
	magic[0]=2;
	magic[1]=1;
	magic[2]=7;
	magic[3]=7;
    }

    protected edu.uci.ece.zen.orb.transport.Transport internalConnect(
            String host, int port, edu.uci.ece.zen.orb.ORB orb,
            edu.uci.ece.zen.orb.ORBImpl orbImpl) {
        System.err.println( "Serial transport: internalConnect() " );
        
        try{
            if( !NativeSerialPort.instance().lock.attempt(0) ){
                ZenProperties.logger.log("------------------------------ Returning null transport in SERIAL connector."); 
                return null; 
            }
	    NativeSerialPort.instance().setMessage( magic , 89 );
        }catch(Exception ie){
            ie.printStackTrace();
        }
                    
        return new Transport(orb, orbImpl);
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
