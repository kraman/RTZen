package javax.realtime;

public class NoHeapRealtimeThread extends RealtimeThread{
    public NoHeapRealtimeThread( Object o1, Object o2, Object o3, Object o4, Object o5, Runnable r ){
        super( r );
    }
}
