package edu.uci.ece.zen.orb.giop.parent;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.utils.*;

/**
* Parent class for different GIOP versions' LocateReplyMessage.  Put any functionality 
* that you want to be common to LocateReplyMessage classes here. 
 *@author bmiller
 *
 */
public abstract class LocateReplyMessage extends GIOPMessage {
    public LocateReplyMessage( ORB orb, ReadBuffer stream) {
        super( orb , stream );
    }

    // Concrete implementations 
    public boolean isRequest() { return false; }
    public boolean isReply() { return true; }

    // Abstract declarations in addition to those in GIOPMessage 
    public abstract int getReplyStatus();
}
