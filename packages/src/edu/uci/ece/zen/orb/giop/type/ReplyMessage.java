package edu.uci.ece.zen.orb.giop.type;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.GIOPMessage;
import edu.uci.ece.zen.utils.ReadBuffer;

/**
 * @author bmiller
 *
  */
public abstract class ReplyMessage extends GIOPMessage {
	public ReplyMessage(ORB orb, ReadBuffer stream) {
		super(orb, stream);
	}

    // Abstract declarations in addition to those in GIOPMessage 
    public abstract int getReplyStatus();
    public abstract org.omg.IOP.ServiceContext[] getServiceContexts();
}
