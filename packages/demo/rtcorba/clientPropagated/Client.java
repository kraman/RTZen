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
    public static void main(String[] args)
    {
        System.out.println( "[client] =====================Creating RT Thread in client==========================" );
        RealtimeThread rt = new RealtimeThread((java.lang.Object)null,(java.lang.Object)null,(java.lang.Object)null,
                            new LTMemory(3000,30000),(java.lang.Object)null,new Client());
        System.out.println( "[client] =====================Starting RT Thread in client==========================" );
        rt.start();
    }

    public void run()
    {
        try
        {
            System.out.println( "[client] =====================Calling ORB Init in client============================" );
            ORB orb = ORB.init((String[])null, null);
            System.out.println( "[client] =====================ORB Init complete in client===========================" );

            RTORB rtorb = RTORBHelper.narrow(orb.resolve_initial_references ("RTORB"));

            ProtocolProperties tcp_properties = rtorb.create_tcp_protocol_properties (
                    64 * 1024, // send buffer
                    64 * 1024, // recv buffer
                    false, // keep alive
                    true, // dont_route
                    true); // no_delay

            Protocol[] plist = new Protocol[1];
            //plist[0].protocol_type = MY_PROTOCOL_TAG;
            //plist[0].trans_protocol_props =// Use implementation specific interface
            plist[0] = new Protocol(org.omg.IOP.TAG_INTERNET_IOP.value, null, tcp_properties);
            ClientProtocolPolicy cpp = rtorb.create_client_protocol_policy(plist);

            Policy[] policies = new Policy[1];
            policies[0] = cpp;


            PolicyManager policyManager = PolicyManagerHelper.narrow(orb.resolve_initial_references ("ORBPolicyManager"));
            //can be ADD_OVERRIDE or SET_OVERRIDE
            policyManager.set_policy_overrides(policies,SetOverrideType.ADD_OVERRIDE);


            System.out.println( "[client] =====================Policy creation complete===========================" );


            String ior = "";
            //File iorfile = new File( "ior.txt" );
            File iorfile = new File( "C:\\ACE_wrappers\\TAO\\tests\\RTCORBA\\Client_Propagated\\Release\\test.ior");
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
/*
            for(int i = 0; i < 13; ++i){
                pmap.to_CORBA ((short)(i), new org.omg.CORBA.ShortHolder());
                //pmap.to_native ((short)(i*1), new org.omg.CORBA.ShortHolder());
            }
            for(int i = 0; i < 400; ++i){
                //pmap.to_CORBA ((short)(i*1), new org.omg.CORBA.ShortHolder());
                pmap.to_native ((short)(i*100), new org.omg.CORBA.ShortHolder());
            }
*/
            short native_priority = 1;

            org.omg.CORBA.ShortHolder desired_priority = new org.omg.CORBA.ShortHolder();

            if (!pmap.to_CORBA (native_priority, desired_priority))
                System.out.println("Cannot convert native priority " + native_priority + " to corba priority");

            long start = System.currentTimeMillis();
            for( int i=0;i<1;i++ ){
                server.test_method((short)3);
            }
            long end = System.currentTimeMillis();

            System.err.println( 10000/((end-start)/1000.0) );
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
