package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.*;
import javax.realtime.*;
import edu.oswego.cs.dl.util.concurrent.*;

public class TwoWayWaitingStrategy extends WaitingStrategy{
    Semaphore clientSem;
    Semaphore transpSem;
    CDRInputStream replyMsg;

    TwoWayWaitingStrategy(){
        clientSem = new Semaphore(0);
        transpSem = new Semaphore(0);
    }
    
    public void replyReceived( GIOPMessage reply ){
        this.replyMsg = reply.getCDRInputStream();
        clientSem.release();
        try{
            transpSem.acquire();
        }catch( Exception e ){
            e.printStackTrace();
        }
    }

    public CDRInputStream waitForReply(){
        try{
            clientSem.acquire();
        }catch( Exception e ){
            e.printStackTrace();
        }
        transpSem.release();
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
