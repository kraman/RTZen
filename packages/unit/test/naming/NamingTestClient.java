package unit.test.naming;

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
 * This class implements a simple Client for the CDR test
 *
 * @author <a href="mailto:yuez@doc.ece.uci.edu">Yue Zhang</a>
 * @version 1.0
 */

public class NamingTestClient extends RealtimeThread
{

	private HelloWorld stub;
	protected NamingContextExt nameService;

	public static void main( String[] args ){
		try{
			//CDRTestClient rt = (CDRTestClient)( new RealtimeThread (null, null, null, new LTMemory( 3000, 300000), null, null));
			RealtimeThread rt = (CDRTestClient) ImmortalMemory.instance().newInstance( CDRTestClient.class );
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

			BufferedReader br = new BufferedReader (new FileReader( "ior.txt" ));
			String ior = br.readLine();

			org.omg.CORBA.Object obj = orb.string_to_object( ior );
			stub = HelloWorldHelper.unchecked_narrow( obj );

			nameService = orb.resolve_initial_references("NameService");

			testToName();



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



}


