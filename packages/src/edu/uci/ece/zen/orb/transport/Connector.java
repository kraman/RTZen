package edu.uci.ece.zen.orb.transport;

import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.ORBImpl;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.ExecuteInRunnable;

public abstract class Connector {
    public Connector() {
    }

    public final ScopedMemory connect(String host, short port,
            edu.uci.ece.zen.orb.ORB orb, ORBImpl orbImpl) {

        ExecuteInRunnable eir = (ExecuteInRunnable) orbImpl.eirCache.dequeue();
        if (eir == null) eir = new ExecuteInRunnable();
        ExecuteInRunnable eir2 = (ExecuteInRunnable) orbImpl.eirCache.dequeue();
        if (eir2 == null) eir2 = new ExecuteInRunnable();
        ConnectorRunnable connRunnable = (ConnectorRunnable) orbImpl.crCache
                .dequeue();
        if (connRunnable == null) connRunnable = new ConnectorRunnable();

        ScopedMemory transportMem = ORB.getScopedRegion();
        connRunnable.init(host, port, this, orb);
        eir.init(eir2, orb.orbImplRegion);
        eir2.init(connRunnable, transportMem);
        try {
            orb.parentMemoryArea.executeInArea(eir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        orbImpl.eirCache.enqueue(eir);
        orbImpl.eirCache.enqueue(eir2);
        orbImpl.crCache.enqueue(connRunnable);
        return transportMem;
    }

    protected abstract Transport internalConnect(String host, int port,
            edu.uci.ece.zen.orb.ORB orb, edu.uci.ece.zen.orb.ORBImpl orbImpl);
}

