/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.poa.mechanism;

import javax.realtime.ScopedMemory;

import org.omg.CORBA.CompletionStatus;
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
import edu.uci.ece.zen.utils.SystemExceptionFactory;
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
            //OBJ_ADAPTER Exception
            throw  SystemExceptionFactory.getInstance().getSystemException(SystemExceptionFactory.OBJ_ADAPTER , 3 , CompletionStatus.COMPLETED_NO);
            
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
