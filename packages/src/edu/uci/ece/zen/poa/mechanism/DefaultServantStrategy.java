package edu.uci.ece.zen.poa.mechanism;

import javax.realtime.ScopedMemory;

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivatorPOA;
import org.omg.PortableServer.ServantLocatorPOA;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.protocol.MSGRunnable;
import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.poa.ObjectKeyHelper;
import edu.uci.ece.zen.poa.POA;
import edu.uci.ece.zen.poa.POACurrent;
import edu.uci.ece.zen.poa.POAHashMap;
import edu.uci.ece.zen.poa.POAImpl;
import edu.uci.ece.zen.poa.POARunnable;
import edu.uci.ece.zen.poa.SynchronizedInt;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.Queue;
import edu.uci.ece.zen.utils.ZenProperties;

// XXX A lot of duplicated code, i have to fix it.

/**
 * Implements the strategy corresponding to the CORBA's USE_DEFAULT_SERVANT
 * Policy.
 * 
 * @author Arvind S. Krishna </a>
 * @author Hojjat Jafarpour </a>
 * @author Juan Colmenares </a>
 * 
 * @version 1.1
 * @since 1.0
 */
public final class DefaultServantStrategy extends RequestProcessingStrategy 
{
    private static final int name = RequestProcessingStrategy.DEFAULT_SERVANT;

    private edu.uci.ece.zen.poa.ActiveObjectMap aom; // Fly-weight reference to
                                                     // the AOM in the POA.

    private ThreadPolicyStrategy threadPolicyStrategy;

    private ServantRetentionStrategy retentionStrategy;

    protected org.omg.PortableServer.Servant defaultServant;

    private Object mutex = new byte[0];
    private Object mutex2 = new byte[0];

    private Queue msgrQueue;

    /**
     * <code> initialize </code> is used to set the handler for the
     * ActiveObjectMap only Strategy.This strategy is a <i>Fly Weight </i> and
     * points to the Active Object Map present in the Retain Strategy associated
     * with the POA.
     * 
     * @param strategy
     *            Retain Strategy that this strategy points to.
     * @param threadStrategy
     *            used to serialize the upcalls if the POA is Single Threaded.
     * @param exceptionValue
     *            exception value holder.
     * @exception org.omg.PortableServer.POAPackage.InvalidPolicy
     *                if the the Servant RetentionStrategy passed is Non-Retain.
     */
    public void initialize(ServantRetentionStrategy strategy,
            ThreadPolicyStrategy threadStrategy, IntHolder exceptionValue)
    {

        //TODO Check this.
        exceptionValue.value = POARunnable.NoException;

        if (strategy instanceof RetainStrategy)
        {
            this.aom = ((RetainStrategy) strategy).getAOM();
        }

        this.retentionStrategy = strategy;
        this.threadPolicyStrategy = threadStrategy;
    }

    /**
     * This operation is not supported by this strategy. Eventually it raises an
     * <code>InvalidPolicyException</code>.
     * 
     * @param servant
     * @param exceptionValue
     */
    public void setInvokeHandler(Object servant, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.NoException;

        if (servant instanceof Servant)
        {
            this.defaultServant = (Servant) servant;
        } else
        {
            exceptionValue.value = POARunnable.WrongPolicyException;
        }
    }

    /**
     * Returns <code>true</code> if the policy name corresponds to the Active
     * Object Map Only policy; otherwise returns <code>false</code>, and the
     * value holds in exceptionValue is different to zero.
     * 
     * @param policyName
     *            the policy ID.
     * @param exceptionValue
     *            exception value holder.
     * @return <code>true</code> if the policy name corresponds to the Active
     *         Object Map Only policy; otherwise returns <code>false</code>
     */
    public boolean validate(int policyName, IntHolder exceptionValue)
    {

        if (RequestProcessingStrategy.DEFAULT_SERVANT != policyName)
        {
            exceptionValue.value = POARunnable.WrongPolicyException;
            return false;
        }

        return true;
    }

    /**
     * @param name
     *            int
     * @return Object
     * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public Object getRequestProcessor(int name, IntHolder exceptionValue)
    {

        // TODO Check it.
        exceptionValue.value = POARunnable.NoException;
        if (this.validate(name, exceptionValue))
        {
            if (this.defaultServant != null)
            {
                return this.defaultServant;
            } else
            {
                exceptionValue.value = POARunnable.ObjNotActiveException;
                return null;
            }
        } else
        {
            exceptionValue.value = POARunnable.WrongPolicyException;
            return null;
        }
    }

    /**
     * Handles the request.
     * 
     * @param request
     *            the equest
     * @param poa
     *            poa facade
     * @param requests
     *            exception value holder.
     * @param exceptionValue
     */

