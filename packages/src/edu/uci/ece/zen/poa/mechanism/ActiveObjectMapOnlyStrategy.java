package edu.uci.ece.zen.poa.mechanism;

// ---- OMG specific imports ----
import javax.realtime.ScopedMemory;

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.portable.InvokeHandler;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.giop.MSGRunnable;
import edu.uci.ece.zen.poa.ObjectKeyHelper;
import edu.uci.ece.zen.poa.POA;
import edu.uci.ece.zen.poa.POACurrent;
import edu.uci.ece.zen.poa.POAHashMap;
import edu.uci.ece.zen.poa.POAImpl;
import edu.uci.ece.zen.poa.POARunnable;
import edu.uci.ece.zen.poa.SynchronizedInt;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.Queue;
import edu.uci.ece.zen.utils.ZenProperties;

/**
 * <code> ActiveObjectMapOnlyStrategy </code> is one of the three policy
 * implementations of the RequestProcessing Strategy. The servant here is a
 * fly-weight that points to the AOM in Zen. The POA uses the
 * ActiveObjectMapOnlyStrategy to invoke operations on the AOM.
 * 
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna </a>
 * @version 1.0
 * @see edu.uci.ece.zen.poa.mechanism.DefaultServantStrategy
 * @see edu.uci.ece.zen.poa.mechanism.ServantActivatorStrategy
 * @see edu.uci.ece.zen.poa.mechanism.ServantLocatorStrategy
 * @since 1.0
 */

