package org.omg.RTCORBA;

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
