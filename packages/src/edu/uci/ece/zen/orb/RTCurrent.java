/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.utils.IntHashtable;
import javax.realtime.ImmortalMemory;
/**
 * Interface definition : Current
 *
 * @author OpenORB Compiler
 */
public class RTCurrent extends org.omg.CORBA.LocalObject implements
        org.omg.RTCORBA.Current {

    private static IntHashtable hash;
    private static RTCurrent instance;

    //short priority;

    //ORB orb;

    public static synchronized RTCurrent instance(){
        if(instance == null){
            try{
                instance = (RTCurrent) (ImmortalMemory.instance().newInstance(RTCurrent.class));
                instance.init();
            }catch(Exception e){
                e.printStackTrace();//TODO better error handling
            }
        }
        return instance;
    }

    private void init() {
        //orbMemoryArea = RealtimeThread.getCurrentMemoryArea();
        //this.orb = orb;
        try{
            hash = (IntHashtable) ImmortalMemory.instance().newInstance(IntHashtable.class);
        }catch(Exception e){
            e.printStackTrace();//TODO
        }
        hash.init(1023);
    }

    /**
     * Read accessor for the_priority attribute
     *
     * @return the attribute value
     */
    public short the_priority() {
        return (short)(hash.get(Thread.currentThread()));
    }

    /**
     * Write accessor for the_priority attribute
     *
     * @param value
     *            the attribute value
     */
    public void the_priority(short value) {
        hash.put(Thread.currentThread(), value);
    }

}