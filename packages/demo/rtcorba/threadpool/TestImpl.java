package demo.rtcorba.threadpool;

import org.omg.PortableServer.*;
import org.omg.CORBA.ORB;
import org.omg.CORBA.*;
import javax.realtime.*;
import org.omg.RTCORBA.*;
import org.omg.Messaging.*;

/**
 * This class implements the RTCORBA threadpool demo server from TAO.
 * @author Mark Panahi
 * @version 1.0
 */

public class TestImpl extends testPOA

{
    ORB orb;
    POA poa;
    long nap_time;

    public TestImpl(ORB orb, POA poa, long nap_time){
        this.orb = orb;
        this.poa = poa;
        this.nap_time = nap_time;
    }

    /**
     * .Still in progress.
     */
    public int method (int client_id, int iteration){
        return iteration;
    }

    public void shutdown (){}

    /**
        The poa used to activate the servant when _this() is called.
        The root poa is returned by default, so we need to override this.
    */
    public POA _default_POA() {
        return poa;
    }
}

