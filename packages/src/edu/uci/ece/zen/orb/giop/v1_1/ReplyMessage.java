package edu.uci.ece.zen.orb.giop.v1_1;

import org.omg.GIOP.ReplyHeader_1_0;
import org.omg.GIOP.ReplyHeader_1_0Helper;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.ReadBuffer;

// Is same as v1_0 reply message.

public class ReplyMessage extends edu.uci.ece.zen.orb.giop.v1_0.ReplyMessage { 
    // v1_1 uses the same reply header type as v1_0.
    private ReplyHeader_1_0 header;

    public ReplyMessage( ORB orb , ReadBuffer stream ) {
        super( orb , stream );
        header = ReplyHeader_1_0Helper.read( istream );
        messageBody = stream;
    }

}
