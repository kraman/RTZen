package edu.uci.ece.zen.orb.giop.parent;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.GIOPMessage;
import edu.uci.ece.zen.utils.ReadBuffer;

/**
 * @author bmiller
 *
  */
public abstract class CancelMessage extends GIOPMessage {
	public CancelMessage(ORB orb, ReadBuffer stream) {
		super(orb, stream);
	}

    // Replacement for implementation in GIOPMessage
    public boolean isCancelRequest() { return true; }

    // Concrete implementations 
    public boolean isRequest() { return false; }
	public boolean isReply() { return false; }

    // Abstract declarations in addition to those in GIOPMessage 
    // NONE
}
