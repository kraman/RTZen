/*******************************************************************************
 * --------------------------------------------------------------------------
 * $Id: ServantActivatorStrategy.java,v 1.7 2003/09/03 20:44:19 spart Exp $
 * --------------------------------------------------------------------------
 */

package edu.uci.ece.zen.poa.mechanism;

// ---- OMG specific imports ---
import org.omg.CORBA.IntHolder;
import org.omg.CORBA.portable.InvokeHandler;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.ResponseHandler;
import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.poa.ObjectKeyHelper;
import edu.uci.ece.zen.poa.POA;
import edu.uci.ece.zen.poa.POAHashMap;
import edu.uci.ece.zen.poa.POAImpl;
import edu.uci.ece.zen.poa.POARunnable;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

public class ServantActivatorStrategy extends ServantManagerStrategy {

    private static final int name = RequestProcessingStrategy.SERVANT_ACTIVATOR;

    // -- Policy Strategies in the POA ---
    protected org.omg.PortableServer.ServantActivator manager;

    protected RetainStrategy retain;

    protected ThreadPolicyStrategy threadPolicyStrategy;

    protected IdUniquenessStrategy uniquenessStrategy;

    protected POAImpl pimpl;

    // --Mutual exclusion lock for incarnate/etherealize
    private Object mutex = new byte[0];

    /**
     * Initialize Strategy
     * 
     * @param retain
     *            ServantRetentionStrategy
     * @param threadStrategy
     *            ThreadPolicyStrategy
     * @param uniqunessStrategy
     *            IdUniquenessStrategy
     * @throws org.omg.PortableServer.POAPackage.InvalidPolicy
     */
    public void init(ServantRetentionStrategy retain,
            ThreadPolicyStrategy threadStrategy,
            IdUniquenessStrategy uniqunessStrategy, POAImpl pimpl,
            IntHolder exceptionValue) {
        if (!(retain instanceof RetainStrategy)) {
            exceptionValue.value = POARunnable.InvalidPolicyException;
            return;
        }

        this.retain = (RetainStrategy) retain;
        this.threadPolicyStrategy = threadStrategy;
        this.uniquenessStrategy = uniquenessStrategy;
    }

    /**
     * set concrete Activator
     * 
     * @param servantManager
     *            Object
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public synchronized void setInvokeHandler(java.lang.Object servantManager,
            IntHolder exceptionValue) {
        if (this.manager != null) {
            exceptionValue.value = POARunnable.BadInvOrderException;
            return;
        }

        if (servantManager instanceof org.omg.PortableServer.ServantActivator) {
            this.manager = (org.omg.PortableServer.ServantActivator) servantManager;
        }

        exceptionValue.value = POARunnable.WrongPolicyException;
    }

    /**
     * Return servant activator
     * 
     * @param name
     *            strategy-type
     * @return Object servant activator
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
     */
    public synchronized Object getRequestProcessor(int name,
            IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        validate(name, exceptionValue);
        if (exceptionValue.value != POARunnable.NoException) return null;
        else if (manager != null) return manager;

        exceptionValue.value = POARunnable.ObjNotActiveException;
        return null;
    }

    /**
     * <code> etherealize </code> calls etherealize on all the servants
     * incarnated by this Servant Manager. This method is invoked during the
     * destruction of the POA.
     * 
     * @param poa
     *            The POA that hosts the object
     * @param clean_up_in_progress
     *            indicates if there is a clean up in progress.
     * @param remaining_activations
     *            indcated whether to leave the remaining activations.
     */
    public void etherealize(org.omg.PortableServer.POA poa,
            boolean clean_up_in_progress, boolean remaining_activations) {
        /*
         * KLUDGE: Ignore for now try { java.util.Enumeration e =
         * this.retain.getAOM().elements(); edu.uci.ece.zen.poa.ObjectKey ok =
         * null; while (e.hasMoreElements()) { ok =
         * (edu.uci.ece.zen.poa.ObjectKey) e.nextElement();
         * org.omg.PortableServer.Servant servant = this.retain.getServant(new
         * ObjectID(ok.getId())); synchronized (mutex) {
         * this.manager.etherealize(ok.getId(), poa, servant,
         * clean_up_in_progress, remaining_activations); } } } catch (Exception
         * ex) {}
         */
    }

