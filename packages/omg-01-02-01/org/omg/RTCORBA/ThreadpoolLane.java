package org.omg.RTCORBA;


/**
 *	Generated from IDL definition of struct "ThreadpoolLane"
 */

public final class ThreadpoolLane
    implements org.omg.CORBA.portable.IDLEntity {
    public ThreadpoolLane() {}
    public short lane_priority;
    public int static_threads;
    public int dynamic_threads;
    public ThreadpoolLane(short lane_priority, int static_threads, int dynamic_threads) {
        this.lane_priority = lane_priority;
        this.static_threads = static_threads;
        this.dynamic_threads = dynamic_threads;
    }
}
