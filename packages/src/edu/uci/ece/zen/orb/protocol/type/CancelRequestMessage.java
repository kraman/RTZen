package edu.uci.ece.zen.orb.protocol.type;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.protocol.Message;
import edu.uci.ece.zen.utils.ReadBuffer;

/**
 * Parent class for different GIOP versions' CancelRequesMessage. Put any
 * functionality that you want to be common to CancelRequestMessage classes
 * here. See CORBA v3.0 Spec section 15.4.4
 * 
 * @author bmiller
 */
public abstract class CancelRequestMessage extends Message {
    public CancelRequestMessage() {
    }

    public CancelRequestMessage(ORB orb, ReadBuffer stream) {
        super(orb, stream);
    }

    public void init(ORB orb, ReadBuffer stream) {
        super.init(orb, stream);
    }

    public boolean getResponseExpected(){
        return false;
    }
    // Abstract declarations in addition to those in GIOPMessage
    // NONE
}
