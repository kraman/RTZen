package edu.uci.ece.zen.orb.giop.v1_2;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;

public class RequestMessage extends edu.uci.ece.zen.orb.giop.type.RequestMessage {
    private RequestHeader_1_2 header;

    public RequestMessage ( ClientRequest clr,  int messageId ) {
        super();

        // Default is no response expected, change if response is expected
        byte responseFlag = 0x00; 
        if (clr.responseExpected) {
            // meaning of 0x03 is on page 15-36 of CORBA v3.0 spec
            responseFlag = 0x03; 
        }
        org.omg.GIOP.TargetAddress targetAddress = new org.omg.GIOP.TargetAddress();
        targetAddress.object_key(clr.objectKey);

        header = new RequestHeader_1_2 (
                                        messageId,
                                        responseFlag,
                                        edu.uci.ece.zen.orb.giop.type.RequestMessage.reserved,
                                        targetAddress,
                                        clr.operation,
                                        new org.omg.IOP.ServiceContext[0]
                                        );
    }

    public RequestMessage( ORB orb, ReadBuffer stream ) {
        super( orb, stream );
        header = RequestHeader_1_2Helper.read( istream );
        messageBody = stream;
    }

    public int getRequestId() { return header.request_id; }

    public int getReplyStatus() { return -1; }
    public org.omg.IOP.ServiceContext[] getServiceContexts() { return header.service_context; }

    public void marshal( CDROutputStream out ) {
        RequestHeader_1_2Helper.write( out, header );
    }
}
