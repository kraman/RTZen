package javax.realtime;

public class NoHeapRealtimeThread extends RealtimeThread{
    public NoHeapRealtimeThread( Runnable r ){
        super( r );
    }
}
