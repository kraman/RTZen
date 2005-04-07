/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.transport;

import javax.realtime.MemoryArea;
import javax.realtime.NoHeapRealtimeThread;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.ORBComponent;
import edu.uci.ece.zen.poa.POA;

import org.omg.IOP.TaggedProfile;

import edu.uci.ece.zen.utils.ExecuteInRunnable;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;
import javax.realtime.PriorityParameters;

import org.omg.IIOP.ProfileBody_1_0;
import org.omg.IIOP.ProfileBody_1_0Helper;
import org.omg.IIOP.ProfileBody_1_1;
import org.omg.IIOP.ProfileBody_1_1Helper;
import org.omg.IIOP.Version;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedComponent;
import org.omg.Messaging.PolicyValue;
import org.omg.Messaging.PolicyValueHelper;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.PriorityMappingImpl;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;

import org.omg.RTCORBA.PRIORITY_MODEL_POLICY_TYPE;

public abstract class Acceptor implements ORBComponent {
    protected edu.uci.ece.zen.orb.ORB orb;

    protected edu.uci.ece.zen.orb.ORBImpl orbImpl;

    protected boolean isActive;

    protected AcceptorLogic acceptorLogic;

    protected RealtimeThread acceptorLogicThread;

    protected short priority;

    public  int threadPoolId;

    public Acceptor(edu.uci.ece.zen.orb.ORB orb,
            edu.uci.ece.zen.orb.ORBImpl orbImpl,
            int threadPoolId ) {
        this.orb = orb;
        this.orbImpl = orbImpl;
        this.threadPoolId = threadPoolId;
    }

    public final void startAccepting( short priority ) {
        this.priority = priority;
        acceptorLogic = new AcceptorLogic(this);
        isActive = true;
        acceptorLogicThread = new NoHeapRealtimeThread( new PriorityParameters(PriorityMappingImpl.toNative(priority)) , null, null,
                RealtimeThread.getCurrentMemoryArea(), null, acceptorLogic);
        acceptorLogicThread.start();
    }

    public final void shutdown( boolean waitForCompletion ) {
        try{
            isActive = false;
            internalShutdown();
            acceptorLogicThread.interrupt();
            synchronized (acceptorLogic) {
                try {
                    acceptorLogic.wait();
                } catch (Exception e) {
                    ZenProperties.logger.log(Logger.WARN, getClass(), "shutdown", e);
                }
            }
        }catch(java.lang.IllegalThreadStateException itse ){
            ZenProperties.logger.log(Logger.INFO, getClass(), "shutdown", itse);
        }
    }

    protected final void registerTransport(Transport t) {
        ((ScopedMemory) RealtimeThread.getCurrentMemoryArea()).setPortal(t);
        RealtimeThread transportThread = new NoHeapRealtimeThread( new PriorityParameters(PriorityMappingImpl.toNative(this.priority)) , null, null, RealtimeThread.getCurrentMemoryArea(), null, t);
        transportThread.start();
    }

    protected abstract void accept();

    protected abstract void internalShutdown();

    private ProfileRunnable prunnable;

