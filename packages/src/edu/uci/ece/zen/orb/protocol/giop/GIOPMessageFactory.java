/*******************************************************************************
 * --------------------------------------------------------------------------
 * $Id: MessageFactory.java,v 1.7 2004/02/25 08:15:19 kraman Exp $
 * --------------------------------------------------------------------------
 */

package edu.uci.ece.zen.orb.protocol.giop;

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
 * This class is a factory for creating GIOP messages for marshalling or
 * demarshalling messages.
 *
 * @author Krishna Raman
 * @author Bruce Miller
 * @author Yue Zhang
 * @version $Revision: 1.8 $ $Date: 2004/08/01 09:25:19 $
 */
public final class GIOPMessageFactory extends MessageFactory{
    private static final byte magic[] = {
            0x47, 0x49, 0x4f, 0x50
    }; // GIOP

    public Message parseStreamImpl(ORB orb, Transport trans)
            throws java.io.IOException {
        ReadBuffer buffer = ReadBuffer.instance();
        buffer.init();

        ProtocolHeaderInfo mainMsgHdr = (ProtocolHeaderInfo) new ProtocolHeaderInfo();

        edu.uci.ece.zen.orb.protocol.Message ret = null;

        do {
            java.io.InputStream in = trans.getInputStream();
            parseStreamForHeaderImpl(in, mainMsgHdr, trans);
            // Read the GIOP message (including any request/reply/etc headers)
            // into the variable "buffer"
            buffer.setEndian(mainMsgHdr.isLittleEndian);
            buffer.appendFromStream(in, mainMsgHdr.messageSize);
            if (ZenProperties.dbg) ZenProperties.logger.log
                            ("In MessageFactory, the message size is "
                            + mainMsgHdr.messageSize);

            if (ZenProperties.dbg) ZenProperties.logger.log
                            ("Inside MessageFactory and mainMsgHdr: "
                            + mainMsgHdr.toString() + " and giopMajorVersion: "
                            + mainMsgHdr.majorVersion
                            + " and minorversion: "
                            + mainMsgHdr.minorVersion
                            + " and messageType: " + mainMsgHdr.messageType);
            switch (mainMsgHdr.majorVersion) { //GIOP major version (byte
                // 4)
                case 1:
                    switch (mainMsgHdr.minorVersion) { //GIOP minor version
                        // (byte 5)
                        case 0:
                            switch (mainMsgHdr.messageType) {
                                case org.omg.GIOP.MsgType_1_0._Request:
                                    ret = edu.uci.ece.zen.orb.protocol.giop.v1_0.RequestMessage
                                            .getMessage();
                                    ret.init(orb, buffer);
                                    break;
                                case org.omg.GIOP.MsgType_1_0._Reply:
                                    ret = edu.uci.ece.zen.orb.protocol.giop.v1_0.ReplyMessage
                                            .getMessage();
                                    ret.init(orb, buffer);
                                    break;
                                case org.omg.GIOP.MsgType_1_0._LocateRequest:
                                    //ret = edu.uci.ece.zen.orb.giop.v1_0.LocateRequestMessage
                                    //        .getMessage();
                                    //ret.init(orb, buffer);

                                    //this is provisional until we get it working right
                                    //just return OBJECT_HERE for now
                                    ret = new edu.uci.ece.zen.orb.protocol.giop.v1_0.
                                            LocateRequestMessage(orb, buffer);
                                    break;
                                case org.omg.GIOP.MsgType_1_0._LocateReply:
                                    ret = edu.uci.ece.zen.orb.protocol.giop.v1_0.LocateReplyMessage
                                            .getMessage();
                                    ret.init(orb, buffer);
                                    break;
                                case org.omg.GIOP.MsgType_1_0._CloseConnection:
                                case org.omg.GIOP.MsgType_1_0._CancelRequest:
                                    ret = edu.uci.ece.zen.orb.protocol.giop.v1_0.CancelRequestMessage
                                            .getMessage();
                                    ret.init(orb, buffer);

                                    // A cancel request header is just the
                                    // request id to
                                    // cancel.
                                    break;
                                case org.omg.GIOP.MsgType_1_0._MessageError:
                                    //TODO: Handle these properly
                                    break;
                            }
                            break;
                        /*
                         * case 1: switch( mainMsgHdr.messageType ){ case
                         * org.omg.GIOP.MsgType_1_1._Request: ret =
                         * edu.uci.ece.zen.orb.giop.v1_1.RequestMessage.getMessage();
                         * ret.init(orb, buffer); if
                         * (mainMsgHdr.nextMessageIsFragment) {
                         * System.err.println("reading fragment");
                         * collectFragmentsv1_1( trans, mainMsgHdr, buffer ); }
                         * break; case org.omg.GIOP.MsgType_1_1._Reply : ret =
                         * edu.uci.ece.zen.orb.giop.v1_1.ReplyMessage.getMessage();
                         * ret.init(orb, buffer); if
                         * (mainMsgHdr.nextMessageIsFragment) {
                         * System.err.println("reading fragment");
                         * collectFragmentsv1_1( trans, mainMsgHdr, buffer ); }
                         * break; case org.omg.GIOP.MsgType_1_1._LocateRequest :
                         * ret =
                         * edu.uci.ece.zen.orb.giop.v1_0.ReplyMessage.getMessage();
                         * ret.init(orb, buffer); break; case
                         * org.omg.GIOP.MsgType_1_1._LocateReply : ret =
                         * edu.uci.ece.zen.orb.giop.v1_1.LocateReplyMessage.getMessage();
                         * ret.init(orb, buffer); break; case
                         * org.omg.GIOP.MsgType_1_1._CancelRequest : ret =
                         * edu.uci.ece.zen.orb.giop.v1_1.CancelRequestMessage.getMessage();
                         * ret.init(orb, buffer); // A cancel request header is
                         * just the request id to cancel. break; case
                         * org.omg.GIOP.MsgType_1_1._CloseConnection : case
                         * org.omg.GIOP.MsgType_1_1._MessageError : case
                         * org.omg.GIOP.MsgType_1_1._Fragment : //throw new
                         * org.omg.CORBA.NO_IMPLEMENT("Fragment read out of
                         * order"); //BM } case 2: // No newer version of
                         * MsgType classes than MsgType_1_1 is generated for
                         * RTZen case 3: switch( mainMsgHdr.messageType ){ case
                         * org.omg.GIOP.MsgType_1_1._Request: ret =
                         * edu.uci.ece.zen.orb.giop.v1_2.RequestMessage.getMessage();
                         * ret.init(orb, buffer); if
                         * (mainMsgHdr.nextMessageIsFragment) {
                         * System.err.println("reading fragment"); int requestId =
                         * ((edu.uci.ece.zen.orb.giop.v1_2.RequestMessage)
                         * ret).getRequestId(); collectFragmentsv1_2( trans,
                         * mainMsgHdr, buffer, requestId ); } break; case
                         * org.omg.GIOP.MsgType_1_1._Reply : ret =
                         * edu.uci.ece.zen.orb.giop.v1_2.ReplyMessage.getMessage();
                         * ret.init(orb, buffer); if
                         * (mainMsgHdr.nextMessageIsFragment) {
                         * System.err.println("reading fragment"); int requestId =
                         * ((edu.uci.ece.zen.orb.giop.v1_2.RequestMessage)
                         * ret).getRequestId(); collectFragmentsv1_2( trans,
                         * mainMsgHdr, buffer, requestId ); } break; case
                         * org.omg.GIOP.MsgType_1_1._LocateRequest : ret =
                         * edu.uci.ece.zen.orb.giop.v1_2.LocateRequestMessage.getMessage();
                         * ret.init(orb, buffer); break; case
                         * org.omg.GIOP.MsgType_1_1._LocateReply : ret =
                         * edu.uci.ece.zen.orb.giop.v1_2.LocateReplyMessage.getMessage();
                         * ret.init(orb, buffer); break; case
                         * org.omg.GIOP.MsgType_1_1._CancelRequest : //throw new
                         * org.omg.CORBA.BAD_CONTEXT("CancelRequest Cannot be
                         * called in GIOP 1.2 and higher"); case
                         * org.omg.GIOP.MsgType_1_1._CloseConnection : case
                         * org.omg.GIOP.MsgType_1_1._MessageError : case
                         * org.omg.GIOP.MsgType_1_1._Fragment : break; } break;
                         */
                        default:
                            throw new RuntimeException(""); //throw GIOP error
                    // here
                    }
                    break;
                default:
                    throw new RuntimeException(""); //throw GIOP error here
            }
        } while (false);
        ZenProperties.logger.log("GMF parse stream 1");
        ScopedMemory transportScope = (ScopedMemory) javax.realtime.MemoryArea.getMemoryArea(trans);
        transportScope.setPortal( trans );
        ret.setTransport( transportScope );
        ZenProperties.logger.log("GMF parse stream 2");
        if(ZenProperties.devDbg) {
            System.out.print("parse stream messageId:");
            System.out.println(ret.getRequestId());
        }
        return ret;
    }

