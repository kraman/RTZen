package edu.uci.ece.zen.orb;

import javax.realtime.*;
import edu.uci.ece.zen.utils.*;

public class MessageComposerRunnable implements Runnable {
    ClientRequest clr;

    CDRInputStream reply;

    boolean success;

    private static MessageComposerRunnable inst;

    public MessageComposerRunnable(){}

    public static MessageComposerRunnable instance() {
        if (inst == null) {
            try {
                inst = (MessageComposerRunnable) ImmortalMemory.instance()
                        .newInstance(MessageComposerRunnable.class);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.FATAL, MessageComposerRunnable.class, "instance", e);
            }
        }
        return inst;
    }

    /**
     * Client upcall:
     * <p>
     * <b>Client scope </b> --ex in --&gt; ORB parent scope --&gt; ORB scope
     * --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public void init(ClientRequest clr) {
        this.clr = clr;
    }

    /**
     * Client upcall:
     * <p>
     * Client scope --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt;
     * <b>Message scope/Waiter region </b> --&gt; Transport scope
     * </p>
     */
    private int statCount = 0;
    public void run() {

            if (statCount % ZenProperties.MEM_STAT_COUNT == 0) {
                edu.uci.ece.zen.utils.Logger.printMemStats(6);
            }
            statCount++;

        //setup waiting straterg
        WaitingStrategy waitingStrategy = null;
        if (clr.responseExpected) {
            //waitingStrategy = TwoWayWaitingStrategy.instance();//new
            // TwoWayWaitingStrategy();
            //TODO: Krishna, make sure this is correct, had to do this to solve
            // mem leak
            waitingStrategy = (WaitingStrategy) (((ScopedMemory) RealtimeThread
                    .getCurrentMemoryArea()).getPortal());
            if (waitingStrategy == null) {
                waitingStrategy = new TwoWayWaitingStrategy();
                ((ScopedMemory) RealtimeThread.getCurrentMemoryArea())
                        .setPortal(waitingStrategy);
            }
            clr.registerWaiter();
        }

        ExecuteInRunnable eir = clr.orb.getEIR();//new ExecuteInRunnable();
        SendMessageRunnable smr = SendMessageRunnable.instance();
        smr.init(clr.transportScope);
        smr.init(clr.out.getBuffer());
        eir.init(smr, clr.transportScope);
        success = true;
        //edu.uci.ece.zen.utils.Logger.printMemStatsImm(383);
        try {
            ZenProperties.logger.log("MCR run 1");
            clr.orb.orbImplRegion.executeInArea(eir);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.SEVERE,
                    getClass(), "run",
                    "Could not sent message on transport", e);
            clr.releaseWaiter();
            waitingStrategy = null;
            success = false;
        } finally {
            clr.out.free();
            //edu.uci.ece.zen.utils.Logger.printMemStatsImm(384);
        }
        ZenProperties.logger.log("MCR run 2");

        if (waitingStrategy != null) {
            reply = waitingStrategy.waitForReply();

            clr.releaseWaiter();
        }

        ZenProperties.logger.log("MCR run 3");
        clr.orb.freeEIR( eir );
      
    }

    public CDRInputStream getReply() {
        return reply;
    }
}

