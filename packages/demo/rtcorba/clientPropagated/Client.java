package demo.rtcorba.clientPropagated;

import java.io.*;

import org.omg.CORBA.ORB;
import org.omg.CORBA.*;
import javax.realtime.*;
import org.omg.RTCORBA.*;
import org.omg.Messaging.*;


/**
 * This class implements the RTCORBA client propagated
    priority demo client from TAO.
 * @author Mark Panahi
 * @version 1.0
 */

public class Client extends RealtimeThread 
{
    String [] args;

    public static void main(String[] args) throws Exception
    {
        if(args.length < 1){
            System.out.println("need to pass in an ior");
            System.exit(-1);
        }

        System.out.println( "[client] =====================Creating RT Thread in client==========================" );
        Client rt = (Client) ImmortalMemory.instance().newInstance( Client.class );
        rt.init(args);
        //RealtimeThread rt = new RealtimeThread(null,null,null,
          //                  new LTMemory(300000,300000),null,new Client());
//        RealtimeThread rt = new RealtimeThread((java.lang.Object)null,(java.lang.Object)null,(java.lang.Object)null,
//                            new LTMemory(3000,30000),(java.lang.Object)null,new Client(args));
        System.out.println( "[client] =====================Starting RT Thread in client==========================" );
        rt.start();
    }

    public void init(String [] args){
        this.args = args;
    }

    public void run()
    {
        try
        {
            System.out.println( "[client] =====================Calling ORB Init in client============================" );
            ORB orb = ORB.init((String[])null, null);
            System.out.println("[client] test 1");

            File iorfile = new File(args[0]);
            //File iorfile = new File("/project/workarea02/mpanahi/RTZen/packages/demo/rtcorba/clientPropagated/test.ior");
            System.out.println("[client] test 2");
            BufferedReader br = new BufferedReader( new FileReader(iorfile) );
            System.out.println("[client] test 2.1");
            String ior = br.readLine();
            System.out.println("[client] test 2.2");

            org.omg.CORBA.Object obj = orb.string_to_object(ior);
            System.out.println("[client] test 2.3");

            //try{Thread.currentThread().sleep(2000);}catch(Exception e){}

            Test server = TestHelper.unchecked_narrow(obj);
            System.out.println("[client] test 3");

            PriorityModelPolicy pmp = PriorityModelPolicyHelper.narrow(server._get_policy(PRIORITY_MODEL_POLICY_TYPE.value));
            PriorityModel pm = pmp.priority_model();
            System.out.println("[client] test 4");

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
            int minPrio = PriorityScheduler.instance().getMinPriority();
            for( int i=0;i<3;i++ ){

                //rtcur.the_priority(desired_priority.value);
                rtcur.the_priority((short)minPrio);

                //if(rtcur.the_priority() != desired_priority.value)
               //     System.out.println("[client] ERROR: Unable to set thread priority to " + desired_priority.value);

                //server.test_method(desired_priority.value);
                server.test_method((short)minPrio);

                //desired_priority.value++;
                minPrio++;
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
