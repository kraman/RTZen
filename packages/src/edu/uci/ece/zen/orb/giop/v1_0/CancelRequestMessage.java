package edu.uci.ece.zen.orb.giop.v1_0;

import javax.realtime.ImmortalMemory;

import org.omg.GIOP.CancelRequestHeaderHelper;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

/**
 * Represents header of GIOP CancelRequest message as described in CORBA Spec
 * General Inter-ORB Protocol standard.
 * 
 * @author Bruce Miller
 */

public class CancelRequestMessage extends
        edu.uci.ece.zen.orb.giop.type.CancelRequestMessage {
    private org.omg.GIOP.CancelRequestHeader header;

    private static CancelRequestMessage crm;

    public CancelRequestMessage(ORB orb, ReadBuffer stream) {
        // Super's constructor sets and filles this.istream
        super(orb, stream);
        header = org.omg.GIOP.CancelRequestHeaderHelper.read(istream);
        messageBody = null;
    }

    public void init(ORB orb, ReadBuffer stream) {
        // Super's constructor sets and filles this.istream
        super.init(orb, stream);
        header = org.omg.GIOP.CancelRequestHeaderHelper.read(istream);
        messageBody = null;
    }

    public int getRequestId() {
        return header.request_id;
    }

    public void marshal(edu.uci.ece.zen.orb.CDROutputStream out) {
        CancelRequestHeaderHelper.write(out, header);
    }

    public static CancelRequestMessage getMessage() {
        try {
            if (crm == null) crm = (CancelRequestMessage) ImmortalMemory
                    .instance().newInstance(CancelRequestMessage.class);
            return crm;
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, CancelRequestMessage.class, "getMessage", e);
        }
        return null;
    }

    public int getGiopVersion() {
        return 10;
    }
}

// client should call
//orb.cancelRequest(requestIdToCancel);

