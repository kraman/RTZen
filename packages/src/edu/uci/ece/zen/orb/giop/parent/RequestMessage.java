/*
 * Created on May 4, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.uci.ece.zen.orb.giop.parent;

import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;

/**
 * @author bmiller
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class RequestMessage extends edu.uci.ece.zen.orb.giop.GIOPMessage {

    public RequestMessage() {
        super();
    }

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
