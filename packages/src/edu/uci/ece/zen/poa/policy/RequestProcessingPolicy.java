/* --------------------------------------------------------------------------*
 * $Id: RequestProcessingPolicy.java,v 1.1 2003/11/26 22:24:46 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa.policy;
import org.omg.PortableServer.RequestProcessingPolicyValue;

/**
 * The class <code>RequestProcessingPolicy</code> is the
 * ZEN specific implementation of RequestProcessingPolicy.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */




public class RequestProcessingPolicy extends org.omg.CORBA.LocalObject
    implements org.omg.PortableServer.RequestProcessingPolicy {

   /**
     *
     * Creates the Id Assignment Policy with the value passed in.
     */

    public RequestProcessingPolicy(RequestProcessingPolicyValue _value) {
        value = _value;

    }


    /**
     * The default Id Assignment Policy is USE_ACTIVE_OBJECT_MAP_ONLY
     */

    public RequestProcessingPolicy() {
        this.value = org.omg.PortableServer.RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY;
    }


    /**
     * Create a copy of this policy
     * @return org.omg.CORBA.Policy
     */

    public org.omg.CORBA.Policy copy() {
        return new RequestProcessingPolicy(value());
    }

    /**
     * Destroy this policy object
     */


    public void destroy() {}

    /**
     * Returns the policy type value
     */
   public int policy_type() {
        return org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID.value;
    }


    /**
     * Returns the policy type value
     */
  public org.omg.PortableServer.RequestProcessingPolicyValue value() {
        return value;
    }


    /**
     * Returns the policy type value
     */

    public int int_value() {
        return this.value.value();
    }

    private org.omg.PortableServer.RequestProcessingPolicyValue  value;
}	

