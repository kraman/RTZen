package edu.uci.ece.zen.orb.policies;

import org.omg.RTCORBA.*;

/**
 * This class implements the Threadpool Policy
 * @author Mark Panahi
 * @version 1.0
 */

public class ThreadpoolPolicyImpl
        extends org.omg.CORBA.LocalObject
        implements ThreadpoolPolicy

{
    private int threadpool;

    public ThreadpoolPolicyImpl(int threadpool){
        this.threadpool = threadpool;

    }
    /**
     * Read accessor for policy_type attribute
     * @return the attribute value
     */
    public int policy_type(){
        return THREADPOOL_POLICY_TYPE.value;
    }

    /**
     * Operation copy
     */
    public org.omg.CORBA.Policy copy(){
        return new ThreadpoolPolicyImpl(threadpool);
    }

    /**
     * Operation destroy
     */
    public void destroy(){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Read accessor for threadpool attribute
     * @return the attribute value
     */
    public int threadpool(){
        return threadpool;
    }
}