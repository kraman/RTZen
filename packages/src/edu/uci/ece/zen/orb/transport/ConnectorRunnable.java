package edu.uci.ece.zen.orb.transport;

import javax.realtime.NoHeapRealtimeThread;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;
import javax.realtime.*;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.ORBImpl;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;

public class ConnectorRunnable implements Runnable {
    public ConnectorRunnable() {
    }

    private FString host;

    private short port;

    private Connector conn;

    private ORB orb;

    public void init(FString host, short port, Connector conn, ORB orb) {
        //this.host.reset();
        //this.host.append(host);
        this.host = host;
        this.port = port;
        this.conn = conn;
        this.orb = orb;
    }
    private int statCount = 0;
    private boolean retVal;
    public boolean getReturnStatus(){ return retVal; }
    public void run() {
        try{
            statCount++;
            if (statCount % ZenBuildProperties.MEM_STAT_COUNT == 0) {
                edu.uci.ece.zen.utils.Logger.printMemStats(ZenBuildProperties.dbgTransportScopeId);
            }

            int iport = 0;
            iport |= port & 0xffff;
            String host2=null;
            if( host != null ){
                host2 = new String(this.host.getTrimDataAsChar());
                FString.free(host);
            }

            Transport trans = conn.internalConnect(host2, iport, orb,
                    (ORBImpl) orb.orbImplRegion.getPortal());
            if( trans != null && trans.success ){
                RealtimeThread transportThread = new NoHeapRealtimeThread( new PriorityParameters( PriorityScheduler.instance().getMaxPriority() ) , null, null, RealtimeThread.getCurrentMemoryArea(), null, trans);
                //RealtimeThread transportThread = new
                // RealtimeThread(null,null,null,RealtimeThread.getCurrentMemoryArea(),null,trans);
                transportThread.start();
                ((ScopedMemory) RealtimeThread.getCurrentMemoryArea()).setPortal(trans);
                retVal = true;
            }else{
                retVal = false;
            }
        }catch( Throwable e ){
            e.printStackTrace();
        }
    }
}
