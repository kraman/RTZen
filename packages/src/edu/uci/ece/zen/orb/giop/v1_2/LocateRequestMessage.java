package edu.uci.ece.zen.orb.giop.v1_2;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.orb.*;
import javax.realtime.ImmortalMemory;

/**
 * GIOP v1.2 LocateRequest Message as discussed in section 15.4.6 of the CORBA v3.0 specification. 
 * @see edu.uci.ece.zen.orb.giop.v1_0.LocateRequestMessage
 * 
 * @author bmiller
*/
public class LocateRequestMessage extends edu.uci.ece.zen.orb.giop.type.LocateRequestMessage {
    private LocateRequestHeader_1_2 header;
    private static LocateRequestMessage lrm;

    public LocateRequestMessage(ORB orb, ReadBuffer stream) {
        super (orb, stream);
        header = LocateRequestHeader_1_2Helper.read(istream);
        messageBody = null;
    }

    public int getRequestId() { return header.request_id; }

    public static LocateRequestMessage getMessage()
    {
        try
        {
            if (lrm == null)
                lrm = (LocateRequestMessage) ImmortalMemory.instance().newInstance(LocateRequestMessage.class);
            return lrm;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see edu.uci.ece.zen.orb.giop.GIOPMessage#marshal(edu.uci.ece.zen.orb.CDROutputStream)
     */
    public void marshal(CDROutputStream out) {
        // LocateRequest messages contain only a GIOP header then the LocateRequestHeader
        LocateRequestHeader_1_2Helper.write(out, header);
    }

    public int getGiopVersion(){ return 12; }
}
