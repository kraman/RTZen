package edu.uci.ece.zen.orb.giop.standard.v1_0;

import javax.realtime.ImmortalMemory;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.Queue;

/**
 * Reply messages as described in section 15.4.3 of the CORBA v3.0 Spec.
 *
 * @author bmiller
 */

public class ReplyMessage extends edu.uci.ece.zen.orb.giop.type.ReplyMessage {
    private ReplyHeader header;

    private static ReplyMessage rm;

    private static  Queue queue = Queue.fromImmortal();

    public ReplyMessage() {
    }
/*
    public ReplyMessage(ORB orb, ReadBuffer stream) {
        super(orb, stream);
        header = ReplyHeaderHelper.read(istream); // read method initializes
        // header variable
        messageBody = stream;
    }
*/
    public void init(ORB orb, ReadBuffer stream) {
        super.init(orb, stream);
        header = ReplyHeaderHelper.read(istream); // read method initializes
        // header variable
        messageBody = stream;
    }

    static int drawn = 0;
    public static ReplyMessage getMessage() {
         drawn++;
        if(ZenProperties.memDbg1) System.out.write('d');
        if(ZenProperties.memDbg1) System.out.write('r');
        if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln(drawn);
       return (ReplyMessage)ORB.getQueuedInstance(ReplyMessage.class,queue);

/*
        try {
            if (rm == null) rm = (ReplyMessage) ImmortalMemory.instance()
                    .newInstance(ReplyMessage.class);
            return rm;
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, ReplyMessage.class, "getMessage", e);
        }
        return null;
        */
    }

    public int getRequestId() {
        return header.request_id;
    }

    public int getReplyStatus() {
        return header.reply_status;
    }

    public FString getServiceContexts() {
        return header.service_context;
    }

    public void marshal(CDROutputStream out) {
        ReplyHeaderHelper.write(out, header);
    }

    public int getGiopVersion() {
        return 10;
    }

    public void release(){
        drawn--;
        queue.enqueue(this);
    }

    public void internalFree(){
        super.free();
        release();
    }
}