    /**
     * Collects all fragments following the initial one in a request or a reply
     * in GIOP v1_1.
     *
     * @param trans
     *            Transport (e.g. iiop) where the inputstream should be found.
     * @param headerInfo
     *            is a reference to the header object that this method can use
     *            and modify.
     * @param firstFragmentBuffer
     *            ReadBuffer of the first fragment in set of fragments from a
     *            v1_1 Request or Reply
     */
    public void collectFragmentsImpl(Transport trans,
            ProtocolHeaderInfo headerInfo, ReadBuffer firstFragmentBuffer)
            throws java.io.IOException {

        ReadBuffer prevMessageBuffer = firstFragmentBuffer;
        boolean moreFragments = true;
        while (moreFragments) {
            java.io.InputStream in = trans.getInputStream();
            parseStreamForHeaderImpl(in, headerInfo, trans);

            // Create the ReadBuffer to store the data of the fragment
            ReadBuffer buffer = ReadBuffer.instance();
            buffer.setEndian(headerInfo.isLittleEndian);
            buffer.appendFromStream(in, headerInfo.messageSize);
            prevMessageBuffer.setNextBuffer(buffer);

            moreFragments = headerInfo.nextMessageIsFragment;
            prevMessageBuffer = buffer;
        }
    }

    /**
     * Collects all fragments following the initial one in a request or a reply
     * in GIOP v1_2.
     *
     * @param trans
     *            Transport (e.g. iiop) where the inputstream should be found.
     * @param headerInfo
     *            is a reference to the header object that this method can use
     *            and modify.
     * @param firstFragmentBuffer
     *            ReadBuffer of the first fragment in set of fragments from a
     *            v1_2 Request or Reply
     * /
    public void collectFragmentsv1_2(Transport trans,
            ProtocolHeaderInfo headerInfo, ReadBuffer firstFragmentBuffer,
            int requestId) throws java.io.IOException {
        // Read the fragment header

        boolean moreFragments = true;
        while (moreFragments) {
            java.io.InputStream in = trans.getInputStream();
            parseStreamForHeader(in, headerInfo, trans);

            // Read the GIOP v1_2 fragment header, which is composed
            // of a single long.
            int fragRequestId = read_long(in, headerInfo);

            // If the request id in the fragment is not the request id
            // that we were collecting fragments regarding, throw an
            // exception.
            if (fragRequestId != requestId) { throw new org.omg.CORBA.MARSHAL(
                    "Tried to read fragment; fragment request id doesn't match expected request id"); }

            // Now that we've read the four byte v1_2 fragment header,
            // there are 4 bytes less in the message.
            int fragmentSize = headerInfo.messageSize - 4;
            firstFragmentBuffer.appendFromStream(in, fragmentSize);
            moreFragments = headerInfo.nextMessageIsFragment;
        }
    }
    */
    /**
     * Read the GIOP Message header from the Transport's stream.
     *
     * @param trans
     *            Transport stream
     * @param headerInfo
     *            ProtocolHeaderInfo object to fill with data read from header
     */
    public void parseStreamForHeaderImpl(java.io.InputStream in,
            ProtocolHeaderInfo headerInfo, Transport trans)
            throws java.io.IOException {
        ZenProperties.logger.log("parseStreamForHeader");
        //byte[] header = new byte[12];
        byte[] header = trans.getGIOPHeader();
        int read = 0;
        ZenProperties.logger.log("parseStreamForHeader: reading");
        if(ZenProperties.devDbg) {
            System.out.print("parseStreamForHeader: buffer size");
            System.out.println(header.length);
        }
        while (read < 12) {
            int tmp = in.read(header, 0, 12);
            //if (ZenProperties.dbg) ZenProperties.logger.log(tmp + "");
            if (tmp < 0) {
                ZenProperties.logger.log(Logger.FATAL, MessageFactory.class, "parseStreamForHeader(InputStream, ProtocolHeaderInfo, Transport)", "RTZen doesnt support closing connection yet :-P ... shutting down");
                System.exit(0);
            }
            read += tmp;
        }
        ZenProperties.logger.log("parseStreamForHeader: done reading");

        // Bytes 0,1,2,3 should equal 'GIOP'
        //System.err.println( "----GIOP Message Header ----" );
        //System.err.write( header , 0 , 12 );
        //System.err.println( "\n---- ----" );

        if (header[0] != GIOPMessageFactory.magic[0] || header[1] != GIOPMessageFactory.magic[1]
                || header[2] != GIOPMessageFactory.magic[2] || header[3] != GIOPMessageFactory.magic[3]) { throw new RuntimeException(
                ""); //THROW GIOP Error here
        }
        headerInfo.majorVersion = header[4];
        headerInfo.minorVersion = header[5];
        headerInfo.isLittleEndian = (header[6] & 0x01) == 1; //Endian byte
        // (byte 6)

        headerInfo.nextMessageIsFragment = false;
        if (headerInfo.majorVersion == 1
                && headerInfo.minorVersion == 1) {
            headerInfo.nextMessageIsFragment = ((header[6] & 0x02) == 1);
        }

        headerInfo.messageType = header[7]; //Message type (byte 7)

        headerInfo.messageSize = 0;
        if (headerInfo.isLittleEndian) {
            //System.out.println( "Little endian msg" );
            //System.out.println( "" + ((int)header[11]) + " " +
            // ((int)header[10]) + " " + ((int)header[9]) + " " +
            // ((int)header[8]) );
            headerInfo.messageSize |= header[11] & 0xFF;
            headerInfo.messageSize <<= 8;
            headerInfo.messageSize |= header[10] & 0xFF;
            headerInfo.messageSize <<= 8;
            headerInfo.messageSize |= header[9] & 0xFF;
            headerInfo.messageSize <<= 8;
            headerInfo.messageSize |= header[8] & 0xFF;
        } else {
            //System.out.println( "Big endian msg" );
            //System.out.println( "" + ((int)header[8]) + " " +
            // ((int)header[9]) + " " + ((int)header[10]) + " " +
            // ((int)header[11]) );
            headerInfo.messageSize |= header[8] & 0xFF;
            headerInfo.messageSize <<= 8;
            headerInfo.messageSize |= header[9] & 0xFF;
            headerInfo.messageSize <<= 8;
            headerInfo.messageSize |= header[10] & 0xFF;
            headerInfo.messageSize <<= 8;
            headerInfo.messageSize |= header[11] & 0xFF;
        }
        //System.out.println( "Message size " + headerInfo.messageSize );
    }

