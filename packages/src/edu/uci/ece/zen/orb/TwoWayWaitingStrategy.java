package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.*;
import javax.realtime.*;
import edu.oswego.cs.dl.util.concurrent.*;

public class TwoWayWaitingStrategy extends WaitingStrategy{
    Semaphore clientSem;
    CDRInputStream replyMsg;

    TwoWayWaitingStrategy(){
        clientSem = new Semaphore(0);
    }
    
    public void replyReceived( GIOPMessage reply ){
        this.replyMsg = reply.getCDRInputStream();
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
