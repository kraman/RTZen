/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.transport.iiop;

import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.ORBImpl;

public class AcceptorRunnable implements Runnable {

    ORB orb;
    short priority;
    public ScopedMemory acceptorArea;
    int threadPoolId;

    public AcceptorRunnable() {

    }

    public void init(ORB orb, short priority , int threadPoolId) {
        this.orb = orb;
        this.priority = priority;
        this.threadPoolId = threadPoolId;
    }

    private int statCount = 0;
    public void run() {
        try{
            if (statCount % edu.uci.ece.zen.utils.ZenBuildProperties.MEM_STAT_COUNT == 0) {
                edu.uci.ece.zen.utils.Logger.printMemStats(7);
            }
            statCount++;

            acceptorArea = (ScopedMemory) RealtimeThread.getCurrentMemoryArea();
            orb.getAcceptorRegistry().addAcceptor(acceptorArea, threadPoolId);
            Acceptor acceptor = new edu.uci.ece.zen.orb.transport.iiop.Acceptor( orb, (ORBImpl) ((ScopedMemory) orb.orbImplRegion).getPortal() , threadPoolId );
            acceptorArea.setPortal(acceptor);
            acceptor.startAccepting( priority );
        }
        catch(Throwable ex){
            ex.printStackTrace();
            System.exit(-1);
        }
    }

}
