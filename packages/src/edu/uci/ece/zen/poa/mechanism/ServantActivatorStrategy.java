/* --------------------------------------------------------------------------*
 * $Id: ServantActivatorStrategy.java,v 1.7 2003/09/03 20:44:19 spart Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa.mechanism;


// ---- OMG specific imports ---
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.portable.InvokeHandler;

import edu.uci.ece.zen.orb.ResponseHandler;
import edu.uci.ece.zen.orb.ServerReply;
import edu.uci.ece.zen.orb.ServerRequest;
import edu.uci.ece.zen.poa.ObjectID;
import edu.uci.ece.zen.poa.POAHashMap;


public class ServantActivatorStrategy extends
            ServantManagerStrategy {
   /**
    * Initialize Strategy
    * @param retain ServantRetentionStrategy
    * @param threadStrategy ThreadPolicyStrategy
    * @param uniqunessStrategy IdUniquenessStrategy
    * @throws org.omg.PortableServer.POAPackage.InvalidPolicy
    */
    public void init(ServantRetentionStrategy retain, ThreadPolicyStrategy threadStrategy, org.omg.CORBA.IntHolder ih
            IdUniquenessStrategy uniqunessStrategy) throws
                org.omg.PortableServer.POAPackage.InvalidPolicy {
        try {
            this.retain = (RetainStrategy) retain;
            this.threadPolicyStrategy = threadStrategy;
            this.uniquenessStrategy = uniquenessStrategy;
        } catch (ClassCastException ex) {
            throw new org.omg.PortableServer.POAPackage.InvalidPolicy();
        }

    }

   /**
    * set concrete Activator
    * @param servantManager Object
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public synchronized void setInvokeHandler(java.lang.Object servantManager)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
        if (this.manager != null) {
            throw new org.omg.CORBA.BAD_INV_ORDER(6,
                    CompletionStatus.COMPLETED_NO);
        }

        if (servantManager instanceof org.omg.PortableServer.ServantActivator) {
            this.manager = (org.omg.PortableServer.ServantActivator)
                    servantManager;
        }

        throw new org.omg.PortableServer.POAPackage.WrongPolicy();
    }

   /**
    * Return servant activator
    * @param name strategy-type
    * @return Object servant activator
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
    */
    public synchronized Object getRequestProcessor(int name) throws
                org.omg.PortableServer.POAPackage.ObjectNotActive,
                org.omg.PortableServer.POAPackage.WrongPolicy {
        if (this.validate(name) && (this.manager != null)) {
            return this.manager;
        }

        throw new org.omg.PortableServer.POAPackage.ObjectNotActive();
    }

    /**
     * <code> etherealize </code> calls etherealize on all the servants
     * incarnated by this Servant Manager.
     * This method is invoked during the destruction of the POA.
     * @param poa The POA that hosts the object
     * @param clean_up_in_progress indicates if there is a clean up in progress.
     * @param remaining_activations indcated whether to leave the remaining
     * activations.
     */
    public void etherealize(org.omg.PortableServer.POA poa,
            boolean clean_up_in_progress,
            boolean remaining_activations) {
        try {
            java.util.Enumeration e = this.retain.getAOM().elements();
            edu.uci.ece.zen.poa.ObjectKey ok = null;

            while (e.hasMoreElements()) {
                ok = (edu.uci.ece.zen.poa.ObjectKey) e.nextElement();
                org.omg.PortableServer.Servant servant = this.retain.getServant(new ObjectID(ok.getId()));

                synchronized (mutex) {
                    this.manager.etherealize(ok.getId(), poa, servant,
                            clean_up_in_progress, remaining_activations);
                }
            }
        } catch (Exception ex) {}

    }
   /**
    * Handle client request
    * @param request ServerRequest
    * @param poa edu.uci.ece.zen.poa.POA
    * @param requests edu.uci.ece.zen.poa.SynchronizedInt
    * @return int
    */
    public int handleRequest(ServerRequest request,
            edu.uci.ece.zen.poa.POA poa,
            edu.uci.ece.zen.poa.SynchronizedInt requests) {
        InvokeHandler invokeHandler = null;
        edu.uci.ece.zen.poa.ObjectKey ok = request.getObjectKey();
        edu.uci.ece.zen.poa.ObjectID oid = new edu.uci.ece.zen.poa.ObjectID(ok.getId());
        POAHashMap map = null;

        // first consult the AOM for the Request Processor
        try {
            org.omg.PortableServer.Servant servant = this.retain.getServant(oid);

            if (servant != null) {
                invokeHandler = (InvokeHandler) servant;
            }
        } catch (Exception ex) {
            if (this.manager == null) {
                throw new org.omg.CORBA.OBJ_ADAPTER(4,
                        CompletionStatus.COMPLETED_NO);
            }

            // --- INCARNATE ---
            try {
                invokeHandler = this.incarnate(ok, poa);
                // Add the association in the AOM
                map = new POAHashMap(oid, (org.omg.PortableServer.Servant)
                        invokeHandler);
                this.retain.add(oid, map);

            } catch (org.omg.PortableServer.ForwardRequest e) {
                edu.uci.ece.zen.orb.Logger.error("There is a forward request exception:"
                        + e);
                // Ask the ORB to handle the ForwardRequest Exception
                ((edu.uci.ece.zen.poa.POA) poa).getORB().handleForwardRequest(e);
                return edu.uci.ece.zen.orb.ServerRequestHandler.REQUEST_NOT_HANDLED;
            } catch (org.omg.PortableServer.POAPackage.WrongPolicy wr) {
                throw new org.omg.CORBA.INTERNAL();
            }

        }
        map = this.retain.getHashMap(oid);

        if (invokeHandler == null) {
            throw new org.omg.CORBA.INTERNAL("POAERROR: The invokeHandler for this ObjectKey is"
                    + " not present in the ActiveObjectMap");
        }

        // --- PRE-INVOKE ---
        synchronized (mutex) {
            requests.increment();
            map.incrementActiveRequests();
        }
        edu.uci.ece.zen.poa.ThreadSpecificPOACurrent.putInvocationContext(poa,
                ok, (org.omg.PortableServer.Servant) invokeHandler);

        ResponseHandler responseHandler = new ResponseHandler(((edu.uci.ece.zen.poa.POA) poa).getORB(),
                request.message.getRequestId(),
                request.message.getGIOPVersion().major,
                request.message.getGIOPVersion().minor);

        this.threadPolicyStrategy.enter(invokeHandler);
        org.omg.PortableServer.Servant myServant = (org.omg.PortableServer.Servant)invokeHandler;
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


       			 reply = (ServerReply) invokeHandler._invoke(request.message.getOperation(),
                	 request.message.getIstream(), responseHandler);
                   }

        this.threadPolicyStrategy.exit(invokeHandler);
        synchronized (mutex) {
            requests.decrementAndNotifyAll(poa.isDestructionApparent());
            try {
                map.decrementActiveRequestsAndDeactivate();
            } catch (Exception ex) {}
        }
        reply.sendUsing(request.getTransport());

        return edu.uci.ece.zen.orb.ServerRequestHandler.REQUEST_HANDLED;

    }

   /**
    * Check if strategy same as this strategy
    * @param name strategy type
    * @return boolean
    */
    public boolean validate(int name) throws
                org.omg.PortableServer.POAPackage.WrongPolicy {
        return (RequestProcessingStrategy.SERVANT_ACTIVATOR == name)
                ? true
                : false;
    }

    /**
     * <code> incarnate </code> is used for the purpose of <i> incarnating </i>
     * servant for the ObjectId the POA specified in the parameter.
     * @param ok specifies the ObjectKey associated with the servant to be
     * incarnated.
     * @param poa specifies the POA for which the ServantManager in incarnating a
     * Servant.
     * @exception ex is thrown if incarnation needs to be performed on a
     * different orb/poa.
     */

    protected InvokeHandler incarnate(edu.uci.ece.zen.poa.ObjectKey ok,
            org.omg.PortableServer.POA poa) throws
                org.omg.PortableServer.ForwardRequest {
        InvokeHandler invokeHandler = null;

        synchronized (mutex) {
            try {
                invokeHandler = (InvokeHandler)
                        this.manager.incarnate(ok.getId(), poa);
                if (this.uniquenessStrategy.validate(IdUniquenessStrategy.UNIQUE_ID)
                        && this.retain.servantPresent((org.omg.PortableServer.Servant) invokeHandler)) {
                    throw new org.omg.CORBA.OBJ_ADAPTER();
                }

                ObjectID oid = new ObjectID(ok.getId());
                POAHashMap map = new POAHashMap(oid,
                        (org.omg.PortableServer.Servant)
                        invokeHandler);

                // Add the Reference to the AOM
                this.retain.add(oid, map);
            } catch (Exception ex) {}
            
            return invokeHandler;
        }
    }

    private static final int name = RequestProcessingStrategy.SERVANT_ACTIVATOR;

    // -- Policy Strategies in the POA ---
    protected org.omg.PortableServer.ServantActivator manager;
    protected RetainStrategy retain;
    protected ThreadPolicyStrategy threadPolicyStrategy;
    protected IdUniquenessStrategy uniquenessStrategy;

    // --Mutual exclusion lock for incarnate/etherealize
    private Object mutex = new byte[0];
    
}
