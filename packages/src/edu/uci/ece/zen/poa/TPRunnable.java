package edu.uci.ece.zen.poa;

import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.type.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.poa.mechanism.*;

public class TPRunnable implements Runnable{

    POA poa;
    ScopedRegion req;

    public TPRunnable(){}

    public void init( ScopedRegion sreq ){
        this.poa = poa;
        this.req = sreq;
    }

    public void run(){
        ThreadPool tp = ((ScopedRegion)RealtimeThread.getCurrentMemoryArea()).getPortal();
        tp.execute( sreq , 0 , 99 );
        sreq.waitTillInvoked();
    }
}