    public void handleRequest(RequestMessage request, POA poa, 
            SynchronizedInt requests, IntHolder exceptionValue)
    {

        exceptionValue.value = POARunnable.NoException;

        Servant myServant = null;
        FString okey = null;
        POAHashMap map = null;

        if (retentionStrategy instanceof RetainStrategy)
        {
            okey = request.getObjectKey();

            int index = ObjectKeyHelper.servDemuxIndex(okey);
            int genCount = ObjectKeyHelper.servDemuxGenCount(okey);

            if (this.aom == null)
            {
                exceptionValue.value = POARunnable.ObjNotExistException;
                return;
            }

            RetainStrategy retainStrategy = (RetainStrategy) this.retentionStrategy;
            int count = retainStrategy.activeMap.getGenCount(index);
            map = (POAHashMap) retainStrategy.activeMap.mapEntry(index);

            if (count != genCount || !map.isActive())
            {
                myServant = defaultServant;
            } else
            {
                myServant = map.getServant();
            }
        } else
        {
            myServant = defaultServant;
        }

        if (myServant == null)
        {
            exceptionValue.value = POARunnable.ObjNotExistException;
            return;
        }

        POAImpl pimpl = (POAImpl) poa.poaMemoryArea.getPortal(); // the portal
                                                                 // == POAImpl

        // Logger.debug("logged debug 0");
        // if (ZenProperties.dbg) ZenProperties.logger.log(pimpl + "");
        // if (ZenProperties.dbg) ZenProperties.logger.log(pimpl.poaCurrent +
        // "");
        // if (ZenProperties.dbg)
        // ZenProperties.logger.log(pimpl.poaCurrent.get() + "");
        /*
        try
        {
            if (pimpl.poaCurrent.get() == null)
                pimpl.poaCurrent.set(poa.poaMemoryArea.newInstance(POACurrent.class));
        } catch (Exception e)
        {
            ZenProperties.logger.log(Logger.WARN, getClass(), "handleRequest", e);
        }

        ((POACurrent) pimpl.poaCurrent.get()).init(poa, okey, myServant);
        */

        CDROutputStream reply = null;

        if (map != null)
        {
            synchronized (mutex)
            {
                requests.increment();
                map.incrementActiveRequests();
            }
        }

        if (this.threadPolicyStrategy instanceof ThreadPolicyStrategy.SingleThreadModelStrategy)
        {
            synchronized(mutex2)
            {
                this.invoke(request, poa, myServant, reply);
            }
        }
        else
        {
            this.invoke(request, poa, myServant, reply);
        }

        if (map != null)
        {
            synchronized (mutex)
            {
                requests.decrementAndNotifyAll(poa.processingState == POA.DESTRUCTION_APPARANT);
                map.decrementActiveRequestsAndDeactivate();
            }
        }
    }

    /**
     * Makes the invocation to the servant. This method creates a memory scope
     * that is a child of the POAImpl scope and runs an instance of
     * 
     * @link{MSGRunnable} MSGRunnable in that scope.
     * @param request
     *            the request.
     * @param poa
     *            the POA facade.
     * @param myServant
     *            the servant.
     * @param reply
     *            the reply.
     */
    private void invoke(RequestMessage request, POA poa, Servant myServant, CDROutputStream reply)
    {
        ScopedMemory sm = ORB.getScopedRegion();
        MSGRunnable msgr = getMSGR();
        msgr.init(request, myServant, reply, poa.getORB());
        sm.enter(msgr);
        retMSGR(msgr);
        ORB.freeScopedRegion(sm);
        request.free();
    }

    /**
     * Returns an instance of MSGRunnable allocated (cached) in immortal memory.
     */
    private MSGRunnable getMSGR()
    {
        if (msgrQueue == null)
            msgrQueue = new Queue();

        Object msgr = msgrQueue.dequeue();
        if (msgr == null)
            return new MSGRunnable();
        else
            return (MSGRunnable) msgr;
    }

    /**
     * Puts the MSGRunnable back in to the cache.
     */
    private void retMSGR(MSGRunnable msgr)
    {
        msgrQueue.enqueue(msgr);
    }
}

