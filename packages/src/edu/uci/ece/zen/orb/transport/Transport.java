package edu.uci.ece.zen.orb.transport;

import javax.realtime.MemoryArea;
import javax.realtime.NoHeapRealtimeThread;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.CDRInputStream;
import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.ExecuteInRunnable;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.poa.POARunnable;
import edu.uci.ece.zen.orb.protocol.ProtocolHeaderInfo;

public abstract class Transport implements Runnable {
    private Object waitObj;
    protected edu.uci.ece.zen.orb.ORB orb;
    protected edu.uci.ece.zen.orb.ORBImpl orbImpl;
    private MessageProcessor messageProcessor;
    public Object objectTable[]; //used to store misc

    // objects with
    // 1instance

    // per transport

    // 0 = POARunnable for POA.handleRequest
    // 1 = ExecuteInRunnable for POA.handleRequest
    // 2 = Used for GIOP header byte array
    // 3 = Protocol Header


    /* This method helps to get the object stored in objectTable[]*/
    public Object getObject(int num){
        try{
            if(num != 0 && num != 1 && num != 2 && num != 3){
                ZenProperties.logger.log(Logger.WARN, Transport.class, "static <getObject>", "Wrong objectTable index in Transport.java");
                return null;
            }
            return objectTable[num];
        }
        catch(Exception ex){
            ZenProperties.logger.log(Logger.WARN, Transport.class, "static <getObject>", ex);
            return null;
        }
    }

    public void setObject(Object obj, int num){
        if((obj != null)&&( num == 0 || num == 1 || num == 2 || num ==3 )){
            objectTable[num] = obj;
        }
        else{
            ZenProperties.logger.log(Logger.WARN, Transport.class, "static <setObject>", "Wrong objectTable index in Transport.java or object is null");
        }
    }

    /**
     * <p>
     * ORBImpl region --&gt; <b>Transport scope </b>
     * </p>
     */
    public Transport(edu.uci.ece.zen.orb.ORB orb,
            edu.uci.ece.zen.orb.ORBImpl orbImpl) {
        this.orb = orb;
        this.orbImpl = orbImpl;
        waitObj = new Integer(0);
        objectTable = new Object[4];
        objectTable[2] = new byte[12];
        if (ZenProperties.dbg) ZenProperties.logger.log("Transport being created "
                + RealtimeThread.getCurrentMemoryArea());
    }

    public byte[] getGIOPHeader() {
        return (byte[]) objectTable[2];
    }

    /**
     * <p>
     * ORBImpl region --&gt; <b>Transport scope </b>
     * </p>
     */
    public final void run() {
        messageProcessor = new MessageProcessor(this, orb);

        if (ZenProperties.dbg) ZenProperties.logger.log(RealtimeThread
                .getCurrentMemoryArea().toString());
        if (ZenProperties.dbg) ZenProperties.logger.log(MemoryArea
                .getMemoryArea(messageProcessor).toString());
        if (ZenProperties.dbg) ZenProperties.logger.log(MemoryArea
                .getMemoryArea(this).toString());

        RealtimeThread messageProcessorThr = new RealtimeThread(null,
                null, null, RealtimeThread.getCurrentMemoryArea(), null,
                messageProcessor);

        messageProcessorThr.setDaemon(true);


        if (ZenProperties.dbg) ZenProperties.logger.log(javax.realtime.RealtimeThread
                    .getCurrentMemoryArea().toString());
        if (ZenProperties.dbg) ZenProperties.logger.log(javax.realtime.MemoryArea
                    .getMemoryArea(messageProcessorThr).toString());
        messageProcessorThr.start();
        try {
            synchronized (waitObj) {
                waitObj.wait();

            }
        } catch (InterruptedException ie) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "run", ie);
        }
        edu.uci.ece.zen.orb.ORB.freeScopedRegion( (ScopedMemory) RealtimeThread.getCurrentMemoryArea() );
    }

    /**
     * <p>
     * ORBImpl region --&gt; <b>Transport scope </b>
     * </p>
     */
    public final void shutdown(boolean waitForCompletion) {
        if (waitForCompletion) {
            messageProcessor.shutdown();
        }
        waitObj.notifyAll();
    }

    public abstract java.io.InputStream getInputStream();

    protected abstract java.io.OutputStream getOutputStream();

    /**
     * <p>
     * ORBImpl region --&gt; <b>Transport scope </b>
     * </p>
     */
    public synchronized final void send(WriteBuffer msg) {
        ZenProperties.logger.log("Transport send 1");
        //if(ZenProperties.devDbg) {
        //    ZenProperties.logger.log("Transport sending: " + toString() + " " + msg.toString());
        //}
        try {
            java.io.OutputStream out = getOutputStream();
            msg.dumpBuffer(out);
            out.flush();
        } catch (java.io.IOException ioex) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "send", ioex);
        }
    }

    private Class protocolFactory;
    public void setProtocolFactory( Class pf ){ this.protocolFactory = pf; }
    public Class getProtocolFactory(){ return this.protocolFactory; }
}

