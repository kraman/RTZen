/* --------------------------------------------------------------------------*
 * $Id: IdUniquenessPolicy.java,v 1.1 2003/11/26 22:24:45 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa.policy;


/**
 * The class <code>IdUniquenessPolicy</code> is the
 * ZEN specific implementation of IdUniquenessPolicy.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */


public class IdUniquenessPolicy extends org.omg.CORBA.LocalObject implements org.omg.PortableServer.IdUniquenessPolicy {

    public static final IdUniquenessPolicy UniqueIdUniquenessPolicy = new IdUniquenessPolicy();
    public static final IdUniquenessPolicy MultipleIdUniquenessPolicy = new IdUniquenessPolicy( IdUniquenessPolicyValue.MULTIPLE_ID );

    public IdUniquenessPolicy() {
        this.value = org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID;
    }

    /**
      * Creates the Id Uniqueness Policy with the value passed in.
     */

    public IdUniquenessPolicy(
            org.omg.PortableServer.IdUniquenessPolicyValue _value) {
        this.value = _value;
    }


    /**
     * Create a copy of this policy
     * @return org.omg.CORBA.Policy
     */

    public org.omg.CORBA.Policy copy() {
        if( value().equals( IdUniquenessPolicyValue.UNIQUE_ID )
            return IdUniquenessPolicy.UniqueIdUniquenessPolicy;
        else
            return IdUniquenessPolicy.MultipleIdUniquenessPolicy;
    }

    /**
     * Destroy this policy object
     */
    public void destroy() {}

    /**
     * Returns the policy type value
     */
    public int policy_type() {
        return org.omg.PortableServer.ID_UNIQUENESS_POLICY_ID.value;
    }

    /**
     * Returns the policy type value
     */
    public org.omg.PortableServer.IdUniquenessPolicyValue value() {
        return value;
    }


     /**
     * Returns the policy type value
     */
    public int int_value() {
        return this.value.value();
    }

    private org.omg.PortableServer.IdUniquenessPolicyValue value;
}

