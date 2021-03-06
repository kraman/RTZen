/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.poa.mechanism;

import org.omg.CORBA.IntHolder;

import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.poa.POAImpl;
import edu.uci.ece.zen.poa.POARunnable;
import edu.uci.ece.zen.utils.ZenProperties;

/**
 * The class <code>RequestProcessingStrategy</code> takes care of creating
 * instances of the various strategies based on the policies of the POA
 * 
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna </a>
 * @version 1.0
 */

public abstract class RequestProcessingStrategy {

    // --- Type constants ---
    public static int DEFAULT_SERVANT = 0;

    public static int SERVANT_MANAGER = 1;

    public static int ACTIVE_OBJECT_MAP = 2;

    public static int SERVANT_LOCATOR = 3;

    public static int SERVANT_ACTIVATOR = 4;

    /**
     * <code> init <code> creates the appropriate request processing strategy
     * based on the policies specified in the POA.
     * @param policy specifies the policy of the POA for which the strategy is constructed.
     * @param uniquenessStrategy is used by the RequestProcessingStrategy for validation.
     * @param threadStrategy specifies if the POA is Suinglethreaded or not.
     * @param retentionStrategy is used by the RequestProcessingStrategy for validation.
     * @exception org.omg.PortableServer.POAPackage.InvalidPolicy
     * If the policies used to create this POA are in conflict.
     */

    public static RequestProcessingStrategy init(org.omg.CORBA.Policy[] policy,
            ServantRetentionStrategy retentionStrategy,
            IdUniquenessStrategy uniquenessStrategy,
            ThreadPolicyStrategy threadStrategy, POAImpl pimpl,
            IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;

        if (PolicyUtils.useServantManagerPolicy(policy)) 
        {
            ZenProperties.logger.log("ReqProcStrat init 1");
            retentionStrategy.validate(ServantRetentionStrategy.RETAIN, exceptionValue);
            if (exceptionValue.value != POARunnable.NoException)
            {   
                return null;
            }
            
            ZenProperties.logger.log("ReqProcStrat init 2");            
            ServantActivatorStrategy activator = new ServantActivatorStrategy();
            activator.init(retentionStrategy, threadStrategy, uniquenessStrategy, pimpl, exceptionValue);

            if (exceptionValue.value == POARunnable.NoException)
            {  
                ZenProperties.logger.log("ReqProcStrat init 3");
                return activator;
            }
            else 
            {
                exceptionValue.value = POARunnable.NoException;
                retentionStrategy.validate(ServantRetentionStrategy.NON_RETAIN,
                        exceptionValue);
                //KLUDGE: ServantLocatorStrategy locator = new ServantLocatorStrategy();
                if (exceptionValue.value == POARunnable.NoException)
                {
                    return null;
                }
                //return locator;
                else 
                {
                    exceptionValue.value = POARunnable.InvalidPolicyException;
                    return null;
                }
            }
        }


        if (PolicyUtils.useDefaultServantPolicy(policy)) 
        {
            ZenProperties.logger.log("ReqProcStrat init DefServPol 1");
// TODO Delete this 
//            if (uniquenessStrategy.validate(IdUniquenessStrategy.UNIQUE_ID))
//            {   
//                exceptionValue.value = POARunnable.InvalidPolicyException;
//                return null;
//            }

            DefaultServantStrategy strat = new DefaultServantStrategy();
            strat.initialize(retentionStrategy, threadStrategy, exceptionValue);
            if (exceptionValue.value == POARunnable.NoException)
            {   ZenProperties.logger.log("ReqProcStrat init DefServPol 3");
                return strat;
            }
            else
            {   ZenProperties.logger.log("ReqProcStrat init DefServPol 4");
                return null;
            }
        }
        
        ActiveObjectMapOnlyStrategy aom = new ActiveObjectMapOnlyStrategy();
        aom.initialize(retentionStrategy, threadStrategy, exceptionValue);
        return aom;
    }

    /**
     * <code> setInvokeHandler </code> initialized the appropriate handler that
     * would service the request for the POA. E.g An activeObject Map,
     * Default-Servant or a Servant Manager.
     * 
     * @param invokeHandler
     *            is used to initialize the handler.
     * @exception org.omg.PortableServer.POAPackage.WrongPolicy
     *                is thrown if a wrong handler is passed as an argument. E.g
     *                if a Servant is passed when an Active Object Map is
     *                expected.
     */

    public abstract void setInvokeHandler(java.lang.Object invokeHandler, IntHolder ExceptionValue);

    /**
     * <code> getRequestProcessor </code> returns the handler that would be used
     * by the POA to service the request and make the upcall on the servant.
     * 
     * @param type
     *            specifies the identity of the request processor e.g AOM,
     *            Default Servant, Servant Locator
     * @exception org.omg.POAPackage.ObjectNotActive
     *                is thrown if the specified handler is not set in the
     *                strategy.
     * @exception org.omg.POAPackage.WrongPolicy
     *                is thrown if there is a mismatch in the type of handler
     *                sepcified in the argument and the strategy. This method is
     *                also used for the purpose of validation in the methods of
     *                the POA for which the appropriate exceptions are thrown.
     */

    public abstract java.lang.Object getRequestProcessor(int type,
            IntHolder exceptionValue);

    /**
     * <code> validate </code> is used for the purpose of validation. Checks if
     * the strategy is of the same type as specified in the Argument.
     * 
     * @param type
     *            specifies the type of the strategy for which the check is
     *            performed.
     * @exception org.omg.PortableServer.POAPackage.WrongPolicy
     *                is thrown in case the validation fails.
     */

    public abstract boolean validate(int type, IntHolder exceptionValue);

    /**
     * <code> handleRequest </code> performs the upcall on the servant, based on
     * the type of strategy that is that is used in the POA.
     * 
     * @param request
     *            is the ServerRequest from which the operation name and the
     *            parameters are demarshalled.
     * @param poa
     *            specifies the POA that is invoking the upcall on the servant
     *            via its Request Processing Strategy.
     * @param requests
     *            gives the current number of requests currently in the POA.
     * @return signifies the successful completion of request/internal errors,
     *         Forward Request Exceptions
     */
    public abstract void handleRequest(RequestMessage request,
            edu.uci.ece.zen.poa.POA poa,
            edu.uci.ece.zen.poa.SynchronizedInt requests,
            IntHolder exceptionValue);
}

