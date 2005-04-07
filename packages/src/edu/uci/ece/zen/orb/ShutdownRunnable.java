/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */
package edu.uci.ece.zen.orb;

import javax.realtime.ImmortalMemory;
import javax.realtime.LTMemory;
import javax.realtime.MemoryArea;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;
import edu.uci.ece.zen.orb.transport.Transport;
import edu.uci.ece.zen.utils.ExecuteInRunnable;
import edu.uci.ece.zen.utils.Queue;

public class ShutdownRunnable implements Runnable{

    private static ShutdownRunnable _instance;
    public static ShutdownRunnable instance(){
        if( _instance == null ){
            try{
                _instance = (ShutdownRunnable) ImmortalMemory.instance().newInstance( ShutdownRunnable.class );
            }catch( Exception e ){
                e.printStackTrace();
            }
        }
        return _instance;
    }

    public ShutdownRunnable(){}

    public void run(){
        ScopedMemory sm = ((ScopedMemory)RealtimeThread.getCurrentMemoryArea());
        Object portal = sm.getPortal();
        if( portal != null && portal instanceof ORBComponent )
            ((ORBComponent)portal).shutdown(true);
    }
}
