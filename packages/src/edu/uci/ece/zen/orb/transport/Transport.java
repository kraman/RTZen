package edu.uci.ece.zen.orb.transport;

import javax.realtime.*;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;

public abstract class Transport implements Runnable{
    private Object waitObj;
    protected edu.uci.ece.zen.orb.ORB orb;
    protected edu.uci.ece.zen.orb.ORBImpl orbImpl;
    private MessageProcessor messageProcessor;

    /**
     * <p>
     *     ORBImpl region --&gt; <b>Transport scope</b>
     * </p>
     */
    public Transport( edu.uci.ece.zen.orb.ORB orb , edu.uci.ece.zen.orb.ORBImpl orbImpl ){
        this.orb = orb;
        this.orbImpl = orbImpl;
        waitObj = new Integer(0);
        if(ZenProperties.devDbg) System.out.println( "Transport being created " + RealtimeThread.getCurrentMemoryArea() );
    }

    /**
     * <p>
     *     ORBImpl region --&gt; <b>Transport scope</b>
     * </p>
     */
    public final void run(){
        messageProcessor = new MessageProcessor( this , orb );

        if(ZenProperties.devDbg) System.out.println( RealtimeThread.getCurrentMemoryArea() );
        if(ZenProperties.devDbg) System.out.println( MemoryArea.getMemoryArea(messageProcessor) );
        if(ZenProperties.devDbg) System.out.println( MemoryArea.getMemoryArea(this) );
        
        RealtimeThread messageProcessorThr = new NoHeapRealtimeThread(null,null,null,RealtimeThread.getCurrentMemoryArea(),null,messageProcessor );
        
        messageProcessorThr.setDaemon( true );
		if (edu.uci.ece.zen.utils.ZenProperties.devDbg) {
			System.out.println(javax.realtime.RealtimeThread.getCurrentMemoryArea());
			System.out.println(javax.realtime.MemoryArea.getMemoryArea(messageProcessorThr) );
		}
        messageProcessorThr.start();
        
        try{
            synchronized( waitObj ){
                waitObj.wait();
            }
        }catch( InterruptedException ie ){
            ie.printStackTrace();
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
        if(ZenProperties.devDbg) System.out.println( "Transport.java/MessageProcessor, the current memory scope is: " + RealtimeThread.getCurrentMemoryArea() );
        this.trans = trans;
        this.orb = orb;
    }

    public void run(){
        isActive = true;
		if(ZenProperties.devDbg) {
			System.out.println(javax.realtime.RealtimeThread.getCurrentMemoryArea()); 
		}
        GIOPMessageRunnable gmr = new GIOPMessageRunnable( orb , trans );
         
        ExecuteInRunnable eir = new ExecuteInRunnable();
         

        while( isActive ){
             
            ScopedMemory messageScope = ORB.getScopedRegion();
             
            gmr.setRequestScope( messageScope );

            eir.init( gmr , messageScope );
             
            try{
                //messageScope.enter(gmr);
                orb.orbImplRegion.executeInArea( eir );
            }catch( Exception e ){
                ZenProperties.logger.log(
                    Logger.SEVERE,
                    "edu.uci.ece.zen.orb.transport.MessageProcessor",
                    "run()",
                    "Could not process message due to exception: " + e.toString()
                    );
                e.printStackTrace();
            }
            gmr.setRequestScope( null );
            ORB.freeScopedRegion( messageScope );
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
			if(ZenProperties.devDbg) {
				System.out.println(javax.realtime.RealtimeThread.getCurrentMemoryArea()); 
			}
            edu.uci.ece.zen.utils.Logger.printThreadStack();
            edu.uci.ece.zen.orb.giop.GIOPMessage message = edu.uci.ece.zen.orb.giop.GIOPMessageFactory.parseStream( orb , trans );
            if( message instanceof edu.uci.ece.zen.orb.giop.type.RequestMessage ){
               
                if(ZenProperties.devDbg) System.out.println( trans );
                if(ZenProperties.devDbg) System.out.println( trans.orbImpl );
                if(ZenProperties.devDbg) System.out.println( trans.orbImpl.getServerRequestHandler() );
                if(ZenProperties.devDbg) System.out.println( message );

                trans.orbImpl.getServerRequestHandler().handleRequest( (edu.uci.ece.zen.orb.giop.type.RequestMessage) message );
            }
            if( message instanceof edu.uci.ece.zen.orb.giop.type.ReplyMessage ){
                ScopedMemory waiterRegion = orb.getWaiterRegion( message.getRequestId() );
                WaitingStratergyNotifyRunnable wsnr = new WaitingStratergyNotifyRunnable( message , waiterRegion );
                ExecuteInRunnable eir = new ExecuteInRunnable();
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
        CDRInputStream inp = message.getCDRInputStream();
        waitingStrategy.replyReceived( message );
    }
}
