package edu.uci.ece.zen.orb;

import java.util.Properties;

import javax.realtime.*;
import org.omg.CORBA.PolicyCurrent;
import edu.uci.ece.zen.orb.policies.PolicyCurrentImpl;
import edu.uci.ece.zen.orb.policies.PolicyManagerImpl;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.Queue;
import edu.uci.ece.zen.utils.ZenProperties;
import org.omg.IOP.TaggedProfile;
import org.omg.Messaging.PolicyValue;
import org.omg.Messaging.PolicyValueSeqHelper;
import org.omg.RTCORBA.CLIENT_PROTOCOL_POLICY_TYPE;
import org.omg.RTCORBA.PRIORITY_BANDED_CONNECTION_POLICY_TYPE;
import org.omg.RTCORBA.PRIORITY_MODEL_POLICY_TYPE;
import org.omg.RTCORBA.PRIVATE_CONNECTION_POLICY_TYPE;
import org.omg.RTCORBA.PriorityModel;
import org.omg.RTCORBA.SERVER_PROTOCOL_POLICY_TYPE;
import org.omg.RTCORBA.THREADPOOL_POLICY_TYPE;
import java.util.Vector;

public class ORBImpl {
    ZenProperties properties;

    public edu.uci.ece.zen.orb.ORB orbFacade;

    ORBImplRunnable orbImplRunnable;

    public ServerRequestHandler serverRequestHandler;

    public ThreadLocal rtCurrent;

    public ThreadLocal policyCurrent;

    public PolicyManagerImpl policyManager;

    //public RTORBImpl rtorb;

    public Queue eirCache;

    public Queue crCache; //ConnectorRunnable cache

    public Vector transportTags;    //List of all the transports that should be loaded for this orb

    public ORBImpl(String args[], Properties props, edu.uci.ece.zen.orb.ORB orbFacade) {
        ZenProperties.logger.log("======================In orb impl region===================================");
        properties = new ZenProperties();
        properties.addPropertiesFromArgs(args);
        properties.addProperties(props);
        transportTags = new Vector(10); //KLUDGE: how many transports to preallocate for?
        this.orbFacade = orbFacade;
        ZenProperties.logger.log("======================Setting portal variable==============================");
        orbFacade.orbImplRegion.setPortal(this);
        orbImplRunnable = new ORBImplRunnable(orbFacade.orbImplRegion);
        ZenProperties.logger.log("======================Creating nhrt sleeper thread=========================");
        if (ZenProperties.dbg) ZenProperties.logger.log(orbFacade.orbImplRegion.toString());
        if (ZenProperties.dbg) ZenProperties.logger.log(MemoryArea .getMemoryArea(orbImplRunnable).toString());
        if (ZenProperties.dbg) ZenProperties.logger.log(NoHeapRealtimeThread .getCurrentMemoryArea().toString());
        if (ZenProperties.dbg) ZenProperties.logger.log(MemoryArea .getMemoryArea(new Integer(42)).toString());
        SchedulingParameters sp = null;
        ReleaseParameters rp = null;
        NoHeapRealtimeThread nhrt = new NoHeapRealtimeThread(sp, rp, (MemoryParameters)null,
                orbFacade.orbImplRegion, (ProcessingGroupParameters)null, orbImplRunnable);

        edu.uci.ece.zen.orb.transport.TransportFactory.registerTransport( org.omg.IOP.TAG_INTERNET_IOP.value , 
            new edu.uci.ece.zen.orb.transport.iiop.TransportFactory() , orbFacade.orbImplRegion );
        ZenProperties.logger.log("======================starting nhrt in orb impl region=====================");

        nhrt.start();
        try {
            rtCurrent = (ThreadLocal) (orbFacade.parentMemoryArea.newInstance(ThreadLocal.class));
            orbFacade.getRTORB().create_threadpool(0,//stacksize,
                    1,//static_threads,
                    0,//dynamic_threads,
                    (short) javax.realtime.PriorityScheduler.instance().getNormPriority(),//default_thread_priority,
                    false,//allow_request_buffering,
                    0,//max_buffered_requests,
                    0//max_request_buffer_size
                    );
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "<init>", e);
        }

