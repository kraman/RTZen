package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.*;
import javax.realtime.*;
import edu.oswego.cs.dl.util.concurrent.*;

public class TwoWayWaitingStrategy extends WaitingStrategy{
    CDRInputStream replyMsg;
    Object waitObject;

    TwoWayWaitingStrategy(){
        waitObject = new Integer(0);
    }
    
    public void replyReceived( GIOPMessage reply ){
        System.out.println( Thread.currentThread() + " TwoWayWaitingStrategy.replyReceived 1" );
        this.replyMsg = reply.getCDRInputStream();
        System.out.println( Thread.currentThread() + " TwoWayWaitingStrategy.replyReceived 2" );
        try{
        System.out.println( Thread.currentThread() + " TwoWayWaitingStrategy.replyReceived 4" );
            synchronized( waitObject ){
                waitObject.notify();
            }
        System.out.println( Thread.currentThread() + " TwoWayWaitingStrategy.replyReceived 5" );
        }catch( Exception e ){
            e.printStackTrace();
        }
        System.out.println( Thread.currentThread() + " TwoWayWaitingStrategy.replyReceived 6" );
    }

    public CDRInputStream waitForReply(){
        System.out.println( Thread.currentThread() + " TwoWayWaitingStrategy.waitForReply 1" );
        try{
        System.out.println( Thread.currentThread() + " TwoWayWaitingStrategy.waitForReply 2" );
            synchronized( waitObject ){
                waitObject.wait();
            }
        System.out.println( Thread.currentThread() + " TwoWayWaitingStrategy.waitForReply 3" );
        }catch( Exception e ){
            e.printStackTrace();
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
