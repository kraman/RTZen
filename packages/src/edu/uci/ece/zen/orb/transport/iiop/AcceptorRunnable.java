package edu.uci.ece.zen.orb.transport.iiop;

import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.ORBImpl;

public class AcceptorRunnable implements Runnable {

    ORB orb;

    public AcceptorRunnable() {

    }

    public void init(ORB orb) {
        this.orb = orb;
    }

    public void run() {
        ScopedMemory acceptorArea = (ScopedMemory) RealtimeThread
                .getCurrentMemoryArea();
        orb.getAcceptorRegistry().addAcceptor(acceptorArea);
        Acceptor acceptor = new edu.uci.ece.zen.orb.transport.iiop.Acceptor(
                orb, (ORBImpl) ((ScopedMemory) orb.orbImplRegion).getPortal());
        acceptorArea.setPortal(acceptor);
        acceptor.startAccepting();
    }

}