package edu.uci.ece.zen.orb;

import javax.realtime.RealtimeThread;
import org.omg.RTCORBA.PriorityMapping;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;

public class PriorityMappingImpl extends PriorityMapping {

    public boolean to_native(short corba_priority,
            org.omg.CORBA.ShortHolder native_priority) {

        if (corba_priority < 0 || corba_priority > 32767) return false;

        //int range = RealtimeThread.MAX_PRIORITY -
        // RealtimeThread.MIN_PRIORITY;

        //double fract = (double)corba_priority/32767;

        //int intval = (int)((corba_priority*(RealtimeThread.MAX_PRIORITY -
        // RealtimeThread.MIN_PRIORITY))/32767.0 + RealtimeThread.MIN_PRIORITY);

        //System.out.println("intval: " + intval);

        native_priority.value = (short) ((corba_priority * (RealtimeThread.MAX_PRIORITY - RealtimeThread.MIN_PRIORITY)) / 32767.0 + RealtimeThread.MIN_PRIORITY);

        if (ZenBuildProperties.dbgORB) ZenProperties.logger.log(/* "range: " + range + " fract: " + fract + */" native_priority.value: "
                        + native_priority.value);
        return true;
    }

    public boolean to_CORBA(short native_priority,
            org.omg.CORBA.ShortHolder corba_priority) {
        if (native_priority < RealtimeThread.MIN_PRIORITY
                || native_priority > RealtimeThread.MAX_PRIORITY) return false;

        //double fract = (double)(native_priority -
        // RealtimeThread.MIN_PRIORITY)/(RealtimeThread.MAX_PRIORITY -
        // RealtimeThread.MIN_PRIORITY);

        corba_priority.value = (short) (((double) (native_priority - RealtimeThread.MIN_PRIORITY) / (RealtimeThread.MAX_PRIORITY - RealtimeThread.MIN_PRIORITY)) * 32767);

        if (ZenBuildProperties.dbgORB) ZenProperties.logger.log(/* "range: " + range + " fract: " + fract + */" corba_priority.value: "
                        + corba_priority.value);

        return true;
    }
}