//public class DefaultServantStrategy extends RequestProcessingStrategy
//{
//    private static final int name = RequestProcessingStrategy.DEFAULT_SERVANT;
//
//    protected ServantRetentionStrategy retentionStrategy = null;
//
//    protected org.omg.PortableServer.Servant servant;
//
//    protected ThreadPolicyStrategy threadPolicyStrategy;
//
//    private Object mutex = new byte[0];
//
//    /**
//     * Initializes the strategy.
//     *
//     * @param retain the servant retention strategy
//     * @param threadStrategy the thread policy strategy
//     * @param exceptionValue exception value holder.
//     */
//    public void initialize(ServantRetentionStrategy retain,
//            ThreadPolicyStrategy threadStrategy, IntHolder exceptionValue)
//    {
//        //TODO Check this.
//        exceptionValue.value = POARunnable.NoException;
//
//        if (retain instanceof RetainStrategy)
//        {
//            this.retentionStrategy = (RetainStrategy) retain;
//        } else
//        {
//            this.retentionStrategy = (NonRetainStrategy) retain;
//            this.threadPolicyStrategy = threadStrategy;
//        }
//    }
//
//    /**
//     * @param servant
//     * java.lang.Object
//     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
//     */
//    public synchronized void setInvokeHandler(Object servant, IntHolder
// exceptionValue)
//    {
//    }
//
//    /**
//     * @param policyName
//     * @return boolean
//     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
//     */
//    public boolean validate(int policyName, IntHolder exceptionValue)
//    {
//        exceptionValue.value = POARunnable.NoException;
//
//        if (RequestProcessingStrategy.DEFAULT_SERVANT != policyName)
//        {
//            exceptionValue.value = POARunnable.WrongPolicyException;
//            return false;
//        }
//
//        return true;
//    }
//
//    /**
//     * @param name
//     * int
//     * @return Object
//     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
//     * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
//     */
//    public Object getRequestProcessor(int name, IntHolder exceptionValue)
//    {
//        exceptionValue.value = POARunnable.NoException;
//        if (this.validate(name, exceptionValue))
//        {
//            if (this.servant != null)
//            {
//                return this.servant;
//            }
//            else
//            {
//                exceptionValue.value = POARunnable.ObjNotActiveException;
//                return null;
//            }
//        }
//        else
//        {
//            exceptionValue.value = POARunnable.WrongPolicyException;
//            return null;
//        }
//    }
//
//    /**
//     * @param request
//     * ServerRequest
//     * @param poa
//     * edu.uci.ece.zen.poa.POA
//     * @param requests
//     * edu.uci.ece.zen.poa.SynchronizedInt
//     * @return int
//     */
//    public void handleRequest(RequestMessage request, POA poa, SynchronizedInt
// requests, IntHolder exceptionValue)
//    {
//        // if strategy == retain -> use AOM
//        // if(servant is not found in AOM use default servant)
//        // else use default servant
//        
//        exceptionValue.value = POARunnable.NoException;
//
//        if (this.retentionStrategy != null)
//        {
//            handleIfRetain(request, poa, requests, exceptionValue);
//        } // can thorw ObjectNotActive, ClassCastException and WrongPolicy
//
//        if (exceptionValue.value != 0)
//        {
//            //something happened here and nothing is done
//            exceptionValue.value = POARunnable.NoException;
//        }
//
//        POAImpl pimpl = (POAImpl) ((ScopedMemory) poa.poaMemoryArea)
//                .getPortal();
//
//        FString okey = request.getObjectKey();
//        FString oid = pimpl.getFString();
//        ObjectKeyHelper.getId(okey, oid);
//
//        // handling as if non retain is in place
//        if (this.servant == null)
//        {
//            exceptionValue.value = POARunnable.ObjAdapterException;
//            pimpl.retFString(oid);
//            return;
//        }
//
//        org.omg.PortableServer.Servant myServant = this.servant;
//        org.omg.CORBA.portable.InvokeHandler invokeHandler =
// (org.omg.CORBA.portable.InvokeHandler) this.servant;
//
//        synchronized (mutex)
//        {
//            requests.increment();
//        }
//
//        ((POACurrent) pimpl.poaCurrent.get()).init(poa, okey, this.servant);
//
//        ResponseHandler responseHandler = new ResponseHandler(poa.getORB(), request);
//
//        this.threadPolicyStrategy.enter(invokeHandler);
//
//        CDROutputStream reply;
//        if (request.getOperation().equals("_is_a"))
//        {
//            boolean _result = myServant._is_a(request.getCDRInputStream()
//                    .read_string());
//            org.omg.CORBA.portable.OutputStream _output = responseHandler
//                    .createReply();
//            _output.write_boolean(_result);
//            reply = (CDROutputStream) _output;
//        } else if (request.getOperation().equals("_non_existent"))
//        {
//            boolean _result = myServant._non_existent();
//            org.omg.CORBA.portable.OutputStream _output = responseHandler
//                    .createReply();
//            _output.write_boolean(_result);
//            reply = (CDROutputStream) _output;
//        } else
//        {
//            reply = (CDROutputStream) invokeHandler._invoke(request
//                    .getOperation().toString(),
//                    (org.omg.CORBA.portable.InputStream) request
//                            .getCDRInputStream(), responseHandler);
//        }
//        pimpl.retFString(oid);
//
//        this.threadPolicyStrategy.exit(invokeHandler);
//
//        // --Post Invoke
//        synchronized (mutex)
//        {
//            requests
//                    .decrementAndNotifyAll(poa.poaState == POA.DESTRUCTION_APPARANT);
//        }
//        //reply.sendUsing(request.getTransport());
//        //return edu.uci.ece.zen.orb.ServerRequestHandler.REQUEST_HANDLED;
//    }
//
//    private void handleIfRetain(RequestMessage request,
//            edu.uci.ece.zen.poa.POA poa,
//            edu.uci.ece.zen.poa.SynchronizedInt requests,
//            IntHolder exceptionValue)
//    {
//        exceptionValue.value = POARunnable.NoException;
//        POAImpl pimpl = (POAImpl) ((ScopedMemory) poa.poaMemoryArea)
//                .getPortal();
//        FString ok = request.getObjectKey();
//        FString oid = pimpl.getFString();
//        ObjectKeyHelper.getId(ok, oid);
//
//        edu.uci.ece.zen.poa.POAHashMap okey = this.retentionStrategy
//                .getHashMap(oid, exceptionValue);
//        if (exceptionValue.value != POARunnable.NoException)
//        {
//            pimpl.retFString(oid);
//        }
//
//        org.omg.PortableServer.Servant myServant = okey.getServant();
//
//        org.omg.CORBA.portable.InvokeHandler invokeHandler =
// (org.omg.CORBA.portable.InvokeHandler) okey
//                .getServant();
//
//        if (okey == null || !okey.isActive())
//        {
//            exceptionValue.value = POARunnable.ObjNotExistException;
//            pimpl.retFString(oid);
//            return;
//        }
//
//        // --PRE-INVOKE
//        synchronized (mutex)
//        {
//            okey.incrementActiveRequests();
//            requests.increment();
//        }
//
//        ((POACurrent) pimpl.poaCurrent.get()).init(poa, ok, okey.getServant());
//
//        ResponseHandler responseHandler = new ResponseHandler(poa.getORB(),
//                request);
//
//        this.threadPolicyStrategy.enter(invokeHandler);
//        CDROutputStream reply;
//        if (request.getOperation().equals("_is_a"))
//        {
//            boolean _result = myServant._is_a(request.getCDRInputStream()
//                    .read_string());
//            org.omg.CORBA.portable.OutputStream _output = responseHandler
//                    .createReply();
//            _output.write_boolean(_result);
//            reply = (CDROutputStream) _output;
//        } else if (request.getOperation().equals("_non_existent"))
//        {
//            boolean _result = myServant._non_existent();
//            org.omg.CORBA.portable.OutputStream _output = responseHandler
//                    .createReply();
//            _output.write_boolean(_result);
//            reply = (CDROutputStream) _output;
//        } else
//        {
//            reply = (CDROutputStream) invokeHandler._invoke(request
//                    .getOperation().toString(),
//                    (org.omg.CORBA.portable.InputStream) request
//                            .getCDRInputStream(), responseHandler);
//        }
//        pimpl.retFString(ok);
//        pimpl.retFString(oid);
//
//        this.threadPolicyStrategy.exit(invokeHandler);
//
//        // --Post Invoke
//        synchronized (mutex)
//        {
//            requests
//                    .decrementAndNotifyAll(poa.poaState == POA.DESTRUCTION_APPARANT);
//            okey.decrementActiveRequestsAndDeactivate();
//        }
//        //reply.sendUsing(request.getTransport());
//        //return edu.uci.ece.zen.orb.ServerRequestHandler.REQUEST_HANDLED;
//    }
//
//}
//
