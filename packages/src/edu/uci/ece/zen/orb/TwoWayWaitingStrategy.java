package edu.uci.ece.zen.orb;

import javax.realtime.ImmortalMemory;

import edu.oswego.cs.dl.util.concurrent.Semaphore;
import edu.uci.ece.zen.orb.giop.GIOPMessage;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

public class TwoWayWaitingStrategy extends WaitingStrategy {
    Semaphore clientSem;

    CDRInputStream replyMsg;

    private static TwoWayWaitingStrategy inst;

    public static TwoWayWaitingStrategy instance() {
        if (inst == null) {
            try {
                inst = (TwoWayWaitingStrategy) ImmortalMemory.instance()
                        .newInstance(TwoWayWaitingStrategy.class);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, TwoWayWaitingStrategy.class, "instance", e);
            }
        }
        return inst;
    }

    TwoWayWaitingStrategy() {
        clientSem = new Semaphore(0);
    }

    public void replyReceived(GIOPMessage reply) {
        this.replyMsg = reply.getCDRInputStream();
        //reply.free();
        //TODO:handle service contexts here ... fix this... you can demarshall
        // stuff here
        /*
         * ServiceContext[] contexts =
         * ((ReplyMessage)reply).getServiceContexts(); for(int i = 0; i <
         * contexts.length; ++i){ if(ZenProperties.devDbg)
         * System.out.println("REPLY CONTEXT id: " + contexts[0].context_id);
         * if(contexts[0].context_id == RTCorbaPriority.value){
         * if(ZenProperties.devDbg) System.out.println("REPLY CONTEXT id:
         * RTCorbaPriority"); if(ZenProperties.devDbg) System.out.println("CUR
         * thread priority: " + replyMsg.orb.getRTCurrent().the_priority());
         * CDRInputStream in1 =
         * CDRInputStream.fromOctetSeq(contexts[0].context_data, replyMsg.orb);
         * short priority = in1.read_short(); if(ZenProperties.devDbg)
         * System.out.println("RECEIVED thread priority: " + priority);
         * replyMsg.orb.getRTCurrent().the_priority(priority);
         * if(ZenProperties.devDbg) System.out.println("NEW thread priority: " +
         * replyMsg.orb.getRTCurrent().the_priority()); in1.free(); } }
         */
        //TODO:remember to release the message....u only have 1
        clientSem.release();
        ((edu.uci.ece.zen.orb.giop.v1_0.ReplyMessage)reply).release();
    }

    public CDRInputStream waitForReply() {
        try {
            clientSem.acquire();
        } catch (InterruptedException ie) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "waitForReply", ie);
        }
        return replyMsg;
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
