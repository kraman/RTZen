package edu.uci.ece.zen.orb.transport;

import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;

public class ConnectorRunnable implements Runnable{
    public ConnectorRunnable(){
        host = new FString();
        try{
            host.init(1024);
        }catch( InstantiationException e1 ){
             ZenProperties.logger.log(
                Logger.SEVERE,
                "edu.uci.ece.zen.orb.ConnectorRunnable",
                "<init>",
                "Could not initialize Connector due to exception: " + e1.toString()
                );
        }catch( IllegalAccessException e2 ){
             ZenProperties.logger.log(
                Logger.SEVERE,
                "edu.uci.ece.zen.orb.ConnectorRunnable",
                "<init>",
                "Could not initialize Connector due to exception: " + e2.toString()
                );
        }
    }

    private FString host;
    private short port;
    private Connector conn;
    private ORB orb;
    public void init( String host , short port , Connector conn , ORB orb ){
        this.host.reset();
        this.host.append( host );
        this.port = port;
        this.conn = conn;
        this.orb = orb;
    }

    public void run(){
        int iport = 0;
        iport |= port & 0xffff;
        String host2 = new String( this.host.getTrimData() );
        System.out.println("Yuez in ConnectorRunnable 1");
        Transport trans = conn.internalConnect( host2 , iport , orb , (ORBImpl) orb.orbImplRegion.getPortal() );
        System.out.println("Yuez in ConnectorRunnable 2");        
        RealtimeThread transportThread = new NoHeapRealtimeThread(null,null,null,RealtimeThread.getCurrentMemoryArea(),null,trans);
        System.out.println("Yuez in ConnectorRunnable 3");        
        transportThread.start();
        ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).setPortal( trans );
    }
}