    /**
     * Handle client request
     * 
     * @param request
     *            ServerRequest
     * @param poa
     *            edu.uci.ece.zen.poa.POA
     * @param requests
     *            edu.uci.ece.zen.poa.SynchronizedInt
     * @return int
     */
    public void handleRequest(RequestMessage request,
            edu.uci.ece.zen.poa.POA poa,
            edu.uci.ece.zen.poa.SynchronizedInt requests,
            IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        InvokeHandler invokeHandler = null;
        FString ok = request.getObjectKey();
        FString oid = pimpl.getFString();
        ObjectKeyHelper.getId(ok, oid);

        POAHashMap map = null;

        // first consult the AOM for the Request Processor
        org.omg.PortableServer.Servant servant = this.retain.getServant(oid,
                exceptionValue);
        if (exceptionValue.value == POARunnable.NoException) {
            if (servant != null) {
                invokeHandler = (InvokeHandler) servant;
            }
        } else {
            if (this.manager == null) {
                exceptionValue.value = POARunnable.ObjAdapterException;
                pimpl.retFString(oid);
                return;
            }

            // --- INCARNATE ---
            exceptionValue.value = POARunnable.NoException;

            invokeHandler = this.incarnate(ok, oid, poa, pimpl, exceptionValue);
            if (exceptionValue.value == POARunnable.NoException) {
                // Add the association in the AOM
                map = pimpl.getPOAHashMap();
                map.init(oid, (org.omg.PortableServer.Servant) invokeHandler);
                this.retain.add(oid, map, exceptionValue);
            }

            if (exceptionValue.value == POARunnable.ForwardRequestException) {
                ZenProperties.logger.log("There is a forward request exception");
                // Ask the ORB to handle the ForwardRequest Exception
                //((edu.uci.ece.zen.poa.POA)
                // poa).getORB().handleForwardRequest(e);
                if (map != null) pimpl.retPOAHashMap(map);
                pimpl.retFString(oid);
                return;// edu.uci.ece.zen.orb.ServerRequestHandler.REQUEST_NOT_HANDLED;
            } else {
                if (map != null) pimpl.retPOAHashMap(map);
                pimpl.retFString(oid);
                exceptionValue.value = POARunnable.InternalException;
                return;
            }
        }

        map = this.retain.getHashMap(oid, exceptionValue);

        if (invokeHandler == null) {
            pimpl.retFString(oid);
            exceptionValue.value = POARunnable.InternalException;
        }

        // --- PRE-INVOKE ---
        synchronized (mutex) {
            requests.increment();
            map.incrementActiveRequests();
        }

        ((edu.uci.ece.zen.poa.POACurrent) pimpl.poaCurrent.get()).init(poa, ok,
                (org.omg.PortableServer.Servant) invokeHandler);

        ResponseHandler responseHandler = new ResponseHandler(poa.getORB(),
                request);

        this.threadPolicyStrategy.enter(invokeHandler);
        org.omg.PortableServer.Servant myServant = (org.omg.PortableServer.Servant) invokeHandler;
        CDROutputStream reply;

        if (request.getOperation().equals("_is_a")) {
            boolean _result = myServant._is_a(request.getCDRInputStream()
                    .read_string());
            org.omg.CORBA.portable.OutputStream _output = responseHandler
                    .createReply();
            _output.write_boolean(_result);
            reply = (CDROutputStream) _output;
        } else if (request.getOperation().equals("_non_existent")) {
            boolean _result = myServant._non_existent();
            org.omg.CORBA.portable.OutputStream _output = responseHandler
                    .createReply();
            _output.write_boolean(_result);
            reply = (CDROutputStream) _output;
        } else {
            reply = (CDROutputStream) invokeHandler._invoke(request
                    .getOperation().toString(),
                    (org.omg.CORBA.portable.InputStream) request
                            .getCDRInputStream(), responseHandler);
        }

        this.threadPolicyStrategy.exit(invokeHandler);

        synchronized (mutex) {
            requests
                    .decrementAndNotifyAll(pimpl.self.processingState == POA.DESTRUCTION_APPARANT);
            try {
                map.decrementActiveRequestsAndDeactivate();
            } catch (Exception ex) {
            }
        }

        //send back reply
        //reply.sendUsing(request.getTransport());

        pimpl.retFString(oid);
        return;
    }

    /**
     * Check if strategy same as this strategy
     * 
     * @param name
     *            strategy type
     * @return boolean
     */
    public boolean validate(int name, IntHolder exceptionValue) {
        return (RequestProcessingStrategy.SERVANT_ACTIVATOR == name);
    }

    /**
     * <code> incarnate </code> is used for the purpose of <i>incarnating </i>
     * servant for the ObjectId the POA specified in the parameter.
     * 
     * @param ok
     *            specifies the ObjectKey associated with the servant to be
     *            incarnated.
     * @param poa
     *            specifies the POA for which the ServantManager in incarnating
     *            a Servant.
     * @exception ex
     *                is thrown if incarnation needs to be performed on a
     *                different orb/poa.
     */

    protected InvokeHandler incarnate(FString ok, FString oid,
            org.omg.PortableServer.POA poa, POAImpl pimpl,
            IntHolder exceptionValue) {
        InvokeHandler invokeHandler = null;
        synchronized (mutex) {
            //KLUDGE:
            try {
                invokeHandler = (InvokeHandler) this.manager.incarnate(oid
                        .getData(), poa);
            } catch (Exception fre) {
                ZenProperties.logger.log(Logger.WARN, getClass(), "incarnate", fre);
            }

            if (this.uniquenessStrategy
                    .validate(IdUniquenessStrategy.UNIQUE_ID)
                    && this.retain.servantPresent(
                            (org.omg.PortableServer.Servant) invokeHandler,
                            exceptionValue)) {
                exceptionValue.value = POARunnable.ObjAdapterException;
                return null;
            }

            POAHashMap map = pimpl.getPOAHashMap();
            map.init(oid, (org.omg.PortableServer.Servant) invokeHandler);

            // Add the Reference to the AOM
            this.retain.add(oid, map, exceptionValue);
            return invokeHandler;
        }
    }
}