/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.orb.protocol.type.RequestMessage;

public class ResponseHandler implements org.omg.CORBA.portable.ResponseHandler {
    private RequestMessage req;

    private ORB orb;

    private static ResponseHandler rh;

    public ResponseHandler() {
    }

    public ResponseHandler(ORB orb, RequestMessage req) {
        this.orb = orb;
        this.req = req;
    }

    public void init(ORB orb, RequestMessage req) {
        this.orb = orb;
        this.req = req;
    }

    /**
     * This method is called by servant code to send back a reply.
     * 
     * @return a <code>ServerReply</code> which inherits from
     *         <code>CDROutputStream</code>.
     */
    public org.omg.CORBA.portable.OutputStream createReply() {
        return edu.uci.ece.zen.orb.protocol.MessageFactory.constructReplyMessage(orb, req);
    }

    /**
     * this method is called by the Servant Code and indicates the case where an
     * User Exception has ocured.
     * 
     * @return a <code>ServerReply</code> which inherits from
     *         <code>CDROutputStream</code>.
     */
    public org.omg.CORBA.portable.OutputStream createExceptionReply() {
        return edu.uci.ece.zen.orb.protocol.MessageFactory.constructExceptionMessage(orb, req);
    }
}
