package edu.uci.ece.zen.orb.transport;

import javax.realtime.*;
import edu.uci.ece.zen.orb.*;

public abstract class Connector{
    public Connector(){
        connRunnable = new ConnectorRunnable();
    }

    private ConnectorRunnable connRunnable;
    public synchronized final ScopedMemory connect( String host , short port , edu.uci.ece.zen.orb.ORB orb ){
        System.out.println( "transport.Connector.connect 1" );
        ScopedMemory transportMem = orb.getScopedRegion();
        System.out.println( "transport.Connector.connect 2" );
        connRunnable.init( host , port , this , orb );
        System.out.println( "transport.Connector.connect 3" );
        transportMem.enter( connRunnable );
        System.out.println( "transport.Connector.connect 4" );
        return transportMem;
    }

    protected abstract Transport internalConnect( String host , int port , edu.uci.ece.zen.orb.ORB orb );
    
}

class ConnectorRunnable implements Runnable{
    public ConnectorRunnable(){
        host = new byte[1024];
    }

    private byte[] host;
    private int hostLen;
    private short port;
    private Connector conn;
    private ORB orb;
    public void init( String host , short port , Connector conn , ORB orb ){
        System.out.println( "ConnectorRunnable 1" );
        byte[] hostBytes = host.getBytes();
        System.out.println( "ConnectorRunnable 1-1" );
        hostLen = host.length();
        System.out.println( "ConnectorRunnable 1-2" );
        System.arraycopy( hostBytes , 0 , this.host , 0 , hostLen );
        System.out.println( "ConnectorRunnable 2" );
        this.port = port;
        System.out.println( "ConnectorRunnable 3" );
        this.conn = conn;
        System.out.println( "ConnectorRunnable 4" );
        this.orb = orb;
        System.out.println( "ConnectorRunnable 5" );
    }

    public void run(){
        System.out.println( "ConnectorRunnable.run 1" );
        int iport = 0;
        System.out.println( "ConnectorRunnable.run 2" );
        iport |= port & 0xffff;
        System.out.println( "ConnectorRunnable.run 3" );
        StringBuffer buf = new StringBuffer();
        for( int i=0;i<hostLen;i++ )
            buf.append( (char)host[i] );
        String host2 = buf.toString();
        System.out.println( host2 );
        Transport trans = conn.internalConnect( host2 , iport , orb );
        System.out.println( "ConnectorRunnable.run 4" );
        ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).setPortal( trans );
        System.out.println( "ConnectorRunnable.run 5" );
        NoHeapRealtimeThread transportThread = new NoHeapRealtimeThread(trans);
        System.out.println( "ConnectorRunnable.run 6" );
        transportThread.start();
        System.out.println( "ConnectorRunnable.run 7" );
    }
}
