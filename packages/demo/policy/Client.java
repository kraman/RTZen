package demo.policy;

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
        System.out.println( "=====================Creating RT Thread in client==========================" );
        RealtimeThread rt = new RealtimeThread((Object)null,(Object)null,(Object)null,new LTMemory(3000,30000),(Object)null,new Client());
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
            File iorfile = new File( "ior.txt" );
            BufferedReader br = new BufferedReader( new FileReader(iorfile) );
            ior = br.readLine();
            org.omg.CORBA.Object object = orb.string_to_object(ior);
            HelloWorld server = HelloWorldHelper.unchecked_narrow(object);
            long start = System.currentTimeMillis();
            for( int i=0;i<10000;i++ ){
                server.getMessage();
            }
            long end = System.currentTimeMillis();

            System.err.println( 10000/((end-start)/1000.0) );
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
