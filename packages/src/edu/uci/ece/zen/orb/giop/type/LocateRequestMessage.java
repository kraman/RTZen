package edu.uci.ece.zen.orb.giop.type;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.utils.*;

/**
* Parent class for different GIOP versions' LocateRequestMessage.  Put 
* any functionality that you want to be common to LocateRequestMessage 
* classes here.  See CORBA v3.0 Spec section 15.4.5
*
* @author bmiller
*/
public abstract class LocateRequestMessage extends GIOPMessage {
    public LocateRequestMessage() {}
    public LocateRequestMessage( ORB orb, ReadBuffer stream) {
        super( orb , stream );
    }
    public void init(ORB orb, ReadBuffer stream)
    {
        super.init(orb, stream);
    }

    // Abstract declarations in addition to those in GIOPMessage 
}
