package edu.uci.ece.zen.orb;

import org.omg.RTCORBA.*;
import edu.uci.ece.zen.orb.policies.*;
import edu.uci.ece.zen.utils.*;
import javax.realtime.*;

/**
 * Implementation of the RTORB
 *
 * @author Mark Panahi
 */
public class RTORBImpl
        extends org.omg.CORBA.LocalObject
        implements RTORB
{
    private ORB orb;
    private ThreadPoolRunnable tpr;

    public RTORBImpl(ORB orb){
        this.orb = orb;
        tpr = new ThreadPoolRunnable();
    }

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
        tpr.init(stacksize, static_threads, dynamic_threads, default_priority, allow_request_buffering, max_buffered_requests, max_request_buffer_size);
        return setUpThreadPool();
    }

    /**
     * Operation create_threadpool_with_lanes
     */
    public int create_threadpool_with_lanes(int stacksize, org.omg.RTCORBA.ThreadpoolLane[] lanes, boolean allow_borrowing, boolean allow_request_buffering, int max_buffered_requests, int max_request_buffer_size){
        tpr.init(stacksize, lanes, allow_borrowing, allow_request_buffering, max_buffered_requests, max_request_buffer_size );
        return setUpThreadPool();
    }

    private int setUpThreadPool(){
        ExecuteInRunnable r = orb.getEIR();
        ScopedMemory sm = orb.getScopedRegion();

        r.init(tpr, sm);
        try{
            orb.orbImplRegion.executeInArea( r );
        }catch( Exception e ){
            ZenProperties.logger.log(
                Logger.FATAL,
                "edu.uci.ece.zen.orb.RTORBImpl",
                "create_threadpool",
                "Could not create threadpool due to exception: " + e.toString()
                );
            System.exit(-1);
        }
        orb.freeEIR( r );

        //KLUDGE: need to set up property for max TPs
        int tmpID = tpID;
        tpID++;

        return tmpID;

        //throw new org.omg.CORBA.NO_IMPLEMENT();
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
        spp.protocols(protocols);
        return spp;
    }

    /**
     * Operation create_client_protocol_policy
     */
    public org.omg.RTCORBA.ClientProtocolPolicy create_client_protocol_policy(org.omg.RTCORBA.Protocol[] protocols){
        //return new ClientProtocolPolicyImpl(protocols);
        cpp.protocols(protocols);
        return cpp;
    }

    /**
     * Operation create_private_connection_policy
     */
    public org.omg.RTCORBA.PrivateConnectionPolicy create_private_connection_policy(){
        return pcp;
    }

    /**
     * Operation create_tcp_protocol_properties
     */
    public org.omg.RTCORBA.TCPProtocolProperties create_tcp_protocol_properties(int send_buffer_size, int recv_buffer_size,
                                                                    boolean keep_alive, boolean dont_route, boolean no_delay){
        //throw new org.omg.CORBA.NO_IMPLEMENT();
        tcpPP.send_buffer_size(send_buffer_size);
        tcpPP.recv_buffer_size(recv_buffer_size);
        tcpPP.keep_alive(keep_alive);
        tcpPP.dont_route(dont_route);
        tcpPP.no_delay(no_delay);
        return tcpPP;

    }

    //these are static for now, should eventually be per-ORB

    public static ServerProtocolPolicyImpl spp = new ServerProtocolPolicyImpl();
    static ClientProtocolPolicyImpl cpp = new ClientProtocolPolicyImpl();
    static PrivateConnectionPolicy pcp = new PrivateConnectionPolicyImpl();
    public static TCPProtocolProperties tcpPP = new TCPProtocolPropertiesImpl();

    int tpID = 0;


    class ThreadPoolRunnable implements Runnable{

        int stacksize;
        org.omg.RTCORBA.ThreadpoolLane[] lanes;
        boolean allowBorrowing;
        boolean allowRequestBuffering;
        int maxBufferedRequests;
        int maxRequestBufferSize;
        int staticThreads;
        int dynamicThreads;
        short defaultPriority;

        public ThreadPoolRunnable(){
            stacksize = -1;
        }

        public void init(int stacksize, int static_threads, int dynamic_threads, short default_priority, boolean allow_request_buffering, int max_buffered_requests, int max_request_buffer_size){
            this.stacksize = stacksize;
            this.staticThreads = static_threads;
            this.dynamicThreads = dynamic_threads;
            this.defaultPriority = default_priority;
            this.allowRequestBuffering = allow_request_buffering;
            this.maxBufferedRequests = max_buffered_requests;
            this.maxRequestBufferSize = max_request_buffer_size;
            this.lanes = null;
        }

        public void init(int stacksize, org.omg.RTCORBA.ThreadpoolLane[] lanes, boolean allow_borrowing, boolean allow_request_buffering, int max_buffered_requests, int max_request_buffer_size){
            this.stacksize = stacksize;
            this.lanes = lanes;
            this.allowBorrowing = allow_borrowing;
            this.allowRequestBuffering = allow_request_buffering;
            this.maxBufferedRequests = max_buffered_requests;
            this.maxRequestBufferSize = max_request_buffer_size;
        }

        public void run(){
            //make sure this has been initialized
            if(stacksize >= 0){
                ThreadPool tp;
                if(lanes == null)
                    tp = new ThreadPool(stacksize, staticThreads, dynamicThreads, defaultPriority, allowRequestBuffering, maxBufferedRequests, maxRequestBufferSize);
                else
                    tp = new ThreadPool(stacksize, allowRequestBuffering, maxBufferedRequests, maxRequestBufferSize, lanes, allowBorrowing );

                orb.threadpoolList[tpID] = RealtimeThread.getCurrentMemoryArea();

                ((ScopedMemory)orb.threadpoolList[tpID]).setPortal(tp);

                stacksize = -1;
            }
        }
    }
}

/*
privilieged aspect TCPProtocolPropertiesAspect{



edu.uci.ece.zen.orb.transport.iiop;

    void around():

            execution(edu.uci.ece.zen.orb.transport.iiop.RTORBImpl.create_tcp_protocol_properties())
            && args()
                                                                    {

    }


    int _send_buffer_size = 1024 * 64;
    int _recv_buffer_size = 1024 * 64;
    boolean _keep_alive = false;
    boolean _dont_route = true;
    boolean _no_delay = true;

    Object around(int send_buffer_size, int recv_buffer_size,boolean keep_alive, boolean dont_route, boolean no_delay):

            execution(org.omg.RTCORBA.TCPProtocolProperties edu.uci.ece.zen.orb.RTORBImpl.create_tcp_protocol_properties(
                                                                    int, int, boolean, boolean, boolean))
            && args(send_buffer_size,recv_buffer_size,keep_alive,dont_route,no_delay)
                                                                    {
        return new org.omg.RTCORBA.TCPProtocolProperties(send_buffer_size,recv_buffer_size,keep_alive,dont_route,no_delay);
    }

    Object around(): execution(org.omg.RTCORBA.ClientProtocolPolicy edu.uci.ece.zen.orb.RTORBImpl.create_client_protocol_policy(..)) {
        return false;
    }

}*/