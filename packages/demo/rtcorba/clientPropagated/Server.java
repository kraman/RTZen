package demo.rtcorba.clientPropagated;

import java.io.*;
import edu.uci.ece.zen.utils.ZenProperties;

import org.omg.CORBA.ORB;
import javax.realtime.*;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;

import org.omg.RTCORBA.RTORB;
import org.omg.RTCORBA.RTORBHelper;
import org.omg.RTCORBA.ThreadpoolLane;

public class Server extends RealtimeThread
{
    String[] args;

    public static void main(String[] args) throws Exception
    {
        System.out.println( "=====================Creating RT Thread in server==========================" );
        Server rt = (Server) ImmortalMemory.instance().newInstance( Server.class );
        System.out.println( "=====================Starting RT Thread in server==========================" );
        rt.init( args );
        rt.start();
    }

    public Server(){
        super(null,null,null,new LTMemory(3000,300000),null,null);
        System.out.println( "====" );
    }

    public void init( String args[] ){
        this.args = args;
    }

    public void run()
    {
        try
        {
            ZenProperties.iiopMinor = 2;
            System.out.println( "=====================Calling ORB Init in server============================" );
            ORB orb = ORB.init( args , null);
            System.out.println( "=====================ORB Init complete in server===========================" );

            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            System.out.println( "=================== RootPOA resolved, starting servant_to_ref ==============" );

            POAManager poaManager = rootPOA.the_POAManager ();

            RTORB rtorb = RTORBHelper.narrow(orb.resolve_initial_references ("RTORB"));
            org.omg.RTCORBA.Current rtcur = org.omg.RTCORBA.CurrentHelper.narrow(orb.resolve_initial_references("RTCurrent"));

            // Create POA with CLIENT_PROPAGATED PriorityModelPolicy,
            // and register Test object with it.
            org.omg.CORBA.Policy [] poa_policy_list = new org.omg.CORBA.Policy[2];

            //NON Standard -- using this until we can get some other stuff working

            //edu.uci.ece.zen.orb.transport.iiop.Acceptor.enableComponents = true;
            //edu.uci.ece.zen.orb.transport.iiop.Acceptor.serverPriority = 0;
            //edu.uci.ece.zen.orb.transport.iiop.Acceptor.priorityModel = org.omg.RTCORBA.PriorityModel.CLIENT_PROPAGATED.value();

            //for standard CORBA, this would be reenabled
            poa_policy_list[0] = rtorb.create_priority_model_policy (org.omg.RTCORBA.PriorityModel.CLIENT_PROPAGATED,(short)0);


            ThreadpoolLane[] lanes = new ThreadpoolLane[3];
            for(int i = 0; i < lanes.length; ++i)
                lanes[i] = new ThreadpoolLane((short)i, 1, 1);
            int threadPoolId = rtorb.create_threadpool_with_lanes(100, lanes, false, false, 10, 10);            

            poa_policy_list[1] = rtorb.create_threadpool_policy(threadPoolId);
            
            
            //POA childPOA = rootPOA.create_POA ("Child_POA",poaManager,poa_policy_list);
            // pass null for manager for now, I think it's the same manager as root poa
            POA childPOA = rootPOA.create_POA ("Child_POA",null,poa_policy_list);


            TestImpl impl = new TestImpl(orb);

            org.omg.CORBA.Object obj = childPOA.servant_to_reference(impl);
            System.out.println( "=================== Going to create IOR ==============" );

            // Print Object IOR.
            String ior = orb.object_to_string(obj);

            System.out.println("Running clientPropagated Example server side... ");
            System.out.println( "[Server] " + ior );

            BufferedWriter bw = new BufferedWriter( new FileWriter("test.ior") );
            bw.write(ior);
            bw.close();

            // Get the initial priority of the current thread.
            short initial_thread_priority = rtcur.the_priority();

            // Run ORB Event loop.
            poaManager.activate();

            orb.run();

            System.out.println("Server ORB event loop finished.");

            // Get the final priority of the current thread.
            short final_thread_priority = rtcur.the_priority();

            if (final_thread_priority != initial_thread_priority)
                System.out.println(
                        "ERROR: Priority of the servant thread " +
                        "has been permanently changed!\n" +
                        "Initial priority: "+initial_thread_priority+ " Final priority: "
                        + final_thread_priority );
            else
                System.out.println("Final priority of the servant thread" +
                        " = its initial priority");


        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}

