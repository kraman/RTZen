/* --------------------------------------------------------------------------*
 * $Id: ServantLocatorStrategy.java,v 1.7 2003/09/03 20:44:19 spart Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa.mechanism;

// --- OMG Specific Imports ---
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.portable.InvokeHandler;
import edu.uci.ece.zen.orb.ResponseHandler;
import edu.uci.ece.zen.orb.giop.type.*;
import edu.uci.ece.zen.poa.*;
import edu.uci.ece.zen.utils.*;
import org.omg.CORBA.IntHolder;

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
    public synchronized void setInvokeHandler(java.lang.Object servantManager , IntHolder exceptionValue ){
        if (this.manager != null) {
            exceptionHolder.value = POARunnable.BadInvOrderException;
            return;
        }

        if (servantManager instanceof org.omg.PortableServer.ServantLocator) {
            this.manager = (org.omg.PortableServer.ServantLocator) servantManager;
        }
        
        exceptionHolder.value = POARunnable.WrongPolicyException;
    }

   /**
    * return servant locator associated with the strategy
    * @param name strategy type
    * @return Object servant locator
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
    */
    public synchronized Object getRequestProcessor(int name , IntHolder exceptionValue ){
        if (validate(name , exceptionValue) && this.manager != null) {
            return this.manager;
        }

        exceptionValue.value = POARunnable.ObjNotActiveException;
    }

   /**
    * Handle client request.
    * @param request client request.
    * @param poa corresponding POA
    * @param requests active requests
    * @return int return state
    */
    public int handleRequest(RequestMessage request, edu.uci.ece.zen.poa.POA poa,
             edu.uci.ece.zen.poa.SynchronizedInt requests , IntHolder exceptionValue) {
        FString ok = request.getObjectKey();
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
    public boolean validate( int name , IntHolder exceptionHolder ){
        exceptionHolder.value = POARunnable.NoException;
        return (RequestProcessingStrategy.SERVANT_LOCATOR == name);
    }

    protected org.omg.PortableServer.ServantLocator manager;
    protected ThreadPolicyStrategy threadPolicyStrategy;
}
