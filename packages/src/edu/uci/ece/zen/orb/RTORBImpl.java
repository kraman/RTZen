package edu.uci.ece.zen.orb;

import org.omg.RTCORBA.*;
import edu.uci.ece.zen.orb.policies.*;

/**
 * Implementation of the RTORB
 *
 * @author Mark Panahi
 */
public class RTORBImpl
        extends org.omg.CORBA.LocalObject
        implements RTORB
{
    /**
     * Operation create_mutex
     */
    public org.omg.RTCORBA.Mutex create_mutex(){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation destroy_mutex
     */
    public void destroy_mutex(org.omg.RTCORBA.Mutex the_mutex){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation create_threadpool
     */
    public int create_threadpool(int stacksize, int static_threads, int dynamic_threads, short default_priority, boolean allow_request_buffering, int max_buffered_requests, int max_request_buffer_size){

        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation create_threadpool_with_lanes
     */
    public int create_threadpool_with_lanes(int stacksize, org.omg.RTCORBA.ThreadpoolLane[] lanes, boolean allow_borrowing, boolean allow_request_buffering, int max_buffered_requests, int max_request_buffer_size){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation destroy_threadpool
     */
    public void destroy_threadpool(int threadpool)
        throws org.omg.RTCORBA.RTORBPackage.InvalidThreadpool{

        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation create_priority_model_policy
     */
    public org.omg.RTCORBA.PriorityModelPolicy create_priority_model_policy(org.omg.RTCORBA.PriorityModel priority_model, short server_priority){
        return new PriorityModelPolicyImpl(priority_model, server_priority);
    }

    /**
     * Operation create_threadpool_policy
     */
    public org.omg.RTCORBA.ThreadpoolPolicy create_threadpool_policy(int threadpool){
        return new ThreadpoolPolicyImpl(threadpool);
    }

    /**
     * Operation create_priority_banded_connection_policy
     */
    public org.omg.RTCORBA.PriorityBandedConnectionPolicy create_priority_banded_connection_policy(org.omg.RTCORBA.PriorityBand[] priority_bands){
        return new PriorityBandedConnectionPolicyImpl(priority_bands);
    }

    /**
     * Operation create_server_protocol_policy
     */
    public org.omg.RTCORBA.ServerProtocolPolicy create_server_protocol_policy(org.omg.RTCORBA.Protocol[] protocols){
        return new ServerProtocolPolicyImpl(protocols);
    }

    /**
     * Operation create_client_protocol_policy
     */
    public org.omg.RTCORBA.ClientProtocolPolicy create_client_protocol_policy(org.omg.RTCORBA.Protocol[] protocols){
        return new ClientProtocolPolicyImpl(protocols);
    }

    /**
     * Operation create_private_connection_policy
     */
    public org.omg.RTCORBA.PrivateConnectionPolicy create_private_connection_policy(){
        return new PrivateConnectionPolicyImpl();
    }

    /**
     * Operation create_tcp_protocol_properties
     */
    public org.omg.RTCORBA.TCPProtocolProperties create_tcp_protocol_properties(int send_buffer_size, int recv_buffer_size, boolean keep_alive, boolean dont_route, boolean no_delay){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

}
