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
 * CORBA v3.0 spec, section 15.4.3. Fill with any functionality desired to be
 * common with ReplyMessage classes.
 * 
 * @author bmiller
 */
public abstract class ReplyMessage extends Message {
    public ReplyMessage() {
    }

    public ReplyMessage(ORB orb, ReadBuffer stream) {
        super(orb, stream);
    }

    public void init(ORB orb, ReadBuffer stream) {
        super.init(orb, stream);
    }

    // Abstract declarations in addition to those in Message
    public abstract int getReplyStatus();
    public abstract FString getServiceContexts();
    public abstract void release();
}
