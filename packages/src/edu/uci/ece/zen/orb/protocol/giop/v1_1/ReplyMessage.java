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
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;
import javax.realtime.ImmortalMemory;

/**
 * Reply messages as described in section 15.4.3 of the CORBA v3.0 Spec. 
 * @author bmiller
 */
// Is similar to v1_0 reply message.
public class ReplyMessage extends edu.uci.ece.zen.orb.protocol.type.ReplyMessage { 
    // v1_1 uses the same reply header type as v1_0.
    private ReplyHeader_1_0 header;
    private static ReplyMessage rm;

    public ReplyMessage( ORB orb , ReadBuffer stream ) {
        super( orb , stream );
        header = ReplyHeader_1_1Helper.read( istream );  // read method initializes header variable
        messageBody = stream;
    }

    public static ReplyMessage getMessage()
    {
        try
        {
            if (rm == null)
                rm = (ReplyMessage) ImmortalMemory.instance().newInstance(ReplyMessage.class);
            return rm;
        }catch (Exception e)
        {
            ZenProperties.logger.log(Logger.WARN, ReplyMessage.class, "getMessage", e);
        }
        return null;
    }
    public int getRequestId() { return header.request_id; }

    public int getReplyStatus() { return header.reply_status.value(); }
    public FString getServiceContexts() { return header.service_context; }

    public void marshal( CDROutputStream out ){
        ReplyHeader_1_1Helper.write( out, header );
    }

    public int getVersion(){ return 11; }
    public void release(){
    }
    public void internalFree(){}
}
