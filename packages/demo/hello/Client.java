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

        //RealtimeThread rt = new RealtimeThread(null,null,null,null,null,(Runnable)new Client());

        System.out.println( "=====================Creating RT Thread in client==========================" );
        RealtimeThread rt = new RealtimeThread(null,null,null,new LTMemory(3000,300000),null,new Client());
        System.out.println( "=====================Starting RT Thread in client==========================" );

        rt.start();
    }

    public void run()
    {
        try
        {
            System.out.println( "=====================Calling ORB Init in client============================" );
            ORB orb = ORB.init((String[])null, null);
            System.out.println( "=====================ORB Init complete in client===========================" );
            String ior = "";
            File iorfile = new File( "/home/yuez/RTZen/packages/demo/hello/ior.txt" );
            BufferedReader br = new BufferedReader( new FileReader(iorfile) );
            ior = br.readLine();
            System.out.println( "===========================IOR read========================================" );
            org.omg.CORBA.Object object = orb.string_to_object(ior);
            System.out.println( "===================Trying to establish connection==========================" );
            HelloWorld server = HelloWorldHelper.unchecked_narrow(object);
            long start = System.currentTimeMillis();
	    System.out.println( server.getMessage() );
		/*
            for( int i=0;i<10000;i++ ){
            }
            long end = System.currentTimeMillis();

            System.err.println( 10000/((end-start)/1000.0) );
            System.exit(0);
		 */
	}
	catch (Exception e)
	{
		e.printStackTrace();
		System.exit(-1);
	}
    }
}
