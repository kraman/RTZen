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
            testToString();
            testToURL();
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
            System.out.println("pass the naming test for a/b/c");
        }
        else{
            System.out.println("didn't pass the naming test for a/b/c");
        }

        name = nameService.to_name("a.b/c.d/.");

       if(
                name.length == 3 &&
                name[0].id.equals("a") &&
                name[0].kind.equals("b") &&
                name[1].id.equals("c") &&
                name[1].kind.equals("d") &&
                name[2].id.equals("") &&
                name[2].kind.equals("") ){
           System.out.println("pass the naming test for a.b/c.d/. ");
       }
       else{
           System.out.println("didn't pass the naming test for a.b/c.d/.");
       }                

        name = nameService.to_name("a/./c.d/.e");

        if(
                name.length == 4 &&
                name[0].id.equals("a") &&
                name[0].kind.equals("") &&
                name[1].id.equals("") &&
                name[1].kind.equals("") &&
                name[2].id.equals("c") &&
                name[2].kind.equals("d") &&
                name[3].id.equals("") &&
                name[3].kind.equals("e") ){
            System.out.println("pass the naming test for a/./c.d/.e ");
        }
        else{
            System.out.println("didn't pass the naming test for a/./c.d/.e");
        }


        name = nameService.to_name("a/x\\/y\\/z/b");

        if(
                name.length == 3 &&
                name[0].id.equals("a") &&
                name[0].kind.equals("") &&
                name[1].id.equals("x/y/z") &&
                name[1].kind.equals("") &&
                name[2].id.equals("b") &&
                name[2].kind.equals("") ){
            System.out.println("pass the naming test for  /x\\/y\\/z/b");
        }
        else{
            System.out.println("didn't pass the naming test for a/x\\/y\\/z/b");
        }


        name = nameService.to_name("a\\.b.c\\.d/e.f");

        if(
                name.length == 2 &&
                name[0].id.equals("a.b") &&
                name[0].kind.equals("c.d") &&
                name[1].id.equals("e") &&
                name[1].kind.equals("f") ){
            System.out.println("pass the naming test for  a\\.b.c\\.d/e.f");
        }
        else{
            System.out.println("didn't pass the naming test for a\\.b.c\\.d/e.f");
        }

/*
        name = nameService.to_name("a/b\\\\/c");

        if(
                name.length == 3 &&
                name[0].id.equals("a") &&
                name[0].kind.equals("") &&
                name[1].id.equals("b\\") &&
                name[1].kind.equals("") &&
                name[2].id.equals("c") &&
                name[2].kind.equals("") ){
            System.out.println("pass the naming test for a/b\\\\/c");
        }
        else{
            System.out.println("didn't pass the naming test for a/b\\\\/c");
        }                

        try
        {
            name = nameService.to_name("a/");

            System.err.println("The exception hasn't been supported in RTZen yet. Name service should have thrown an exception about a/ but didn't");
        }
        catch (InvalidName e)
        {
            // Exception is supposed to happen; continue
        }
/*
        try
        {
            name = nameService.to_name("/a");

            System.err.println("The exception hasn't been supported in RTZen yet. Name service should have thrown an exception about /a but didn't");
        }
        catch (InvalidName e)
        {
            // Exception is supposed to happen; continue
        }

        try
        {
            name = nameService.to_name("a.");

            System.err.println("The exception hasn't been supported in RTZen yet. Name service should have thrown an exception about a. but didn't");
        }
        catch (InvalidName e)
        {
            // Exception is supposed to happen; continue
        }

        try
        {
            name = nameService.to_name("");

            System.err.println("The exception hasn't been supported in RTZen yet. Name service should have thrown an exception about \" \" but didn't");
        }
        catch (InvalidName e)
        {
            // Exception is supposed to happen; continue
        }

 */
    }
    public void testToString() throws InvalidName
    {
        NameComponent[] nameComponent;

        nameComponent = new NameComponent[1];
        nameComponent[0] = new NameComponent("id", "kind");
        if( nameService.to_string(nameComponent).equals("id.kind") ){
            System.out.println("Pass the testToString() id.kind");
        }
        else{
            System.out.println("Didn't pass the testToString() id.kind");
        }

        


        nameComponent = new NameComponent[2];
        nameComponent[0] = new NameComponent("id", "kind");
        nameComponent[1] = new NameComponent("id", "kind");
        if( nameService.to_string(nameComponent).equals("id.kind/id.kind") ){
            System.out.println("Pass the testToString() id.kind/id.kind");
        }
        else{
            System.out.println("Didn't pass the testToString() id.kind/id.kind");
        }


        nameComponent = new NameComponent[1];
        nameComponent[0] = new NameComponent("id.", "/kind\\");
        if( nameService.to_string(nameComponent).equals("id\\..\\/kind\\\\") ){
            System.out.println("Pass the testToString() id\\..\\/kind\\\\");
        }
        else{
            System.out.println("Didn't pass the testToString() id\\..\\/kind\\\\");
        }

        /*
           try
           {
           nameService.to_string(new NameComponent[0]);

           fail("Name service should have thrown an exception but didn't");
           }
           catch (InvalidName e)
           {
        // Exception is supposed to happen; continue
        }*/
    }
    public void testToURL() throws InvalidName, InvalidAddress
    {
        String url;

        url = nameService.to_url(":myhost.555xyz.com/a/b/c","");
        System.out.println("url.toLowerCase() is "+url.toLowerCase());

        if( url.toLowerCase().equals("corbaloc:%3amyhost.555xyz.com/id.%5c%2fkind") ){
            System.out.println("Pass the testToURL() corbaloc:%3amyhost.555xyz.com/id.%5c%2fkind");
        }
        else{
            System.out.println("Didn't pass the testToURL() corbaloc:%3amyhost.555xyz.com/id.%5c%2fkind");
        }

        /*
           try
           {
           url = nameService.to_url("", "id.kind");

           fail("Name service should have thrown an exception but didn't");
           }
           catch (InvalidAddress e)
           {
        // Exception is supposed to happen; continue
        }*/
    }
    public void testBind() //throws Exception
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
            try{
                org.omg.CORBA.Object helloObject = nameService.resolve(nameService.to_name(prefix + i)); 
                System.out.println("The object get is "+helloObject);
                HelloWorld hello = HelloWorldHelper.unchecked_narrow( helloObject );
                System.out.println(hello);
                System.out.println(hello.id());            
                if(hello.id() != i)
                    System.out.println("TestBind()failed with id"+i);
                else{
                    System.out.println("TestBind() succeed with id "+i);
                }
            }catch(Exception ex){
                ex.printStackTrace();
                System.exit(-1);
            }



        }
        // Unbind the servants one at a time and check that they no longer exist in the name service
        for (int i = 0; i < 3; i++)
        {
            try
            {

                nameService.unbind( nameService.to_name(prefix + i) );

                nameService.resolve( nameService.to_name(prefix + i) );

                System.err.println("The exception hasn't been supported in RTZen yet. The name service should have thrown a NotFound exception but didn't");
            }
            catch (NotFound e)
            {
                System.out.println("Catched the NotFound exception");
                // Exception is supposed to happen; continue
            }
            catch(Exception ex){
                ex.printStackTrace();
                System.exit(-1);
            }


        }
    }
}
