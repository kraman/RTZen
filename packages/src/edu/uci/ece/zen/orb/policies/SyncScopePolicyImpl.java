/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.policies;

import org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE;
import org.omg.Messaging.SyncScopePolicy;

/**
 * This class implements the SyncScope Policy
 * 
 * @author Mark Panahi
 * @version 1.0
 */

public class SyncScopePolicyImpl extends org.omg.CORBA.LocalObject implements
        SyncScopePolicy

{
    private short sync;

    public SyncScopePolicyImpl(short sync) {
        this.sync = sync;

    }

    /**
     * Read accessor for policy_type attribute
     * 
     * @return the attribute value
     */
    public int policy_type() {
        return SYNC_SCOPE_POLICY_TYPE.value;
    }

    /**
     * Operation copy
     */
    public org.omg.CORBA.Policy copy() {
        return new SyncScopePolicyImpl(sync);
    }

    /**
     * Operation destroy
     */
    public void destroy() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Read accessor for synchronization attribute
     * 
     * @return the attribute value
     */
    public short synchronization() {
        return sync;
    }
}