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
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  " TwoWayWaitingStrategy.replyReceived 1" );
        this.replyMsg = reply.getCDRInputStream();
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  " TwoWayWaitingStrategy.replyReceived 2" );
        clientSem.release();
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  " TwoWayWaitingStrategy.replyReceived 4" );
    }

    public CDRInputStream waitForReply(){
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  " TwoWayWaitingStrategy.waitForReply 1" );
        try{
            clientSem.acquire();
        }catch( InterruptedException ie ){
            ie.printStackTrace();
        }
        System.out.println( Thread.currentThread() + " TwoWayWaitingStrategy.waitForReply 5" );
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
