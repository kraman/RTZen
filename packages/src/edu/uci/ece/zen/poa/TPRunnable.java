package edu.uci.ece.zen.poa;

import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.protocol.Message;
import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.utils.ThreadPool;

public class TPRunnable implements Runnable {

    POA poa;

    Message mseg;

    public TPRunnable() {
    }

    public void init(POA poa, Message mseg) {
        this.poa = poa;
        this.mseg = mseg;
    }

    public void run() {
        ThreadPool tp = (ThreadPool) ((ScopedMemory) RealtimeThread
                .getCurrentMemoryArea()).getPortal();
        tp.execute((RequestMessage) mseg, (short) 0, (short) 99);
        //KLUDGE: ?? sreq.waitTillInvoked();
    }
}

