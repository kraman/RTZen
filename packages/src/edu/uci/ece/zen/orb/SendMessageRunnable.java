package edu.uci.ece.zen.orb;

import javax.realtime.ImmortalMemory;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.utils.WriteBuffer;

public class SendMessageRunnable implements Runnable {
    WriteBuffer msg;

    ScopedMemory transScope;

    private static SendMessageRunnable inst;

    public static SendMessageRunnable instance() {
        if (inst == null) {
            try {
                inst = (SendMessageRunnable) ImmortalMemory.instance()
                        .newInstance(SendMessageRunnable.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return inst;
    }

    public void init(ScopedMemory transScope) {
        this.transScope = transScope;
    }

    /**
     * Client upcall:
     * <p>
     * Client scope --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt;
     * <b>Message scope/Waiter region </b> --&gt; Transport scope
     * </p>
     */
    public void init(WriteBuffer buffer) {
        this.msg = buffer;
    }

    /**
     * Client upcall:
     * <p>
     * Client scope --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt;
     * Message scope/Waiter region --&gt; <b>Transport scope </b>
     * </p>
     */
    public void run() {
        //edu.uci.ece.zen.orb.transport.Transport trans =
        // (edu.uci.ece.zen.orb.transport.Transport) transScope.getPortal();
        edu.uci.ece.zen.orb.transport.Transport trans = (edu.uci.ece.zen.orb.transport.Transport) ((ScopedMemory) RealtimeThread
                .getCurrentMemoryArea()).getPortal();
        trans.send(msg);
    }
}