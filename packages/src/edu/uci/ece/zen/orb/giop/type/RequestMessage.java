package edu.uci.ece.zen.orb.giop.type;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.utils.*;

/** Top level representation ofor a GIOP RequestMessage Message, as
 * described in CORBA v3.0 spec, section 15.4.2
 *
 * @author bmiller
 */
public abstract class RequestMessage extends GIOPMessage {
    protected static final byte reserved[] = { 0x00, 0x00, 0x00 };

    public RequestMessage() { super(); }

    public RequestMessage( ORB orb, ReadBuffer stream) {
        super( orb , stream );
    }

    // Abstract declarations in addition to those in GIOPMessage 
    public abstract org.omg.IOP.ServiceContext[] getServiceContexts();
    public abstract byte[] getObjectKey();
    public abstract String getOperation();
    public abstract int getResponseExpected();
}
