package demo.rtcorba.policy;

import java.io.*;

import org.omg.CORBA.ORB;
import org.omg.CORBA.*;
import javax.realtime.*;
import org.omg.RTCORBA.*;

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
        System.out.println( "=====================Creating RT Thread in client==========================" );
        RealtimeThread rt = new RealtimeThread((java.lang.Object)null,(java.lang.Object)null,(java.lang.Object)null,
                            new LTMemory(3000,30000),(java.lang.Object)null,new Client());
        System.out.println( "=====================Starting RT Thread in client==========================" );
        rt.start();
    }

    public void run()
    {
        try
        {
            System.out.println( "=====================Calling ORB Init in client============================" );
            ORB orb = ORB.init((String[])null, null);
            System.out.println( "=====================ORB Init complete in client===========================" );

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


            System.out.println( "=====================Policy creation complete===========================" );


            String ior = "";
            File iorfile = new File( "ior.txt" );
            BufferedReader br = new BufferedReader( new FileReader(iorfile) );
            ior = br.readLine();
            org.omg.CORBA.Object object = orb.string_to_object(ior);
            HelloWorld server = HelloWorldHelper.unchecked_narrow(object);
            long start = System.currentTimeMillis();
            for( int i=0;i<10;i++ ){
                System.out.println(server.getMessage());
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
