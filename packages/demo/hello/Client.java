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
        RealtimeThread rtThread = new RealtimeThread( (Runnable)new Client() );
        rtThread.start();
    }

    public void run()
    {
        System.out.println( "In Client run()" );
        try
        {
            System.out.println( "going to do ORB.init(...)" );
            ORB orb = ORB.init((String[])null, null);
            System.out.println( "got an ORB at "+orb );
            String ior = "";
            File iorfile = new File( "/home/kraman/RTZen/packages/demo/hello/ior.txt" );
            System.out.println( iorfile );
            BufferedReader br = new BufferedReader( new FileReader(iorfile) );
            ior = br.readLine();
            System.out.println("[Client] " + ior);
            System.out.println( "going to do string_to_object" );
            org.omg.CORBA.Object object = orb.string_to_object(ior);
            System.out.println( "got object : " + object );
            //System.out.println( orb.object_to_string( object ) );
            HelloWorld server = HelloWorldHelper.unchecked_narrow(object);

            for( int i=0;i<10000;i++ )
                server.getMessage();

            long start = System.currentTimeMillis();
            for( int i=0;i<10000;i++ )
                server.getMessage();
            long end = System.currentTimeMillis();
            System.out.println( ((end-start)/1000) + " sec = " + (10000.0/((end-start)/1000.0)) + " req/sec" );
            System.exit(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
    }
}
