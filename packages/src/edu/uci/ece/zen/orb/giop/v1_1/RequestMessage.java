package edu.uci.ece.zen.orb.giop.v1_1;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;

/**
 * @author Bruce Miller
 */
public class RequestMessage extends edu.uci.ece.zen.orb.giop.type.RequestMessage {
    private RequestHeader_1_1 header;

    public RequestMessage ( ClientRequest clr,  int messageId ) {
        super();

        header = new RequestHeader_1_1 (
                                        clr.contexts,
                                        messageId,
                                        clr.responseExpected,
                                        edu.uci.ece.zen.orb.giop.type.RequestMessage.reserved,
                                        clr.objectKey,
                                        clr.operation,
                                        new byte[0]
                                        );
    }

    public RequestMessage( ORB orb, ReadBuffer stream ) {
        super( orb, stream );
        header = RequestHeader_1_1Helper.read( istream );
        messageBody = stream;
    }

    public int getRequestId() { return header.request_id; }

    public org.omg.IOP.ServiceContext[] getServiceContexts() { return header.service_context; }
    public void getObjectKey( FString id_out ){ id_out.reset(); id_out.append( header.object_key ); }
    public String getOperation(){ return header.operation; }
    public int getResponseExpected(){ return header.response_expected?1:0; }

    public void marshal( CDROutputStream out ) {
        RequestHeader_1_1Helper.write( out, header );
    }

    public int getGiopVersion(){ return 11; }
}
