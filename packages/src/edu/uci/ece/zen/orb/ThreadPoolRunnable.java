package edu.uci.ece.zen.orb;

import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.utils.ThreadPool;

public class ThreadPoolRunnable implements Runnable {

    int stacksize;

    org.omg.RTCORBA.ThreadpoolLane[] lanes;

    boolean allowBorrowing;

    boolean allowRequestBuffering;

    int maxBufferedRequests;

    int maxRequestBufferSize;

    int staticThreads;

    int dynamicThreads;

    short defaultPriority;

    ORB orb;

    RTORBImpl rtorb;

    public ThreadPoolRunnable() {
        stacksize = -1;
    }

    public void init(RTORBImpl rtorb, ORB orb, int stacksize,
            int static_threads, int dynamic_threads, short default_priority,
            boolean allow_request_buffering, int max_buffered_requests,
            int max_request_buffer_size) {
        this.orb = orb;
        this.rtorb = rtorb;
        this.stacksize = stacksize;
        this.staticThreads = static_threads;
        this.dynamicThreads = dynamic_threads;
        this.defaultPriority = default_priority;
        this.allowRequestBuffering = allow_request_buffering;
        this.maxBufferedRequests = max_buffered_requests;
        this.maxRequestBufferSize = max_request_buffer_size;
        this.lanes = null;
    }

    public void init(RTORBImpl rtorb, ORB orb, int stacksize,
            org.omg.RTCORBA.ThreadpoolLane[] lanes, boolean allow_borrowing,
            boolean allow_request_buffering, int max_buffered_requests,
            int max_request_buffer_size) {
        this.orb = orb;
        this.rtorb = rtorb;
        this.stacksize = stacksize;
        this.lanes = lanes;
        this.allowBorrowing = allow_borrowing;
        this.allowRequestBuffering = allow_request_buffering;
        this.maxBufferedRequests = max_buffered_requests;
        this.maxRequestBufferSize = max_request_buffer_size;
    }

    public void run() {
        //make sure this has been initialized
        if (stacksize >= 0) {
            ThreadPool tp;
            if (lanes == null)
            {
                tp = new ThreadPool(stacksize, staticThreads,
                    dynamicThreads, defaultPriority, allowRequestBuffering,
                    maxBufferedRequests, maxRequestBufferSize, orb);
            }
            else
            {
                tp = new ThreadPool(stacksize, allowRequestBuffering,
                    maxBufferedRequests, maxRequestBufferSize, lanes,
                    allowBorrowing, orb);
            }

            orb.threadpoolList[rtorb.tpID] = 
                (ScopedMemory) RealtimeThread.getCurrentMemoryArea();

            ((ScopedMemory) orb.threadpoolList[rtorb.tpID]).setPortal(tp);
            stacksize = -1;
        }
    }
}