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
        //byte[] hostBytes = new byte[]{'1','2','8','.','1','9','5','.','1','7','4','.','3','4'};
        byte[] tmpBytes = host.getBytes();
        byte[] hostBytes = new byte[tmpBytes.length];
        System.arraycopy( tmpBytes , 0 , hostBytes , 0 , tmpBytes.length );
        hostLen = host.length();
        System.arraycopy( hostBytes , 0 , this.host , 0 , hostLen );
        this.port = port;
        this.conn = conn;
        this.orb = orb;
    }

    public void run(){
        int iport = 0;
        iport |= port & 0xffff;
        StringBuffer buf = new StringBuffer();
        for( int i=0;i<hostLen;i++ )
            buf.append( (char)host[i] );
        String host2 = buf.toString();
        Transport trans = conn.internalConnect( host2 , iport , orb );
        ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).setPortal( trans );
        NoHeapRealtimeThread transportThread = new NoHeapRealtimeThread(trans);
        transportThread.start();
    }
}
