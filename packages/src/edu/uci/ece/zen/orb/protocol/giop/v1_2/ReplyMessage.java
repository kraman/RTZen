package edu.uci.ece.zen.orb.protocol.giop.v1_2;

import org.omg.GIOP.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.ReadBuffer;
import javax.realtime.ImmortalMemory;

/**
 * Reply messages as described in section 15.4.3 of the CORBA v3.0 Spec. 
 * @author bmiller
 */
// Is similar to v1_0 reply message.
public class ReplyMessage extends edu.uci.ece.zen.orb.protocol.type.ReplyMessage { 
    private ReplyHeader_1_2 header;
    private static ReplyMessage rm;

    public ReplyMessage( ORB orb , ReadBuffer stream ) {
        super( orb , stream );
        header = ReplyHeader_1_2Helper.read( istream ); // read method initializes header variable
        messageBody = stream;
    }

    public int getRequestId() { return header.request_id; }
    public static ReplyMessage getMessage()
    {
        try
        {
            if (rm == null)
                rm = (ReplyMessage) ImmortalMemory.instance().newInstance(ReplyMessage.class);
            return rm;
        }catch (Exception e)
        {
            ZenProperties.logger.log(Logger.WARN, ReplyMessage.class, "getMessage", e);
        }
        return null;
    }

    public int getReplyStatus() { return header.reply_status.value(); }
    public org.omg.IOP.ServiceContext[] getServiceContexts() { return header.service_context; }

    public void marshal( CDROutputStream out ){
        ReplyHeader_1_2Helper.write( out, header );
    }

    public int getVersion(){ return 12; }
}