package edu.uci.ece.zen.orb.giop.v1_2;

import org.omg.GIOP.*;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.ReadBuffer;

/**
 * Reply messages as described in section 15.4.3 of the CORBA v3.0 Spec. 
 * @author bmiller
 */
// Is same as v1_1 reply message.
public class ReplyMessage extends edu.uci.ece.zen.orb.giop.v1_1.ReplyMessage { 
    private ReplyHeader_1_2 header;

    public ReplyMessage( ORB orb , ReadBuffer stream ) {
        super( orb , stream );
        header = ReplyHeader_1_2Helper.read( istream ); // read method initializes header variable
        messageBody = stream;
    }

    public int getRequestId() {
        return header.request_id;
    }

}
