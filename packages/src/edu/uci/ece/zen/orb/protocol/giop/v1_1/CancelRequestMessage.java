package edu.uci.ece.zen.orb.protocol.giop.v1_1;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

import javax.realtime.ImmortalMemory;

// Same as for version 1.0

/**
 * @see edu.uci.ece.zen.orb.giop.v1_0.CancelRequest
 * @author Bruce Miller
 */
public class CancelRequestMessage extends edu.uci.ece.zen.orb.protocol.type.CancelRequestMessage { 
    private static CancelRequestMessage crm;
    public CancelRequestMessage(ORB orb, ReadBuffer stream) {
        super(orb, stream);
    }
    public int getRequestId() { return 0; } // no implement: Peter
    public void marshal(edu.uci.ece.zen.orb.CDROutputStream ostream) { }
    public int getVersion() { return  11; } // not sure: Peter

    public static CancelRequestMessage getMessage()
    {
        try
        {
            if (crm == null)
                crm = (CancelRequestMessage) ImmortalMemory.instance().newInstance(CancelRequestMessage.class);
            return crm;
        }catch (Exception e)
        {
            ZenProperties.logger.log(Logger.WARN, CancelRequestMessage.class, "getMessage", e);
        }
        return null;
    }

    public void internalFree(){}
}
