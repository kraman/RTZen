package unit.test.naming_JacORB;

import org.omg.CORBA.*;
import javax.realtime.RealtimeThread;
import javax.realtime.LTMemory;
import javax.realtime.ImmortalMemory;
import java.io.BufferedReader;
import java.io.FileReader;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CosNaming.NamingContextExtPackage.*;

/**
 * This class implements a simple Naming test client
 *
 * @author <a href="mailto:yuez@doc.ece.uci.edu">Yue Zhang</a>
 * @version 1.0
 */

public class NamingTestClient extends RealtimeThread
{

	//private HelloWorld stub;
	protected NamingContextExt nameService;
    //protected NamingTestServer server;

	public static void main( String[] args ){
		try{
			//CDRTestClient rt = (CDRTestClient)( new RealtimeThread (null, null, null, new LTMemory( 3000, 300000), null, null));
			RealtimeThread rt = (NamingTestClient) ImmortalMemory.instance().newInstance( NamingTestClient.class );
			rt.start();
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(-1);
		}

	}

	public void run(){
		try{

			ORB orb = ORB.init((String[]) null, null);

//			BufferedReader br = new BufferedReader (new FileReader( "ior.txt" ));
//			String ior = br.readLine();

//			org.omg.CORBA.Object obj = orb.string_to_object( ior );
//			stub = HelloWorldHelper.unchecked_narrow( obj );

			org.omg.CORBA.Object nameServiceObject = orb.resolve_initial_references("NameService");

            nameService = NamingContextExtHelper.unchecked_narrow(nameServiceObject);
      //      server = new NamingTestServer();

			testToName();
            testBind();



		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit( -1 );
		}
	}

	public void testToName() throws InvalidName
		{
			NameComponent[] name;

			name = nameService.to_name("a/b/c");

			if( name.length == 3 &&
				name[0].id.equals("a") &&
				name[0].kind.equals("") &&
				name[1].id.equals("b") &&
				name[1].kind.equals("") &&
				name[2].id.equals("c") &&
				name[2].kind.equals("") ){
					System.out.println("pass the naming test");
				}
				else{
					System.out.println("didn't pass the naming test");
				}

            /*
	        name = nameService.to_name("a.b/c.d/.");

			assertTrue(
				name.length == 3 &&
				name[0].id.equals("a") &&
				name[0].kind.equals("b") &&
				name[1].id.equals("c") &&
				name[1].kind.equals("d") &&
				name[2].id.equals("") &&
				name[2].kind.equals("") );

	        name = nameService.to_name("a/./c.d/.e");

	        assertTrue(
	            name.length == 4 &&
				name[0].id.equals("a") &&
				name[0].kind.equals("") &&
				name[1].id.equals("") &&
				name[1].kind.equals("") &&
				name[2].id.equals("c") &&
				name[2].kind.equals("d") &&
	            name[3].id.equals("") &&
	            name[3].kind.equals("e") );

	        name = nameService.to_name("a/x\\/y\\/z/b");

	        assertTrue(
	            name.length == 3 &&
				name[0].id.equals("a") &&
				name[0].kind.equals("") &&
				name[1].id.equals("x/y/z") &&
				name[1].kind.equals("") &&
				name[2].id.equals("b") &&
				name[2].kind.equals("") );

	        name = nameService.to_name("a\\.b.c\\.d/e.f");

	        assertTrue(
	            name.length == 2 &&
				name[0].id.equals("a.b") &&
				name[0].kind.equals("c.d") &&
				name[1].id.equals("e") &&
				name[1].kind.equals("f") );

	        name = nameService.to_name("a/b\\\\/c");

	        assertTrue(
	            name.length == 3 &&
				name[0].id.equals("a") &&
				name[0].kind.equals("") &&
				name[1].id.equals("b\\") &&
				name[1].kind.equals("") &&
				name[2].id.equals("c") &&
				name[2].kind.equals("") );

	        try
	        {
	            name = nameService.to_name("a/");

	            fail("Name service should have thrown an exception but didn't");
	        }
	        catch (InvalidName e)
	        {
	            // Exception is supposed to happen; continue
	        }

	        try
	        {
	            name = nameService.to_name("/a");

	            fail("Name service should have thrown an exception but didn't");
	        }
	        catch (InvalidName e)
	        {
	            // Exception is supposed to happen; continue
	        }

	        try
	        {
	            name = nameService.to_name("a.");

	            fail("Name service should have thrown an exception but didn't");
	        }
	        catch (InvalidName e)
	        {
	            // Exception is supposed to happen; continue
	        }

	        try
	        {
	            name = nameService.to_name("");

	            fail("Name service should have thrown an exception but didn't");
	        }
	        catch (InvalidName e)
	        {
                // Exception is supposed to happen; continue
            }*/
        }
    public void testBind() throws Exception
    {
        String prefix = "hello";
/*
        // Bind three servants
        for (int i = 0; i < 3; i++)
        {
            ((NamingTestServer)server).bindHello(nameService, prefix, i);
        }
*/
        // Resolve the three servants and confirm that their identities are correct
        for (int i = 0; i < 3; i++)
        {
            System.out.println("The naming is "+(nameService.to_name(prefix + i))[0]);
            org.omg.CORBA.Object helloObject = nameService.resolve(nameService.to_name(prefix + i)); 
            System.out.println("The object get is "+helloObject);
            HelloWorld hello = HelloWorldHelper.unchecked_narrow( helloObject );
            System.out.println(hello);
            System.out.println(hello.id());            
            if(hello.id() != i)
                System.out.println("TestBind()failed!");
            else{
                System.out.println("TestBind() succeed with id "+i);
            }

        }
        // Unbind the servants one at a time and check that they no longer exist in the name service
        for (int i = 0; i < 3; i++)
        {
            nameService.unbind( nameService.to_name(prefix + i) );

            try
            {
                nameService.resolve( nameService.to_name(prefix + i) );

                System.err.println("The name service should have thrown a NotFound exception but didn't");
            }
            catch (NotFound e)
            {
                // Exception is supposed to happen; continue
            }

        }
    }
}
