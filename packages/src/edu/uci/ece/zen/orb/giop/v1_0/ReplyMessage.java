package edu.uci.ece.zen.orb.giop.v1_0;

import org.omg.GIOP.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;

public class ReplyMessage extends edu.uci.ece.zen.orb.giop.GIOPMessage{
    private ReplyHeader_1_0 header;
    
    ///////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Message Read ///////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public ReplyMessage( ORB orb , ReadBuffer stream ){
        super( orb , stream );
        header = ReplyHeader_1_0Helper.read( istream );
    }

    public int getRequestId(){ return header.request_id; }
    public int getReplyStatus(){ return header.reply_status.value(); }
    public org.omg.IOP.ServiceContext[] getServiceContexts(){ return header.service_context; }

    public boolean isRequest(){ return false; }
    public boolean isReply(){ return true; }

    public void marshall( CDROutputStream out ){
        ReplyHeader_1_0Helper.write( out , header );
    }
}
