/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.policies;

import org.omg.Messaging.REBIND_POLICY_TYPE;
import org.omg.Messaging.RebindPolicy;

/**
 * This class implements the Rebind Policy
 * 
 * @author Mark Panahi
 * @version 1.0
 */

public class RebindPolicyImpl extends org.omg.CORBA.LocalObject implements
        RebindPolicy

{
    private short rebindMode;

    public RebindPolicyImpl(short rebindMode) {
        this.rebindMode = rebindMode;

    }

    /**
     * Read accessor for policy_type attribute
     * 
     * @return the attribute value
     */
    public int policy_type() {
        return REBIND_POLICY_TYPE.value;
    }

    /**
     * Operation copy
     */
    public org.omg.CORBA.Policy copy() {
        return new RebindPolicyImpl(rebindMode);
    }

    /**
     * Operation destroy
     */
    public void destroy() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Read accessor for rebind_mode attribute
     * 
     * @return the attribute value
     */
    public short rebind_mode() {
        return rebindMode;
    }
}