package javax.realtime;

public class PriorityScheduler{
    public static PriorityScheduler instance(){
        return new PriorityScheduler();
    }

    public PriorityScheduler(){}
    
    public short getNormPriority(){
        return RealtimeThread.NORM_PRIORITY;
    }

    public short getMinPriority(){
        return RealtimeThread.MIN_PRIORITY;
    }

    public short getMaxPriority(){
        return RealtimeThread.MAX_PRIORITY;
    }
}
