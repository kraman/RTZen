package unit.test.naming;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import javax.realtime.RealtimeThread;
import javax.realtime.ImmortalMemory;
import javax.realtime.LTMemory;
import java.io.BufferedWriter;
import java.io.FileWriter;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

/**
 * This class implements a simple Server for the Naming Service test
 *
 * @author <a href="mailto:yuez@doc.ece.uci.edu">Yue Zhang</a>
 * @version 1.0
 */


public class NamingTestServer extends RealtimeThread{

    protected NamingContextExt nameService;
    protected POA rootPOA;

	public static void main( String[] args){
		try{
			RealtimeThread rt = (NamingTestServer) ImmortalMemory.instance().newInstance( NamingTestServer.class );
			rt.start();
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	public void run(){

		try{

			ORB orb = ORB.init((String [])null, null );

			rootPOA = POAHelper.unchecked_narrow( orb.resolve_initial_references( "RootPOA" ));
			rootPOA.the_POAManager().activate();

			//DataTypesImpl impl = new DataTypesImpl();
			//org.omg.CORBA.Object obj = rootPOA.servant_to_reference( impl );


			//String ior = orb.object_to_string( obj );
			//System.out.println ( "Running Naming Service test case..." );
			//System.out.println ( " [Server] " + ior);


            //BufferedWriter bw = new BufferedWriter( new FileWriter ( "ior.txt" ) );
            //bw.write( ior );
            //bw.close();
            org.omg.CORBA.Object nameServiceObject = orb.resolve_initial_references("NameService");

            if (nameServiceObject == null){
                System.out.println("Could not resolve reference to Name Service; check that it is running");
                System.exit(-1);
            }
            else
                nameService = NamingContextExtHelper.unchecked_narrow(nameServiceObject);
            String prefix = "hello";

            // Bind three servants
            for (int i = 0; i < 3; i++)
            {
                bindHello(nameService, prefix, i);
            }

            HelloWorld hello;

            // Bind a servant with id "1" and make sure it was bound correctly
            bindHello(nameService, prefix, 1, false, false);
            hello = HelloWorldHelper.unchecked_narrow( nameService.resolve(nameService.to_name(prefix)) );
            if(hello.id() == 1){
                System.out.println("Pass the testRebind() first try");
            }
            else{
                System.out.println("Didn't pass the testRebind() first try");
            }
            // Try to bind another servant with id "2" and make sure an AlreadyBound exception is thrown
            /*
            try
            {

                bindHello(nameService, prefix, 2, false, false);

                System.err.println("The name service should have thrown an AlreadyBound exception but didn't");
            }
            catch (AlreadyBound e)
            {
                // Exception is supposed to happen; continue
            }
*/
            // Rebind a servant with id "2" and make sure it replaces the old
            
            bindHello(nameService, prefix, 2, false, true);
            hello = HelloWorldHelper.unchecked_narrow( nameService.resolve(nameService.to_name(prefix)) );
            
            if(hello.id() == 2){
                System.out.println("Pass the testRebind() second try");
            }
            else{
                System.out.println("Didn't pass the testRebind() second try");
            }

            
            


            orb.run();
        }
        catch( Exception ex ){
            ex.printStackTrace();
            System.exit( -1 );
        }
    }
    void bindHello(NamingContextExt context, String prefix, int id, boolean useIdInName, boolean rebind) throws Exception
    {
        HelloWorldImpl impl;
        org.omg.CORBA.Object obj;

        impl = new HelloWorldImpl();
        impl.id(id);

        obj = rootPOA.servant_to_reference(impl);


        NameComponent[] name = nameService.to_name(useIdInName ? prefix + id : prefix);

        if (rebind)
            context.rebind(name, obj);
        else
            context.bind(name, obj);
    }

    void bindHello(NamingContextExt context, String prefix, int id) throws Exception
    {
        bindHello(context, prefix, id, true, false);
    }
}


