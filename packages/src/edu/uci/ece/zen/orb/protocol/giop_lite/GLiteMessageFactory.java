package edu.uci.ece.zen.orb.protocol.giop_lite;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.ClientRequest;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.orb.transport.Transport;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;
import javax.realtime.*;
import edu.uci.ece.zen.orb.protocol.*;

/**
 * This class is a factory for creating GIOP Lite messages for marshalling or
 * demarshalling messages.
 * 
 * @author Krishna Raman
 */
public final class GLiteMessageFactory extends MessageFactory{
    public Message parseStreamImpl(ORB orb, Transport trans) throws java.io.IOException {
        ReadBuffer buffer = ReadBuffer.instance();
        buffer.init();
        buffer.setAlignment( false );
       
        Object obj = trans.getObject(3);
        ProtocolHeaderInfo mainMsgHdr;
        if(obj == null){
            mainMsgHdr = new edu.uci.ece.zen.orb.protocol.ProtocolHeaderInfo();
            trans.setObject(mainMsgHdr, 3);
        }
        else{
            mainMsgHdr = (edu.uci.ece.zen.orb.protocol.ProtocolHeaderInfo)obj;
        }
        
        edu.uci.ece.zen.orb.protocol.Message ret = null;

        do {
            java.io.InputStream in = trans.getInputStream();
            parseStreamForHeaderImpl(in, mainMsgHdr, trans);
            buffer.setEndian(false);
            buffer.appendFromStream(in, mainMsgHdr.messageSize);
            //only oneway request messages possible in this protocol
            ret = edu.uci.ece.zen.orb.protocol.giop.v1_0.RequestMessage.getMessage();
            ret.init(orb, buffer);
        } while (false);
        ScopedMemory transportScope = (ScopedMemory) javax.realtime.MemoryArea.getMemoryArea(trans);
        transportScope.setPortal( trans );
        ret.setTransport( transportScope );
        if(ZenProperties.devDbg) {
            System.out.println(ret.getRequestId());
        }        
        return ret;
    }

    public void collectFragmentsImpl(Transport trans,
            ProtocolHeaderInfo headerInfo, ReadBuffer firstFragmentBuffer)
            throws java.io.IOException {
        //No fragments possible in this protocol
    }

    public void parseStreamForHeaderImpl(java.io.InputStream in, ProtocolHeaderInfo headerInfo, Transport trans) throws java.io.IOException {
        byte[] header = trans.getGIOPHeader();
        int read = 0;
        while (read < 2) {          
            int tmp = in.read(header, 0, 12);
            //if (ZenProperties.dbg) ZenProperties.logger.log(tmp + "");
            if (tmp < 0) {
                ZenProperties.logger.log(Logger.FATAL, MessageFactory.class, "parseStreamForHeader(InputStream, ProtocolHeaderInfo, Transport)", "RTZen doesnt support closing connection yet :-P ... shutting down");
                System.exit(0);
            }
            read += tmp;
        }
        headerInfo.majorVersion = 1;
        headerInfo.minorVersion = 0;
        headerInfo.isLittleEndian = false;
        headerInfo.nextMessageIsFragment = false;
        headerInfo.messageType = 0;
        headerInfo.messageSize = 0;
     
        //no messages larger than 65535 bytes   
        headerInfo.messageSize |= header[8] & 0xFF;
        headerInfo.messageSize <<= 8;
        headerInfo.messageSize |= header[9] & 0xFF;
    }

    public void constructMessageImpl(ClientRequest req, int messageId, CDROutputStream out) {
        out.setAlignment( false );
        out.setLocationMemento();
        out.write_short((short)0);
        edu.uci.ece.zen.orb.protocol.giop.v1_0.RequestMessage rm = edu.uci.ece.zen.orb.protocol.giop.v1_0.RequestMessage.getMessage();
        rm.init(req, messageId);
        rm.marshal(out);
        rm.free();
    }

    public CDROutputStream constructReplyMessageImpl(ORB orb, RequestMessage req) {
        //no replies allowed
        return null;
    }
     
    public CDROutputStream constructLocateReplyMessageImpl(ORB orb, edu.uci.ece.zen.orb.protocol.type.LocateRequestMessage req)
    {
        //no locate reqests allowed
        return null;
    }
    
    public CDROutputStream constructExceptionMessageImpl(ORB orb, RequestMessage req) {
        //no exceptions allowed
        return null;
    }
}
