package demo.poa2;

import java.io.*;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;

import javax.realtime.*;
import org.omg.PortableServer.*;
import org.omg.RTCORBA.RTORB;
import org.omg.RTCORBA.RTORBHelper;
import org.omg.RTCORBA.ThreadpoolIdHelper;
import org.omg.RTCORBA.ThreadpoolPolicy;

/**
 * This class implements a simple Server for the Hello World CORBA
 * demo
 *
 * @author <a href="mailto:mpanahi@doc.ece.uci.edu">Mark Panahi</a>
 * @version 1.0
 */

public class Server extends RealtimeThread
{
    String[] args;
    
    public static void main(String[] args) throws Exception
    {
        //System.out.println( "=====================Creating RT Thread in server==========================" );
        Server rt = (Server) ImmortalMemory.instance().newInstance( Server.class );
        //System.out.println( "=====================Starting RT Thread in server==========================" );
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
            //System.out.println( "=====================Calling ORB Init in server============================" );
            ORB orb = ORB.init( args , null);
            //System.out.println( "=====================ORB Init complete in server===========================" );

            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();
            //System.out.println( "=================== RootPOA resolved, starting servant_to_ref ==============" );

            HelloWorldImpl impl = new HelloWorldImpl(1);
            org.omg.CORBA.Object obj = rootPOA.servant_to_reference(impl);
            //System.out.println( "=================== Servant registered, getting IOR ========================" );
            String ior = orb.object_to_string(obj);

            System.out.println("Running Hello World Example... ");
            System.out.println( "[Server] " + ior );

            BufferedWriter bw = new BufferedWriter( new FileWriter("ior.txt") );
            bw.write(ior);
            bw.close();

            // Creating a child poa with a threadpool
            
            RTORB rtorb = RTORBHelper.narrow(orb.resolve_initial_references ("RTORB"));
            
            System.out.println("Max prio " + PriorityScheduler.MAX_PRIORITY);
            System.out.println("Min prio " + PriorityScheduler.MIN_PRIORITY);
            System.out.println("Norm prio " + PriorityScheduler.getNormPriority(RealtimeThread.currentThread()));
            short priority = (short) (10 + (PriorityScheduler.getNormPriority(RealtimeThread.currentThread()))) ;
            priority = (short) PriorityScheduler.MAX_PRIORITY;
            
            int threadPoolId = rtorb.create_threadpool(100, 50, 50, priority, false, 10, 10);            
            Policy[] policy = new Policy[1];
            policy[0] = rtorb.create_threadpool_policy(threadPoolId);
//            policy[0] = rootPOA.create_thread_policy(ThreadPolicyValue.SINGLE_THREAD_MODEL);
            
            System.out.println("Creatin' a child POA"); 
            POA childPOA = rootPOA.create_POA("childPOA", null, policy); 
            childPOA.the_POAManager().activate();
          
            HelloWorldImpl impl2 = new HelloWorldImpl(2);
            
            
            childPOA.activate_object(impl2);
            org.omg.CORBA.Object obj2 = childPOA.servant_to_reference(impl2);
            //System.out.println( "=================== Servant registered, getting IOR ========================" );
            String ior2 = orb.object_to_string(obj2);

            System.out.println("Running Hello World Example... ");
            System.out.println( "[Server] " + ior2 );

            BufferedWriter bw2 = new BufferedWriter( new FileWriter("ior2.txt") );
            bw2.write(ior2);
            bw2.close();
            
            
            // Creating a another child poa
//            System.out.println("Creatin' another child POA"); 
//            POA childPOA3 = childPOA.create_POA("OtherchildPOA", null, null); 
//          
//            HelloWorldImpl impl3 = new HelloWorldImpl();
//            org.omg.CORBA.Object obj3  = childPOA3.servant_to_reference(impl3);
//            //System.out.println( "=================== Servant registered, getting IOR ========================" );
//            String ior3 = zen.object_to_string(obj3);
//
//            System.out.println("Running Hello World Example... ");
//            System.out.println( "[Server] " + ior3 );
//
//            BufferedWriter bw3 = new BufferedWriter( new FileWriter("ior3.txt") );
//            bw3.write(ior3);
//            bw3.close();
            
            //System.out.println( "============================ ZEN.run() ====================================" );
			orb.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
