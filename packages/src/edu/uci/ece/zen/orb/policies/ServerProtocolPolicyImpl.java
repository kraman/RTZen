/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.policies;

import org.omg.RTCORBA.Protocol;
import org.omg.RTCORBA.SERVER_PROTOCOL_POLICY_TYPE;
import org.omg.RTCORBA.ServerProtocolPolicy;

/**
 * This class implements the Server Protocol Policy
 * 
 * @author Mark Panahi
 * @version 1.0
 */

public class ServerProtocolPolicyImpl extends org.omg.CORBA.LocalObject
        implements ServerProtocolPolicy {
    private Protocol[] protocols;

    public ServerProtocolPolicyImpl() {
        //right now there's only IIOP
        //protocols = new Protocol[1];
    }

    /*
     * public ServerProtocolPolicyImpl(org.omg.RTCORBA.Protocol[] protocols){
     * this.protocols = protocols; }
     */
    /**
     * Read accessor for policy_type attribute
     * 
     * @return the attribute value
     */
    public int policy_type() {
        return SERVER_PROTOCOL_POLICY_TYPE.value;
    }

    /**
     * Operation copy
     */
    public org.omg.CORBA.Policy copy() {
        ServerProtocolPolicyImpl spp = new ServerProtocolPolicyImpl();
        spp.protocols(protocols);
        return spp;
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
        this.protocols = protocols;
    }
}