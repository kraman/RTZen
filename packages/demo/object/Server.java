package demo.object;

import java.io.*;

import org.omg.CORBA.ORB;
import javax.realtime.*;
import org.omg.PortableServer.*;

/**
 * This class implements a simple Server for the Test Object
 * demo
 *
 * @author <a href="mailto:yuez@doc.ece.uci.edu">Yue Zhang</a>
 * @version 1.0
 */

public class Server extends RealtimeThread
{
    public static void main(String[] args) throws Exception
    {
        //System.out.println( "=====================Creating RT Thread in server==========================" );
        RealtimeThread rt = (Server) ImmortalMemory.instance().newInstance( Server.class );
        //System.out.println( "=====================Starting RT Thread in server==========================" );
        rt.start();
    }
    /*
    public Server(){
        super(null,null,null,new LTMemory(3000,300000),null,null);
    }
    */

    public void run()
    {
        try
        {
            //System.out.println( "=====================Calling ORB Init in server============================" );
            ORB zen = ORB.init((String[])null, null);
            //System.out.println( "=====================ORB Init complete in server===========================" );

            POA rootPOA = POAHelper.narrow(zen.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();
            //System.out.println( "=================== RootPOA resolved, starting servant_to_ref ==============" );

            TestObjectImpl impl1 = new TestObjectImpl("Mary");
            TestObjectImpl impl2 = new TestObjectImpl("Roby");
            ExchangeObjectImpl impl3 = new ExchangeObjectImpl();
            org.omg.CORBA.Object obj1 = rootPOA.servant_to_reference(impl1);
            org.omg.CORBA.Object obj2 = rootPOA.servant_to_reference(impl2);
            org.omg.CORBA.Object obj3 = rootPOA.servant_to_reference(impl3);
                                   
            
            //System.out.println( "=================== Servant registered, getting IOR ========================" );
            String ior1 = zen.object_to_string(obj1);
            String ior2 = zen.object_to_string(obj2);
            String ior3 = zen.object_to_string(obj3);
            

            System.out.println("Running Test Object Example... ");
            System.out.println( "[TestObject Server1] " + ior1 );
            System.out.println( "[TestObject Server2] " + ior2 );
            System.out.println( "[ExchangeObject Server2] " + ior3 );      
            

            BufferedWriter bw = new BufferedWriter( new FileWriter("ior1.txt") );
            bw.write(ior1);            
            bw.close();

            bw = new BufferedWriter( new FileWriter("ior2.txt") );
            bw.write(ior2);
            bw.close();

            bw = new BufferedWriter( new FileWriter("ior3.txt") );
            bw.write(ior3);
            bw.close();


            //System.out.println( "============================ ZEN.run() ====================================" );
			zen.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
