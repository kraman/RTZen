package edu.uci.ece.zen.orb.transport;

import javax.realtime.*;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.utils.*;

public abstract class Transport implements Runnable{
    private Object waitObj;
    protected edu.uci.ece.zen.orb.ORB orb;
    private MessageProcessor messageProcessor;

    public Transport( edu.uci.ece.zen.orb.ORB orb ){
        this.orb = orb;
        waitObj = new Integer(0);
    }

    public final void run(){
        messageProcessor = new MessageProcessor( this , orb );
        NoHeapRealtimeThread messageProcessorThr = new NoHeapRealtimeThread( messageProcessor );
        messageProcessorThr.setDaemon( true );
        messageProcessorThr.start();
        try{
            synchronized( waitObj ){
                waitObj.wait();
            }
        }catch( InterruptedException ie ){
            //ignore exception. Always happens on shutdown
        }
    }

    public final void shutdown( boolean waitForCompletion ){
        if( waitForCompletion ){
            messageProcessor.shutdown();
        }
        waitObj.notifyAll();
    }

    public abstract java.io.InputStream getInputStream();
    protected abstract java.io.OutputStream getOutputStream();
    public synchronized final void send( WriteBuffer msg ){
        System.out.println( "Sending message" );
        try{
            java.io.OutputStream out = getOutputStream();
            msg.dumpBuffer( out );
        }catch( java.io.IOException ioex ){
            ioex.printStackTrace();
        }
    }
}

/**
 * Call scoped region graph:
 * <p>
 *      Transport thread:<br/>
 *      <p>
 *          <b>Transport scope</b> --ex in--&gt; Immortal --&gt; Message --ex in--&gt; Immortal --&gt; Waiter region 
 *      </p>
 *      Client Thread:<br/>
 *      <p>
 *          Client scope --ex in--&gt; MessageScope/Waiter region --ex in--&gt; Immortal --gt; Transport <br/>
 *      </p>
 *  </p>
 */
class MessageProcessor implements Runnable{
    private Transport trans;
    private edu.uci.ece.zen.orb.ORB orb;
    private boolean isActive;

    public MessageProcessor( Transport trans , edu.uci.ece.zen.orb.ORB orb ){
        this.trans = trans;
        this.orb = orb;
    }

    public void run(){
        isActive = true;
        ExecuteInRunnable eir = new ExecuteInRunnable();
        GIOPMessageRunnable gmr = new GIOPMessageRunnable( orb , trans );
        while( isActive ){
            System.err.println( "Waiting for message to come in..." );
            ScopedMemory messageScope = orb.getScopedRegion();
            gmr.setRequestScope( messageScope );
            eir.init( gmr , messageScope );
            ImmortalMemory.instance().executeInArea( eir );
        }
        synchronized( this ){
            this.notifyAll();
        }
    }

    public void shutdown(){
        isActive = false;
        try{
            synchronized( this ){
                this.wait();
            }
        }catch( InterruptedException ie ){
            //Ignore
        }
    }
}

class ExecuteInRunnable implements Runnable{
    Runnable runnable;
    MemoryArea ma;

    public ExecuteInRunnable(){}
    public void init( Runnable runnable , MemoryArea ma ){
        this.runnable = runnable;
        this.ma = ma;
    }
    public void run(){
        ma.enter( runnable );
    }
}

/**
 * Call scoped region graph:
 * <p>
 *      Transport thread:<br/>
 *      <p>
 *          Transport scope --ex in--&gt; Immortal --&gt; <b>Message</b> --ex in--&gt; Immortal --&gt; Waiter region 
 *      </p>
 *      Client Thread:<br/>
 *      <p>
 *          Client scope --ex in--&gt; MessageScope/Waiter region --ex in--&gt; Immortal --gt; Transport <br/>
 *      </p>
 *  </p>
 */
class GIOPMessageRunnable implements Runnable{
    edu.uci.ece.zen.orb.ORB orb;
    Transport trans;
    ScopedMemory requestScope;
    
    public GIOPMessageRunnable( edu.uci.ece.zen.orb.ORB orb , Transport trans ){
        this.orb = orb;
        this.trans = trans;
    }

    public void setRequestScope( ScopedMemory requestScope ){
        this.requestScope = requestScope;
    }

    public void run(){
        try{
            edu.uci.ece.zen.orb.giop.GIOPMessage message = edu.uci.ece.zen.orb.giop.GIOPMessageFactory.parseStream( orb , trans );
            System.err.println( "Got a new message....." );
            message.setScope( requestScope );
            requestScope.setPortal( message );
            if( message.isRequest() ){
                //ThreadPoolProcessor tpProc = new ThreadPoolProcessor();
                //POADispatchRunnable pdispatcher = new POADispatchRunnable( message , tpProc , orb );
                //ImmortalMemory.instance().executeInArea( pdispatcher );
            }
            if( message.isReply() ){
                System.err.println( "Message received w/ id: " + message.getRequestId() );
                ScopedMemory waiterRegion = orb.getWaiterRegion( message.getRequestId() );
                System.err.println( "Waiter region determined to be: " + waiterRegion );
                WaitingStratergyNotifyRunnable wsnr = new WaitingStratergyNotifyRunnable( message , waiterRegion );
                ExecuteInRunnable eir = new ExecuteInRunnable();
                eir.init( wsnr , waiterRegion );
                ImmortalMemory.instance().executeInArea( eir );
            }
        }catch( java.io.IOException ioex ){
            //TODO: do something here    
        }
    }
}

/**
 * Call scoped region graph:
 * <p>
 *      Transport thread:<br/>
 *      <p>
 *          Transport scope --ex in--&gt; Immortal --&gt; Message --ex in--&gt; Immortal --&gt; <b>Waiter region</b>
 *      </p>
 *      Client Thread:<br/>
 *      <p>
 *          Client scope --&gt; MessageScope/Waiter region --ex in--&gt; Immortal --&gt; Transport<br/>
            Client scope --&gt; MessageScope/Waiter region --&gt; Reply Message region --ex in--&gt; Client scope<br/>
 *      </p>
 *  </p>
 */
class WaitingStratergyNotifyRunnable implements Runnable{
    edu.uci.ece.zen.orb.giop.GIOPMessage message;
    ScopedMemory waiterRegion;
    
   WaitingStratergyNotifyRunnable( edu.uci.ece.zen.orb.giop.GIOPMessage message , ScopedMemory waiterRegion ){
        this.message = message;
        this.waiterRegion = waiterRegion;
    }

    public void run(){
        edu.uci.ece.zen.orb.WaitingStrategy waitingStrategy = ((edu.uci.ece.zen.orb.WaitingStrategy) waiterRegion.getPortal());
        System.err.println( "Waiter found: " + waitingStrategy );
        System.err.println( "Reply is " + message );
        waitingStrategy.replyReceived( message );
        try{
            synchronized(message){
                message.wait();
            }
        }catch( InterruptedException e ){
            e.printStackTrace();
        }
    }
}
