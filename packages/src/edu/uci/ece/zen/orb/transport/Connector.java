package edu.uci.ece.zen.orb.transport;

import javax.realtime.*;
import edu.uci.ece.zen.orb.*;

public abstract class Connector{
    public Connector(){
    }

    private ConnectorRunnable connRunnable;
    public synchronized final ScopedMemory connect( String host , short port , edu.uci.ece.zen.orb.ORB orb ){
        if( connRunnable == null )
            connRunnable = new ConnectorRunnable();
        ScopedMemory transportMem = orb.getScopedRegion();
        connRunnable.init( host , port , this , orb );
        transportMem.enter( connRunnable );
        return transportMem;
    }

    protected abstract Transport internalConnect( String host , int port , edu.uci.ece.zen.orb.ORB orb );
    
}

class ConnectorRunnable implements Runnable{
    public ConnectorRunnable(){}

    private String host;
    private short port;
    private Connector conn;
    private ORB orb;
    public void init( String host , short port , Connector conn , ORB orb ){
        this.host = host;
        this.port = port;
        this.conn = conn;
        this.orb = orb;
    }

    public void run(){
        int iport = 0;
        iport |= port & 0xffff;
        Transport trans = conn.internalConnect( host , iport , orb );
        ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).setPortal( trans );
        NoHeapRealtimeThread transportThread = new NoHeapRealtimeThread(trans);
        transportThread.start();
    }
}
