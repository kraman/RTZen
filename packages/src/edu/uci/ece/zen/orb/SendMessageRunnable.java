package edu.uci.ece.zen.orb;

import javax.realtime.*;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.utils.*;

public class SendMessageRunnable implements Runnable{
    WriteBuffer msg;

    public SendMessageRunnable(){}

    /**
     * Client upcall:
     * <p>
     *     Client scope --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt; <b>Message scope/Waiter region</b> --&gt; Transport scope
     * </p>
     */
    public void init( WriteBuffer buffer ){
        this.msg = buffer;
    }

    /**
     * Client upcall:
     * <p>
     *     Client scope --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt; Message scope/Waiter region --&gt; <b>Transport scope</b>
     * </p>
     */
    public void run(){
        edu.uci.ece.zen.orb.transport.Transport trans = (edu.uci.ece.zen.orb.transport.Transport) ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).getPortal();
        trans.send( msg );
    }
}
