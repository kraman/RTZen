package javax.realtime;


public final class PriorityScheduler {

    private static PriorityScheduler inst;

    public static final int MAX_PRIORITY = Thread.MAX_PRIORITY;

    public static final int MIN_PRIORITY = Thread.MIN_PRIORITY;

    public static final int NORM_PRIORITY = (Thread.MAX_PRIORITY - Thread.MIN_PRIORITY)/2;


    public static PriorityScheduler instance(){
        if(inst == null)
            inst = new PriorityScheduler();

        return inst;
    }

    public int getMinPriority(){
        return MIN_PRIORITY;
    }

    public int getMaxPriority(){
        return MAX_PRIORITY;
    }

    public int getNormPriority(){
        return NORM_PRIORITY;
    }

    public static int getNormPriority(Thread t){
        return NORM_PRIORITY;
    }

}
