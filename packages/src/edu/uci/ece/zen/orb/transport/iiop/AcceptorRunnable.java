package edu.uci.ece.zen.orb.transport.iiop;

import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.ORBImpl;

public class AcceptorRunnable implements Runnable {

    ORB orb;
    int priority;
    public ScopedMemory acceptorArea;

    public AcceptorRunnable() {

    }

    public void init(ORB orb, int priority ) {
        this.orb = orb;
        this.priority = priority;
    }

    private int statCount = 0;
    public void run() {
        if (statCount % edu.uci.ece.zen.utils.ZenProperties.MEM_STAT_COUNT == 0) {
            edu.uci.ece.zen.utils.Logger.printMemStats(7);
        }
        statCount++;

        acceptorArea = (ScopedMemory) RealtimeThread.getCurrentMemoryArea();
        orb.getAcceptorRegistry().addAcceptor(acceptorArea);
        Acceptor acceptor = new edu.uci.ece.zen.orb.transport.iiop.Acceptor(
                orb, (ORBImpl) ((ScopedMemory) orb.orbImplRegion).getPortal());
        acceptorArea.setPortal(acceptor);
        acceptor.startAccepting( priority );
    }

}
