package edu.uci.ece.zen.orb.giop.v1_0;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;

/**
 * Reply messages as described in section 15.4.3 of the CORBA v3.0 Spec. 
 * @author bmiller
 */

public class ReplyMessage extends edu.uci.ece.zen.orb.giop.type.ReplyMessage {
    private ReplyHeader_1_0 header;
    
    public ReplyMessage( ORB orb , ReadBuffer stream ){
        super( orb , stream );
        header = ReplyHeader_1_0Helper.read( istream );  // read method initializes header variable
        messageBody = stream;
    }

    public int getRequestId() { return header.request_id; }

    public int getReplyStatus() { return header.reply_status.value(); }
    public org.omg.IOP.ServiceContext[] getServiceContexts() { return header.service_context; }

    public void marshal( CDROutputStream out ){
        ReplyHeader_1_0Helper.write( out, header );
    }

    public int getGiopVersion(){ return 10; }
}
