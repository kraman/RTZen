package edu.uci.ece.zen.orb.giop.v1_2;

import org.omg.GIOP.*; 
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.orb.*;

/**
 * GIOP v1.2 reply message sent in reply to a LocateRequest message, as discussed in section 15.4.6 of the CORBA v3.0 specification. 
 * 
 * @author bmiller
 */
public class LocateReplyMessage extends edu.uci.ece.zen.orb.giop.type.LocateReplyMessage {
    private LocateReplyHeader_1_2 header;

	/** Constructor
	 * @param orb ORB to associate with
	 * @param stream stream holding header and message body
	 */
	public LocateReplyMessage(ORB orb, ReadBuffer stream) {
		super(orb, stream);
        LocateReplyHeader_1_2Helper.read(istream);
        readBody();
        messageBody = null; // message body is read by the call to readBody(), retrieve it using accessor methods
	}

	public int getRequestId() { return header.request_id; }

    /**
     * @see edu.uci.ece.zen.orb.giop.GIOPMessage#marshal(edu.uci.ece.zen.orb.CDROutputStream)
     */
    public void marshal(CDROutputStream out) {
        LocateReplyHeader_1_2Helper.write(out, header);
    }

    public int getLocateStatusValue() {
        return header.locate_status.value();
    }

}
