package edu.uci.ece.zen.services.naming;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import javax.realtime.RealtimeThread;
import javax.realtime.ImmortalMemory;
import javax.realtime.LTMemory;
import java.io.BufferedWriter;
import java.io.FileWriter;
import edu.uci.ece.zen.utils.ExecuteInRunnable;

/**
 * This class implements a simple Naming Service server
 *
 * @author <a href="mailto:yuez@doc.ece.uci.edu">Yue Zhang</a>
 * @version 1.0
 */

//import java.io.File;
//import java.io.FileOutputStream;

//import org.omg.CORBA.ORB;
//import org.omg.PortableServer.POA;

public class NamingService{
    

    public static void main(String[] args)
    {
        
            RealtimeThread rt = new RealtimeThread(){
                public void run(){
                    LTMemory sm = new LTMemory(100, 16*1024);
                    sm.enter(new R1());

                }
            };
            rt.start();
        

            
    }
}

 

class R1 implements Runnable{  

    public void run(){
        try{

            ORB orb = ORB.init((String [])null, null );

            POA rootPOA = POAHelper.unchecked_narrow( orb.resolve_initial_references( "RootPOA" ));
            rootPOA.the_POAManager().activate();

            System.out.println("Create and init the NamingService impl");

            NamingContextExtImpl impl = new NamingContextExtImpl();

            impl.init(rootPOA);

            System.out.println("write out the NamingService IOR");

            org.omg.CORBA.Object obj = rootPOA.servant_to_reference(impl);

            String ior = orb.object_to_string(obj);

            String fileName =
                edu.uci.ece.zen.utils.ZenProperties.getGlobalProperty(                                                "naming.ior_file.for_writing", "");

            BufferedWriter bw = new BufferedWriter( new FileWriter(fileName) );
            bw.write(ior);
            bw.close();

            orb.run();
        }
        catch(Exception e)
        {
            System.out.println("Unable to start naming service.");
            e.printStackTrace();
        }
    }
}
