package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.orb.giop.type.*;
import javax.realtime.*;
import edu.oswego.cs.dl.util.concurrent.*;
import org.omg.IOP.*;
import edu.uci.ece.zen.utils.*;

public class TwoWayWaitingStrategy extends WaitingStrategy{
    Semaphore clientSem;
    CDRInputStream replyMsg;

    TwoWayWaitingStrategy(){
        clientSem = new Semaphore(0);
    }

    public void replyReceived( GIOPMessage reply ){
        this.replyMsg = reply.getCDRInputStream();

        //TODO:handle service contexts here ... fix this... you can demarshall stuff here
        ServiceContext[] contexts = ((ReplyMessage)reply).getServiceContexts();

        for(int i = 0; i < contexts.length; ++i){
            if(ZenProperties.devDbg) System.out.println("REPLY CONTEXT id: " + contexts[0].context_id);
            if(contexts[0].context_id == RTCorbaPriority.value){
                if(ZenProperties.devDbg) System.out.println("REPLY CONTEXT id: RTCorbaPriority");
                if(ZenProperties.devDbg) System.out.println("CUR thread priority: " + replyMsg.orb.getRTCurrent().the_priority());
                CDRInputStream in1 = CDRInputStream.fromOctetSeq(contexts[0].context_data, replyMsg.orb);
                short priority = in1.read_short();
                if(ZenProperties.devDbg) System.out.println("RECEIVED thread priority: " + priority);
                replyMsg.orb.getRTCurrent().the_priority(priority);
                if(ZenProperties.devDbg) System.out.println("NEW thread priority: " + replyMsg.orb.getRTCurrent().the_priority());
                in1.free();
            }
        }

        //TODO:remember to release the message....u only have 1

        clientSem.release();
    }

    public CDRInputStream waitForReply(){
        try{
            clientSem.acquire();
        }catch( InterruptedException ie ){
            ie.printStackTrace();
        }
        return replyMsg;
    }
}

class ReplyMessageRunnable implements Runnable{
    TwoWayWaitingStrategy str;

    public ReplyMessageRunnable( TwoWayWaitingStrategy str ){
        this.str = str;
    }

    public void run(){
        synchronized( str ){
            str.notify();
        }
    }
}
