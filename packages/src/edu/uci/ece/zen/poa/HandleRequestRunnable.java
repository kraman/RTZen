/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.poa;

import org.omg.CORBA.IntHolder;

import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

public class HandleRequestRunnable implements Runnable {
    POA poa;

    RequestMessage req;

    IntHolder exceptionValue = new IntHolder(0);

    public void init(RequestMessage req) {
        this.req = req;
        this.poa = (POA) req.getAssociatedPOA();
    }

    public void run() {
        POAImpl pimpl = ((POAImpl) poa.poaMemoryArea.getPortal());
        try {
            pimpl.requestProcessingStrategy.handleRequest(req, poa, poa.numberOfCurrentRequests, exceptionValue);
        } catch (Throwable e) {
	    //e.printStackTrace();
            ZenProperties.logger.log(Logger.WARN, getClass(), "run", e);
        }
    }
}

