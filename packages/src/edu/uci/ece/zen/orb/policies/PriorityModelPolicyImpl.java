package edu.uci.ece.zen.orb.policies;

import org.omg.RTCORBA.PRIORITY_MODEL_POLICY_TYPE;
import org.omg.RTCORBA.PriorityModel;
import org.omg.RTCORBA.PriorityModelPolicy;

/**
 * This class implements the PriorityModelPolicy
 * 
 * @author Mark Panahi
 * @version 1.0
 */

public class PriorityModelPolicyImpl extends org.omg.CORBA.LocalObject
        implements PriorityModelPolicy

{
    private PriorityModel priority_model;

    private short server_priority;

    public PriorityModelPolicyImpl(PriorityModel priority_model,
            short server_priority) {
        this.priority_model = priority_model;
        this.server_priority = server_priority;

    }

    /**
     * Read accessor for policy_type attribute
     * 
     * @return the attribute value
     */
    public int policy_type() {
        return PRIORITY_MODEL_POLICY_TYPE.value;
    }

    /**
     * Operation copy
     */
    public org.omg.CORBA.Policy copy() {
        return new PriorityModelPolicyImpl(priority_model, server_priority);
    }

    /**
     * Operation destroy
     */
    public void destroy() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Read accessor for priority_model attribute
     * 
     * @return the attribute value
     */
    public org.omg.RTCORBA.PriorityModel priority_model() {
        //return PriorityModel.SERVER_DECLARED;
        return priority_model;
    }

    /**
     * Read accessor for server_priority attribute
     * 
     * @return the attribute value
     */
    public short server_priority() {
        return server_priority;
    }
}