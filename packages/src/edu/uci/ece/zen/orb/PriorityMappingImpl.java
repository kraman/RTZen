/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import javax.realtime.RealtimeThread;
import org.omg.RTCORBA.PriorityMapping;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;
import edu.uci.ece.zen.utils.Logger;
import org.omg.RTCORBA.maxPriority;
import org.omg.RTCORBA.minPriority;
import javax.realtime.PriorityScheduler;

public class PriorityMappingImpl extends PriorityMapping {
    
    private static final int MAX_PRIORITY = PriorityScheduler.instance().getMaxPriority();
    private static final int MIN_PRIORITY = PriorityScheduler.instance().getMinPriority();

    public boolean to_native(short corba_priority,
            org.omg.CORBA.ShortHolder native_priority) {
                
        if (corba_priority < minPriority.value 
                || corba_priority > maxPriority.value) 
            return false;
        
        native_priority.value = toNative(corba_priority);

        return true;
    }
    
    public static short toNative(short corba_priority) {

        short nativePriority;
        if (ZenBuildProperties.dbgORB) ZenProperties.logger.log("corba_priority: "
                        + corba_priority);             
        
        if (corba_priority < minPriority.value || corba_priority > maxPriority.value){
            ZenProperties.logger.log(Logger.WARN, PriorityMappingImpl.class, "toNative", "Cannot map requested priority. Assigning lowest value.");
            nativePriority = (short) MIN_PRIORITY;  //TODO: make sure number doesn't change b/c of conversion
        }else{
            nativePriority = (short) Math.round((corba_priority * (MAX_PRIORITY - MIN_PRIORITY)) / (double)maxPriority.value + MIN_PRIORITY);
        }
        
        if (ZenBuildProperties.dbgORB) ZenProperties.logger.log("native result: "
                        + nativePriority);
        
        return nativePriority;
    }

    public boolean to_CORBA(short native_priority,
            org.omg.CORBA.ShortHolder corba_priority) {

        if (native_priority < MIN_PRIORITY
                || native_priority > MAX_PRIORITY) 
            return false;

        corba_priority.value = toCORBA(native_priority);

        return true;                
    }
    
    public static short toCORBA(short native_priority) {

        short corbaPriority;
        if (ZenBuildProperties.dbgORB) ZenProperties.logger.log("native_priority: "
                        + native_priority);   
        
        if (native_priority < MIN_PRIORITY || native_priority > MAX_PRIORITY){
            ZenProperties.logger.log(Logger.WARN, PriorityMappingImpl.class, "toCORBA", "Cannot map requested priority. Assigning lowest value.");
            corbaPriority =  minPriority.value;
        }else{     
            corbaPriority = (short) Math.round( ((double) (native_priority - MIN_PRIORITY) / (MAX_PRIORITY - MIN_PRIORITY))  * maxPriority.value );
        }

        if (ZenBuildProperties.dbgORB) ZenProperties.logger.log("corba result: "
                        + corbaPriority);      
        
        return corbaPriority;
    }
}
