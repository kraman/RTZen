package javax.realtime;


public final class PriorityScheduler {

    private static PriorityScheduler inst;


    public static PriorityScheduler instance(){
        if(inst == null)
            inst = new PriorityScheduler();

        return inst;
    }

    public int getMinPriority(){
        return 0;
    }

    public int getMaxPriority(){
        return 0;
    }

    public int getNormPriority(){
        return 0;
    }

}
