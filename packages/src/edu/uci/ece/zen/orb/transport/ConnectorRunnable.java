package edu.uci.ece.zen.orb.transport;

import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;

public class ConnectorRunnable implements Runnable{
    public ConnectorRunnable(){
    }

    private String host;
    private short port;
    private Connector conn;
    private ORB orb;

    public void init( String host , short port , Connector conn , ORB orb ){
        this.port = port;
        this.host = host;
        this.conn = conn;
        this.orb = orb;
    }

    public void run(){
        GetHostRunnable ghr = new GetHostRunnable( host );
        try{
            HeapMemory.instance().executeInArea( ghr );
        }catch( Exception e ){
            e.printStackTrace();
        }

        int iport = 0;
        iport |= port & 0xffff;
        if(ZenProperties.devDbg) System.out.println("Yuez in ConnectorRunnable 1");
        Transport trans = conn.internalConnect( ghr.inetaddr , iport , orb , (ORBImpl) orb.orbImplRegion.getPortal() );
        if(ZenProperties.devDbg) System.out.println("Yuez in ConnectorRunnable 2 " + RealtimeThread.getCurrentMemoryArea());        
        RealtimeThread transportThread = new NoHeapRealtimeThread(null,null,null,RealtimeThread.getCurrentMemoryArea(),null,trans);
        //RealtimeThread transportThread = new RealtimeThread(null,null,null,RealtimeThread.getCurrentMemoryArea(),null,trans);
        if(ZenProperties.devDbg) System.out.println("Yuez in ConnectorRunnable 3");        
        transportThread.start();
        ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).setPortal( trans );
    }
}
