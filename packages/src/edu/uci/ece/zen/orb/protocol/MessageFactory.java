/*******************************************************************************
 * --------------------------------------------------------------------------
 * $Id: MessageFactory.java,v 1.7 2004/02/25 08:15:19 kraman Exp $
 * --------------------------------------------------------------------------
 */

package edu.uci.ece.zen.orb.protocol;

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

/**
 * This class is a factory for marshalling or demarshalling messages.
 * 
 * @author Krishna Raman
 * @author Bruce Miller
 * @author Yue Zhang
 * @version $Revision: 1.8 $ $Date: 2004/08/01 09:25:19 $
 */
public abstract class MessageFactory {
    /** Register default giop message factory */
    static{
        MessageFactory.registerProtocolFactory( edu.uci.ece.zen.orb.protocol.giop.GIOPMessageFactory.class );
    }

    /**
     * Hashtable to store instances of protocol message factory.
     */
    private static java.util.Hashtable protocolFactoryMap;
        /**
     * Method for pluggable protocols to register their message factory with the ORB.
     * @param protocolFactory The Message factory to register
     */
    public static void registerProtocolFactory( Class protocolFactory ){
        if( protocolFactoryMap == null ){
            try{
                protocolFactoryMap = (java.util.Hashtable) ImmortalMemory.instance().newInstance( java.util.Hashtable.class );
            }catch( Exception e ){
                e.printStackTrace();
                System.exit(-1);
            }
        }
        try{
            protocolFactoryMap.put( protocolFactory , ImmortalMemory.instance().newInstance( protocolFactory ) );
        }catch( Exception e ){
            System.err.println( "Unable to add protocol: " + protocolFactory + e );
        }
    }
    

    /**
     * Method to retrieve an instance of the message factory for a given message factory class
     * @param protocolFactory The Message factory to retrieve instance of
     */
    public static MessageFactory getFactoryInstance( Class cls ){
        System.out.println( "getFactoryInstance( " + cls + ");" );
        System.out.println( "getFactoryInstance: " +  protocolFactoryMap.get( cls ) );
        return (MessageFactory) protocolFactoryMap.get( cls );
    }
    
    /**
     * Method to create a message based on an incomming stream.
     */
    public final static Message parseStream(ORB orb, Transport trans) throws java.io.IOException {
        Message msg = MessageFactory.getFactoryInstance( trans.getProtocolFactory() ).parseStreamImpl( orb , trans );
        msg.setProtocolFactory( trans.getProtocolFactory() );
        return msg;
    }
    public abstract Message parseStreamImpl( ORB orb , Transport trans ) throws java.io.IOException;

    /**
     * Method to create a message from framented streams
     */
    public final static void collectFragments( Transport trans , ProtocolHeaderInfo headerInfo , ReadBuffer firstFragmentBuffer ) throws java.io.IOException {
        MessageFactory.getFactoryInstance( trans.getProtocolFactory() ).collectFragmentsImpl( trans , headerInfo , firstFragmentBuffer );
    }
    public abstract void collectFragmentsImpl( Transport trans , ProtocolHeaderInfo headerInfo , ReadBuffer firstFragmentBuffer ) throws java.io.IOException;

    public final static void constructMessage(ClientRequest req, int messageId, CDROutputStream out) {
        MessageFactory.getFactoryInstance( req.del.getLane().getProtocolFactory() ).constructMessageImpl( req , messageId , out );
    }
    public abstract void constructMessageImpl(ClientRequest req, int messageId, CDROutputStream out);

    public final static CDROutputStream constructReplyMessage(ORB orb, RequestMessage req) {
        return MessageFactory.getFactoryInstance( req.getProtocolFactory() ).constructReplyMessageImpl( orb , req );
    }
    public abstract CDROutputStream constructReplyMessageImpl(ORB orb, RequestMessage req);
     
    public final static CDROutputStream constructLocateReplyMessage(ORB orb, edu.uci.ece.zen.orb.protocol.type.LocateRequestMessage req)
    {
        return MessageFactory.getFactoryInstance( req.getProtocolFactory() ).constructLocateReplyMessageImpl( orb , req );
    }
    public abstract CDROutputStream constructLocateReplyMessageImpl(ORB orb, edu.uci.ece.zen.orb.protocol.type.LocateRequestMessage req);
    
    public final static CDROutputStream constructExceptionMessage(ORB orb, RequestMessage req) {
        return MessageFactory.getFactoryInstance( req.getProtocolFactory() ).constructExceptionMessageImpl( orb , req );
    }
    public abstract CDROutputStream constructExceptionMessageImpl(ORB orb, RequestMessage req);
}
