package demo.hello;

import java.io.*;

import org.omg.CORBA.ORB;
import javax.realtime.*;

/**
 * This class implements a simple Client for the Hello World CORBA
 * demo
 *
 * @author <a href="mailto:mpanahi@doc.ece.uci.edu">Mark Panahi</a>
 * @version 1.0
 */

public class Client implements Runnable
{
    public static void main(String[] args)
    {
        RealtimeThread rt = new RealtimeThread(new Client());
        rt.start();
    }

    public void run()
    {
        System.out.println( "In Client run()" );
        try
        {
            System.out.println( Thread.currentThread() + " Client.run 1" );
            ORB orb = ORB.init((String[])null, null);
            System.out.println( Thread.currentThread() + " Client.run 2" );
            String ior = "";
            System.out.println( Thread.currentThread() + " Client.run 3" );
            File iorfile = new File( "/home/kraman/RTZen/packages/demo/hello/ior.txt" );
            System.out.println( Thread.currentThread() + " Client.run 4" );
            System.out.println( iorfile );
            System.out.println( Thread.currentThread() + " Client.run 5" );
            BufferedReader br = new BufferedReader( new FileReader(iorfile) );
            System.out.println( Thread.currentThread() + " Client.run 6" );
            ior = br.readLine();
            System.out.println( Thread.currentThread() + " Client.run 7" );
            System.out.println("[Client] " + ior);
            System.out.println( Thread.currentThread() + " Client.run 8" );
            System.out.println( "going to do string_to_object" );
            System.out.println( Thread.currentThread() + " Client.run 9" );
            org.omg.CORBA.Object object = orb.string_to_object(ior);
            System.out.println( Thread.currentThread() + " Client.run 10" );
            System.out.println( "got object : " + object );
            System.out.println( Thread.currentThread() + " Client.run 11" );
            //System.out.println( orb.object_to_string( object ) );
            System.out.println( Thread.currentThread() + " Client.run 12" );
            HelloWorld server = HelloWorldHelper.unchecked_narrow(object);
            System.out.println( Thread.currentThread() + " Client.run 13" );
            server.getMessage();
            System.out.println( Thread.currentThread() + " Client.run 14" );
            server.getMessage();
            System.out.println( Thread.currentThread() + " Client.run 15" );
            System.exit(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
    }
}
