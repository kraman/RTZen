/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.transport;

import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.ORBImpl;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.ExecuteInRunnable;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.FString;

public abstract class Connector {
    public Connector() {
    }

    public final ScopedMemory connect(FString host, short port, edu.uci.ece.zen.orb.ORB orb, ORBImpl orbImpl) {
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
            ZenProperties.logger.log(Logger.WARN, getClass(), "connect", e);
        }
        orbImpl.eirCache.enqueue(eir);
        orbImpl.eirCache.enqueue(eir2);
        orbImpl.crCache.enqueue(connRunnable);
        if( !connRunnable.getReturnStatus() ){
            ORB.freeScopedRegion( transportMem );
            return null;
        }
        return transportMem;
    }

    protected abstract Transport internalConnect(String host, int port,
            edu.uci.ece.zen.orb.ORB orb, edu.uci.ece.zen.orb.ORBImpl orbImpl);
}

