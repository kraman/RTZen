package edu.uci.ece.zen.orb.giop.v1_0;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;

public class LocateRequestMessage extends edu.uci.ece.zen.orb.giop.GIOPMessage {
    private LocateRequestHeader_1_0 header;


    public LocateRequestMessage( ORB orb, ReadBuffer stream) {
        super (orb, stream);
        header = LocateRequestHeader_1_0Helper.read(istream);
        messageBody = stream;
    }

    public boolean isRequest() { return true; }
    public boolean isReply() { return false; }
    public int getRequestId() { return header.request_id; }

    // LocateRequest messages contain only a GIOP header then the LocateRequestHeader
    public void marshal(CDROutputStream out) {
        LocateRequestHeader_1_0Helper.write(out, header);
    }

}
