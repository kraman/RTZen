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

