package edu.uci.ece.zen.orb.protocol.type;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.protocol.Message;
import edu.uci.ece.zen.orb.protocol.type.*;
import edu.uci.ece.zen.utils.ReadBuffer;

/**
 * Parent class for different GIOP versions' LocateRequestMessage. Put any
 * functionality that you want to be common to LocateRequestMessage classes
 * here. See CORBA v3.0 Spec section 15.4.5
 * 
 * @author bmiller
 */
public abstract class LocateRequestMessage extends Message {
    public LocateRequestMessage() {
    }

    public LocateRequestMessage(ORB orb, ReadBuffer stream) {
        super(orb, stream);
    }

    public void init(ORB orb, ReadBuffer stream) {
        super.init(orb, stream);
    }

    public int getResponseExpected(){
        return 1;
    }

    // Abstract declarations in addition to those in GIOPMessage
}
