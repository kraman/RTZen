package edu.uci.ece.zen.poa;

import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.type.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.poa.mechanism.*;

public class TPRunnable implements Runnable{

    public TPRunnable(){}

    public void init( POAImpl poaImpl , SynchronizedInt numReq , RequestProcessingStrategy reqProcStratgy , RequestMessage sreq ){
    }

    public void run(){
//        ThreadPool tp = ((ScopedRegion)RealtimeThread.getCurrentMemoryArea()).getPortal();
//        tp.execute( sreq , 0 , 99 );
//        sreq.waitTillInvoked();
    }
}

