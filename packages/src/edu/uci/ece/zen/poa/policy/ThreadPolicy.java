/* --------------------------------------------------------------------------*
 * $Id: ThreadPolicy.java,v 1.1 2003/11/26 22:24:47 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa.policy;


/**
 * The class <code>ThreadPolicy</code> is
 * ZEN specific implementation of CORBA ThreadPolicy
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */


import org.omg.PortableServer.ThreadPolicyValue;


public class ThreadPolicy extends org.omg.CORBA.LocalObject
    implements org.omg.PortableServer.ThreadPolicy {

    /**
     *
     * Creates the Id Assignment Policy with the value passed in.
     */
    public ThreadPolicy(ThreadPolicyValue the_value) {
        value = the_value;
    }


    /**
     * The default Id Assignment Policy is ORB_CTRL_MODEL
     */
    public ThreadPolicy() {
        value = ThreadPolicyValue.ORB_CTRL_MODEL;
    }


    /**
     * Returns the policy type value
     */

    public int policy_type() {
        return org.omg.PortableServer.THREAD_POLICY_ID.value;
    }


    /**
     * Returns the policy type value
     */
    public org.omg.PortableServer.ThreadPolicyValue value() {
        return value;
    }

    /**
     * Create a copy of this policy
     * @return org.omg.CORBA.Policy
     */
     public org.omg.CORBA.Policy copy() {
        return new ThreadPolicy(value());
    }


    /**
     * Destroy this policy object
     */
    public void destroy() {// No operation
    }


    /**
     * Returns the policy type value
     */
    public int int_value() {
        return this.value.value();
    }

    private org.omg.PortableServer.ThreadPolicyValue value;
}
