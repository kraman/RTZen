package edu.uci.ece.zen.orb.giop.v1_2;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.orb.*;

/**
 * GIOP v1.2 LocateRequest Message as discussed in section 15.4.6 of the CORBA v3.0 specification. 
 * @see edu.uci.ece.zen.orb.giop.v1_0.LocateRequestMessage
 * 
 * @author bmiller
*/
public class LocateRequestMessage extends edu.uci.ece.zen.orb.giop.type.LocateRequestMessage {
    private LocateRequestHeader_1_2 header;

    public LocateRequestMessage(ORB orb, ReadBuffer stream) {
        super (orb, stream);
        header = LocateRequestHeader_1_2Helper.read(istream);
        messageBody = null;
    }

    public int getRequestId() { return header.request_id; }


    /**
     * @see edu.uci.ece.zen.orb.giop.GIOPMessage#marshal(edu.uci.ece.zen.orb.CDROutputStream)
     */
    public void marshal(CDROutputStream out) {
        // LocateRequest messages contain only a GIOP header then the LocateRequestHeader
        LocateRequestHeader_1_2Helper.write(out, header);
    }
}