package edu.uci.ece.zen.orb.protocol.giop.v1_0;

import javax.realtime.ImmortalMemory;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.ClientRequest;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.Queue;

/**
 * @author Bruce Miller
 * @author Mark Panahi
 */
public class RequestMessage extends
        edu.uci.ece.zen.orb.protocol.type.RequestMessage {
    private RequestHeader header;

    //private static RequestMessage rm;

    private byte[] reqPrin = new byte[0];

    private static  Queue queue = Queue.fromImmortal();

    public RequestMessage() {
        super();
    }

    // client side
    public void init(ClientRequest clr, int messageId) {
        //super();
        ZenProperties.logger.log("RequestMessage1");
        header = RequestHeader.instance(header);

        header.init(clr.contexts, messageId, clr.responseExpected,
                clr.objectKey, clr.operation, reqPrin);
    }

    //server side
    public void init(ORB orb, ReadBuffer stream) {
        super.init(orb, stream);
        header = RequestHeaderHelper.read(istream, RequestHeader.instance(header));
        messageBody = stream;
    }

    static int drawn = 0;
    //private boolean inUse = false;
    public static RequestMessage getMessage() {
        drawn++;
        //if(ZenProperties.memDbg1) System.out.write('d');
        //if(ZenProperties.memDbg1) System.out.write('r');
        //if(ZenProperties.memDbg1) edu.uci.ece.zen.utils.Logger.writeln(drawn);
        RequestMessage rm = (RequestMessage)Queue.getQueuedInstance(RequestMessage.class,queue);
        //rm.inUse = true;
        return rm;
    }

    public int getRequestId() {
        return header.request_id;
    }

    public FString getServiceContexts() {
        return header.service_context;
    }

    public FString getObjectKey() {
        return header.object_key;
    }

    public FString getOperation() {
        return header.operation;
    }

    public int getResponseExpected() {
        return header.response_expected ? 1 : 0;
    }

    public void marshal(CDROutputStream out) {
        // LocateRequest messages contain only a GIOP header then the
        // LocateRequestHeader
        RequestHeaderHelper.write(out, header);
    }

    public int getVersion() {
        return 10;
    }

    public void internalFree(){
        //if(!inUse)
        //    System.out.println("____________________________RM already freed.");
        drawn--;
        header.reset();
        queue.enqueue(this);
        //inUse = false;
    }
}
