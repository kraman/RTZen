package edu.uci.ece.zen.orb.giop.v1_2;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;

// Is same as v1_1 reply message.

public class ReplyMessage extends edu.uci.ece.zen.orb.giop.v1_1.ReplyMessage { 
    private ReplyHeader_1_2 header;

    public ReplyMessage( ORB orb , ReadBuffer stream ) {
        super( orb , stream );
        header = ReplyHeader_1_2Helper.read( istream );
    }

}
