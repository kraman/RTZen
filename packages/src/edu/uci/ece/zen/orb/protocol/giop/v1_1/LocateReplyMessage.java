package edu.uci.ece.zen.orb.protocol.giop.v1_1;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;
import javax.realtime.ImmortalMemory;

/**
 * GIOP v1.1 reply message to a LocateRequest message, as discussed in section 15.4.6 of the CORBA v3.0 specification. 
 *
 * @author bmiller
 */
public class LocateReplyMessage extends edu.uci.ece.zen.orb.protocol.type.LocateReplyMessage {
    private org.omg.GIOP.LocateReplyHeader_1_0 header; // v1.1 also uses the v1.0 header structure
    private static LocateReplyMessage lrm;

	public LocateReplyMessage(ORB orb, ReadBuffer stream) {
        super (orb, stream);
        header = LocateReplyHeader_1_1Helper.read(istream);
        readBody();
        messageBody = null; // message body is read by the call to readBody(), retrieve it using accessor methods
	}

    public static LocateReplyMessage getMessage()
    {
        try
        {
            if (lrm == null)
                lrm = (LocateReplyMessage) ImmortalMemory.instance().newInstance(LocateReplyMessage.class);
            return lrm;
        }catch (Exception e)
        {
            ZenProperties.logger.log(Logger.WARN, LocateReplyMessage.class, "getMessage", e);
        }
        return null;
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

    public int getVersion(){ return 11; }
}
