package javax.realtime;


public final class PriorityScheduler {

    private static PriorityScheduler inst;

    public static final int MAX_PRIORITY = 0;

    public static final int MIN_PRIORITY = 0;

    //public static final int NORM_PRIORITY = 0;


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
        return 0;
    }

    public static int getNormPriority(Thread t){
        return 0;
    }

}
