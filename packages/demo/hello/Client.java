package demo.hello;

import java.io.*;

import org.omg.CORBA.ORB;
import javax.realtime.*;
import org.omg.PortableServer.*;
import edu.uci.ece.zen.utils.Logger;
/**
 * This class implements a simple Client for the Hello World CORBA
 * demo
 *
 * @author <a href="mailto:mpanahi@doc.ece.uci.edu">Mark Panahi</a>
 * @version 1.0
 */

public class Client extends RealtimeThread
{

    public static String iorfile = "ior.txt";
    static ORB orb;
    public static void main(String[] args) throws Exception
    {
        //if(args.length > 0)
        //    runNum = Integer.parseInt(args[0]);
        System.out.println( "=====================Creating RT Thread in client==========================" );
        RealtimeThread rt = (Client) ImmortalMemory.instance().newInstance( Client.class );
        System.out.println( "=====================Starting RT Thread in client==========================" );
        rt.start();
    }

    public Client(){
        //super(null,new LTMemory(3000,300000));
    }

    public static int warmupNum = 5000;
    public static int runNum = 50000;

    public void run()
    {
        try
        {

           System.out.println( "=====================Calling ORB Init in client============================" );
            if (orb == null) orb = ORB.init((String[])null, null);
            System.out.println( "=====================ORB Init complete in client===========================" );
            String ior = "";
            File iorfile2 = new File( iorfile );
            BufferedReader br = new BufferedReader( new FileReader(iorfile2) );
            ior = br.readLine();
            System.out.println( "===========================IOR read========================================" );
            org.omg.CORBA.Object object = orb.string_to_object(ior);
            System.out.println( "===================Trying to establish connection==========================" );
            final HelloWorld server = HelloWorldHelper.unchecked_narrow(object);
            //System.out.println(  );
            server.aa();

/*
            // Create a scope for running requests in, so that we don't waste the scope we are in.
            ScopedMemory sm = new LTMemory(32000, 100000);
            Runnable r = new Runnable() {
                public void run() {
                    server.aa();
                }
            };
*/
            System.out.println( "====================== Performance warmup =================================" );
            for( int i=0;i<warmupNum;i++ ){

                server.aa();
                //sleep(500);
                //sm.enter(r);
                if(i % 100 == 0){
                    Logger.write(i);
                    Logger.writeln();
                }
            }

            System.out.println( "====================== Performance benchmark ==============================" );
            long start = System.currentTimeMillis();
            for( int i=0;i<runNum;i++ ){
                server.aa();
                //sleep(500);
                //sm.enter(r);
                if(i % 500 == 0){
                    Logger.write(i);
                    Logger.writeln();
                }
            }
            long end = System.currentTimeMillis();

            System.err.println( (double)runNum/((end-start)/1000.0));
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
