/*******************************************************************************
 * --------------------------------------------------------------------------
 * $Id: IdAssignmentPolicy.java,v 1.1 2003/11/26 22:24:45 nshankar Exp $
 * --------------------------------------------------------------------------
 */

package edu.uci.ece.zen.poa.policy;

import org.omg.PortableServer.IdAssignmentPolicyValue;

/**
 * The class <code>IdAssignmentPolicy</code> is the ZEN specific
 * implementation of IdAssignmentPolicy.
 * 
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna </a>
 * @version 1.0
 */

public class IdAssignmentPolicy extends org.omg.CORBA.LocalObject implements
        org.omg.PortableServer.IdAssignmentPolicy {

    public static final IdAssignmentPolicy SystemIdAssignmentPolicy = new IdAssignmentPolicy();

    public static final IdAssignmentPolicy UserIdAssignmentPolicy = new IdAssignmentPolicy(
            IdAssignmentPolicyValue.USER_ID);

    /**
     * The default Id Assignment Policy is SYSTEM_ID
     */
    public IdAssignmentPolicy() {
        this.value = IdAssignmentPolicyValue.SYSTEM_ID;
    }

    /**
     * Creates the Id Assignment Policy with the value passed in.
     */

    public IdAssignmentPolicy(IdAssignmentPolicyValue value) {
        this.value = value;
    }

    /**
     * Create a copy of this policy
     * 
     * @return org.omg.CORBA.Policy
     */

    public org.omg.CORBA.Policy copy() {
        if (this.value.equals(IdAssignmentPolicyValue.USER_ID)) return IdAssignmentPolicy.UserIdAssignmentPolicy;
        else return IdAssignmentPolicy.SystemIdAssignmentPolicy;
    }

    /**
     * Destroy this policy object
     */

    public void destroy() {// No-Op
    }

    /**
     * Returns the policy type value
     */
    public int policy_type() {
        return org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID.value;
    }

    /**
     * Returns the policy type value
     */
    public org.omg.PortableServer.IdAssignmentPolicyValue value() {
        return value;
    }

    /**
     * Returns the policy type value
     */

    public int int_value() {
        return this.value.value();
    }

    private org.omg.PortableServer.IdAssignmentPolicyValue value;
}