public final class ActiveObjectMapOnlyStrategy extends
        RequestProcessingStrategy {
    // --Fly Weight reference to the AOM in the POA ---
    private edu.uci.ece.zen.poa.ActiveObjectMap servant;

    private ThreadPolicyStrategy threadPolicyStrategy;

    private RetainStrategy retainStr;

    // ---Mutex ----
    private Object mutex = new byte[0];

    private static final int name = RequestProcessingStrategy.ACTIVE_OBJECT_MAP;

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
     * @exception org.omg.PortableServer.POAPackage.InvalidPolicy
     *                if the the Servant RetentionStrategy passed is Non-Retain.
     */

    public void initialize(ServantRetentionStrategy strategy,
            ThreadPolicyStrategy threadStrategy, IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        if (strategy instanceof RetainStrategy) {
            this.retainStr = (RetainStrategy) strategy;
            this.servant = this.retainStr.getAOM();
            this.threadPolicyStrategy = threadStrategy;
        } else {
            exceptionValue.value = POARunnable.InvalidPolicyException;
        }
    }

    /**
     * @param servant
     *            java.lang.Object
     */
    public void setInvokeHandler(java.lang.Object servant,
            IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.InvalidPolicyException;
    }

    public boolean validate(int policyName, IntHolder exceptionValue) {
        if (RequestProcessingStrategy.ACTIVE_OBJECT_MAP == policyName) {
            return true;
        } else {
            exceptionValue.value = POARunnable.WrongPolicyException;
            return false;
        }
    }

    /**
     * @param name
     *            int
     * @return Object
     * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public Object getRequestProcessor(int name, IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return null;
    }

    /**
     * @param request
     *            ServerRequest
     * @param poa
     *            edu.uci.ece.zen.poa.POA
     * @param requests
     *            edu.uci.ece.zen.poa.SynchronizedInt
     * @return int
     */

    public void handleRequest(
            edu.uci.ece.zen.orb.giop.type.RequestMessage request, POA poa,
            SynchronizedInt requests, IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        FString okey = request.getObjectKey();

        int index = ObjectKeyHelper.servDemuxIndex(okey);
        int genCount = ObjectKeyHelper.servDemuxGenCount(okey);

        if (this.servant == null) {
            exceptionValue.value = POARunnable.ObjNotExistException;
            return;
        }

        int count = this.retainStr.activeMap.getGenCount(index);
        POAHashMap map = (POAHashMap) this.retainStr.activeMap.mapEntry(index);

        if (count != genCount || !map.isActive()) {
            exceptionValue.value = POARunnable.ObjNotExistException;
            return;
        }

        org.omg.PortableServer.Servant myServant = ((POAHashMap) this.retainStr.activeMap
                .mapEntry(index)).getServant();
        InvokeHandler invokeHandler = (InvokeHandler) myServant;

        POAImpl pimpl = (POAImpl) ((ScopedMemory) poa.poaMemoryArea)
                .getPortal();
        // Logger.debug("logged debug 0");
        ZenProperties.logger.log(pimpl + "");
        ZenProperties.logger.log(pimpl.poaCurrent + "");
        ZenProperties.logger.log(pimpl.poaCurrent.get() + "");

        try {
            if (pimpl.poaCurrent.get() == null) pimpl.poaCurrent
                    .set(poa.poaMemoryArea.newInstance(POACurrent.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((POACurrent) pimpl.poaCurrent.get()).init(poa, okey,
                (org.omg.PortableServer.Servant) invokeHandler);

        //ResponseHandler responseHandler = ResponseHandler.instance();
        //responseHandler.init(((edu.uci.ece.zen.poa.POA) poa).getORB(),request
        // );

        CDROutputStream reply = null;
        synchronized (mutex) {
            requests.increment();
            map.incrementActiveRequests();
        }
        this.threadPolicyStrategy.enter(invokeHandler);

        ScopedMemory sm = poa.getORB().getScopedRegion();
        MSGRunnable msgr = getMSGR();
        msgr.init(request, myServant, reply, poa.getORB());
        sm.enter(msgr);
        retMSGR(msgr);
        poa.getORB().freeScopedRegion(sm);

        /*
         * if (request.getOperation().equals("_is_a") ) { boolean _result =
         * myServant._is_a(request.getCDRInputStream().read_string());
         * org.omg.CORBA.portable.OutputStream _output =
         * responseHandler.createReply(); _output.write_boolean(_result); reply =
         * (CDROutputStream) _output; } else if
         * (request.getOperation().equals("_non_existent") ) { boolean _result =
         * myServant._non_existent(); org.omg.CORBA.portable.OutputStream
         * _output = responseHandler.createReply();
         * _output.write_boolean(_result); reply = (CDROutputStream) _output; }
         * else { reply = (CDROutputStream)
         * invokeHandler._invoke(request.getOperation().toString(),
         * (org.omg.CORBA.portable.InputStream)request.getCDRInputStream(),
         * responseHandler); }
         */

        // --- POST-INVOKE ---
        this.threadPolicyStrategy.exit(invokeHandler);
        synchronized (mutex) {
            requests
                    .decrementAndNotifyAll(poa.processingState == POA.DESTRUCTION_APPARANT);
            map.decrementActiveRequestsAndDeactivate();
        }
        /*
         * try { reply.updateLength(); WriteBuffer wbret = reply.getBuffer();
         * if(ZenProperties.devDbg) System.out.println( "MsgLen: " +
         * (wbret.getPosition()-12) );
         * //((edu.uci.ece.zen.orb.transport.Transport)request.getTransport().getPortal()).send(
         * wbret ); ExecuteInRunnable eir = poa.getORB().getEIR();
         * msgr.init(wbret); eir.init(msgr, request.getTransport());
         * poa.getORB().orbImplRegion.executeInArea(eir); reply.free();
         * poa.getORB().freeEIR(eir); }catch (Exception e) {
         * e.printStackTrace(); }
         */
        //return edu.uci.ece.zen.orb.ServerRequestHandler.REQUEST_HANDLED;
    }

    private Queue msgrQueue;

    private MSGRunnable getMSGR() {
        if (msgrQueue == null) msgrQueue = new Queue();

        Object msgr = msgrQueue.dequeue();
        if (msgr == null) return new MSGRunnable();
        else return (MSGRunnable) msgr;
    }

    private void retMSGR(MSGRunnable msgr) {
        msgrQueue.enqueue(msgr);
    }
}