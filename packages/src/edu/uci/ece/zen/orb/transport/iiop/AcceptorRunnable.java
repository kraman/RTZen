package edu.uci.ece.zen.orb.transport.iiop;

import org.omg.RTCORBA.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.transport.*;
import edu.uci.ece.zen.utils.*;
import javax.realtime.*;

public class AcceptorRunnable implements Runnable{

    ORB orb;
    ORBImpl orbImpl;

    public AcceptorRunnable(){

    }

    public void init(ORB orb, ORBImpl orbImpl){
        this.orb = orb;
        this.orbImpl = orbImpl;
    }

    public void run(){

        ScopedMemory acceptorArea = (ScopedMemory) RealtimeThread.getCurrentMemoryArea();

        orb.getAcceptorRegistry().addAcceptor(acceptorArea);
        Acceptor acceptor = new edu.uci.ece.zen.orb.transport.iiop.Acceptor(orb, orbImpl);
        acceptorArea.setPortal(acceptor);
        acceptor.startAccepting();
    }

}
