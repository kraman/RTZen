/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.protocol.type;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.protocol.Message;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ReadBuffer;

/**
 * Top level representation ofor a GIOP RequestMessage Message, as described in
 * CORBA v3.0 spec, section 15.4.2
 * 
 * @author bmiller
 * @author Krishna Raman
 */
public abstract class RequestMessage extends Message {
    public RequestMessage() {
        super();
    }

    public RequestMessage(ORB orb, ReadBuffer stream) {
        super(orb, stream);
    }

    public void init(ORB orb, ReadBuffer stream) {
        super.init(orb, stream);
    }

    Object poa;

    public void associatePOA(Object poa) {
        this.poa = poa;
    }

    public Object getAssociatedPOA() {
        return poa;
    }

    // Abstract declarations in addition to those in GIOPMessage
    public abstract FString getServiceContexts();

    public abstract FString getObjectKey();

    public abstract FString getOperation();

    public abstract int getResponseExpected();
}
