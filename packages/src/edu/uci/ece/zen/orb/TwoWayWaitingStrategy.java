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

import edu.oswego.cs.dl.util.concurrent.Semaphore;
import edu.uci.ece.zen.orb.protocol.Message;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.orb.protocol.type.ReplyMessage;

public class TwoWayWaitingStrategy extends WaitingStrategy {
    Semaphore clientSem;

    CDRInputStream replyMsg;
    int replyStatus = -1;
    private static TwoWayWaitingStrategy inst;

    TwoWayWaitingStrategy() {
        clientSem = new Semaphore(0);
    }

    public void replyReceived(Message reply) {
        this.replyMsg = reply.getCDRInputStream();
        if( replyMsg == null )
            if (ZenBuildProperties.dbgInvocations) ZenProperties.logger.log( "Reply Msg Null" );
        FString contexts = ((ReplyMessage)reply).getServiceContexts();
        if (ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("REPLY SC: " + contexts.decode());
        ReadBuffer rb = contexts.toReadBuffer();
        int size = rb.readLong();
        if(ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("REPLY CONTEXT size: " + size);
        for(int i = 0; i < size; ++i){
            int id = rb.readLong();
            if(ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("REPLY CONTEXT id: " + id);
            if(id == org.omg.IOP.RTCorbaPriority.value){
                if(ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("REPLY CONTEXT id:RTCorbaPriority");
                if(ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("CUR thread priority: " + replyMsg.orb.getRTCurrent().the_priority());
                rb.readLong(); //eat length
                short priority = (short)rb.readLong();
                if(ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("RECEIVED thread priority: " + priority);
                replyMsg.orb.getRTCurrent().the_priority(priority);
                if(ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("NEW thread priority: " + replyMsg.orb.getRTCurrent().the_priority());
            } else{ // just eat
                if(ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("Skipping unknown service context " + id);
                int byteLen = rb.readLong();
                for(int i1 = 0; i1 < byteLen; ++i1)
                    rb.readByte();
            }
        }

        this.replyStatus = ((ReplyMessage)reply).getReplyStatus();
        rb.free();
        clientSem.release();
        ((ReplyMessage)reply).release();
    }

    public CDRInputStream waitForReply() {
        try {
            ZenProperties.logger.log("waiting for reply...");
            clientSem.acquire();
        } catch (InterruptedException ie) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "waitForReply", ie);
        }
        return replyMsg;
    }

    public int getReplyStatus(){
        return replyStatus;
    }
}

class ReplyMessageRunnable implements Runnable {
    TwoWayWaitingStrategy str;

    public ReplyMessageRunnable(TwoWayWaitingStrategy str) {
        this.str = str;
    }

    public void run() {
        synchronized (str) {
            str.notify();
        }
    }
}
