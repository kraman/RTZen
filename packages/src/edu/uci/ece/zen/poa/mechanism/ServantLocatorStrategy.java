/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

/* --------------------------------------------------------------------------*
 * $Id: ServantLocatorStrategy.java,v 1.7 2003/09/03 20:44:19 spart Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa.mechanism;

// --- OMG Specific Imports ---
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.portable.InvokeHandler;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.protocol.type.*;
import edu.uci.ece.zen.poa.*;
import edu.uci.ece.zen.utils.*;
import org.omg.CORBA.IntHolder;

public class ServantLocatorStrategy extends ServantManagerStrategy {
    
   /**
    * Initialize Servant Locator
    * @param nonRetain ServantRetentionStrategy
    * @param threadStrategy ThreadPolicyStrategy
    * @throws org.omg.PortableServer.POAPackage.InvalidPolicy
    */
    public void init(ServantRetentionStrategy nonRetain, ThreadPolicyStrategy threadStrategy , org.omg.CORBA.IntHolder ih ) throws
                org.omg.PortableServer.POAPackage.InvalidPolicy {
        this.threadPolicyStrategy = threadStrategy;
    }

   /**
    * set concrete servant locator
    * @param servantManager java.lang.Object
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public synchronized void setInvokeHandler(java.lang.Object servantManager , IntHolder exceptionValue ){
        if (this.manager != null) {
        	exceptionValue.value = POARunnable.BadInvOrderException;
            return;
        }

        if (servantManager instanceof org.omg.PortableServer.ServantLocator) {
            this.manager = (org.omg.PortableServer.ServantLocator) servantManager;
        }
        
        exceptionValue.value = POARunnable.WrongPolicyException;
    }

   /**
    * return servant locator associated with the strategy
    * @param name strategy type
    * @return Object servant locator
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
    */
    public synchronized Object getRequestProcessor(int name , IntHolder exceptionValue ){
        if (validate(name , exceptionValue) && this.manager != null) {
            return this.manager;
        }

        exceptionValue.value = POARunnable.ObjNotActiveException;
        return null;
    }

   /**
    * Handle client request.
    * @param request client request.
    * @param poa corresponding POA
    * @param requests active requests
    * @return int return state
    */
    public void handleRequest(RequestMessage request, edu.uci.ece.zen.poa.POA poa,
             edu.uci.ece.zen.poa.SynchronizedInt requests , IntHolder exceptionValue) {
        //TODO implement handleRequest and be careful with ThreadStrategy
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

   /**
    * check if strategy same as this strategy
    * @param name 
    * @return boolean
    */
    public boolean validate( int name , IntHolder exceptionHolder ){
        exceptionHolder.value = POARunnable.NoException;
        return (RequestProcessingStrategy.SERVANT_LOCATOR == name);
    }

    protected org.omg.PortableServer.ServantLocator manager;
    protected ThreadPolicyStrategy threadPolicyStrategy;
}
