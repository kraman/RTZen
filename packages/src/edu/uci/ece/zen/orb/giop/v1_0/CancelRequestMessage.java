package edu.uci.ece.zen.orb.giop.v1_0;

import org.omg.GIOP.*;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.*;

/**
 * Represents header of GIOP CancelRequest message as described in
 * CORBA Spec General Inter-ORB Protocol standard.
 * @author Bruce Miller
 */

public class CancelRequestMessage extends edu.uci.ece.zen.orb.giop.type.CancelRequestMessage {
    private org.omg.GIOP.CancelRequestHeader header;

    public CancelRequestMessage(ORB orb, ReadBuffer stream) {
        // Super's constructor sets and filles this.istream
        super (orb, stream);
        header = org.omg.GIOP.CancelRequestHeaderHelper.read (istream);
        messageBody = null;
    }

    public int getRequestId() { return header.request_id; }

    public void marshal (edu.uci.ece.zen.orb.CDROutputStream out) {
        CancelRequestHeaderHelper.write( out , header );
    }

}

// client should call
//orb.cancelRequest(requestIdToCancel);

