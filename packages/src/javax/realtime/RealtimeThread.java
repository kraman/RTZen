package javax.realtime;

public class RealtimeThread extends Thread {
    public RealtimeThread(Object o1, Object o2, Object o3, MemoryArea lt,
            Object o5, Runnable r) {
        super(r);
    }

    public RealtimeThread() {
    }

    private static java.util.Hashtable memHash = new java.util.Hashtable();

    public static void setCurrentMemoryArea(MemoryArea currentMem) {
        if (currentMem == null) currentMem = HeapMemory.instance();
        memHash.put(Thread.currentThread(), currentMem);

    }

    public static MemoryArea getCurrentMemoryArea() {
        MemoryArea mem = (MemoryArea) memHash.get(Thread.currentThread());

        if (mem == null) return HeapMemory.instance();
        else return mem;
    }

    public static RealtimeThread currentRealtimeThread() {

        System.out.println("current thread is: " + Thread.currentThread()
                + " class:" + Thread.currentThread().getClass().toString());
        return new RealtimeThread(null, null, null, null, null, null);
    }

    public static MemoryArea getOuterMemoryArea(int index) {
        return (MemoryArea) memHash.get(Thread.currentThread());
    }

    public static int getMemoryAreaStackDepth() {
        return 0;
    }
}
