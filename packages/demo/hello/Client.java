package demo.hello;

import java.io.*;

import org.omg.CORBA.ORB;

/**
 * This class implements a simple Client for the Hello World CORBA
 * demo
 *
 * @author <a href="mailto:mpanahi@doc.ece.uci.edu">Mark Panahi</a>
 * @version 1.0
 */

public class Client
{
    public static void main(String[] args)
    {
        try
        {
            ORB orb = ORB.init((String[])null, null);
            String ior = "";
            File iorfile = new File( "/home/kraman/RTZen/packages/demo/hello/ior.txt" );
            System.out.println( iorfile );
            BufferedReader br = new BufferedReader( new FileReader(iorfile) );
            ior = br.readLine();
            System.out.println("[Client] " + ior);
            org.omg.CORBA.Object object = orb.string_to_object(ior);
            System.out.println( orb.object_to_string( object ) );
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
