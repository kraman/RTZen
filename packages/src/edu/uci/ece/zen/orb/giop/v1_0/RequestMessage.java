package edu.uci.ece.zen.orb.giop.v1_0;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;
import javax.realtime.ImmortalMemory;

/**
 * @author Bruce Miller
 */
public class RequestMessage extends edu.uci.ece.zen.orb.giop.type.RequestMessage {
    private RequestHeader header;
    private static RequestMessage rm;

    public RequestMessage() {}
    public RequestMessage( ClientRequest clr , int messageId ){
        super();
        if(ZenProperties.devDbg) System.out.println( "RequestMessage1" );
        header = RequestHeader.instance();

        header.init (
            clr.contexts,
            messageId,
            clr.responseExpected,
            clr.objectKey,
            clr.operation,
            new byte[0] );
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
            e.printStackTrace();
        }
        return null;
    }


    public RequestMessage( ORB orb , ReadBuffer stream ){
        super( orb , stream );
        header = RequestHeaderHelper.read( istream );
        messageBody = stream;
    }

    public void init( ORB orb , ReadBuffer stream ){
        super.init( orb , stream );
        header = RequestHeaderHelper.read( istream );
        messageBody = stream;
    }

    public int getRequestId() { return header.request_id; }

    public FString getServiceContexts() { return header.service_context; }
    public FString getObjectKey(){ return header.object_key; }
    public FString getOperation(){ return header.operation; }
    public int getResponseExpected(){ return header.response_expected?1:0; }

    public void marshal( CDROutputStream out ){
        // LocateRequest messages contain only a GIOP header then the LocateRequestHeader
        RequestHeaderHelper.write( out , header );
    }

    public int getGiopVersion(){ return 10; }
}
