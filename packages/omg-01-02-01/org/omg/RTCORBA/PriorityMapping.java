package org.omg.RTCORBA;


abstract public class PriorityMapping {

    abstract public boolean to_native(short corba_priority,
            org.omg.CORBA.ShortHolder native_priority);

    abstract public boolean to_CORBA(short native_priority,
            org.omg.CORBA.ShortHolder corba_priority);
}
