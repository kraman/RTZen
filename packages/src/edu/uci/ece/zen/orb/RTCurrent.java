package edu.uci.ece.zen.orb;

/**
 * Interface definition : Current
 *
 * @author OpenORB Compiler
 */
public class RTCurrent
    extends org.omg.CORBA.LocalObject
    implements org.omg.RTCORBA.Current
{

    short priority;

    ORB orb;

    public void init(ORB orb){
        //orbMemoryArea = RealtimeThread.getCurrentMemoryArea();
        this.orb = orb;
    }

    /**
     * Read accessor for the_priority attribute
     * @return the attribute value
     */
    public short the_priority(){
        return priority;
    }

    /**
     * Write accessor for the_priority attribute
     * @param value the attribute value
     */
    public void the_priority(short value){
        value = priority;
    }

}
