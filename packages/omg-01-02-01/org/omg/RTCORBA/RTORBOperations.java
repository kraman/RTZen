package org.omg.RTCORBA;


/**
 *	Generated from IDL definition of interface "RTORB"
 *	  
 */

public interface RTORBOperations {
    org.omg.RTCORBA.Mutex create_mutex();

    void destroy_mutex(org.omg.RTCORBA.Mutex the_mutex);

    int create_threadpool(int stacksize,
            int static_threads,
            int dynamic_threads,
            short default_priority,
            boolean allow_request_buffering,
            int max_buffered_requests,
            int max_request_buffer_size);

    int create_threadpool_with_lanes(int stacksize,
            org.omg.RTCORBA.ThreadpoolLane[] lanes,
            boolean allow_borrowing,
            boolean allow_request_buffering,
            int max_buffered_requests,
            int max_request_buffer_size);

    void destroy_threadpool(int threadpool)
        throws org.omg.RTCORBA.RTORBPackage.InvalidThreadpool;

    org.omg.RTCORBA.PriorityModelPolicy create_priority_model_policy(
            org.omg.RTCORBA.PriorityModel priority_model,
            short server_priority);

    org.omg.RTCORBA.ThreadpoolPolicy create_threadpool_policy(int threadpool);

    org.omg.RTCORBA.PriorityBandedConnectionPolicy create_priority_banded_connection_policy(
            org.omg.RTCORBA.PriorityBand[] priority_bands);

    org.omg.RTCORBA.ServerProtocolPolicy create_server_protocol_policy(
            org.omg.RTCORBA.Protocol[] protocols);

    org.omg.RTCORBA.ClientProtocolPolicy create_client_protocol_policy(
            org.omg.RTCORBA.Protocol[] protocols);

    org.omg.RTCORBA.PrivateConnectionPolicy create_private_connection_policy();
}
