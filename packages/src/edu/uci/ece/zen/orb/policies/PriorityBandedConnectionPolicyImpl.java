package edu.uci.ece.zen.orb.policies;

import org.omg.RTCORBA.PRIORITY_BANDED_CONNECTION_POLICY_TYPE;
import org.omg.RTCORBA.PriorityBandedConnectionPolicy;

/**
 * This class implements the Priority Banded Connection Policy
 * 
 * @author Mark Panahi
 * @version 1.0
 */

public class PriorityBandedConnectionPolicyImpl extends
        org.omg.CORBA.LocalObject implements PriorityBandedConnectionPolicy {
    private org.omg.RTCORBA.PriorityBand[] priorityBand;

    public PriorityBandedConnectionPolicyImpl(
            org.omg.RTCORBA.PriorityBand[] priorityBand) {
        this.priorityBand = priorityBand;

    }

    /**
     * Read accessor for policy_type attribute
     * 
     * @return the attribute value
     */
    public int policy_type() {
        return PRIORITY_BANDED_CONNECTION_POLICY_TYPE.value;
    }

    /**
     * Operation copy
     */
    public org.omg.CORBA.Policy copy() {
        return new PriorityBandedConnectionPolicyImpl(priorityBand);
    }

    /**
     * Operation destroy
     */
    public void destroy() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Read accessor for priority_bands attribute
     * 
     * @return the attribute value
     */
    public org.omg.RTCORBA.PriorityBand[] priority_bands() {
        return priorityBand;
    }

}