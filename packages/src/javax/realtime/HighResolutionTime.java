package javax.realtime;


public class HighResolutionTime {

    long millis = 0;
    long nanos = 0;

    public HighResolutionTime (){
    }

    public long getNanoseconds(){
        return nanos;
    }

    public long getMilliseconds(){
        //return System.currentTimeMillis();
        return millis;
    }

}