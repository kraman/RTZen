package demo.jpl;

import java.io.BufferedWriter;
import java.io.FileWriter;

import javax.realtime.ImmortalMemory;
import javax.realtime.LTMemory;
import javax.realtime.PriorityScheduler;
import javax.realtime.RealtimeThread;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.RTCORBA.RTORB;
import org.omg.RTCORBA.RTORBHelper;

/**
 * This class implements a simple CORBA Server.
 * 
 * @author Juan Colmenares
 * @author Hojjat Jafarpour 
 * @version 1.0
 */

public class Server extends RealtimeThread
{
    private static final int ITERATION_FACTOR_2 = 1;
    private static final int ITERTATION_FACTOR_1 = 20;
    public String[] args;
    
    public static void main(String[] args) throws Exception
    {
        // System.out.println( "=====================Creating RT Thread in server==========================" );
        Server rt = (Server) ImmortalMemory.instance().newInstance( Server.class );
        // System.out.println( "=====================Starting RT Thread in server==========================" );
        rt.init( args );
        rt.start();
    }

    public Server(){
        super(null,null,null,new LTMemory(3000,300000),null,null);
    }

    public void init( String args[] ){
        this.args = args;
    }

    public void run()
    {
        try
        {
            System.out.println( "=====================Calling ORB Init in server============================" );
            ORB orb = ORB.init(args , null);
            System.out.println( "=====================ORB Init complete in server===========================" );

            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            System.out.println( "=================== RootPOA resolved, starting servant_to_ref ==============" );
            rootPOA.the_POAManager().activate();
            System.out.println( "=================== Activating POA Manager ==============" );

            HelloWorldImpl impl = new HelloWorldImpl(ITERTATION_FACTOR_1);
            org.omg.CORBA.Object obj = rootPOA.servant_to_reference(impl);
            System.out.println( "=================== Servant registered, getting IOR ========================" );
            String ior = orb.object_to_string(obj);

            System.out.println( "[Server] " + ior );
            BufferedWriter bw = new BufferedWriter( new FileWriter("ior1.txt") );
            bw.write(ior);
            bw.close();

            // Creating a child poa with a threadpool
            
            RTORB rtorb = RTORBHelper.narrow(orb.resolve_initial_references ("RTORB"));
            
            System.out.println("Max prio " + PriorityScheduler.instance().getMaxPriority());
            System.out.println("Min prio " + PriorityScheduler.instance().getMinPriority());
            System.out.println("Norm prio " + PriorityScheduler.instance().getNormPriority());
            //short priority = (short) (30 + (PriorityScheduler.getNormPriority(RealtimeThread.currentThread()))) ;
            short priority = (short) PriorityScheduler.instance().getMaxPriority();
            System.out.println("Higher priority is: " + priority);
            
            int threadPoolId = rtorb.create_threadpool(100, 50, 50, priority, false, 10, 10);            
            Policy[] policy = new Policy[1];
            policy[0] = rtorb.create_threadpool_policy(threadPoolId);
            
            System.out.println("Creating a child POA"); 
            POA childPOA = rootPOA.create_POA("childPOA", null, policy); 
            childPOA.the_POAManager().activate();
          
            HelloWorldImpl impl2 = new HelloWorldImpl(ITERATION_FACTOR_2);
            
            childPOA.activate_object(impl2);
            org.omg.CORBA.Object obj2 = childPOA.servant_to_reference(impl2);
            //System.out.println( "=================== Servant registered, getting IOR ========================" );
            String ior2 = orb.object_to_string(obj2);
            System.out.println( "[Server] " + ior2 );
            BufferedWriter bw2 = new BufferedWriter( new FileWriter("ior2.txt") );
            bw2.write(ior2);
            bw2.close();
            
            System.out.println( "RTZen is running ...." );
			orb.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
