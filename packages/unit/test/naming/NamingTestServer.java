package unit.test.naming_JacORB;

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
            }/*
            for (int i = 0; i < 3; i++)
            {
                System.out.println("Begin to resolve "+(prefix+i));
                String naming = prefix+i;
                org.omg.CORBA.Object helloObject = nameService.resolve(nameService.to_name(naming));
                System.out.println("The object is "+helloObject);
                HelloWorld hello = HelloWorldHelper.unchecked_narrow( helloObject );
                System.out.println(hello);
                System.out.println(hello.id());
                if(hello.id() != i){
                    System.out.println("TestBind()failed!");
                }
                else{
                    System.out.println("TestBind() succeed in id "+i);
                }
            }*/


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

        System.out.println("It's about bind obj to name: "+id+" "+obj);

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


