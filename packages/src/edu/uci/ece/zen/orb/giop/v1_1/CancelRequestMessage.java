package edu.uci.ece.zen.orb.giop.v1_1;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.ReadBuffer;

// Same as for version 1.0

/**
 * @see edu.uci.ece.zen.orb.giop.v1_0.CancelRequest
 * @author Bruce Miller
 */
public class CancelRequestMessage extends edu.uci.ece.zen.orb.giop.v1_0.CancelRequestMessage { 
    public CancelRequestMessage(ORB orb, ReadBuffer stream) {
        super(orb, stream);
    }
}
