/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import javax.realtime.*;
import edu.uci.ece.zen.utils.*;

public class MessageComposerRunnable implements Runnable {
    ClientRequest clr;
    CDRInputStream reply;
    int replyStatus;
    boolean success;
    private static Queue queue = Queue.fromImmortal();
    SendMessageRunnable smr;
    public static MessageComposerRunnable instance() {
        MessageComposerRunnable mcr = (MessageComposerRunnable)Queue.getQueuedInstance(MessageComposerRunnable.class,queue);
        return mcr;
    }

    public void release(){
        MessageComposerRunnable.queue.enqueue( this );
    }

    public MessageComposerRunnable(){
        smr = new SendMessageRunnable();
    }

    /**
     * Client upcall:
     * <p>
     * <b>Client scope </b> --ex in --&gt; ORB parent scope --&gt; ORB scope
     * --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public void init(ClientRequest clr) {
        this.clr = clr;
    }

    /**
     * Client upcall:
     * <p>
     * Client scope --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt;
     * <b>Message scope/Waiter region </b> --&gt; Transport scope
     * </p>
     */
    public void run() {
        //setup waiting straterg
        WaitingStrategy waitingStrategy = null;
        if (clr.responseExpected) {
            waitingStrategy = (WaitingStrategy) (((ScopedMemory) RealtimeThread.getCurrentMemoryArea()).getPortal());
            if (waitingStrategy == null) {
                waitingStrategy = new TwoWayWaitingStrategy();
                ((ScopedMemory) RealtimeThread.getCurrentMemoryArea()).setPortal(waitingStrategy);
            }
            clr.registerWaiter();
        }

        ExecuteInRunnable eir = clr.orb.getEIR();//new ExecuteInRunnable();
        smr.init(clr.transportScope);
        smr.init(clr.out.getBuffer());
        eir.init(smr, clr.transportScope);
        success = true;
        //edu.uci.ece.zen.utils.Logger.printMemStatsImm(383);
        try {
            ZenProperties.logger.log("MCR run 1");
            clr.orb.orbImplRegion.executeInArea(eir);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.SEVERE, getClass(), "run", "Could not sent message on transport", e);
            clr.releaseWaiter();
            waitingStrategy = null;
            success = false;
        } finally {
            clr.out.free();
            //edu.uci.ece.zen.utils.Logger.printMemStatsImm(384);
        }
        ZenProperties.logger.log("MCR run 2");
        if (waitingStrategy != null) {
            reply = waitingStrategy.waitForReply();
            replyStatus = waitingStrategy.getReplyStatus();
            clr.releaseWaiter();
        }
        ZenProperties.logger.log("MCR run 3");
        clr.orb.freeEIR( eir );
    }

    public CDRInputStream getReply() {
        return reply;
    }

    public int getReplyStatus(){
        return replyStatus;
    }
}

