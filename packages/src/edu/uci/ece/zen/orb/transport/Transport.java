package edu.uci.ece.zen.orb.transport;

import javax.realtime.MemoryArea;
import javax.realtime.NoHeapRealtimeThread;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.CDRInputStream;
import edu.uci.ece.zen.utils.ExecuteInRunnable;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.utils.ZenProperties;

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
    // 2 = GIOP Header

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
        objectTable = new Object[3];
        objectTable[2] = new byte[12];
        if (ZenProperties.devDbg) System.out.println("Transport being created "
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

        if (ZenProperties.devDbg) System.out.println(RealtimeThread
                .getCurrentMemoryArea());
        if (ZenProperties.devDbg) System.out.println(MemoryArea
                .getMemoryArea(messageProcessor));
        if (ZenProperties.devDbg) System.out.println(MemoryArea
                .getMemoryArea(this));

        RealtimeThread messageProcessorThr = new NoHeapRealtimeThread(null,
                null, null, RealtimeThread.getCurrentMemoryArea(), null,
                messageProcessor);

        messageProcessorThr.setDaemon(true);
        if (edu.uci.ece.zen.utils.ZenProperties.devDbg) {
            System.out.println(javax.realtime.RealtimeThread
                    .getCurrentMemoryArea());
            System.out.println(javax.realtime.MemoryArea
                    .getMemoryArea(messageProcessorThr));
        }
        messageProcessorThr.start();

        try {
            synchronized (waitObj) {
                waitObj.wait();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
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
        if (ZenProperties.devDbg) System.out.println("Transport send 1");
        try {
            java.io.OutputStream out = getOutputStream();
            msg.dumpBuffer(out);
        } catch (java.io.IOException ioex) {
            ioex.printStackTrace();
        }
    }
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
        if (ZenProperties.devDbg) System.out
                .println("Transport.java/MessageProcessor, the current memory scope is: "
                        + RealtimeThread.getCurrentMemoryArea());
        this.trans = trans;
        this.orb = orb;
    }

    public void run() {
        isActive = true;
        if (ZenProperties.devDbg) {
            System.out.println(javax.realtime.RealtimeThread
                    .getCurrentMemoryArea());
        }
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
                ZenProperties.logger.log(Logger.SEVERE,
                        "edu.uci.ece.zen.orb.transport.MessageProcessor",
                        "run()", "Could not process message due to exception: "
                                + e.toString());
                e.printStackTrace();
            }
            gmr.setRequestScope(null);
            //ORB.freeScopedRegion( messageScope );
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

    WaitingStratergyNotifyRunnable wsnr;

    ExecuteInRunnable eir;

    public GIOPMessageRunnable(edu.uci.ece.zen.orb.ORB orb, Transport trans) {
        this.orb = orb;
        this.trans = trans;
        eir = new ExecuteInRunnable();
        wsnr = new WaitingStratergyNotifyRunnable();
    }

    public void setRequestScope(ScopedMemory requestScope) {
        this.requestScope = requestScope;
    }

    private int statCount = 0;

    private int count = 300;

    //private static final String name = "Trans: ";
    public void run() {
        try {

            statCount++;
            if (statCount % 100 == 0) {
                //System.out.print(name);
                edu.uci.ece.zen.utils.Logger.printMemStats(3);
                edu.uci.ece.zen.utils.Logger.printMemStats(orb);
            }

            if (ZenProperties.dbg) System.out
                    .println("Inside Transport and mem area: "
                            + RealtimeThread.getCurrentMemoryArea());
            edu.uci.ece.zen.orb.giop.GIOPMessage message = edu.uci.ece.zen.orb.giop.GIOPMessageFactory
                    .parseStream(orb, trans);
            if (message instanceof edu.uci.ece.zen.orb.giop.type.RequestMessage) {
                trans.orbImpl.getServerRequestHandler().handleRequest(
                        (edu.uci.ece.zen.orb.giop.type.RequestMessage) message);
            }
            if (message instanceof edu.uci.ece.zen.orb.giop.type.ReplyMessage) {
                ScopedMemory waiterRegion = orb.getWaiterRegion(message
                        .getRequestId());
                wsnr.init(message, waiterRegion);
                eir.init(wsnr, waiterRegion);
                try {
                    orb.orbImplRegion.executeInArea(eir);
                } catch (Exception e) {
                    ZenProperties.logger
                            .log(
                                    Logger.SEVERE,
                                    "edu.uci.ece.zen.orb.transport.GIOPMessageRunnable",
                                    "run()",
                                    "Could not process reply message due to exception: "
                                            + e.toString());
                }
            }
        } catch (java.io.IOException ioex) {
            //TODO: do something here
        }
    }
}

class WaitingStratergyNotifyRunnable implements Runnable {
    edu.uci.ece.zen.orb.giop.GIOPMessage message;

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
    public WaitingStratergyNotifyRunnable() {
    }

    public void init(edu.uci.ece.zen.orb.giop.GIOPMessage message,
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