/**
 * Call scoped region graph:
 * <p>
 * Transport thread: <br/>
 * <p>
 * <b>Transport scope </b> --ex in--&gt; ORBImpl scope --&gt; Message --ex
 * in--&gt; ORBImpl scope --&gt; Waiter region
 * </p>
 * Client Thread: <br/>
 * <p>
 * Client scope --ex in --&gt; ORB parent scope --&gt; ORBImpl scope --&gt;
 * <b>Message scope/Waiter region </b> --&gt; Transport scope
 * </p>
 * </p>
 */

class MessageProcessor implements Runnable {
    private Transport trans;

    private edu.uci.ece.zen.orb.ORB orb;

    private boolean isActive;

    public MessageProcessor(Transport trans, edu.uci.ece.zen.orb.ORB orb) {
        if (ZenProperties.dbg) ZenProperties.logger.log("Transport.java/MessageProcessor, the current memory scope is: "
                        + RealtimeThread.getCurrentMemoryArea());
        this.trans = trans;
        this.orb = orb;
    }

    public void run() {
        isActive = true;
        if (ZenProperties.dbg) ZenProperties.logger.log(javax.realtime.RealtimeThread.getCurrentMemoryArea().toString());
        GIOPMessageRunnable gmr = new GIOPMessageRunnable(orb, trans);

        //ExecuteInRunnable eir = new ExecuteInRunnable();

        while (isActive) {
            //ScopedMemory messageScope = ORB.getScopedRegion();
            // Check here to see if we allocate the memmory here
            //ImmortalMemory messageScope = ORB.getScopedRegion();
            //gmr.setRequestScope( messageScope );
            //eir.init( gmr , messageScope );

            try {
                //messageScope.enter(gmr);
                //messageScope.enter(gmr);
                gmr.run();
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.SEVERE, getClass(), "run", "Could not process message", e);
        e.printStackTrace();
            }
            gmr.setRequestScope(null);
        }
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void shutdown() {
        isActive = false;
        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException ie) {
            //Ignore
        }
    }
}

class GIOPMessageRunnable implements Runnable {
    edu.uci.ece.zen.orb.ORB orb;

    Transport trans;

    ScopedMemory requestScope;

    WaitingStrategyNotifyRunnable wsnr;

    ExecuteInRunnable eir;

    public GIOPMessageRunnable(edu.uci.ece.zen.orb.ORB orb, Transport trans) {
        this.orb = orb;
        this.trans = trans;
        eir = new ExecuteInRunnable();
        wsnr = new WaitingStrategyNotifyRunnable();
    }

    public void setRequestScope(ScopedMemory requestScope) {
        this.requestScope = requestScope;
    }

    private int statCount = 0;

    private int count = 300;