    /**
     * Client upcall:
     * <p>
     * Client scope --&gt; <b>Message scope/Waiter region </b> --ex in --&gt;
     * Immortal --&gt; Transport scope
     * </p>
     */
    public void constructMessageImpl(ClientRequest req, int messageId,
            CDROutputStream out) {
        //edu.uci.ece.zen.utils.Logger.printMemStats(304);
        out.write_octet_array(magic, 0, 4);
        //giop version
        out.write_octet((byte) 1);
        out.write_octet((byte) 0);
        //endian
        out.write_boolean(false);
        //message type
        out.write_octet((byte) org.omg.GIOP.MsgType_1_0._Request);
        out.setLocationMemento();
        out.write_long(0);
        //(new edu.uci.ece.zen.orb.giop.v1_0.RequestMessage( req , messageId
        // )).marshal( out );

        edu.uci.ece.zen.orb.protocol.giop.v1_0.RequestMessage rm = edu.uci.ece.zen.orb.protocol.giop.v1_0.RequestMessage
                .getMessage();
        rm.init(req, messageId);
        rm.marshal(out);
        rm.free();
    }

    public CDROutputStream constructReplyMessageImpl(ORB orb,
            RequestMessage req) {
        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);

        if(ZenProperties.devDbg) {
            System.out.print("construct reply for messageId:");
            System.out.println(req.getRequestId());
        }

