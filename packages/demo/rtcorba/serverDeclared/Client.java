package demo.rtcorba.serverDeclared;

import java.io.*;

import org.omg.CORBA.ORB;
import org.omg.CORBA.*;
import javax.realtime.*;
import org.omg.RTCORBA.*;
import org.omg.Messaging.*;

/**
 * This class implements the RTCORBA server declared
    priority demo client from TAO.
 * @author Mark Panahi
 * @version 1.0
 */

public class Client implements Runnable
{
    String [] args;

    public static void main(String[] args)
    {
        if(args.length < 2){
            System.out.println("need to pass in two iors");
            System.exit(-1);
        }

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

            // Test object 1.
            //File iorfile = new File( "C:/ACE_wrappers/TAO/tests/RTCORBA/Server_Declared/Release/iorfile1");
            File iorfile = new File(args[0]);
            BufferedReader br = new BufferedReader( new FileReader(iorfile) );
            String ior = br.readLine();

            Test server1 = TestHelper.unchecked_narrow(orb.string_to_object(ior));

            // Test object 2.
            iorfile = new File(args[1]);
            br = new BufferedReader( new FileReader(iorfile) );
            ior = br.readLine();

            Test server2 = TestHelper.unchecked_narrow(orb.string_to_object(ior));

            // Check that test objects are configured with SERVER_DECLARED
            // PriorityModelPolicy, and get their server priorities.

            // Test object 1
            PriorityModelPolicy pmp = PriorityModelPolicyHelper.narrow(server1._get_policy(PRIORITY_MODEL_POLICY_TYPE.value));
            PriorityModel pm = pmp.priority_model();

            if(pm.value() != PriorityModel._SERVER_DECLARED)
                System.out.println("[client] ERROR: server1 priority_model != RTCORBA::SERVER_DECLARED!");

            short server1_priority = pmp.server_priority();
            System.out.println("[client] PriorityModelPolicy server1 priority: " + server1_priority);

            // Test object 2
            pmp = PriorityModelPolicyHelper.narrow(server2._get_policy(PRIORITY_MODEL_POLICY_TYPE.value));
            pm = pmp.priority_model();

            if(pm.value() != PriorityModel._SERVER_DECLARED)
                System.out.println("[client] ERROR: server2 priority_model != RTCORBA::SERVER_DECLARED!");

            short server2_priority = pmp.server_priority();
            System.out.println("[client] PriorityModelPolicy server2 priority: " + server2_priority);

            // Testing: make several invocations on test objects.
            for (int i = 0; i < 5; ++i)
            {
              server1.test_method (server1_priority);

              server2.test_method (server2_priority);
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