    public synchronized TaggedProfile [] getProfiles(byte iiopMajorVersion,
            byte iiopMinorVersion, byte[] objKey, MemoryArea clientRegion, POA poa) {
        //if( this.threadPoolId == threadPoolId )
        {
            try {
                if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("Acceptor client region: " + clientRegion);
                edu.uci.ece.zen.utils.Logger.printThreadStack();
                if (prunnable == null) prunnable = new ProfileRunnable();
                prunnable.init(iiopMajorVersion, iiopMinorVersion, objKey, this, poa);
                clientRegion.executeInArea(prunnable);
                return prunnable.getRetVal();
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, getClass(), "getProfile", e);
            }
        }
        return null;
    }

    protected abstract TaggedProfile [] getInternalProfiles(byte iiopMajorVersion,
            byte iiopMinorVersion, byte[] objKey, POA poa);

    public void finalize() {
        ZenProperties.logger.log("Acceptor region has been GC'd");
    }

    protected TaggedComponent[] getComponents(POA poa) {
        ZenProperties.logger.log("getComponents()");
        TaggedComponent[] tcarr;

        TaggedComponent polComp = getPolicyComponent(poa);

        if(polComp != null){
            tcarr = new TaggedComponent[1];
            tcarr[0] = polComp;
        }else{
            tcarr = new TaggedComponent[0];
        }

        return tcarr;
    }

    private TaggedComponent getPolicyComponent(POA poa) {
        ZenProperties.logger.log("getPolicyComponent()");
        CDROutputStream out = poa.getClientExposedPolicies(priority);

        if(out == null)
            return null;

        TaggedComponent tc = new TaggedComponent();
        tc.tag = org.omg.IOP.TAG_POLICIES.value;
        tc.component_data = new byte[(int) out.getBuffer().getLimit()];
        out.getBuffer().getReadBuffer().readByteArray(tc.component_data, 0,
                (int) out.getBuffer().getLimit());
        out.free();

        return tc;
    }

    public static PolicyValue marshalPriorityModelValue(
            org.omg.RTCORBA.PriorityModelPolicy pol, ORB orb, CDROutputStream outRet,
            short priority) {
        ZenProperties.logger.log("createPriorityModelValue()");
        PolicyValue pv = new PolicyValue();
        pv.ptype = pol.priority_model().value();

        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);
        out.write_boolean(false); //BIGENDIAN

        out.write_long(pol.priority_model().value());
        //out.write_short(pol.server_priority());
        //override to the acceptor's priority
        out.write_short((short)priority);
        if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("createPriorityModelValue() -- Priority: " + priority);

        pv.pvalue = new byte[(int)out.getBuffer().getLimit()];
        pv.ptype = PRIORITY_MODEL_POLICY_TYPE.value;

        out.getBuffer().getReadBuffer().readByteArray(pv.pvalue, 0 ,
                (int)out.getBuffer().getLimit());

        out.free();

        PolicyValueHelper.write(outRet, pv);

        return pv;
    }

    private PolicyValue createServerProtocolPolicyValue() {
        return null;
    }
}

class AcceptorLogic implements Runnable {
    Acceptor acc;

    public AcceptorLogic(Acceptor acc) {
        this.acc = acc;
    }

    public void run() {
        AcceptRunnable runnable = new AcceptRunnable(acc);
        ExecuteInRunnable eir = new ExecuteInRunnable();
        ScopedMemory transportMem = null;
        while (acc.isActive) {
            try {
                transportMem = ORB.getScopedRegion();
                eir.init(runnable, transportMem);
                acc.orb.orbImplRegion.executeInArea(eir);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, getClass(), "run", e);
                if( transportMem != null )
                    ORB.freeScopedRegion( transportMem );
            } 
        }
        //notify the exit of this thread
        synchronized (this) {
            this.notifyAll();
        }
    }

}

class ProfileRunnable implements Runnable {
    private byte major;

    private byte minor;

    private byte[] objKey;

    private Acceptor acc;

    private TaggedProfile [] retVal;

    private POA poa;

    public ProfileRunnable() {
    }

    public void init(byte major, byte minor, byte[] objKey, Acceptor acc, POA poa) {
        this.major = major;
        this.minor = minor;
        this.acc = acc;
        this.objKey = objKey;
        this.poa = poa;
    }

    public TaggedProfile [] getRetVal() {
        return retVal;
    }

    public void run() {
        retVal = acc.getInternalProfiles(major, minor, objKey, poa);
    }
}

class AcceptRunnable implements Runnable {
    private Acceptor acc;

    public AcceptRunnable(Acceptor acc) {
        this.acc = acc;
    }

    public void run() {
        acc.accept();
    }
}