        out.write_octet_array(magic, 0, 4);
        //giop version
        out.write_octet((byte) 1);
        out.write_octet((byte) 0);
        //endian
        out.write_boolean(false);
        //message type
        out.write_octet((byte) org.omg.GIOP.MsgType_1_0._Reply);
        out.setLocationMemento();
        out.write_long(0);

        switch (req.getVersion()) {
            case 10:
                edu.uci.ece.zen.orb.protocol.giop.v1_0.ReplyHeader rh = edu.uci.ece.zen.orb.protocol.giop.v1_0.ReplyHeader
                        .instance();
                rh.init(FString.instance(rh.service_context), req
                        .getRequestId(),
                        org.omg.GIOP.ReplyStatusType_1_0._NO_EXCEPTION);

                // add Reply service context here, should probably be factored out

                //using these global variables still a hack for now
                if (edu.uci.ece.zen.orb.transport.iiop.Acceptor.priorityModel == org.omg.RTCORBA.PriorityModel._CLIENT_PROPAGATED
                        && edu.uci.ece.zen.orb.transport.iiop.Acceptor.serverPriority >= 0) {

                    ZenProperties.logger.log("Sending CLIENT PROPAGATED service context");

                    rh.service_context.append(1); //list size

                    rh.service_context.append(org.omg.IOP.RTCorbaPriority.value);
                    //contexts.append(0); //big endian

                    rh.service_context.append(4); //length of data
                    rh.service_context.append((int) orb.getRTCurrent().the_priority());

                } else {
                    rh.service_context.append(0); //empty list
                }

                edu.uci.ece.zen.orb.protocol.giop.v1_0.ReplyHeaderHelper.write(out, rh);
                break;
            /*
             * case 11: org.omg.GIOP.ReplyHeader_1_1Helper.write( out , new
             * org.omg.GIOP.ReplyHeader_1_0( new org.omg.IOP.ServiceContext[0] ,
             * req.getRequestId() ,
             * org.omg.GIOP.ReplyStatusType_1_0.NO_EXCEPTION )); break; case 12:
             * org.omg.GIOP.ReplyHeader_1_2Helper.write( out , new
             * org.omg.GIOP.ReplyHeader_1_2( req.getRequestId() ,
             * org.omg.GIOP.ReplyStatusType_1_2.NO_EXCEPTION , new
             * org.omg.IOP.ServiceContext[0] )); break;
             */
            default:
                ZenProperties.logger.log(Logger.WARN, MessageFactory.class, "constructReplyMessage", "giop version not supported");
        }
        return out;
    }
    /**.
     */

    public CDROutputStream constructLocateReplyMessageImpl(ORB orb,
            edu.uci.ece.zen.orb.protocol.type.LocateRequestMessage req)
    {
        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);

        out.write_octet_array(magic, 0, 4);
        //giop version
        out.write_octet((byte) 1);
        out.write_octet((byte) 0);
        //endian
        out.write_boolean(false);
        //message type
        out.write_octet((byte) org.omg.GIOP.MsgType_1_0._LocateReply);
        out.setLocationMemento();
        out.write_long(0);

        switch (req.getVersion()) {
            case 10:
                out.write_ulong(req.getRequestId());
                out.write_ulong(org.omg.GIOP.LocateStatusType_1_0._OBJECT_HERE);
                out.updateLength();
                break;
            default:
                ZenProperties.logger.log(Logger.WARN, MessageFactory.class, "constructLocateReplyMessage", "giop version not supported");
        }
        return out;
    }

    public CDROutputStream constructExceptionMessageImpl(ORB orb,
            RequestMessage req) {
        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);

        out.write_octet_array(magic, 0, 4);
        //giop version
        out.write_octet((byte) 1);
        out.write_octet((byte) 0);
        //endian
        out.write_boolean(false);
        //message type
        out.write_octet((byte) org.omg.GIOP.MsgType_1_0._Reply);
        out.setLocationMemento();
        out.write_long(0);

        switch (req.getVersion()) {
            case 10:
                edu.uci.ece.zen.orb.protocol.giop.v1_0.ReplyHeader rh = edu.uci.ece.zen.orb.protocol.giop.v1_0.ReplyHeader
                        .instance();
                rh.init(FString.instance(rh.service_context), req
                        .getRequestId(),
                        org.omg.GIOP.ReplyStatusType_1_0._USER_EXCEPTION);
                rh.service_context.append(0);
                edu.uci.ece.zen.orb.protocol.giop.v1_0.ReplyHeaderHelper.write(out, rh);
                break;
            /*
             * case 11: org.omg.GIOP.ReplyHeader_1_1Helper.write( out , new
             * org.omg.GIOP.ReplyHeader_1_0( new org.omg.IOP.ServiceContext[0] ,
             * req.getRequestId() ,
             * org.omg.GIOP.ReplyStatusType_1_0.USER_EXCEPTION )); break; case
             * 12: org.omg.GIOP.ReplyHeader_1_2Helper.write( out , new
             * org.omg.GIOP.ReplyHeader_1_2( req.getRequestId() ,
             * org.omg.GIOP.ReplyStatusType_1_2.USER_EXCEPTION , new
             * org.omg.IOP.ServiceContext[0] )); break;
             */
            default:
                ZenProperties.logger.log(Logger.WARN, MessageFactory.class, "constructExceptionMessage", "giop version not supported");
        }
        return out;
    }

    /**
     * Read a CORBA long (Java int) from the input stream, using
     * headerInfo.isLittleEndian to decide if it should be treated as little
     * endian or big endian.
     *
     * @param in
     *            InputStream to read four bytes from
     * @param headerInfo
     *            ProtocolHeaderInfo object whose isLittleEndian member will be used
     *            to determine how to treat input stream
     */
    public int read_long(java.io.InputStream in,
            ProtocolHeaderInfo headerInfo) throws java.io.IOException {
        byte[] bytesOfLong = new byte[4];
        in.read(bytesOfLong, 0, 4);
        int readLong = 0;

        if (!headerInfo.isLittleEndian) {
            readLong += ((short) (bytesOfLong[0] & 0xFF)) << 24;
            readLong += ((short) (bytesOfLong[1] & 0xFF)) << 16;
            readLong += ((short) (bytesOfLong[2] & 0xFF)) << 8;
            readLong += ((short) (bytesOfLong[3] & 0xFF)) << 0;
        } else {
            readLong += ((short) (bytesOfLong[3] & 0xFF)) << 24;
            readLong += ((short) (bytesOfLong[2] & 0xFF)) << 16;
            readLong += ((short) (bytesOfLong[1] & 0xFF)) << 8;
            readLong += ((short) (bytesOfLong[0] & 0xFF)) << 0;
        }

        return readLong;
    }

}