        eirCache = new Queue();
        crCache = new Queue();
    }

    public PolicyCurrent getPolicyCurrent() {
        PolicyCurrentImpl ret = (PolicyCurrentImpl) (policyCurrent.get());

        if (ret == null) {
            try {
                ret = (PolicyCurrentImpl) (orbFacade.parentMemoryArea
                        .newInstance(PolicyCurrentImpl.class));//new
                // RTCurrentImpl(this);
                ret.init(orbFacade);
                policyCurrent.set(ret);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, getClass(), "getPolicyCurrent", e);
            }
        }

        return ret;
    }

    public RTCurrent getRTCurrent() {
        RTCurrent ret = (RTCurrent) (rtCurrent.get());

        if (ret == null) {
            try {
                ret = (RTCurrent) (orbFacade.parentMemoryArea
                        .newInstance(RTCurrent.class));//new
                // RTCurrentImpl(this);
                ret.init(orbFacade);
                rtCurrent.set(ret);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, getClass(), "getRTCurrent", e);
            }
        }

        return ret;
    }

    public void processPolicyTagComponent( CDRInputStream in ){
        int byteLen = in.read_ulong();
        in.read_boolean(); //endianess
        int numPol = in.read_ulong();
        if (ZenProperties.dbg) ZenProperties.logger.log("number of policies: " + numPol);

        for (int j = 0; j < numPol; ++j) {
            int polType = in.read_ulong();
            int byteLen1 = in.read_ulong();
            in.read_boolean(); //endianess
            if (ZenProperties.dbg) ZenProperties.logger.log("found policy value: " + polType);

            switch (polType) {
                case PRIORITY_MODEL_POLICY_TYPE.value:
                    ZenProperties.logger.log("\tPRIORITY_MODEL_POLICY_TYPE");
                    int priorityModel = in.read_long();
                    int serverPriority = in.read_short();
                    if (ZenProperties.dbg) ZenProperties.logger.log("\tpriority model: " + priorityModel);
                    if (ZenProperties.dbg) ZenProperties.logger.log("\tpriority: " + serverPriority);
                    break;

                case THREADPOOL_POLICY_TYPE.value:
                    for(int i1 = 0; i1 < byteLen-1; ++i1)
                        in.read_octet();
                    ZenProperties.logger.log("\tTHREADPOOL_POLICY_TYPE");
                    break;

                case SERVER_PROTOCOL_POLICY_TYPE.value:
                    for(int i1 = 0; i1 < byteLen-1; ++i1)
                        in.read_octet();
                    ZenProperties.logger.log("\tSERVER_PROTOCOL_POLICY_TYPE");
                    break;

                case CLIENT_PROTOCOL_POLICY_TYPE.value:
                    for(int i1 = 0; i1 < byteLen-1; ++i1)
                        in.read_octet();
                    ZenProperties.logger.log("\tCLIENT_PROTOCOL_POLICY_TYPE");
                    break;

                case PRIVATE_CONNECTION_POLICY_TYPE.value:
                    for(int i1 = 0; i1 < byteLen-1; ++i1)
                        in.read_octet();
                    ZenProperties.logger.log("\tPRIVATE_CONNECTION_POLICY_TYPE");
                    break;

                case PRIORITY_BANDED_CONNECTION_POLICY_TYPE.value:
                    for(int i1 = 0; i1 < byteLen-1; ++i1)
                        in.read_octet();
                    ZenProperties.logger.log("\tPRIORITY_BANDED_CONNECTION_POLICY_TYPE");
                    break;

                default:
                    for(int i1 = 0; i1 < byteLen-1; ++i1)
                        in.read_octet();
                    ZenProperties.logger.log("ERROR: Invalid policy type");
            }
        }
    }

    public synchronized void ensureAcceptorAtPriority( short priority ){

    }

    public void string_to_object(org.omg.IOP.IOR ior,
            org.omg.CORBA.portable.ObjectImpl objImpl) {
        //System.out.println("ORBImpl string_to_object 1");
        ObjRefDelegate delegate = ObjRefDelegate.instance();
        delegate.init(ior, (edu.uci.ece.zen.orb.ObjectImpl) objImpl, orbFacade,
                this);
        objImpl._set_delegate(delegate);
    }

    public void setServerRequestHandler(ServerRequestHandler handler) {
        serverRequestHandler = handler;
    }

    public ServerRequestHandler getServerRequestHandler() {
        return serverRequestHandler;
    }
}

class ORBImplRunnable implements Runnable {
    private boolean active;

    private ScopedMemory sm;

    public ORBImplRunnable(ScopedMemory sm) {
        active = true;
        this.sm = sm;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean val) {
        this.active = val;
    }

    public void run() {
        if (ZenProperties.dbg) ZenProperties.logger.log("getting portal for: "
                + sm);
        if (ZenProperties.dbg) ZenProperties.logger.log("inner thread: "
                + Thread.currentThread().toString());

        ORBImpl orbImpl = (ORBImpl) sm.getPortal();
        if (ZenProperties.dbg) ZenProperties.logger.log("orb impl is " + orbImpl);
        synchronized (orbImpl) {
            try {
                while (active) {
                    orbImpl.wait();
                }
            } catch (InterruptedException ie) {
                ZenProperties.logger.log(Logger.INFO,
                        getClass(), "run()",
                        "ORB is shutting down.");
            }
            active = false;
        }
        synchronized (orbImpl.orbFacade.orbRunningLock) {
            orbImpl.orbFacade.orbRunningLock.notifyAll();
        }
    }
}

class AcceptorCreatorRunnable implements Runnable {
    ORB orb;

    ORBImpl orbImpl;

    short acceptorPriority;

    ScopedMemory retVal;

    public AcceptorCreatorRunnable(ORB orb, ORBImpl orbImpl) {
        this.orb = orb;
        this.orbImpl = orbImpl;
    }

    public void init(short acceptorPriority) {
        this.acceptorPriority = acceptorPriority;
    }

    public void run() {

    }
}
