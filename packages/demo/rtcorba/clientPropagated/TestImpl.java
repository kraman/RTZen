/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.rtcorba.clientPropagated;

import org.omg.CORBA.ORB;
/**
 * This class implements the RTCORBA client propagated
    priority demo server from TAO.
 * @author Mark Panahi
 * @version 1.0
 */

public class TestImpl extends TestPOA

{
    private ORB orb;

    public TestImpl(ORB orb){
        this.orb = orb;
    }

    /**
     *
     */
    public void test_method (short priority)
    {
        try{
            //org.omg.RTCORBA.Current rtcur = org.omg.RTCORBA.CurrentHelper.narrow(_orb().resolve_initial_references("RTCurrent"));
            org.omg.RTCORBA.Current rtcur = org.omg.RTCORBA.CurrentHelper.narrow(orb.resolve_initial_references("RTCurrent"));
            System.out.println("\n\tPriority: " + priority);

            short servant_thread_priority = rtcur.the_priority();

            // Print out the info.
            if (servant_thread_priority != priority)
                System.out.println( "ERROR: servant thread priority is not equal to method argument.");

            System.out.println("\n\tClient priority: " + priority + " Servant thread priority: " + servant_thread_priority);
            System.out.println();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

