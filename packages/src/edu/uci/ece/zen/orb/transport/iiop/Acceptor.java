package edu.uci.ece.zen.orb.transport.iiop;

import edu.uci.ece.zen.utils.*;

public class Acceptor extends edu.uci.ece.zen.orb.transport.Acceptor{
    private java.net.ServerSocket ssock;
    
    public Acceptor( edu.uci.ece.zen.orb.ORB orb ){
        super( orb );
        try{
            ssock = new java.net.ServerSocket();
        }catch( Exception ex ){
            ZenProperties.logger.log(
                Logger.WARN,
                "edu.uci.ece.zen.orb.transport.iiop.Acceptor",
                "<cinit>",
                "Error binding to post. " + ex.toString() );
        }
    }
    
    protected void accept(){
        try{
            Transport t = new Transport( orb , ssock.accept() );
            registerTransport( t );
        }catch( java.io.IOException ioex ){
            ZenProperties.logger.log(
                Logger.WARN,
                "edu.uci.ece.zen.orb.transport.iiop.Acceptor",
                "accept",
                "IOException occured " + ioex.toString() );
        }
    }
    
    protected void internalShutdown(){
    }
    
    public org.omg.CORBA.portable.IDLEntity getProfile( byte iiopMajorVersion , byte iiopMinorVersion ){
        return null;
    }
}
