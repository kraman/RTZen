package demo.rtcorba.clientPropagated;

import java.io.*;

import org.omg.CORBA.ORB;
import org.omg.CORBA.*;
import javax.realtime.*;
import org.omg.RTCORBA.*;
import org.omg.Messaging.*;

/**
 * This class implements a simple Client for the Hello World CORBA
 * demo
 *
 * @author <a href="mailto:mpanahi@doc.ece.uci.edu">Mark Panahi</a>
 * @version 1.0
 */

public class Client implements Runnable
{
    String [] args;

    public static void main(String[] args)
    {
        System.out.println( "[client] =====================Creating RT Thread in client==========================" );
        RealtimeThread rt = new RealtimeThread((java.lang.Object)null,(java.lang.Object)null,(java.lang.Object)null,
                            new LTMemory(3000,30000),(java.lang.Object)null,new Client(args));
        System.out.println( "[client] =====================Starting RT Thread in client==========================" );
        rt.start();
    }

    public Client(String [] args){
        this.args = args;
    }

    public void run()
    {
        try
        {
            System.out.println( "[client] =====================Calling ORB Init in client============================" );
            ORB orb = ORB.init((String[])null, null);

            String ior = "";
            //File iorfile = new File( "ior.txt" );
            //File iorfile = new File( "C:\\ACE_wrappers\\TAO\\tests\\RTCORBA\\Client_Propagated\\Release\\test.ior");
            File iorfile = new File( "C:/ACE_wrappers/TAO/tests/RTCORBA/Client_Propagated/Release/test.ior");
            BufferedReader br = new BufferedReader( new FileReader(iorfile) );
            ior = br.readLine();
            org.omg.CORBA.Object object = orb.string_to_object(ior);
            Test server = TestHelper.unchecked_narrow(object);

            PriorityModelPolicy pmp = PriorityModelPolicyHelper.narrow(server._get_policy(PRIORITY_MODEL_POLICY_TYPE.value));
            PriorityModel pm = pmp.priority_model();

            if(pm.value() != PriorityModel._CLIENT_PROPAGATED)
                System.out.println("[client] ERROR: priority_model != RTCORBA::CLIENT_PROPAGATED!");

            System.out.println("[client] PriorityModelPolicy server priority: " + pmp.server_priority());

            //should create a better way of getting the PriorityMapping
            PriorityMapping pmap = new edu.uci.ece.zen.orb.PriorityMappingImpl();

            org.omg.RTCORBA.Current rtcur = org.omg.RTCORBA.CurrentHelper.narrow(orb.resolve_initial_references("RTCurrent"));

            short native_priority = 1;

            org.omg.CORBA.ShortHolder desired_priority = new org.omg.CORBA.ShortHolder();

            if (!pmap.to_CORBA (native_priority, desired_priority))
                System.out.println("[client] Cannot convert native priority " + native_priority + " to corba priority");

            for( int i=0;i<3;i++ ){

                rtcur.the_priority(desired_priority.value);

                if(rtcur.the_priority() != desired_priority.value)
                    System.out.println("[client] ERROR: Unable to set thread priority to " + desired_priority.value);

                server.test_method(desired_priority.value);

                desired_priority.value++;
            }
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
