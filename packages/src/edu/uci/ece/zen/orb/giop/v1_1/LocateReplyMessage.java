package edu.uci.ece.zen.orb.giop.v1_1;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.orb.*;

/**
 * GIOP v1.1 reply message to a LocateRequest message, as discussed in section 15.4.6 of the CORBA v3.0 specification. 
 *
 * @author bmiller
 */
public class LocateReplyMessage extends edu.uci.ece.zen.orb.giop.type.LocateReplyMessage {
    private org.omg.GIOP.LocateReplyHeader_1_0 header; // v1.1 also uses the v1.0 header structure

	public LocateReplyMessage(ORB orb, ReadBuffer stream) {
        super (orb, stream);
        header = LocateReplyHeader_1_1Helper.read(istream);
        readBody();
        messageBody = null; // message body is read by the call to readBody(), retrieve it using accessor methods
	}

    public int getRequestId() { return header.request_id; }

    /**
     * @see edu.uci.ece.zen.orb.giop.GIOPMessage#marshal(edu.uci.ece.zen.orb.CDROutputStream)
     */
    public void marshal(CDROutputStream out) {
        // LocateRequest messages contain only a GIOP header then the LocateRequestHeader
        LocateReplyHeader_1_1Helper.write(out, header);
    }

    public int getLocateStatusValue() {
        return header.locate_status.value();
    }

    public int getGiopVersion(){ return 11; }
}
