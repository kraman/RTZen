package edu.uci.ece.zen.orb.giop.v1_0;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;

/**
 * @author Bruce Miller
 */
public class RequestMessage extends edu.uci.ece.zen.orb.giop.type.RequestMessage {
    private RequestHeader_1_0 header;

    public RequestMessage( ClientRequest clr , int messageId ){
        super();
        header = new RequestHeader_1_0 (
            //new org.omg.IOP.ServiceContext[0],
            clr.contexts,
            messageId,
            clr.responseExpected,
            clr.objectKey,
            clr.operation,
            new byte[0] );
    }

    public RequestMessage( ORB orb , ReadBuffer stream ){
        super( orb , stream );
        header = RequestHeader_1_0Helper.read( istream );
        messageBody = stream;
    }

    public int getRequestId() { return header.request_id; }

    public org.omg.IOP.ServiceContext[] getServiceContexts() { return header.service_context; }
    public byte[] getObjectKey(){ return header.object_key; }
    public String getOperation(){ return header.operation; }
    public int getResponseExpected(){ return header.response_expected?1:0; }

    public void marshal( CDROutputStream out ){
        // LocateRequest messages contain only a GIOP header then the LocateRequestHeader
        RequestHeader_1_0Helper.write( out , header );
    }
}
