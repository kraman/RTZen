package javax.realtime;

public class NoHeapRealtimeThread extends RealtimeThread {
    public NoHeapRealtimeThread(Object o1, Object o2, Object o3, MemoryArea lt,
            Object o5, Runnable r) {
        super(o1, o2, o3, lt, o5, r);
    }

    public NoHeapRealtimeThread(Object o1, Object o2) {
    }
}