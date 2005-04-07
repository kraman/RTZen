/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import org.omg.RTCORBA.RTORB;
import org.omg.RTCORBA.TCPProtocolProperties;

import edu.uci.ece.zen.orb.policies.ClientProtocolPolicyImpl;
import edu.uci.ece.zen.orb.policies.PriorityBandedConnectionPolicyImpl;
import edu.uci.ece.zen.orb.policies.PriorityModelPolicyImpl;
import edu.uci.ece.zen.orb.policies.ServerProtocolPolicyImpl;
import edu.uci.ece.zen.orb.policies.TCPProtocolPropertiesImpl;
import edu.uci.ece.zen.orb.policies.ThreadpoolPolicyImpl;
import edu.uci.ece.zen.orb.transport.iiop.AcceptorRunnable;
import edu.uci.ece.zen.utils.ZenProperties;

/**
 * Implementation of the RTORB
 * 
 * @author Mark Panahi
 */
public class RTORBImpl extends org.omg.CORBA.LocalObject implements RTORB {
    private ORB orb;

    private ThreadPoolRunnable tpr;

    public AcceptorRunnable acceptorRunnable;

    public void init(ORB orb) {
        this.orb = orb;

        tpr = new ThreadPoolRunnable();
        acceptorRunnable = new AcceptorRunnable();
        /*
         * try{ tpr = (ThreadPoolRunnable)(orb.parentMemoryArea.newInstance(
         * ThreadPoolRunnable.class )); acceptorRunnable =
         * (AcceptorRunnable)(orb.parentMemoryArea.newInstance(
         * AcceptorRunnable.class )); acceptorRunnable.init(orb);
         * }catch(Exception e){ e.printStackTrace(); }
         */
    }

    /**
     * Operation create_mutex
     */
    public org.omg.RTCORBA.Mutex create_mutex() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation destroy_mutex
     */
    public void destroy_mutex(org.omg.RTCORBA.Mutex the_mutex) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation create_threadpool
     */
    public int create_threadpool(int stacksize, int static_threads,
            int dynamic_threads, short default_priority,
            boolean allow_request_buffering, int max_buffered_requests,
            int max_request_buffer_size) {
        ZenProperties.logger.log("_+_+_+_+_+_+_+_+_+_+_+_+_+_+_ CREATING THREADPOOL +_+_+_+_+_+_+_+_+_+_+_+_+_");
        tpr.init(this, orb, stacksize, static_threads, dynamic_threads,
                default_priority, allow_request_buffering,
                max_buffered_requests, max_request_buffer_size);
        orb.setUpORBChildRegion(tpr);
        return nextID();
    }

    /**
     * Operation create_threadpool_with_lanes
     */
    public int create_threadpool_with_lanes(int stacksize,
            org.omg.RTCORBA.ThreadpoolLane[] lanes, boolean allow_borrowing,
            boolean allow_request_buffering, int max_buffered_requests,
            int max_request_buffer_size) {
        tpr.init(this, orb, stacksize, lanes, allow_borrowing,
                allow_request_buffering, max_buffered_requests,
                max_request_buffer_size);
        orb.setUpORBChildRegion(tpr);
        return nextID();
    }

    private int nextID() {
        //KLUDGE: need to set up property for max TPs
        int tmpID = tpID;
        tpID++;
        return tmpID;
    }

    /**
     * Operation destroy_threadpool
     */
    public void destroy_threadpool(int threadpool)
            throws org.omg.RTCORBA.RTORBPackage.InvalidThreadpool {

        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation create_priority_model_policy
     */
    public org.omg.RTCORBA.PriorityModelPolicy create_priority_model_policy(
            org.omg.RTCORBA.PriorityModel priority_model, short server_priority) {
        return new PriorityModelPolicyImpl(priority_model, server_priority);
    }

    /**
     * Operation create_threadpool_policy
     */
    public org.omg.RTCORBA.ThreadpoolPolicy create_threadpool_policy(
            int threadpool) {
        return new ThreadpoolPolicyImpl(threadpool);
    }

    /**
     * Operation create_priority_banded_connection_policy
     */
    public org.omg.RTCORBA.PriorityBandedConnectionPolicy create_priority_banded_connection_policy(
            org.omg.RTCORBA.PriorityBand[] priority_bands) {
        return new PriorityBandedConnectionPolicyImpl(priority_bands);
    }

    /**
     * Operation create_server_protocol_policy
     */
    public org.omg.RTCORBA.ServerProtocolPolicy create_server_protocol_policy(
            org.omg.RTCORBA.Protocol[] protocols) {
        ServerProtocolPolicyImpl spp = new ServerProtocolPolicyImpl();
        spp.protocols(protocols);
        return spp;
    }

    /**
     * Operation create_client_protocol_policy
     */
    public org.omg.RTCORBA.ClientProtocolPolicy create_client_protocol_policy(
            org.omg.RTCORBA.Protocol[] protocols) {
        ClientProtocolPolicyImpl cpp = new ClientProtocolPolicyImpl();
        cpp.protocols(protocols);
        return cpp;
    }

    /**
     * Operation create_private_connection_policy
     */
    public org.omg.RTCORBA.PrivateConnectionPolicy create_private_connection_policy() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation create_tcp_protocol_properties
     */
    public org.omg.RTCORBA.TCPProtocolProperties create_tcp_protocol_properties(
            int send_buffer_size, int recv_buffer_size, boolean keep_alive,
            boolean dont_route, boolean no_delay) {

        TCPProtocolProperties tcpPP = new TCPProtocolPropertiesImpl();
        tcpPP.send_buffer_size(send_buffer_size);
        tcpPP.recv_buffer_size(recv_buffer_size);
        tcpPP.keep_alive(keep_alive);
        tcpPP.dont_route(dont_route);
        tcpPP.no_delay(no_delay);
        return tcpPP;
    }

    int tpID = 0;

    class RTORBInitRunnable implements Runnable {

        public void run() {
        }
    }

}
