package edu.uci.ece.zen.orb.giop.parent;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.GIOPMessage;
import edu.uci.ece.zen.utils.ReadBuffer;

/**
 * @author bmiller
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class ReplyMessage extends GIOPMessage {
	public ReplyMessage(ORB orb, ReadBuffer stream) {
		super(orb, stream);
	}

    // Concrete implementations 
    public boolean isRequest() { return false; }
	public boolean isReply() { return true; }

    // Abstract declarations in addition to those in GIOPMessage 
    public abstract int getReplyStatus();
    public abstract org.omg.IOP.ServiceContext[] getServiceContexts();
}
