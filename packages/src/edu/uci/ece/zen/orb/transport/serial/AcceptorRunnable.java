package edu.uci.ece.zen.orb.transport.serial;

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

    private int statCount = 0;
    public void run() {
            if (statCount % 100 == 0) {
                edu.uci.ece.zen.utils.Logger.printMemStats(7);
            }
             statCount++;

        ScopedMemory acceptorArea = (ScopedMemory) RealtimeThread .getCurrentMemoryArea();
        orb.getAcceptorRegistry().addAcceptor(acceptorArea);
        Acceptor acceptor = new edu.uci.ece.zen.orb.transport.serial.Acceptor( 
                orb, (ORBImpl) ((ScopedMemory) orb.orbImplRegion).getPortal());
        acceptorArea.setPortal(acceptor);
        acceptor.startAccepting();
    }

}