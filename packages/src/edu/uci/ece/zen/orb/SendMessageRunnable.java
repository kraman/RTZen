/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import javax.realtime.ImmortalMemory;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

public class SendMessageRunnable implements Runnable {
    WriteBuffer msg;
    ScopedMemory transScope;

    public void init(ScopedMemory transScope) {
        this.transScope = transScope;
    }

    /**
     * Client upcall:
     * <p>
     * Client scope --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt;
     * <b>Message scope/Waiter region </b> --&gt; Transport scope
     * </p>
     */
    public void init(WriteBuffer buffer) {
        this.msg = buffer;
    }

    /**
     * Client upcall:
     * <p>
     * Client scope --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt;
     * Message scope/Waiter region --&gt; <b>Transport scope </b>
     * </p>
     */
    public void run() {
        //edu.uci.ece.zen.orb.transport.Transport trans =
        // (edu.uci.ece.zen.orb.transport.Transport) transScope.getPortal();
        edu.uci.ece.zen.orb.transport.Transport trans = (edu.uci.ece.zen.orb.transport.Transport) ((ScopedMemory) RealtimeThread
                .getCurrentMemoryArea()).getPortal();
        if (trans != null && msg != null){
	    //System.out.println( "sending message" );
            trans.send(msg);
	    //System.out.println( "msg sent" );
        }else
            ZenProperties.logger.log(Logger.SEVERE, "---------------------------------------Transport null or write buffer null");
    }
}
