/*******************************************************************************
 * --------------------------------------------------------------------------
 * $Id: GIOPMessageFactory.java,v 1.7 2004/02/25 08:15:19 kraman Exp $
 * --------------------------------------------------------------------------
 */

package edu.uci.ece.zen.orb.giop;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.ClientRequest;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.giop.type.RequestMessage;
import edu.uci.ece.zen.orb.transport.Transport;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;
import javax.realtime.*;

/**
 * This class is a factory for creating GIOP messages for marshalling or
 * demarshalling messages.
 * 
 * @author Krishna Raman
 * @author Bruce Miller
 * @author Yue Zhang
 * @version $Revision: 1.8 $ $Date: 2004/08/01 09:25:19 $
 */
public final class GIOPMessageFactory {
    private static final byte magic[] = {
            0x47, 0x49, 0x4f, 0x50
    }; // GIOP

    public static GIOPMessage parseStream(ORB orb, Transport trans)
            throws java.io.IOException {
        if( trans.giopType == edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.TYPE ){
            return edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.parseStream( orb , trans );
        }
        return null;
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
    private static void collectFragmentsv1_1(Transport trans, GIOPHeaderInfo headerInfo, ReadBuffer firstFragmentBuffer) throws java.io.IOException {
        if( trans.giopType == edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.TYPE ){
            edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.collectFragmentsv1_1( trans , headerInfo , firstFragmentBuffer );
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
     */
    private static void collectFragmentsv1_2(Transport trans, GIOPHeaderInfo headerInfo, ReadBuffer firstFragmentBuffer, int requestId) throws java.io.IOException {
        if( trans.giopType == edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.TYPE ){
            edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.collectFragmentsv1_2( trans , headerInfo , firstFragmentBuffer , requestId );
        }
    }

    /**
     * Read the GIOP Message header from the Transport's stream.
     * 
     * @param trans
     *            Transport stream
     * @param headerInfo
     *            GIOPHeaderInfo object to fill with data read from header
     */
    public static void parseStreamForHeader(java.io.InputStream in, GIOPHeaderInfo headerInfo, Transport trans) throws java.io.IOException {
        if( trans.giopType == edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.TYPE ){
            edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.parseStreamForHeader( in , headerInfo , trans );
        }
    }

    /**
     * Client upcall:
     * <p>
     * Client scope --&gt; <b>Message scope/Waiter region </b> --ex in --&gt;
     * Immortal --&gt; Transport scope
     * </p>
     */
    public static void constructMessage(ClientRequest req, int messageId, CDROutputStream out) {
        if( req.del.giopType == edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.TYPE ){
            edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.constructMessage( req , messageId , out );
        }
    }

    public static CDROutputStream constructReplyMessage(ORB orb, RequestMessage req) {
        if( req.getGiopType() == edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.TYPE ){
            return edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.constructReplyMessage( orb , req );
        }
        return null;
    }
     
    public static CDROutputStream constructLocateReplyMessage(ORB orb, edu.uci.ece.zen.orb.giop.type.LocateRequestMessage req)
    {
        if( req.getGiopType() == edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.TYPE ){
            return edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.constructLocateReplyMessage( orb , req );
        }
        return null;
    }
    
    public static CDROutputStream constructExceptionMessage(ORB orb, RequestMessage req) {
        if( req.getGiopType() == edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.TYPE ){
            return edu.uci.ece.zen.orb.giop.standard.GIOPMessageFactory.constructExceptionMessage( orb , req );
        }
        return null;
    }
}
