package edu.uci.ece.zen.poa;

import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.type.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.poa.mechanism.*;

public class TPRunnable implements Runnable{

    POA poa;
    ScopedMemory req;

    public TPRunnable(){}

    public void init( POA poa , ScopedMemory sreq ){
        this.poa = poa;
        this.req = sreq;
    }

    public void run(){
        ThreadPool tp = (ThreadPool) ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).getPortal();
        tp.execute( req , (short)0 , (short)99 );
        //KLUDGE: ?? sreq.waitTillInvoked();
    }
}

