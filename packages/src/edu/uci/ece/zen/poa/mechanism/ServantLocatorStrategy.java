/* --------------------------------------------------------------------------*
 * $Id: ServantLocatorStrategy.java,v 1.7 2003/09/03 20:44:19 spart Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa.mechanism;

// --- OMG Specific Imports ---
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.portable.InvokeHandler;
import edu.uci.ece.zen.orb.ResponseHandler;

public class ServantLocatorStrategy extends ServantManagerStrategy {
    
   /**
    * Initialize Servant Locator
    * @param nonRetain ServantRetentionStrategy
    * @param threadStrategy ThreadPolicyStrategy
    * @throws org.omg.PortableServer.POAPackage.InvalidPolicy
    */
    public void init(ServantRetentionStrategy nonRetain, ThreadPolicyStrategy threadStrategy , org.omg.CORBA.IntHolder ih ) throws
                org.omg.PortableServer.POAPackage.InvalidPolicy {
        this.threadPolicyStrategy = threadStrategy;
    }

   /**
    * set concrete servant locator
    * @param servantManager java.lang.Object
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public synchronized void setInvokeHandler(java.lang.Object servantManager)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
        if (this.manager != null) {
            throw new org.omg.CORBA.BAD_INV_ORDER(6,
                    CompletionStatus.COMPLETED_NO);
        }

        if (servantManager instanceof org.omg.PortableServer.ServantLocator) {
            this.manager = (org.omg.PortableServer.ServantLocator) servantManager;
        }

        throw new org.omg.PortableServer.POAPackage.WrongPolicy();
    }

   /**
    * return servant locator associated with the strategy
    * @param name strategy type
    * @return Object servant locator
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
    */
    public synchronized Object getRequestProcessor(int name) throws
                org.omg.PortableServer.POAPackage.ObjectNotActive,
                org.omg.PortableServer.POAPackage.WrongPolicy {
        if (validate(name) && this.manager != null) {
            return this.manager;
        }

        throw new org.omg.PortableServer.POAPackage.ObjectNotActive();
    }

   /**
    * Handle client request.
    * @param request client request.
    * @param poa corresponding POA
    * @param requests active requests
    * @return int return state
    */
    public int handleRequest(ServerRequest request,
            edu.uci.ece.zen.poa.POA poa,
            edu.uci.ece.zen.poa.SynchronizedInt requests) {
        edu.uci.ece.zen.poa.ObjectKey ok = request.getObjectKey();
        byte[] id = ok.getId();

        InvokeHandler invokeHandler;

        if (this.manager == null) {
            throw new org.omg.CORBA.OBJ_ADAPTER(4, CompletionStatus.COMPLETED_NO);
        }

        String method = request.message.getOperation();

        // --- PRE INVOKE ---
        try {
            this.threadPolicyStrategy.enter();

            invokeHandler = (InvokeHandler)
                    this.manager.preinvoke(id, poa, method, null);

            this.threadPolicyStrategy.exit();

        } catch (org.omg.PortableServer.ForwardRequest ex) {
            // Let the ORB Handle the Forward Request Exception
            ((edu.uci.ece.zen.poa.POA) poa).getORB().handleForwardRequest(ex);

            return edu.uci.ece.zen.orb.ServerRequestHandler.REQUEST_NOT_HANDLED;
        }
        edu.uci.ece.zen.poa.ThreadSpecificPOACurrent.putInvocationContext(poa,
                ok, (org.omg.PortableServer.Servant) invokeHandler);

        ResponseHandler responseHandler = new ResponseHandler(((edu.uci.ece.zen.poa.POA) poa).getORB(),
                request.message.getRequestId(),
                request.message.getGIOPVersion().major,
                request.message.getGIOPVersion().minor);

        this.threadPolicyStrategy.enter(invokeHandler);
        org.omg.PortableServer.Servant myServant = (org.omg.PortableServer.Servant) invokeHandler;

        ServerReply reply;
               if (request.message.getOperation().equals("_is_a") )
        	{
        		boolean _result = myServant._is_a(request.message.getIstream().read_string());
                        org.omg.CORBA.portable.OutputStream _output = responseHandler.createReply();
                        _output.write_boolean(_result);
                        reply = (ServerReply) _output;

        }
        else if (request.message.getOperation().equals("_non_existent") )
                 {
                 	boolean _result = myServant._non_existent();
                        org.omg.CORBA.portable.OutputStream _output = responseHandler.createReply();
                        _output.write_boolean(_result);
                        reply = (ServerReply) _output;

                  }
       else


                  {

 	        	reply= (ServerReply) invokeHandler._invoke(method,
                	request.message.getIstream(), responseHandler);
                   }

        this.threadPolicyStrategy.exit(invokeHandler);
        reply.sendUsing(request.getTransport());

        // --POSTINVOKE ---
        try {
            this.threadPolicyStrategy.enter();
            this.manager.postinvoke(id, poa, method, null,
                    (org.omg.PortableServer.Servant) invokeHandler);
            this.threadPolicyStrategy.exit();
        } catch (org.omg.PortableServer.ForwardRequest ex) {
            // Let the ORB Handle the Forwar Request Exception
            ((edu.uci.ece.zen.poa.POA) poa).getORB().handleForwardRequest(ex);
        }
        return edu.uci.ece.zen.orb.ServerRequestHandler.REQUEST_HANDLED;
    }

   /**
    * check if strategy same as this strategy
    * @param name 
    * @return boolean
    */
    public boolean validate( int name , IntHolder exceptionHolder )
        return (RequestProcessingStrategy.SERVANT_LOCATOR == name);
    }

    protected org.omg.PortableServer.ServantLocator manager;
    protected ThreadPolicyStrategy threadPolicyStrategy;
}
