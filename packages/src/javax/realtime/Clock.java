package javax.realtime;


public final class Clock {

    private static Clock inst;

    public Clock (){
    }

    public static Clock getRealtimeClock(){
        if(inst == null)
            inst = new Clock();

        return inst;
    }

    public void getTime(AbsoluteTime at){
        at.millis = System.currentTimeMillis();
    }
}