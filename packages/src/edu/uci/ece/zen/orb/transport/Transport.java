package edu.uci.ece.zen.orb.transport;

import javax.realtime.*;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;

public abstract class Transport implements Runnable{
    private Object waitObj;
    protected edu.uci.ece.zen.orb.ORB orb;
    private MessageProcessor messageProcessor;

    /**
     * <p>
     *     ORBImpl region --&gt; <b>Transport scope</b>
     * </p>
     */
    public Transport( edu.uci.ece.zen.orb.ORB orb ){
        this.orb = orb;
        waitObj = new Integer(0);
    }

    /**
     * <p>
     *     ORBImpl region --&gt; <b>Transport scope</b>
     * </p>
     */
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
    
    /**
     * <p>
     *     ORBImpl region --&gt; <b>Transport scope</b>
     * </p>
     */
    public final void shutdown( boolean waitForCompletion ){
        if( waitForCompletion ){
            messageProcessor.shutdown();
        }
        waitObj.notifyAll();
    }

    public abstract java.io.InputStream getInputStream();
    protected abstract java.io.OutputStream getOutputStream();

    /**
     * <p>
     *     ORBImpl region --&gt; <b>Transport scope</b>
     * </p>
     */
    public synchronized final void send( WriteBuffer msg ){
        //System.out.println( "Sending message" );
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
 *          <b>Transport scope</b> --ex in--&gt; ORBImpl scope --&gt; Message --ex in--&gt; ORBImpl scope --&gt; Waiter region 
 *      </p>
 *      Client Thread:<br/>
 *      <p>
 *          Client scope --ex in --&gt; ORB parent scope --&gt; ORBImpl scope --&gt; <b>Message scope/Waiter region</b> --&gt; Transport scope
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
        GIOPMessageRunnable gmr = new GIOPMessageRunnable( orb , trans );
        while( isActive ){
            //System.err.println( "Waiting for message to come in..." );
            ScopedMemory messageScope = ORB.getScopedRegion();
            gmr.setRequestScope( messageScope );

            ExecuteInRunnable eir = ExecuteInRunnable.instance();
            eir.init( gmr , messageScope );
            try{
                orb.orbImplRegion.executeInArea( eir );
            }catch( Exception e ){
                ZenProperties.logger.log(
                    Logger.SEVERE,
                    "edu.uci.ece.zen.orb.transport.MessageProcessor",
                    "run()",
                    "Could not process message due to exception: " + e.toString()
                    );
            }
            ORB.freeScopedRegion( messageScope );
            eir.free();
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

class GIOPMessageRunnable implements Runnable{
    edu.uci.ece.zen.orb.ORB orb;
    Transport trans;
    ScopedMemory requestScope;

    /**
     * Call scoped region graph:
     * <p>
     *      Transport thread:<br/>
     *      <p>
     *          <b>Transport scope</b> --ex in--&gt; ORBImpl scope --&gt; Message --ex in--&gt; ORBImpl scope --&gt; Waiter region 
     *      </p>
     *      Client Thread:<br/>
     *      <p>
     *          Client scope --ex in --&gt; ORB parent scope --&gt; ORBImpl scope --&gt; <b>Message scope/Waiter region</b> --&gt; Transport scope
     *      </p>
     *  </p>
     */
    public GIOPMessageRunnable( edu.uci.ece.zen.orb.ORB orb , Transport trans ){
        this.orb = orb;
        this.trans = trans;
    }

    /**
     * Call scoped region graph:
     * <p>
     *      Transport thread:<br/>
     *      <p>
     *          <b>Transport scope</b> --ex in--&gt; ORBImpl scope --&gt; Message --ex in--&gt; ORBImpl scope --&gt; Waiter region 
     *      </p>
     *      Client Thread:<br/>
     *      <p>
     *          Client scope --ex in --&gt; ORB parent scope --&gt; ORBImpl scope --&gt; <b>Message scope/Waiter region</b> --&gt; Transport scope
     *      </p>
     *  </p>
     */
    public void setRequestScope( ScopedMemory requestScope ){
        this.requestScope = requestScope;
    }

    /**
     * Call scoped region graph:
     * <p>
     *      Transport thread:<br/>
     *      <p>
     *          Transport scope --ex in--&gt; ORBImpl scope --&gt; <b>Message</b> --ex in--&gt; ORBImpl scope --&gt; Waiter region 
     *      </p>
     *      Client Thread:<br/>
     *      <p>
     *          Client scope --ex in --&gt; ORB parent scope --&gt; ORBImpl scope --&gt; <b>Message scope/Waiter region</b> --&gt; Transport scope
     *      </p>
     *  </p>
     */
    public void run(){
        try{
            edu.uci.ece.zen.orb.giop.GIOPMessage message = edu.uci.ece.zen.orb.giop.GIOPMessageFactory.parseStream( orb , trans );
            //System.err.println( "Got a new message....." );
            message.setScope( requestScope );
            requestScope.setPortal( message );
            if( message.isRequest() ){
                //ThreadPoolProcessor tpProc = new ThreadPoolProcessor();
                //POADispatchRunnable pdispatcher = new POADispatchRunnable( message , tpProc , orb );
                //ImmortalMemory.instance().executeInArea( pdispatcher );
            }
            if( message.isReply() ){
                //System.err.println( "Message received w/ id: " + message.getRequestId() );
                ScopedMemory waiterRegion = orb.getWaiterRegion( message.getRequestId() );
                //System.err.println( "Waiter region determined to be: " + waiterRegion );
                WaitingStratergyNotifyRunnable wsnr = new WaitingStratergyNotifyRunnable( message , waiterRegion );
                ExecuteInRunnable eir = ExecuteInRunnable.instance();
                eir.init( wsnr , waiterRegion );
                try{
                    orb.orbImplRegion.executeInArea( eir );
                }catch( Exception e ){
                    ZenProperties.logger.log(
                        Logger.SEVERE,
                        "edu.uci.ece.zen.orb.transport.GIOPMessageRunnable",
                        "run()",
                        "Could not process reply message due to exception: " + e.toString()
                        );
                }
                eir.free();
            }
        }catch( java.io.IOException ioex ){
            //TODO: do something here    
        }
    }
}

class WaitingStratergyNotifyRunnable implements Runnable{
    edu.uci.ece.zen.orb.giop.GIOPMessage message;
    ScopedMemory waiterRegion;

    /**
     * Call scoped region graph:
     * <p>
     *      Transport thread:<br/>
     *      <p>
     *          Transport scope --ex in--&gt; ORBImpl scope --&gt; <b>Message</b> --ex in--&gt; ORBImpl scope --&gt; Waiter region 
     *      </p>
     *      Client Thread:<br/>
     *      <p>
     *          Client scope --ex in --&gt; ORB parent scope --&gt; ORBImpl scope --&gt; <b>Message scope/Waiter region</b> --&gt; Transport scope
     *      </p>
     *  </p>
     */   
    WaitingStratergyNotifyRunnable( edu.uci.ece.zen.orb.giop.GIOPMessage message , ScopedMemory waiterRegion ){
        this.message = message;
        this.waiterRegion = waiterRegion;
    }

    /**
     * Call scoped region graph:
     * <p>
     *      Transport thread:<br/>
     *      <p>
     *          Transport scope --ex in--&gt; ORBImpl scope --&gt; Message --ex in--&gt; ORBImpl scope --&gt; <b>Waiter region</b>
     *      </p>
     *      Client Thread:<br/>
     *      <p>
     *          Client scope --ex in --&gt; ORB parent scope --&gt; ORBImpl scope --&gt; <b>Message scope/Waiter region</b> --&gt; Transport scope
     *      </p>
     *  </p>
     */   
    public void run(){
        edu.uci.ece.zen.orb.WaitingStrategy waitingStrategy = ((edu.uci.ece.zen.orb.WaitingStrategy) waiterRegion.getPortal());
        //System.err.println( "Waiter found: " + waitingStrategy );
        //System.err.println( "Reply is " + message );
        CDRInputStream inp = message.getCDRInputStream();
        waitingStrategy.replyReceived( message );
    }
}
