package edu.uci.ece.zen.orb.giop.v1_0;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;

public class RequestMessage extends edu.uci.ece.zen.orb.giop.GIOPMessage{
    private RequestHeader_1_0 header;
    
    ///////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Message Read ///////////////////////////////
    ///////////////////////////////////////////////////////////////////////////


    /** Do not directly call this method It is provided only to give
     * access to the default constructor of GIOPMessage.
     */
    public RequestMessage() {
        super();
    };

    public RequestMessage( ClientRequest clr , int messageId ){
        super();
        header = new RequestHeader_1_0(
            new org.omg.IOP.ServiceContext[0],
            messageId,
            clr.responseExpected,
            clr.objectKey,
            clr.operation,
            new byte[0] );
    }

    public RequestMessage( ORB orb , ReadBuffer stream ){
        super( orb , stream );
        header = RequestHeader_1_0Helper.read( istream );
    }

    public int getRequestId(){ return header.request_id; }
    public int getReplyStatus(){ return -1; }
    public org.omg.IOP.ServiceContext[] getServiceContexts(){ return header.service_context; }

    public boolean isRequest(){ return true; }
    public boolean isReply(){ return false; }

    public void marshall( CDROutputStream out ){
        RequestHeader_1_0Helper.write( out , header );
    }
}
