package edu.uci.ece.zen.orb.policies;

import org.omg.RTCORBA.CLIENT_PROTOCOL_POLICY_TYPE;
import org.omg.RTCORBA.ClientProtocolPolicy;
import org.omg.RTCORBA.Protocol;

/**
 * This class implements the Client Protocol Policy
 * 
 * @author Mark Panahi
 * @version 1.0
 */

public class ClientProtocolPolicyImpl extends org.omg.CORBA.LocalObject
        implements ClientProtocolPolicy {
    private Protocol[] protocols;

    public ClientProtocolPolicyImpl() {
        //right now there's only IIOP
        //protocols = new Protocol[1];
    }

    /*
     * public ClientProtocolPolicyImpl(org.omg.RTCORBA.Protocol[] protocols){
     * this.protocols = protocols; }
     */
    /**
     * Read accessor for policy_type attribute
     * 
     * @return the attribute value
     */
    public int policy_type() {
        return CLIENT_PROTOCOL_POLICY_TYPE.value;
    }

    /**
     * Operation copy
     */
    public org.omg.CORBA.Policy copy() {
        ClientProtocolPolicyImpl cpp = new ClientProtocolPolicyImpl();
        cpp.protocols(protocols);
        return cpp;
    }

    /**
     * Operation destroy
     */
    public void destroy() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Read accessor for protocols attribute
     * 
     * @return the attribute value
     */
    public Protocol[] protocols() {
        return protocols;
    }

    public void protocols(Protocol[] protocols) {
        //this.protocols[0] = protocols[0];
        this.protocols = protocols;
    }

}