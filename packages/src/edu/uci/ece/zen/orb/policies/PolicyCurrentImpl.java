/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.policies;

import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyCurrent;
import org.omg.CORBA.SetOverrideType;

import edu.uci.ece.zen.orb.ORB;

/**
 * This class implements the PolicyCurrent
 * 
 * @author Mark Panahi
 * @version 1.0
 */

public class PolicyCurrentImpl extends org.omg.CORBA.LocalObject implements
        PolicyCurrent {

    ORB orb;

    public void init(ORB orb) {
        //orbMemoryArea = RealtimeThread.getCurrentMemoryArea();
        this.orb = orb;
    }

    /**
     * Operation get_policy_overrides
     */
    public Policy[] get_policy_overrides(int[] ts) {

        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation set_policy_overrides
     */
    public void set_policy_overrides(Policy[] policies, SetOverrideType set_add)
            throws org.omg.CORBA.InvalidPolicies {
        throw new org.omg.CORBA.NO_IMPLEMENT();

    }
}