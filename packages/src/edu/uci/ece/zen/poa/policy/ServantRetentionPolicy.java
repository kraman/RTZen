/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

/*******************************************************************************
 * --------------------------------------------------------------------------
 * $Id: ServantRetentionPolicy.java,v 1.1 2003/11/26 22:24:46 nshankar Exp $
 * --------------------------------------------------------------------------
 */
package edu.uci.ece.zen.poa.policy;

import org.omg.PortableServer.ServantRetentionPolicyValue;

import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;

/**
 * The class <code>ServantRetentionPolicy</code> is the ZEN specific
 * implementation of ServantRetentionPolicy.
 * 
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna </a>
 * @version 1.0
 */
public class ServantRetentionPolicy extends org.omg.CORBA.LocalObject implements
        org.omg.PortableServer.ServantRetentionPolicy {

    public static final ServantRetentionPolicy RetainServant = new ServantRetentionPolicy();

    public static final ServantRetentionPolicy NonRetainServant = new ServantRetentionPolicy(
            ServantRetentionPolicyValue.NON_RETAIN);

    /**
     * The default Id Assignment Policy is RETAIN
     */

    public ServantRetentionPolicy() {
        this.value = org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN;
    }

    /**
     * Creates the Id Assignment Policy with the value passed in.
     */

    public ServantRetentionPolicy(
            org.omg.PortableServer.ServantRetentionPolicyValue _value) {
        this.value = _value;
        if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("Created RT-policy with value " + _value.value());
    }

    /**
     * Create a copy of this policy
     * 
     * @return org.omg.CORBA.Policy
     */

    public org.omg.CORBA.Policy copy() {
        switch (int_value()) {
            case ServantRetentionPolicyValue._RETAIN:
                return ServantRetentionPolicy.RetainServant;
            case ServantRetentionPolicyValue._NON_RETAIN:
                return ServantRetentionPolicy.NonRetainServant;
        }
        return null;
    }

    /**
     * Destroy this policy object
     */

    public void destroy() {
    }

    /**
     * Returns the policy type value
     */
    public int policy_type() {
        return org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID.value;
    }

    /**
     * Returns the policy type value
     */
    public org.omg.PortableServer.ServantRetentionPolicyValue value() {
        return value;
    }

    /**
     * Returns the policy type value
     */
    public int int_value() {
        return this.value.value();
    }

    private org.omg.PortableServer.ServantRetentionPolicyValue value;
}
