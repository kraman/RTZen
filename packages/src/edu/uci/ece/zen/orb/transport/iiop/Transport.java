package edu.uci.ece.zen.orb.transport.iiop;

import edu.uci.ece.zen.utils.*;

public class Transport extends edu.uci.ece.zen.orb.transport.Transport{
    private java.net.Socket sock;

    public Transport( edu.uci.ece.zen.orb.ORB orb , java.net.Socket sock ){
        super( orb );
        sock = sock;
    }
    
    public Transport( edu.uci.ece.zen.orb.ORB orb , String host , int port ){
        super( orb );
        try{
            System.out.println( "Connecting to " + host + ":"+port );
            sock = new java.net.Socket( java.net.InetAddress.getByName( host ) , port );
        }catch( Exception ex ){
            ZenProperties.logger.log(
                Logger.WARN,
                "edu.uci.ece.zen.orb.transport.iiop.Transport",
                "<cinit>",
                "Error connecting to remote location. " + ex.toString() );
        }
    }
    public java.io.InputStream getInputStream(){
        try{
            return sock.getInputStream();
        }catch( java.io.IOException ioex ){
            ZenProperties.logger.log(
                Logger.WARN,
                "edu.uci.ece.zen.orb.transport.iiop.Transport",
                "getInputStream",
                "Error getting stream. " + ioex.toString() );
            return null;
        }
    }
    public java.io.OutputStream getOutputStream(){
        try{
            return sock.getOutputStream();
        }catch( java.io.IOException ioex ){
            ZenProperties.logger.log(
                Logger.WARN,
                "edu.uci.ece.zen.orb.transport.iiop.Transport",
                "getInputStream",
                "Error getting stream. " + ioex.toString() );
            return null;
        }
    }
}
