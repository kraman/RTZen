/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.policies;

import org.omg.RTCORBA.THREADPOOL_POLICY_TYPE;
import org.omg.RTCORBA.ThreadpoolPolicy;

/**
 * This class implements the Threadpool Policy
 * 
 * @author Mark Panahi
 * @version 1.0
 */

public class ThreadpoolPolicyImpl extends org.omg.CORBA.LocalObject implements
        ThreadpoolPolicy

{
    private int threadpool;

    public ThreadpoolPolicyImpl(int threadpool) {
        this.threadpool = threadpool;

    }

    /**
     * Read accessor for policy_type attribute
     * 
     * @return the attribute value
     */
    public int policy_type() {
        return THREADPOOL_POLICY_TYPE.value;
    }

    /**
     * Operation copy
     */
    public org.omg.CORBA.Policy copy() {
        return new ThreadpoolPolicyImpl(threadpool);
    }

    /**
     * Operation destroy
     */
    public void destroy() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Read accessor for threadpool attribute
     * 
     * @return the attribute value
     */
    public int threadpool() {
        return threadpool;
    }
}