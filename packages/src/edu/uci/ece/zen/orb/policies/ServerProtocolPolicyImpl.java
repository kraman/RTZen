package edu.uci.ece.zen.orb.policies;

import org.omg.RTCORBA.*;

/**
 * This class implements the Server Protocol Policy
 * @author Mark Panahi
 * @version 1.0
 */

public class ServerProtocolPolicyImpl
        extends org.omg.CORBA.LocalObject
        implements ServerProtocolPolicy
{
    private org.omg.RTCORBA.Protocol[] protocols;

    public ServerProtocolPolicyImpl(org.omg.RTCORBA.Protocol[] protocols){
        this.protocols = protocols;

    }
    /**
     * Read accessor for policy_type attribute
     * @return the attribute value
     */
    public int policy_type(){
        return SERVER_PROTOCOL_POLICY_TYPE.value;
    }

    /**
     * Operation copy
     */
    public org.omg.CORBA.Policy copy(){
        return new ServerProtocolPolicyImpl(protocols);
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
