package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.*;
import javax.realtime.*;

public class TwoWayWaitingStrategy extends WaitingStrategy{
    Object waitObj;
    GIOPMessage replyMsg;

    TwoWayWaitingStrategy(){
         waitObj = new Integer(0);
    }
    
    public void replyReceived( GIOPMessage reply ){
        this.replyMsg = reply;
        synchronized( waitObj ){
            waitObj.notify();
        }
        try{
            synchronized( this ){
                this.wait();
            }
        }catch( InterruptedException ie ){
            ie.printStackTrace();
        }
    }

    public GIOPMessage waitForReply(){
        ReplyMessageRunnable rmr = new ReplyMessageRunnable( this );
        try{
            synchronized( waitObj ){
                waitObj.wait();
            }
        }catch( InterruptedException ie ){
            ie.printStackTrace();
        }
        ScopedMemory replyScope = replyMsg.getScope();
        replyScope.enter( rmr );
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
