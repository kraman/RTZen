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
    public static void main(String[] args) throws Exception
    {
        System.out.println( "=====================Creating RT Thread in client==========================" );
        RealtimeThread rt = (Client) ImmortalMemory.instance().newInstance( Client.class );
        System.out.println( "=====================Starting RT Thread in client==========================" );
        rt.start();
    }

    public Client(){
        //super(null,new LTMemory(3000,300000));
    }

    public static final int runNum = 5000;

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
            System.out.println( "===========================IOR read========================================" );
            org.omg.CORBA.Object object = orb.string_to_object(ior);
            System.out.println( "===================Trying to establish connection==========================" );
            HelloWorld server = HelloWorldHelper.unchecked_narrow(object);
            System.out.println( server.getMessage() );

            System.out.println( "====================== Performance warmup =================================" );
            for( int i=0;i<runNum;i++ ){
                server.getMessage();
                if(i % 250 == 0){
                    Logger.write(i);
                    Logger.writeln();
                }
            }

            System.out.println( "====================== Performance benchmark ==============================" );
            long start = System.currentTimeMillis();
            for( int i=0;i<runNum;i++ ){
                server.getMessage();
                if(i % 500 == 0){
                    Logger.write(i);
                    Logger.writeln();
                }
            }
            long end = System.currentTimeMillis();

            System.err.println( runNum/((end-start)/((double)runNum)) );
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
