/*******************************************************************************
 * --------------------------------------------------------------------------
 * $Id: ImplicitActivationPolicy.java,v 1.1 2003/11/26 22:24:45 nshankar Exp $
 * --------------------------------------------------------------------------
 */
package edu.uci.ece.zen.poa.policy;

import org.omg.PortableServer.ImplicitActivationPolicyValue;

/**
 * The class <code>ImplicitActivationPolicy</code> is the ZEN specific
 * implementation of ImplicitActivationPolicy.
 * 
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna </a>
 * @version 1.0
 */

public class ImplicitActivationPolicy extends org.omg.CORBA.LocalObject
        implements org.omg.PortableServer.ImplicitActivationPolicy {

    public static final ImplicitActivationPolicy ImplicitActivation = new ImplicitActivationPolicy();

    public static final ImplicitActivationPolicy ExplicitActivation = new ImplicitActivationPolicy(
            ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION);

    /**
     * The default Id Assignment IMPLICIT_ACTIVATION
     */
    public ImplicitActivationPolicy() {
        value = ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION;
    }

    /**
     * Creates the Id Assignment Policy with the value passed in.
     */
    public ImplicitActivationPolicy(ImplicitActivationPolicyValue value) {
        this.value = value;
    }

    /**
     * Create a copy of this policy
     * 
     * @return org.omg.CORBA.Policy
     */
    public org.omg.CORBA.Policy copy() {
        if (value().equals(ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION)) return ImplicitActivationPolicy.ImplicitActivation;
        else return ImplicitActivationPolicy.ExplicitActivation;
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
        return org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID.value;
    }

    /**
     * Returns the policy type value
     */
    public ImplicitActivationPolicyValue value() {
        return this.value;
    }

    /**
     * Returns the policy type value
     */
    public int int_value() {
        return this.value.value();
    }

    private ImplicitActivationPolicyValue value;
}

