package edu.uci.ece.zen.orb;

import javax.realtime.ImmortalMemory;

import edu.oswego.cs.dl.util.concurrent.Semaphore;
import edu.uci.ece.zen.orb.protocol.Message;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.orb.protocol.type.ReplyMessage;

public class TwoWayWaitingStrategy extends WaitingStrategy {
    Semaphore clientSem;

    CDRInputStream replyMsg;

    private static TwoWayWaitingStrategy inst;

    TwoWayWaitingStrategy() {
        clientSem = new Semaphore(0);
    }

    public void replyReceived(Message reply) {
        this.replyMsg = reply.getCDRInputStream();
        if( replyMsg == null )
            System.out.println ( "---------------------------------" );

        FString contexts = ((ReplyMessage)reply).getServiceContexts();
        if (ZenProperties.dbg) System.out.println("REPLY SC: " + contexts.decode());
        ReadBuffer rb = contexts.toReadBuffer();
        //if (ZenProperties.dbg) System.out.println("#############REPLY RB: " + rb.toString());
        int size = rb.readLong();
        if(ZenProperties.devDbg) System.out.println("REPLY CONTEXT size: " + size);
        for(int i = 0; i < size; ++i){
            int id = rb.readLong();
            if(ZenProperties.devDbg) System.out.println("REPLY CONTEXT id: " + id);
            if(id == org.omg.IOP.RTCorbaPriority.value){
                if(ZenProperties.devDbg) System.out.println("REPLY CONTEXT id:RTCorbaPriority");
                if(ZenProperties.devDbg) System.out.println("CUR thread priority: " + replyMsg.orb.getRTCurrent().the_priority());
                rb.readLong(); //eat length
                short priority = (short)rb.readLong();
                if(ZenProperties.devDbg) System.out.println("RECEIVED thread priority: " + priority);
                replyMsg.orb.getRTCurrent().the_priority(priority);
                if(ZenProperties.devDbg) System.out.println("NEW thread priority: " + replyMsg.orb.getRTCurrent().the_priority());
            } else{ // just eat
                if(ZenProperties.devDbg) System.out.println("Skipping unknown service context " + id);
                int byteLen = rb.readLong();
                for(int i1 = 0; i1 < byteLen; ++i1)
                    rb.readByte();
            }
        }
        rb.free();
        /*
         ServiceContext[] contexts =((ReplyMessage)reply).getServiceContexts();
         for(int i = 0; i < contexts.length; ++i){
            if(ZenProperties.devDbg)
            System.out.println("REPLY CONTEXT id: " + contexts[0].context_id);
            if(contexts[0].context_id == RTCorbaPriority.value){
                if(ZenProperties.devDbg) System.out.println("REPLY CONTEXT id:RTCorbaPriority");
                if(ZenProperties.devDbg) System.out.println("CUR thread priority: " + replyMsg.orb.getRTCurrent().the_priority());
                CDRInputStream in1 = CDRInputStream.fromOctetSeq(contexts[0].context_data, replyMsg.orb);
                short priority = in1.read_short();
                if(ZenProperties.devDbg) System.out.println("RECEIVED thread priority: " + priority);
                replyMsg.orb.getRTCurrent().the_priority(priority);
                if(ZenProperties.devDbg) System.out.println("NEW thread priority: " + replyMsg.orb.getRTCurrent().the_priority());
                in1.free();
            }
         }
         */
        //TODO:remember to release the message....u only have 1
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
