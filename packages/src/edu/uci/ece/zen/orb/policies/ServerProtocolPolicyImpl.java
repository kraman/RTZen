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
    private Protocol[] protocols;

    public ServerProtocolPolicyImpl(){
        //right now there's only IIOP
        protocols = new Protocol[1];
    }
/*
    public ServerProtocolPolicyImpl(org.omg.RTCORBA.Protocol[] protocols){
        this.protocols = protocols;

    }*/
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
        ServerProtocolPolicyImpl spp = new ServerProtocolPolicyImpl();
        spp.protocols(protocols);
        return spp;
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
    public Protocol[] protocols(){
        return protocols;
    }

    public void protocols(Protocol[] protocols){
        this.protocols[0] = protocols[0];
    }
}
