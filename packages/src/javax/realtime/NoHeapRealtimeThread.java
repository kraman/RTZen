/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package javax.realtime;

public class NoHeapRealtimeThread extends RealtimeThread {
    public NoHeapRealtimeThread(Object o1, Object o2, Object o3, MemoryArea lt,
            Object o5, Runnable r) {
        super(o1, o2, o3, lt, o5, r);
    }

    public NoHeapRealtimeThread(Object o1, Object o2) {

    }

    public NoHeapRealtimeThread() {
        super();
    }
}
