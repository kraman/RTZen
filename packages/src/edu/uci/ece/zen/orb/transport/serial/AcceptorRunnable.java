/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.transport.serial;

import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.ORBImpl;

public class AcceptorRunnable implements Runnable {
    ORB orb;

    public AcceptorRunnable() {

    }

    public void init(ORB orb) {
        this.orb = orb;
    }

    private int statCount = 0;
    public void run() {
            if (statCount % edu.uci.ece.zen.utils.ZenProperties.MEM_STAT_COUNT == 0) {
                edu.uci.ece.zen.utils.Logger.printMemStats(7);
            }
             statCount++;

        ScopedMemory acceptorArea = (ScopedMemory) RealtimeThread .getCurrentMemoryArea();
        orb.getAcceptorRegistry().addAcceptor(acceptorArea);
        Acceptor acceptor = new edu.uci.ece.zen.orb.transport.serial.Acceptor( 
                orb, (ORBImpl) ((ScopedMemory) orb.orbImplRegion).getPortal());
        acceptorArea.setPortal(acceptor);
        acceptor.startAccepting();
    }

}
