package edu.uci.ece.zen.orb.transport;

import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;

public class ConnectorRunnable implements Runnable{
    public ConnectorRunnable(){
        host = new FString();
        try{
            host.init(1024);
        }catch( Exception e2 ){
             ZenProperties.logger.log(
                Logger.SEVERE,
                getClass(),
                "<init>",
                "Could not initialize Connector",
				e2);
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
        Transport trans = conn.internalConnect( host2 , iport , orb , (ORBImpl) orb.orbImplRegion.getPortal() );
        RealtimeThread transportThread = new NoHeapRealtimeThread(null,null,null,RealtimeThread.getCurrentMemoryArea(),null,trans);
        //RealtimeThread transportThread = new RealtimeThread(null,null,null,RealtimeThread.getCurrentMemoryArea(),null,trans);
        transportThread.start();
        ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).setPortal( trans );
    }
}
