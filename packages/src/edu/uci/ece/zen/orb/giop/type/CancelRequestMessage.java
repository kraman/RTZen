package edu.uci.ece.zen.orb.giop.type;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.GIOPMessage;
import edu.uci.ece.zen.utils.ReadBuffer;

/**
* Parent class for different GIOP versions' CancelRequesMessage.  Put 
* any functionality that you want to be common to CancelRequestMessage 
* classes here.  See CORBA v3.0 Spec section 15.4.4
*
* @author bmiller
*/
public abstract class CancelRequestMessage extends GIOPMessage {
	public CancelRequestMessage(ORB orb, ReadBuffer stream) {
		super(orb, stream);
	}

    // Abstract declarations in addition to those in GIOPMessage 
    // NONE
}
