package edu.uci.ece.zen.orb.giop.v1_1;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.orb.*;

/**
 * GIOP v1.1 LocateRequest Message as discussed in section 15.4.6 of the CORBA v3.0 specification. 
 * @see edu.uci.ece.zen.orb.giop.v1_0.LocateRequestMessage
 * 
 * @author bmiller
*/
public class LocateRequestMessage extends  edu.uci.ece.zen.orb.giop.type.LocateRequestMessage {
    protected LocateRequestHeader_1_0 header;
    
    public LocateRequestMessage(ORB orb, ReadBuffer stream) {
        super (orb, stream);  // I need to call GIOPMessage's constructor here, so I couldn't derive from v1.0's LocateRequestMessage
        header = LocateRequestHeader_1_1Helper.read(istream);
        messageBody = null;
    }

    public int getRequestId() { return header.request_id; }

    public int getReplyStatus() { return -1; }

    /**
     * @see edu.uci.ece.zen.orb.giop.GIOPMessage#marshal(edu.uci.ece.zen.orb.CDROutputStream)
     */
    public void marshal(CDROutputStream out) {
        // LocateRequest messages contain only a GIOP header then the LocateRequestHeader
        LocateRequestHeader_1_1Helper.write(out, header);
    }
}
