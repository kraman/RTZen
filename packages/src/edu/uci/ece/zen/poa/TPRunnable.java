package edu.uci.ece.zen.poa;

import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.type.*;
import edu.uci.ece.zen.orb.giop.GIOPMessage;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.poa.mechanism.*;

public class TPRunnable implements Runnable{

    POA poa;
    GIOPMessage mseg;

    public TPRunnable(){}

    public void init( POA poa , GIOPMessage mseg ){
        this.poa = poa;
        this.mseg= mseg;
    }

    public void run(){
        ThreadPool tp = (ThreadPool) ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).getPortal();
        tp.execute( (RequestMessage) mseg , (short)0 , (short)99 );
        //KLUDGE: ?? sreq.waitTillInvoked();
    }
}

