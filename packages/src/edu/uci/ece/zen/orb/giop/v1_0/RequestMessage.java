package edu.uci.ece.zen.orb.giop.v1_0;

import javax.realtime.ImmortalMemory;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.ClientRequest;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.utils.ZenProperties;

/**
 * @author Bruce Miller
 */
public class RequestMessage extends
        edu.uci.ece.zen.orb.giop.type.RequestMessage {
    private RequestHeader header;

    private static RequestMessage rm;

    private byte[] reqPrin = new byte[0];

    public RequestMessage() {
        super();
    }

    public void init(ClientRequest clr, int messageId) {
        //super();
        ZenProperties.logger.log("RequestMessage1");
        header = RequestHeader.instance();

        header.init(clr.contexts, messageId, clr.responseExpected,
                clr.objectKey, clr.operation, reqPrin);
    }

    public static RequestMessage getMessage() {
        try {
            if (rm == null) rm = (RequestMessage) ImmortalMemory.instance()
                    .newInstance(RequestMessage.class);
            return rm;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public RequestMessage(ORB orb, ReadBuffer stream) {
        super(orb, stream);
        header = RequestHeaderHelper.read(istream);
        messageBody = stream;
    }

    public void init(ORB orb, ReadBuffer stream) {
        super.init(orb, stream);
        header = RequestHeaderHelper.read(istream);
        messageBody = stream;
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

    public int getGiopVersion() {
        return 10;
    }
}