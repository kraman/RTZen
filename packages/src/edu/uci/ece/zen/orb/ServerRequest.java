/* --------------------------------------------------------------------------*
 * $Id: ServerRequest.java,v 1.1 2004/02/06 00:20:49 kraman Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.orb.giop.RequestMessage;
import edu.uci.ece.zen.orb.transport.Transport;
import edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy;

/**
 * This class represents a request on the server side.
 *
 * @author Mayur Deshpande
 * @author Raymond Kelfstad
 * @author Arvind Krishna
 * @author Krishna Raman
 * @version $Revision: 1.1 $ $Date: 2004/02/06 00:20:49 $
 */
public class ServerRequest implements Executable {

    private static Queue serverRequestQueue;
    private static ImmortalMemory imm;

    static{
        try{
            ImmortalMemory imm = ImmortalMemory.instance();
            serverRequestQueue = imm.newInstance( edu.uci.ece.zen.utils.Queue.class );
            for( int i=0;i<10;i++ )
                serverRequestQueue.enqueue( imm.newInstance( ServerRequest.class ) );
        }catch( Exception e ){
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    public synchronized static ServerRequest instance(){
        if( queue.isEmpty() ){
            try{
                return (ServerRequest) imm.newInstance( ServerRequest.class );
            }catch( Exception e ){
                e.printStackTrace();
                System.exit( -1 );
            }
        }
        return (ServerRequest) queue.dequeue();
    }

    private static void release( ServerRequest req ){
        queue.enqueue( req );
    }

    public void free(){
        ServerRequest.release( this );
    }
    
    /**
     * Constructor to create a new request object on the server size.
     * @param trans The transport to associate this request with.
     * @param mesg The request message to wrap.
     */
    public void init(Transport trans, RequestMessage mesg) {
        this.transport = trans;
        this.message = mesg;
    }

    /**
     * Returns the transport associated with this request.
     */
    public Transport getTransport() {
        return transport;
    }

    /**
     * Returns the object key of the servant associated with this message.
     */
    public edu.uci.ece.zen.poa.ObjectKey getObjectKey() {
        return message.getObjectKey();
    }

    public String toString() {
        return "ServerRequest: " + message.toString();
    }

    /**
     * 
     */
    public void run() {
        try {
            this.requestProcessor.handleRequest(this, this.poa, this.numRequests);
        } catch (org.omg.CORBA.SystemException ex) {
            // Logger.debug("Caught the System exception");
            edu.uci.ece.zen.orb.ExceptionHandler.handleException(ex, message,
                    transport, poa.getORB());
        } catch (java.lang.Exception unknown) {
            unknown.printStackTrace();
            edu.uci.ece.zen.orb.ExceptionHandler.handleUnknownException(unknown.getMessage(),message, transport, poa.getORB());
        }
    }

    public void setHandlers(edu.uci.ece.zen.poa.POA poa,
            edu.uci.ece.zen.poa.SynchronizedInt requests,
            RequestProcessingStrategy requestProcessor) {
        this.poa = poa;
        this.numRequests = requests;
        this.requestProcessor = requestProcessor;
    }

    public RequestMessage message;
    
    protected Transport transport;
    protected edu.uci.ece.zen.poa.POA poa;
    protected edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy
            requestProcessor;
    protected edu.uci.ece.zen.poa.SynchronizedInt numRequests;
}