    //private static final String name = "Trans: ";
    public void run() {
        //edu.uci.ece.zen.utils.Logger.printMemStats(301);

        edu.uci.ece.zen.orb.protocol.Message message = null;

        //edu.uci.ece.zen.utils.Logger.printMemStats(302);


        edu.uci.ece.zen.utils.Logger.printMemStatsImm(2220);

 //       edu.uci.ece.zen.utils.Logger.printMemStats(303);

        try {

            statCount++;
 //         edu.uci.ece.zen.utils.Logger.printMemStats(304);

            if (statCount % ZenProperties.MEM_STAT_COUNT == 0) {
                //System.out.print(name);
                edu.uci.ece.zen.utils.Logger.printMemStats(3);
                edu.uci.ece.zen.utils.Logger.printMemStats(orb);
            }
 //         edu.uci.ece.zen.utils.Logger.printMemStats(305);

            ZenProperties.logger.log("Inside GMR run");
            if (ZenProperties.dbg) ZenProperties.logger.log(RealtimeThread.getCurrentMemoryArea().toString());
 //         edu.uci.ece.zen.utils.Logger.printMemStats(306);

            message = edu.uci.ece.zen.orb.protocol.MessageFactory.parseStream(orb, trans);

            if( message == null )   //connection closed
            {
                trans.shutdown(true);
                return;
            }

 //         edu.uci.ece.zen.utils.Logger.printMemStats(307);

            if (message instanceof edu.uci.ece.zen.orb.protocol.type.RequestMessage) {
                ZenProperties.logger.log("Inside GMR run: RequestMessage");
 //             edu.uci.ece.zen.utils.Logger.printMemStats(308);
                trans.orbImpl.getServerRequestHandler().handleRequest( (edu.uci.ece.zen.orb.protocol.type.RequestMessage) message);
 //             edu.uci.ece.zen.utils.Logger.printMemStats(309);


            }else if (message instanceof edu.uci.ece.zen.orb.protocol.type.LocateRequestMessage) {
                //this is provisional until we get it working right
                //just return OBJECT_HERE for now
        //edu.uci.ece.zen.utils.Logger.printMemStats(310);
            CDROutputStream out = edu.uci.ece.zen.orb.protocol.MessageFactory.constructLocateReplyMessage(orb, (edu.uci.ece.zen.orb.protocol.type.LocateRequestMessage)message);
 //               edu.uci.ece.zen.utils.Logger.printMemStats(311);
            trans.send(out.getBuffer());
 //               edu.uci.ece.zen.utils.Logger.printMemStats(312);
            out.free();
 //               edu.uci.ece.zen.utils.Logger.printMemStats(313);
            }else if (message instanceof edu.uci.ece.zen.orb.protocol.type.ReplyMessage) {
                ZenProperties.logger.log("Inside GMR run: ReplyMessage");
 //             edu.uci.ece.zen.utils.Logger.printMemStats(314);
                ScopedMemory waiterRegion = orb.getWaiterRegion(message.getRequestId());
                if( waiterRegion == null ){
                    ZenProperties.logger.log( Logger.WARN, getClass(), "run", "ODD: Waiter region is missing");
                    return;
                }
 //             edu.uci.ece.zen.utils.Logger.printMemStats(315);
                wsnr.init(message, waiterRegion);
 //             edu.uci.ece.zen.utils.Logger.printMemStats(316);
                eir.init(wsnr, waiterRegion);
 //             edu.uci.ece.zen.utils.Logger.printMemStats(317);
                try {
 //                 edu.uci.ece.zen.utils.Logger.printMemStats(318);
                    orb.orbImplRegion.executeInArea(eir);
 //                 edu.uci.ece.zen.utils.Logger.printMemStats(319);
                } catch (Exception e) {
                    ZenProperties.logger.log( Logger.SEVERE, getClass(), "run", "Could not process reply message", e);
                }
            }else{
                    ZenProperties.logger.log(Logger.SEVERE, getClass(), "run", "Message type not supported.");
            }
            //edu.uci.ece.zen.utils.Logger.printMemStatsImm(2223);
        } catch (java.io.IOException ioex) {
            //TODO: do something here
            if(ZenProperties.devDbg)
                System.out.println("Exception");
            try{
                Thread.currentThread().sleep(1000);
            }catch(Exception e){
                e.printStackTrace();
            }
            ZenProperties.logger.log(Logger.SEVERE, getClass(), "run", ioex);
        }
    }
}

class WaitingStrategyNotifyRunnable implements Runnable {
    edu.uci.ece.zen.orb.protocol.Message message;

    ScopedMemory waiterRegion;

    /**
     * Call scoped region graph:
     * <p>
     * Transport thread: <br/>
     * <p>
     * Transport scope --ex in--&gt; ORBImpl scope --&gt; <b>Message </b> --ex
     * in--&gt; ORBImpl scope --&gt; Waiter region
     * </p>
     * Client Thread: <br/>
     * <p>
     * Client scope --ex in --&gt; ORB parent scope --&gt; ORBImpl scope --&gt;
     * <b>Message scope/Waiter region </b> --&gt; Transport scope
     * </p>
     * </p>
     */
    public WaitingStrategyNotifyRunnable() {
    }

    public void init(edu.uci.ece.zen.orb.protocol.Message message,
            ScopedMemory waiterRegion) {
        this.message = message;
        this.waiterRegion = waiterRegion;
    }

    /**
     * Call scoped region graph:
     * <p>
     * Transport thread: <br/>
     * <p>
     * Transport scope --ex in--&gt; ORBImpl scope --&gt; Message --ex in--&gt;
     * ORBImpl scope --&gt; <b>Waiter region </b>
     * </p>
     * Client Thread: <br/>
     * <p>
     * Client scope --ex in --&gt; ORB parent scope --&gt; ORBImpl scope --&gt;
     * <b>Message scope/Waiter region </b> --&gt; Transport scope
     * </p>
     * </p>
     */
    public void run() {
        edu.uci.ece.zen.orb.WaitingStrategy waitingStrategy = ((edu.uci.ece.zen.orb.WaitingStrategy) waiterRegion
                .getPortal());
        CDRInputStream inp = message.getCDRInputStream();
        waitingStrategy.replyReceived(message);
    }
}
