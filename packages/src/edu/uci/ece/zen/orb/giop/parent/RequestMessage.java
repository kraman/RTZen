package edu.uci.ece.zen.orb.giop.parent;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.utils.*;

/**
 * @author bmiller
 *
 */
public abstract class RequestMessage extends GIOPMessage {
    protected static final byte reserved[] = { 0x00, 0x00, 0x00 };

    public RequestMessage() { super(); }

    public RequestMessage( ORB orb, ReadBuffer stream) {
        super( orb , stream );
    }

    // Concrete implementations 
    public boolean isRequest() { return true; }
    public boolean isReply() { return false; }

    // Abstract declarations in addition to those in GIOPMessage 
    public abstract int getReplyStatus();
    public abstract org.omg.IOP.ServiceContext[] getServiceContexts();
}
