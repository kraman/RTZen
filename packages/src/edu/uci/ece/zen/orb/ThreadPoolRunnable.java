/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;
import javax.realtime.MemoryArea;
import edu.uci.ece.zen.utils.ThreadPool;
import edu.uci.ece.zen.orb.transport.iiop.AcceptorRunnable;

import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;
import edu.uci.ece.zen.orb.CDROutputStream;

public class ThreadPoolRunnable implements Runnable {

    int stacksize;

    //org.omg.RTCORBA.ThreadpoolLane[] lanes;
    CDROutputStream lanes;

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
        //ZenProperties.logger.log("@#@#@#@#@#@#@#@#@#@#@#@#@#@#@#@ThreadPoolRunnable 3");
        //if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("orb mem" + MemoryArea.getMemoryArea(orb));
        //if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("lanes mem" + MemoryArea.getMemoryArea(lanes));
        this.lanes = ThreadPool.marshalLanes(lanes,orb);
        //ZenProperties.logger.log("ThreadPoolRunnable 4");
        this.allowBorrowing = allow_borrowing;
        this.allowRequestBuffering = allow_request_buffering;
        this.maxBufferedRequests = max_buffered_requests;
        this.maxRequestBufferSize = max_request_buffer_size;
    }

    public void run() {
        //make sure this has been initialized
        if (stacksize >= 0) {
            ThreadPool tp;
            if (lanes == null) {
                tp = new ThreadPool(stacksize, staticThreads,
                    dynamicThreads, defaultPriority, allowRequestBuffering,
                    maxBufferedRequests, maxRequestBufferSize, orb, 
                    rtorb.acceptorRunnable, rtorb.tpID );
            } else {
                tp = new ThreadPool(stacksize, allowRequestBuffering,
                    maxBufferedRequests, maxRequestBufferSize, lanes,
                    allowBorrowing, orb, rtorb.acceptorRunnable, rtorb.tpID );
                lanes.free();
            }
            orb.threadpoolList[rtorb.tpID] = (ScopedMemory) RealtimeThread.getCurrentMemoryArea();

            ((ScopedMemory) orb.threadpoolList[rtorb.tpID]).setPortal(tp);
            stacksize = -1;
        }
    }
}
