package edu.uci.ece.zen.orb.protocol.giop.v1_0;

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

public class ReplyMessage extends edu.uci.ece.zen.orb.protocol.type.ReplyMessage {
    private ReplyHeader header;

    private static ReplyMessage rm;

    private static  Queue queue = Queue.fromImmortal();

    public ReplyMessage() {
    }

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
    }

    public int getRequestId() {
        return header.request_id;
    }

    public int getReplyStatus() {
        return header.reply_status;
    }

    public void setReplyStatus(int stat) {
        header.reply_status = stat;
    }

    public FString getServiceContexts() {
        return header.service_context;
    }

    public void marshal(CDROutputStream out) {
        ReplyHeaderHelper.write(out, header);
    }

    public int getVersion() {
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
