/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.protocol.giop.v1_1;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;
import javax.realtime.ImmortalMemory;

/**
 * GIOP v1.1 LocateRequest Message as discussed in section 15.4.6 of the CORBA v3.0 specification. 
 * @see edu.uci.ece.zen.orb.giop.v1_0.LocateRequestMessage
 * 
 * @author bmiller
*/
public class LocateRequestMessage extends  edu.uci.ece.zen.orb.protocol.type.LocateRequestMessage {
    protected LocateRequestHeader_1_0 header;
    private static LocateRequestMessage lrm;
    
    public LocateRequestMessage(ORB orb, ReadBuffer stream) {
        super (orb, stream);  // I need to call GIOPMessage's constructor here, so I couldn't derive from v1.0's LocateRequestMessage
        header = LocateRequestHeader_1_1Helper.read(istream);
        messageBody = null;
    }

    public int getRequestId() { return header.request_id; }


    public static LocateRequestMessage getMessage()
    {
        try
        {
            if (lrm == null)
                lrm = (LocateRequestMessage) ImmortalMemory.instance().newInstance(LocateRequestMessage.class);
            return lrm;
        }catch (Exception e)
        {
            ZenProperties.logger.log(Logger.WARN, LocateRequestMessage.class, "getMessage", e);
        }
        return null;
    }
    /**
     * @see edu.uci.ece.zen.orb.giop.GIOPMessage#marshal(edu.uci.ece.zen.orb.CDROutputStream)
     */
    public void marshal(CDROutputStream out) {
        // LocateRequest messages contain only a GIOP header then the LocateRequestHeader
        LocateRequestHeader_1_1Helper.write(out, header);
    }

    public int getVersion(){ return 11; }
    public void internalFree(){};
}
