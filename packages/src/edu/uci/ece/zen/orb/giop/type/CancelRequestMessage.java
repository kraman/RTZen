package edu.uci.ece.zen.orb.giop.type;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.GIOPMessage;
import edu.uci.ece.zen.utils.ReadBuffer;

/**
 * @author bmiller
 *
  */
public abstract class CancelRequestMessage extends GIOPMessage {
	public CancelRequestMessage(ORB orb, ReadBuffer stream) {
		super(orb, stream);
	}

    // Abstract declarations in addition to those in GIOPMessage 
    // NONE
}
