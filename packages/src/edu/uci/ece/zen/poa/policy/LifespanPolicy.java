/* --------------------------------------------------------------------------*
 * $Id: LifespanPolicy.java,v 1.1 2003/11/26 22:24:45 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa.policy;
import org.omg.PortableServer.LifespanPolicyValue;

/**
 * The class <code>LifespanPolicy</code> is the
 * ZEN specific implementation of LifespanPolicy.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */




public class LifespanPolicy extends org.omg.CORBA.LocalObject implements org.omg.PortableServer.LifespanPolicy {

   public static final TransientLifespan = new LifespanPolicy();
   public static final PersistantLifespan = new LifespanPolicy( LifespanPolicyValue.PERSISTENT );

   /**
     * The default Id Assignment Policy is TRANSIENT
     */

      public LifespanPolicy() {
        this.value = LifespanPolicyValue.TRANSIENT;
    }

    /**
     *
     * Creates the Id Assignment Policy with the value passed in.
     */
  public LifespanPolicy(org.omg.PortableServer.LifespanPolicyValue _value) {
        value = _value;
    }


     /**
     * Create a copy of this policy
     * @return org.omg.CORBA.Policy
     */
    public org.omg.CORBA.Policy copy() {
        if( value().equals( LifespanPolicyValue.TRANSIENT ) )
            return TransientLifespan;
        else
            return PersistantLifespan;
    }


    /**
     * Destroy this policy object
     */
  public void destroy() {}

    /**
     * Returns the policy type value
     */
      public int policy_type() {
        return org.omg.PortableServer.LIFESPAN_POLICY_ID.value;
    }



    /**
     * Returns the policy type value
     */
     public org.omg.PortableServer.LifespanPolicyValue value() {
        return value;
    }



    /**
     * Returns the policy type value
     */
      public int int_value() {
        return this.value.value();
    }

    private org.omg.PortableServer.LifespanPolicyValue value;
}

