/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.orb.protocol.type.CancelRequestMessage;
import edu.uci.ece.zen.orb.protocol.type.LocateReplyMessage;
import edu.uci.ece.zen.orb.protocol.type.LocateRequestMessage;
import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.utils.FString;

public abstract class ServerRequestHandler {
    protected ORB orb;

    public ServerRequestHandler() {
    }

    public void init(ORB orb) {
        this.orb = orb;
    }

    public abstract int addPOA(FString path, org.omg.PortableServer.POA poa);

    /**
     * Call scoped region graph:
     * <p>
     * Transport thread: <br/>
     * <p>
     * Transport scope --ex in--&gt; ORBImpl scope --&gt; <b>Message </b> ==>
     * POA regions
     * </p>
     * </p>
     */
    public abstract void handleRequest(RequestMessage req);

    public abstract LocateReplyMessage handleLocateRequest(
            LocateRequestMessage req);

    public abstract void handleCancelRequest(CancelRequestMessage req);

    public abstract org.omg.CORBA.Object getRoot();

    public abstract void initiateShutDown(boolean waitForRequestsToComplete);
    /*
     * public abstract edu.uci.ece.zen.poa.POA find_POA(String poaName); public
     * abstract ObjectLocation locateObject(edu.uci.ece.zen.poa.ObjectKey key);
     */
}
