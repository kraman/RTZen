package edu.uci.ece.zen.orb;

import java.util.Properties;

//import javax.realtime.MemoryArea;
//import javax.realtime.NoHeapRealtimeThread;
//import javax.realtime.RealtimeThread;
//import javax.realtime.ScopedMemory;
import javax.realtime.*;

import org.omg.CORBA.PolicyCurrent;

import edu.uci.ece.zen.orb.policies.PolicyCurrentImpl;
import edu.uci.ece.zen.orb.policies.PolicyManagerImpl;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.Queue;
import edu.uci.ece.zen.utils.ZenProperties;
//import edu.uci.ece.zen.utils.ThreadLocal;

public class ORBImpl {
    ZenProperties properties;

    edu.uci.ece.zen.orb.ORB orbFacade;

    ORBImplRunnable orbImplRunnable;

    public ServerRequestHandler serverRequestHandler;

    public ThreadLocal rtCurrent;

    public ThreadLocal policyCurrent;

    public PolicyManagerImpl policyManager;

    //public RTORBImpl rtorb;

    public Queue eirCache;

    public Queue crCache; //ConnectorRunnable cache

    public ORBImpl(String args[], Properties props,
            edu.uci.ece.zen.orb.ORB orbFacade) {
        ZenProperties.logger.log("======================In orb impl region===================================");
        properties = new ZenProperties();
        properties.addPropertiesFromArgs(args);
        properties.addProperties(props);
        this.orbFacade = orbFacade;
        ZenProperties.logger.log("======================Setting portal variable==============================");
        orbFacade.orbImplRegion.setPortal(this);
        orbImplRunnable = new ORBImplRunnable(orbFacade.orbImplRegion);
        ZenProperties.logger.log("======================Creating nhrt sleeper thread=========================");
        if (ZenProperties.dbg) ZenProperties.logger.log(orbFacade.orbImplRegion.toString());
        if (ZenProperties.dbg) ZenProperties.logger.log(MemoryArea
                .getMemoryArea(orbImplRunnable).toString());
        if (ZenProperties.dbg) ZenProperties.logger.log(NoHeapRealtimeThread
                .getCurrentMemoryArea().toString());
        //NoHeapRealtimeThread nhrt = new NoHeapRealtimeThread( null,
        // null,null,(ScopedMemory)orbFacade.orbImplRegion,null,(java.lang.Runnable
        // )orbImplRunnable );
        if (ZenProperties.dbg) ZenProperties.logger.log(MemoryArea
                .getMemoryArea(new Integer(42)).toString());
        SchedulingParameters sp = null;
        ReleaseParameters rp = null;
        NoHeapRealtimeThread nhrt = new NoHeapRealtimeThread(sp, rp, (MemoryParameters)null,
                orbFacade.orbImplRegion, (ProcessingGroupParameters)null, orbImplRunnable);
        ZenProperties.logger.log("======================starting nhrt in orb impl region=====================");

        nhrt.start();
        try {
/*
            rtCurrent = (ThreadLocal) (orbFacade.parentMemoryArea.newInstance(ThreadLocal.class));
            //rtCurrent = new ThreadLocal();
            policyCurrent = (ThreadLocal) (orbFacade.parentMemoryArea.newInstance(ThreadLocal.class));
            policyManager = (PolicyManagerImpl) (orbFacade.parentMemoryArea.newInstance(PolicyManagerImpl.class));
            policyManager.init(orbFacade);
*/            
            /*
             * rtorb = (RTORBImpl)(orbFacade.parentMemoryArea.newInstance(
             * RTORBImpl.class )); rtorb.init(orbFacade);
             */
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

        ZenProperties.logger.log("======================Performing post initialization steps====================");
        boolean startSerialTransportAcceptor = this. properties.getProperty( "edu.uci.ece.zen.orb.transport.serial" , "" ).equals("1");
        //boolean startSerialTransportAcceptor = ZenProperties.getGlobalProperty("edu.uci.ece.zen.orb.transport.serial" , "" ).equals("1");
        /*
        if( startSerialTransportAcceptor ){
            ZenProperties.logger.log("**** STARTING SERIAL ACCEPTOR ****");
            edu.uci.ece.zen.orb.transport.serial.AcceptorRunnable r =
                new edu.uci.ece.zen.orb.transport.serial.AcceptorRunnable();
            r.init(this.orbFacade);
            orbFacade.setUpORBChildRegion( r );
        }*/
        
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

    protected void handleInvocation() {
    }

    protected void setProperties(String[] args, Properties props) {
    }

    public void registerTransport(ScopedMemory mem) {
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
