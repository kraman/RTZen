package edu.uci.ece.zen.orb.transport;

import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;

public class ConnectorRunnable implements Runnable{
    public ConnectorRunnable(){
        host = new byte[1024];
    }

    private byte[] host;
    private int hostLen;
    private short port;
    private Connector conn;
    private ORB orb;
    public void init( String host , short port , Connector conn , ORB orb ){
        System.out.println( Thread.currentThread() + "ConnectorRunnable 1" );
        byte[] hostBytes = host.getBytes();
        System.out.println( Thread.currentThread() + "ConnectorRunnable 1-1" );
        hostLen = host.length();
        System.out.println( Thread.currentThread() + "ConnectorRunnable 1-2" );
        System.arraycopy( hostBytes , 0 , this.host , 0 , hostLen );
        System.out.println( Thread.currentThread() + "ConnectorRunnable 2" );
        this.port = port;
        System.out.println( Thread.currentThread() + "ConnectorRunnable 3" );
        this.conn = conn;
        System.out.println( Thread.currentThread() + "ConnectorRunnable 4" );
        this.orb = orb;
        System.out.println( Thread.currentThread() + "ConnectorRunnable 5" );
    }

    public void run(){
        System.out.println( Thread.currentThread() + "ConnectorRunnable.run 1" );
        int iport = 0;
        System.out.println( Thread.currentThread() + "ConnectorRunnable.run 2" );
        iport |= port & 0xffff;
        System.out.println( Thread.currentThread() + "ConnectorRunnable.run 3" );
        StringBuffer buf = new StringBuffer();
        for( int i=0;i<hostLen;i++ )
            buf.append( (char)host[i] );
        String host2 = buf.toString();
        System.out.println( Thread.currentThread() + host2 );
        Transport trans = conn.internalConnect( host2 , iport , orb );
        System.out.println( Thread.currentThread() + "ConnectorRunnable.run 4" );
        ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).setPortal( trans );
        System.out.println( Thread.currentThread() + "ConnectorRunnable.run 5" );
        NoHeapRealtimeThread transportThread = new NoHeapRealtimeThread(trans);
        System.out.println( Thread.currentThread() + "ConnectorRunnable.run 6" );
        transportThread.start();
        System.out.println( Thread.currentThread() + "ConnectorRunnable.run 7" );
    }
}
