package edu.uci.ece.zen.orb.giop.v1_1;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.ReadBuffer;
import javax.realtime.ImmortalMemory;

// Same as for version 1.0

/**
 * @see edu.uci.ece.zen.orb.giop.v1_0.CancelRequest
 * @author Bruce Miller
 */
public class CancelRequestMessage extends edu.uci.ece.zen.orb.giop.type.CancelRequestMessage { 
    private static CancelRequestMessage crm;
    public CancelRequestMessage(ORB orb, ReadBuffer stream) {
        super(orb, stream);
    }
    public int getRequestId() { return 0; } // no implement: Peter
    public void marshal(edu.uci.ece.zen.orb.CDROutputStream ostream) { }
    public int getGiopVersion() { return  11; } // not sure: Peter

    public static CancelRequestMessage getMessage()
    {
        try
        {
            if (crm == null)
                crm = (CancelRequestMessage) ImmortalMemory.instance().newInstance(CancelRequestMessage.class);
            return crm;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
