/* --------------------------------------------------------------------------*
 * $Id: ActiveObjectMapOnlyStrategy.java,v 1.1 2003/11/26 22:28:50 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa.mechanism;


// ---- OMG specific imports ----
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.portable.InvokeHandler;

import edu.uci.ece.zen.orb.ResponseHandler;
import edu.uci.ece.zen.orb.ServerReply;
import edu.uci.ece.zen.orb.ServerRequest;
import edu.uci.ece.zen.poa.ActiveDemuxLoc;
import edu.uci.ece.zen.poa.POAHashMap;


/**
 * <code> ActiveObjectMapOnlyStrategy </code> is one of the three policy
 * implementations of the RequestProcessing Strategy.
 * The servant here is a fly-weight that points to the AOM in Zen.
 * The POA uses the ActiveObjectMapOnlyStrategy to invoke operations
 * on the AOM.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 * @see edu.uci.ece.zen.poa.mechanism.DefaultServantStrategy
 * @see edu.uci.ece.zen.poa.mechanism.ServantActivatorStrategy
 * @see edu.uci.ece.zen.poa.mechanism.ServantLocatorStrategy
 * @since 1.0
 */

public final class ActiveObjectMapOnlyStrategy extends
            RequestProcessingStrategy {

    /**
     * <code> initialize </code> is used to set the handler for the
     * ActiveObjectMap only Strategy.This strategy is a <i> Fly Weight </i>
     * and points to the Active Object Map present in the Retain Strategy
     * associated with the POA.
     * @param strategy Retain Strategy that this strategy points to.
     * @param threadStrategy used to serialize the upcalls if the POA is
     Single Threaded.
     * @exception org.omg.PortableServer.POAPackage.InvalidPolicy
     if the the Servant RetentionStrategy passed is Non-Retain.
     */

    public void initialize(ServantRetentionStrategy strategy,
            ThreadPolicyStrategy threadStrategy)
        throws org.omg.PortableServer.POAPackage.InvalidPolicy {
        try {
            this.retainStr = (RetainStrategy) strategy;
            this.servant = this.retainStr.getAOM();
            this.threadPolicyStrategy = threadStrategy;
        } catch (ClassCastException ex) {
            throw new org.omg.PortableServer.POAPackage.InvalidPolicy();
        } catch (org.omg.PortableServer.POAPackage.WrongPolicy wrPl) {
            throw new org.omg.PortableServer.POAPackage.InvalidPolicy();
        }
    }

/**
 *
 * @param servant java.lang.Object
 */
    public void setInvokeHandler(java.lang.Object servant)throws
                org.omg.PortableServer.POAPackage.WrongPolicy {
        throw new org.omg.PortableServer.POAPackage.WrongPolicy();
    }

    public boolean validate(int policyName) throws
                org.omg.PortableServer.POAPackage.WrongPolicy {
        if (RequestProcessingStrategy.ACTIVE_OBJECT_MAP == policyName) {
            return true;
        } else {
            throw new org.omg.PortableServer.POAPackage.WrongPolicy();
        }
    }
/**
 *
 * @param name int
 * @return Object
 * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
 * @throws org.omg.PortableServer.POAPackage.WrongPolicy
 */
    public Object getRequestProcessor(int name) throws
                org.omg.PortableServer.POAPackage.ObjectNotActive,
                org.omg.PortableServer.POAPackage.WrongPolicy {
        throw new org.omg.PortableServer.POAPackage.WrongPolicy();
    }

/**
 *
 * @param request ServerRequest
 * @param poa edu.uci.ece.zen.poa.POA
 * @param requests edu.uci.ece.zen.poa.SynchronizedInt
 * @return int
 */
    public int handleRequest(ServerRequest request,
            edu.uci.ece.zen.poa.POA poa,
            edu.uci.ece.zen.poa.SynchronizedInt requests) {
        edu.uci.ece.zen.poa.ObjectKey okey = request.getObjectKey();
        // edu.uci.ece.zen.poa.ObjectID  oid  = new ObjectID(okey.getId());
        ActiveDemuxLoc loc = okey.servDemuxIndex();

        if (this.servant == null) {
            throw new org.omg.CORBA.OBJ_ADAPTER(2, CompletionStatus.COMPLETED_NO);
        }

        int count = this.retainStr.activeMap.getGenCount(loc.index);
        POAHashMap map = this.retainStr.activeMap.mapEntry(loc.index);

        if (count != loc.count || !map.isActive()) {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST(2,
                    CompletionStatus.COMPLETED_NO);
        }

        org.omg.PortableServer.Servant myServant = this.retainStr.activeMap.mapEntry(loc.index).getServant();
        InvokeHandler invokeHandler = (InvokeHandler) myServant;



        // Logger.debug("logged debug 0");
        edu.uci.ece.zen.poa.ThreadSpecificPOACurrent.putInvocationContext(poa,
                okey, (org.omg.PortableServer.Servant) invokeHandler);

        ResponseHandler responseHandler = new ResponseHandler(((edu.uci.ece.zen.poa.POA) poa).getORB(),
                request.message.getRequestId(),
                request.message.getGIOPVersion().major,
                request.message.getGIOPVersion().minor);

        ServerReply reply;
        synchronized (mutex) {
            requests.increment();
            map.incrementActiveRequests();
        }
        this.threadPolicyStrategy.enter(invokeHandler);


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
        		reply = (ServerReply)
                	invokeHandler._invoke(request.message.getOperation(),
                	request.message.getIstream(), responseHandler);
                  }


        // --- POST-INVOKE ---
        this.threadPolicyStrategy.exit(invokeHandler);
        synchronized (mutex) {
            requests.decrementAndNotifyAll(poa.isDestructionApparent());
            map.decrementActiveRequestsAndDeactivate();
        }
        reply.sendUsing(request.getTransport());
        reply = null; // Enable GC of the reply Object
        return edu.uci.ece.zen.orb.ServerRequestHandler.REQUEST_HANDLED;

    }

    private static final int name = RequestProcessingStrategy.ACTIVE_OBJECT_MAP;
    // --Fly Weight reference to the AOM in the POA ---
    private edu.uci.ece.zen.poa.ActiveObjectMap servant;
    private ThreadPolicyStrategy threadPolicyStrategy;
    private RetainStrategy retainStr;
    // ---Mutex ----
    private Object mutex = new byte[0];
}
