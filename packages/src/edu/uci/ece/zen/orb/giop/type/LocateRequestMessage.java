package edu.uci.ece.zen.orb.giop.type;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.utils.*;

/**
 * @author bmiller
 *
 */
public abstract class LocateRequestMessage extends GIOPMessage {
    public LocateRequestMessage( ORB orb, ReadBuffer stream) {
        super( orb , stream );
    }

    // Abstract declarations in addition to those in GIOPMessage 
    public abstract int getReplyStatus();
}
