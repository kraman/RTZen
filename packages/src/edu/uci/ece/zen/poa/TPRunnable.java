/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.poa;

import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.protocol.Message;
import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.utils.ThreadPool;

public class TPRunnable implements Runnable {

    POA poa;

    Message mseg;

    short priority;
    
    public TPRunnable() { }

    public void init(POA poa, Message mseg, short priority) {
        this.poa = poa;
        this.mseg = mseg;
        this.priority = priority;
    }

    public void run() {
        ThreadPool tp = 
            (ThreadPool) ((ScopedMemory) RealtimeThread.getCurrentMemoryArea()).getPortal();
//        tp.execute((RequestMessage) mseg, 
//                   (short) javax.realtime.PriorityScheduler.instance().getMinPriority(), 
//                   (short) javax.realtime.PriorityScheduler.instance().getMaxPriority());
        //KLUDGE: ?? sreq.waitTillInvoked();
 
      tp.execute((RequestMessage) mseg, this.priority, this.priority);  
    }
}

