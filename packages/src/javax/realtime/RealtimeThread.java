package javax.realtime;

public class RealtimeThread extends Thread{
    RealtimeThread( Runnable r ){
        super( r );
    }

    private static java.util.Hashtable memHash = new java.util.Hashtable();
    public static void setCurrentMemoryArea( MemoryArea currentMem ){
        if( currentMem == null )
            currentMem = HeapMemory.instance();
        memHash.put(Thread.currentThread(),currentMem);  
    }
    public static MemoryArea getCurrentMemoryArea(){ return (MemoryArea)memHash.get(Thread.currentThread());}
}
