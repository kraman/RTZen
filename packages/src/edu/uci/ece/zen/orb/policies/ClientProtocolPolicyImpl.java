package edu.uci.ece.zen.orb.policies;

import org.omg.RTCORBA.*;

/**
 * This class implements the Client Protocol Policy
 * @author Mark Panahi
 * @version 1.0
 */

public class ClientProtocolPolicyImpl
        extends org.omg.CORBA.LocalObject
        implements ClientProtocolPolicy
{
    private org.omg.RTCORBA.Protocol[] protocols;

    public ClientProtocolPolicyImpl(org.omg.RTCORBA.Protocol[] protocols){
        this.protocols = protocols;

    }
    /**
     * Read accessor for policy_type attribute
     * @return the attribute value
     */
    public int policy_type(){
        return CLIENT_PROTOCOL_POLICY_TYPE.value;
    }

    /**
     * Operation copy
     */
    public org.omg.CORBA.Policy copy(){
        return new ClientProtocolPolicyImpl(protocols);
    }

    /**
     * Operation destroy
     */
    public void destroy(){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Read accessor for protocols attribute
     * @return the attribute value
     */
    public org.omg.RTCORBA.Protocol[] protocols(){
        return protocols;
    }


}