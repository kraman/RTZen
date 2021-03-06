/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.protocol.giop_lite;

import javax.realtime.ImmortalMemory;

import org.omg.GIOP.LocateRequestHeader_1_0;
import org.omg.GIOP.LocateRequestHeader_1_0Helper;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.*;

/**
 * GIOP v1.0 LocateRequest Message as discussed in section 15.4.6 of the CORBA
 * v3.0 specification.
 * 
 * @author bmiller
 */
public class LocateRequestMessage extends
        edu.uci.ece.zen.orb.protocol.type.LocateRequestMessage {
    protected LocateRequestHeader_1_0 header;

    private static LocateRequestMessage lrm;

    /**
     * Constructor
     * 
     * @param orb
     *            ORB to associate this message with
     * @param stream
     *            ReadBuffer populated with the header of the message
     */

    public LocateRequestMessage() {
    }

    public LocateRequestMessage(ORB orb, ReadBuffer stream) {
        super(orb, stream);
        header = LocateRequestHeader_1_0Helper.read(istream);
        messageBody = null; // LocateRequestMessages have no body.
    }

    public void init(ORB orb, ReadBuffer stream) {
        super.init(orb, stream);
        header = LocateRequestHeader_1_0Helper.read(istream);
        messageBody = null; // LocateRequestMessages have no body.
    }

    public int getRequestId() {
        return header.request_id;
    }

    public static LocateRequestMessage getMessage() {
        try {
            if (lrm == null) lrm = (LocateRequestMessage) ImmortalMemory
                    .instance().newInstance(LocateRequestMessage.class);
            return lrm;
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, LocateRequestMessage.class, "getMessage", e);
        }
        return null;
    }

    /**
     * @see edu.uci.ece.zen.orb.giop.GIOPMessage#marshal(edu.uci.ece.zen.orb.CDROutputStream)
     */
    public void marshal(CDROutputStream out) {
        // LocateRequest messages contain only a GIOP header then the
        // LocateRequestHeader
        LocateRequestHeader_1_0Helper.write(out, header);
    }

    public int getVersion() {
        return 10;
    }

    public void internalFree(){}
}
