package edu.uci.ece.zen.orb.protocol.giop.v1_1;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;
import javax.realtime.ImmortalMemory;

/**
 * @author Bruce Miller
 */
public class RequestMessage extends edu.uci.ece.zen.orb.protocol.type.RequestMessage {
    private RequestHeader_1_1 header; //mark needs to fix
    private static RequestMessage rm;

    public RequestMessage ( ClientRequest clr,  int messageId ) {
        super();

        header = new RequestHeader_1_1 (
                                        clr.contexts,
                                        messageId,
                                        clr.responseExpected,
                                        edu.uci.ece.zen.orb.protocol.type.RequestMessage.reserved,
                                        clr.objectKey,
                                        clr.operation,
                                        new byte[0]
                                        );
    }

    public RequestMessage( ORB orb, ReadBuffer stream ) {
        super( orb, stream );
        header = RequestHeaderHelper_1_1.read( istream );
        messageBody = stream;
    }

    public static RequestMessage getMessage()
    {
        try
        {
            if (rm == null)
                rm = (RequestMessage) ImmortalMemory.instance().newInstance(RequestMessage.class);
            return rm;
        }catch (Exception e)
        {
            ZenProperties.logger.log(Logger.WARN, RequestMessage.class, "getMessage", e);
        }
        return null;
    }
    public int getRequestId() { return header.request_id; }

    public String getServiceContexts() { return header.service_context; }
    public void getObjectKey( FString id_out ){ id_out.reset(); id_out.append( header.object_key ); }
    public String getOperation(){ return header.operation; }
    public int getResponseExpected(){ return header.response_expected?1:0; }

    public void marshal( CDROutputStream out ) {
        RequestHeaderHelper.write( out, header );
    }

    public int getVersion(){ return 11; }
}
