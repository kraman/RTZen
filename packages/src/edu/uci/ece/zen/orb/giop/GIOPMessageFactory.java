/* --------------------------------------------------------------------------*
 * $Id: GIOPMessageFactory.java,v 1.7 2004/02/25 08:15:19 kraman Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.orb.giop;

import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.transport.*;

/**
 * This class is a factory for creating GIOP messages for marshalling or 
 * demarshalling messages.
 *
 * @author Krishna Raman
 * @version $Revision: 1.7 $ $Date: 2004/02/25 08:15:19 $
 */
public final class GIOPMessageFactory
{
    private static final byte magic[] = { 0x47, 0x49, 0x4f, 0x50 }; // GIOP
    
    public static GIOPMessage parseStream( ORB orb , Transport trans ) throws java.io.IOException{
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 1" );
        ReadBuffer buffer = ReadBuffer.instance();
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 2" );
        boolean nextMessageIsFragment = false;
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 3" );
        byte[] header = ByteArrayCache.instance().getByteArray();
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 4" );
        GIOPMessage ret = null;
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 5" );

        do{
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 6" );
            int read = 0;
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 7" );
            while( read < 12 ){
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 7-2" );
                read += trans.getInputStream().read( header , 0 , 12 );
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 7-3" );
            }
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 8" );
            
            // Bytes 0,1,2,3 should equal 'GIOP'
            if (   header[0] != magic[0]
                || header[1] != magic[1]
                || header[2] != magic[2]
                || header[3] != magic[3])
            {
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 9" );
                throw new RuntimeException(""); //THROW GIOP Error here
            }

            int messageSize = 0;
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 11" );
            boolean isLittleEndian;
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 12" );

            byte giopMajorVersion = header[4];
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 13" );
            byte giopMinorVersion = header[5];
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 14" );
            isLittleEndian = (header[6] & 0x01) == 1;   //Endian byte (byte 6)
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 15" );
            
            byte messageType = header[7];      //Message type (byte 7)
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 16" );
            buffer.setEndian( isLittleEndian );
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 17" );
            if( !isLittleEndian ){
                messageSize += ((short)(header[8] & 0xFF)) << 24;
                messageSize += ((short)(header[9] & 0xFF)) << 16;
                messageSize += ((short)(header[10] & 0xFF)) << 8;
                messageSize += ((short)(header[11] & 0xFF)) << 0;
            }else{
                messageSize += ((short)(header[11] & 0xFF)) << 24;
                messageSize += ((short)(header[10] & 0xFF)) << 16;
                messageSize += ((short)(header[9] & 0xFF)) << 8;
                messageSize += ((short)(header[8] & 0xFF)) << 0;
            }
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 18" );
            nextMessageIsFragment=false;
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 19" );
            System.err.println( "----GIOP Message Header ----" );
            System.err.write( header , 0 , 12 );
            System.err.println( "---- ----" );
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 20" );

            
            buffer.appendFromStream( trans.getInputStream() , messageSize );
                                                                                    System.out.println( Thread.currentThread() + "GIOPMessage.parseStream 21" );

            switch( giopMajorVersion ){                   //GIOP major version (byte 4)
                case 1:
                    switch( giopMinorVersion ){           //GIOP minor version (byte 5)
                        case 0:
                            switch( messageType ){
                                case org.omg.GIOP.MsgType_1_0._Request:
                                    ret = new edu.uci.ece.zen.orb.giop.v1_0.RequestMessage( orb , buffer );
                                    break;
                                case org.omg.GIOP.MsgType_1_0._Reply :
                                    ret = new edu.uci.ece.zen.orb.giop.v1_0.ReplyMessage( orb , buffer );
                                    break;
                                case org.omg.GIOP.MsgType_1_0._LocateRequest :
                                case org.omg.GIOP.MsgType_1_0._LocateReply :
                                case org.omg.GIOP.MsgType_1_0._CloseConnection :
                                case org.omg.GIOP.MsgType_1_0._CancelRequest :
                                case org.omg.GIOP.MsgType_1_0._MessageError :
                                    //TODO: Handle these properly
                                    break;
                            }
                            break;
                        case 1:
                            switch( messageType ){
                                case org.omg.GIOP.MsgType_1_1._Request:
                                    ret = new edu.uci.ece.zen.orb.giop.v1_1.RequestMessage( orb , buffer );
                                    break;
                                case org.omg.GIOP.MsgType_1_1._Reply :
                                    ret = new edu.uci.ece.zen.orb.giop.v1_1.ReplyMessage( orb , buffer );
                                    break;
                                case org.omg.GIOP.MsgType_1_1._LocateRequest :
                                case org.omg.GIOP.MsgType_1_1._LocateReply :
                                case org.omg.GIOP.MsgType_1_1._CancelRequest :
                                case org.omg.GIOP.MsgType_1_1._CloseConnection :
                                case org.omg.GIOP.MsgType_1_1._MessageError :
                                case org.omg.GIOP.MsgType_1_1._Fragment :
                                    break;
                            }
                            break;
                        case 2: // No newer version than MsgType_1_1 is generated for RTZen
                        case 3:
                            switch( messageType ){
                                case org.omg.GIOP.MsgType_1_1._Request:
                                    ret = new edu.uci.ece.zen.orb.giop.v1_2.RequestMessage( orb , buffer );
                                    break;
                                case org.omg.GIOP.MsgType_1_1._Reply :
                                    ret = new edu.uci.ece.zen.orb.giop.v1_2.ReplyMessage( orb , buffer );
                                    break;
                                case org.omg.GIOP.MsgType_1_1._LocateRequest :
                                case org.omg.GIOP.MsgType_1_1._LocateReply :
                                case org.omg.GIOP.MsgType_1_1._CancelRequest :
                                case org.omg.GIOP.MsgType_1_1._CloseConnection :
                                case org.omg.GIOP.MsgType_1_1._MessageError :
                                case org.omg.GIOP.MsgType_1_1._Fragment :
                                    break;
                            }
                            break;
                        default:
                            throw new RuntimeException(""); //throw GIOP error here
                    }
                    break;
                default:
                    throw new RuntimeException(""); //throw GIOP error here
            }   
        }while( nextMessageIsFragment );
        return ret;
    }

    /**
     * Client upcall:
     * <p>
     *     Client scope --&gt; <b>Message scope/Waiter region</b> --ex in --&gt; Immortal --&gt; Transport scope
     * </p>
     */
    public static void constructMessage( ClientRequest req , int messageId , CDROutputStream out ){
        out.write_octet_array( magic , 0 , 4 );
        //giop version
        out.write_octet( (byte)1 );
        out.write_octet( (byte)0 );
        //message type
        out.write_octet( (byte)org.omg.GIOP.MsgType_1_0._Request );
        //endian
        out.write_boolean( false );
        out.setLocationMemento();
        out.write_long(0);
        (new edu.uci.ece.zen.orb.giop.v1_0.RequestMessage( req , messageId )).marshall( out );
    }
}
