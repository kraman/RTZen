package edu.uci.ece.zen.orb.giop.v1_2;

import org.omg.GIOP.ReplyHeader_1_2;
import org.omg.GIOP.ReplyHeader_1_2Helper;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.ReadBuffer;

// Is same as v1_1 reply message.

public class ReplyMessage extends edu.uci.ece.zen.orb.giop.v1_1.ReplyMessage { 
    private ReplyHeader_1_2 header;

    public ReplyMessage( ORB orb , ReadBuffer stream ) {
        super( orb , stream );
        header = ReplyHeader_1_2Helper.read( istream );
        messageBody = stream;
    }

    public int getRequestId() {
        return header.request_id;
    }

}
