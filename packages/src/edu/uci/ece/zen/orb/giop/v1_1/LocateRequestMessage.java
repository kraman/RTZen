package edu.uci.ece.zen.orb.giop.v1_1;

import org.omg.GIOP.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.ReadBuffer;

// Same as for version 1.0

/**
 * @see edu.uci.ece.zen.orb.giop.v1_0.LocateRequestMessage
 * @author Bruce Miller
 */
public class LocateRequestMessage extends  edu.uci.ece.zen.orb.giop.v1_0.LocateRequestMessage {
    public LocateRequestMessage(ORB orb, ReadBuffer stream) {
        super (orb, stream);
        header = LocateRequestHeader_1_1Helper.read(istream);
        messageBody = stream;
    }

    // LocateRequest messages contain only a GIOP header then the LocateRequestHeader
    public void marshal(CDROutputStream out) {
        LocateRequestHeader_1_1Helper.write(out, header);
    }
}
