package org.omg.RTCORBA;


public interface MutexOperations {
    void lock();
    void unlock();
    boolean try_lock(long max_wait);
}
