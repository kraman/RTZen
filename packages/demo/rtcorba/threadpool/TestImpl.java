package demo.rtcorba.threadpool;

import org.omg.PortableServer.*;
import org.omg.CORBA.ORB;
import org.omg.CORBA.*;
import javax.realtime.*;
import org.omg.RTCORBA.*;
import org.omg.Messaging.*;

/**
 * This class implements the simple Hello World server.
 * @author Angelo Corsaro
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
     * Gets a message from the Hello World Server.
     */
    public int method (int client_id, int iteration){
        //return "Hello To the Zen World!!!";
        //System.out.println("Priority: " + priority);

        return iteration;
    }

    public void shutdown (){}

    public POA _default_POA() {
        return poa;
    }
}

