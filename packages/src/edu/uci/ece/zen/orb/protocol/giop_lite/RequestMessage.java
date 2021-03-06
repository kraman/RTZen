/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.protocol.giop_lite;

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
        RequestMessage rm = (RequestMessage)ORB.getQueuedInstance(RequestMessage.class,queue);
        //rm.inUse = true;
        return rm;
/*
        try {
            if (rm == null) rm = (RequestMessage) ImmortalMemory.instance()
                    .newInstance(RequestMessage.class);
            return rm;
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, RequestMessage.class, "getMessage", e);
        }
        return null;
*/
    }
/*
    public RequestMessage(ORB orb, ReadBuffer stream) {
        super(orb, stream);
        header = RequestHeaderHelper.read(istream);
        messageBody = stream;
    }
*/


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
