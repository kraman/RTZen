package edu.uci.ece.zen.orb.transport.iiop;

public class Connector extends edu.uci.ece.zen.orb.transport.Connector{
    public Connector(){
    }
    
    protected edu.uci.ece.zen.orb.transport.Transport internalConnect( String host , int port , edu.uci.ece.zen.orb.ORB orb ){
        return new Transport( orb , host , port );
    }

    private static Connector _instance;
    public static Connector instance(){
        if( _instance == null )
            _instance = new Connector();
        return _instance;
    }

}
