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
import org.omg.CORBA.Policy;

/**
 * Strategy for implementing the Threading-Policy of the POA
 * 
 * @author Arvind S. Krishna.
 * @author Juan Colmenares
 * @author Hojjat Jafarpour
 * @version 1.0
 */
abstract public class ThreadPolicyStrategy
{
    public static final ThreadPolicyStrategy ORB_CONTROL_MODEL_STRATEGY =
        new OrbControlModelStrategy();
    
    public static final ThreadPolicyStrategy SINGLE_THREAD_MODEL_STRATEGY =
        new SingleThreadModelStrategy();
    
    public static ThreadPolicyStrategy init(Policy[] policy, IntHolder ih)
    {
        if (PolicyUtils.useSingleThreadedPolicy(policy))
        {
            return SINGLE_THREAD_MODEL_STRATEGY;
        } 
        else
        {
            return ORB_CONTROL_MODEL_STRATEGY;            
        }
    }

    /**      */
    public static class SingleThreadModelStrategy extends ThreadPolicyStrategy
    {
        private SingleThreadModelStrategy() {}
    }
    
    /**      */
    public static class OrbControlModelStrategy extends ThreadPolicyStrategy
    {
        private OrbControlModelStrategy() {}
    }
    
    
}

