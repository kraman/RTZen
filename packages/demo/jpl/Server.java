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
import org.omg.PortableServer.POAManager;
import org.omg.RTCORBA.RTORB;
import org.omg.RTCORBA.RTORBHelper;
import org.omg.RTCORBA.ThreadpoolLane;
import org.omg.RTCORBA.maxPriority;
import org.omg.RTCORBA.minPriority;

/**
 * This class implements a simple CORBA Server.
 * 
 * @author Juan Colmenares
 * @author Hojjat Jafarpour 
 * @author Mark Panahi 
 * @version 1.0
 */

public class Server extends RealtimeThread
{
    private static final int ITERATION_FACTOR_2 = 1;
    private static final int ITERATION_FACTOR_1 = 20;
    public String[] args;
    private static boolean isClientPropagated = true;
    ORB orb;
    
    public static void main(String[] args) throws Exception
    {
        if(args.length == 0){
            System.out.println( "Need to specify \"sd\" for server declared or \"cp\" for client propagated." );
            System.exit(-1);
        }
        
        if(args[0].equals("sd"))
            isClientPropagated = false;
        else if (args[0].equals("cp"))
            isClientPropagated = true;
        else{
            System.out.println( "Need to specify \"sd\" for server declared or \"cp\" for client propagated." );
            System.out.println( "You passed in: " + args[0]);
            System.exit(-1);
        }
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
            //edu.uci.ece.zen.utils.IntHashtable.main(null);
            System.out.println( "=====================Calling ORB Init in server============================" );
            orb = ORB.init(args , null);
            System.out.println( "=====================ORB Init complete in server===========================" );

            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            System.out.println( "=================== RootPOA resolved, starting servant_to_ref ==============" );
            
            POAManager poaManager = rootPOA.the_POAManager ();   
            poaManager.activate();
            System.out.println( "=================== Activated POA Manager ==============" );

            // Creating a child poa with a threadpool
            
            RTORB rtorb = RTORBHelper.narrow(orb.resolve_initial_references ("RTORB"));
            
            System.out.println("Max prio " + PriorityScheduler.instance().getMaxPriority());
            System.out.println("Min prio " + PriorityScheduler.instance().getMinPriority());
            System.out.println("Norm prio " + PriorityScheduler.instance().getNormPriority());
            //short priority = (short) (30 + (PriorityScheduler.getNormPriority(RealtimeThread.currentThread()))) ;
            //short priority = (short) PriorityScheduler.instance().getMaxPriority();
            //System.out.println("Higher priority is: " + priority);
            
            int threadPoolId;
            Policy[] policy;
            
            if(isClientPropagated){
                System.out.println("Using client-propagated policy.....");
                policy = new Policy[2];
                policy[0] = rtorb.create_priority_model_policy (
                        org.omg.RTCORBA.PriorityModel.CLIENT_PROPAGATED,
                        (short)0);
                        
                ThreadpoolLane[] lanes = new ThreadpoolLane[2];
                lanes[0] = new ThreadpoolLane(minPriority.value, 1, 0);
                lanes[1] = new ThreadpoolLane(maxPriority.value, 1, 0);
                    
                threadPoolId = rtorb.create_threadpool_with_lanes(10, lanes, false, false, 10, 10);
                policy[1] = rtorb.create_threadpool_policy(threadPoolId);
            }else{
                System.out.println("Using server-declared policy.....");    
                threadPoolId = rtorb.create_threadpool(
                        0,//stacksize,
                        1,//static_threads,
                        0,//dynamic_threads,
                        maxPriority.value,//default_thread_priority,
                        false,//allow_request_buffering,
                        0,//max_buffered_requests,
                        0//max_request_buffer_size
                        );                
                //threadPoolId = rtorb.create_threadpool(10, 5, 5, maxPriority.value, false, 0, 10);            
                policy = new Policy[1];
                policy[0] = rtorb.create_threadpool_policy(threadPoolId);                
            }
            
            System.out.println("Creating a child POA"); 
            POA childPOA = rootPOA.create_POA("childPOA", poaManager, policy); 

            if(isClientPropagated){
                createObj(ITERATION_FACTOR_1, childPOA, "ior1.txt");
            }else{
                createObj(ITERATION_FACTOR_1, rootPOA, "ior1.txt");
            }
            
            createObj(ITERATION_FACTOR_2, childPOA, "ior2.txt");

            System.out.println( "RTZen is running ...." );
			orb.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    private void createObj(int iFactor, POA poa, String iorFile){
        try{
            HelloWorldImpl impl = new HelloWorldImpl(iFactor);
            org.omg.CORBA.Object obj = poa.servant_to_reference(impl);
            System.out.println( "=================== Servant registered, getting IOR ========================" );
            String ior = orb.object_to_string(obj);

            System.out.println( "[Server] " + ior );
            BufferedWriter bw = new BufferedWriter( new FileWriter(iorFile) );
            bw.write(ior);
            bw.close();

        }catch(Exception e){
            e.printStackTrace();
            System.exit(-1);            
        }
            
    }
}
