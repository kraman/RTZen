package demo.jpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.realtime.ImmortalMemory;
import javax.realtime.RealtimeThread;

import org.omg.CORBA.ORB;

import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.NativeTimeStamp;

/**
 * This class implements a simple Client for the Hello World CORBA
 * demo
 *
 * @version 1.0
 */

public class Client2 extends RealtimeThread
{
    private static final int A_SECOND = 1000;
    private static final int INITIAL_SLEEP = A_SECOND;
    private static final int REQUEST_SLEEP = 10;
    public static final int RUN_NUM = 10000;
    public static final int ARRAY_SIZE = 10;
    public static final int WARM_UP = 3000;

    static int[] array1 = new int[ARRAY_SIZE];

    static
    {
        for (int i = 0; i < array1.length; i++)
        {
            array1[i] = i + 10;
        }
    }

    static String fileName;

    static int id;

    /**
     * Main function.
     */
    public static void main(String[] args)
    {
        try
        {
            if (args.length < 2)
            {
                System.out.println("Usage: Client2 <iorfile> <#id>");
                System.exit(0);
            }

            fileName = args[0];
            id = Integer.parseInt(args[1]);
            System.out.println("==============Creating RT Thread in client ==============");
            RealtimeThread rt = (Client2) ImmortalMemory.instance().newInstance(Client2.class);
            System.out.println("==============Starting RT Thread in client==============");
            rt.start();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void run()
    {
        try
        {
            System.out.println("==============Calling ORB Init in client==============");
            //ORB orb = ORB.init((String[]) null, null);
            ORB orb = Client1.getORB();
            System.out.println("==============ORB Init complete in client==============");
            String ior = "";
            File iorfile = new File("ior2.txt");//new File(fileName);
            id = 2;
            BufferedReader br = new BufferedReader(new FileReader(iorfile));
            ior = br.readLine();
            System.out.println("==============IOR read==============");
            org.omg.CORBA.Object object = orb.string_to_object(ior);
            System.out.println("==============Trying to establish connection==============");
            final HelloWorld server = HelloWorldHelper.unchecked_narrow(object);

            System.out.println( "===================Trying to initialize the NativeTimeStamp================" );
            NativeTimeStamp rtts = new NativeTimeStamp();
            NativeTimeStamp.Init(1, 20.0);
            System.out.println( "===================NativeTimeStamp gets initialized================" );


            sleep(INITIAL_SLEEP);

            System.out.println("==============Warm Up 2==============");
            
            for (int i=0; i<WARM_UP; i++){
                NativeTimeStamp.RecordTime(20);
                server.getMessage(id, array1);
                NativeTimeStamp.RecordTime(20);
                sleep(REQUEST_SLEEP);

                if (i != 0 && i % 500 == 0)
                {
                    Logger.write(i);
                    Logger.writeln();
                }

 
            }


            System.out.println("==============Performance benchmark 2 ==============");
            long start = System.currentTimeMillis();
            for (int i = 0; i < RUN_NUM; i++)
            {
                //System.out.print("# ");
                //System.out.println(server.getMessage(id, array1));
                NativeTimeStamp.RecordTime(21);
                server.getMessage(id, array1);
            
                NativeTimeStamp.RecordTime(21);
                 
                sleep(REQUEST_SLEEP);

                

                if (i != 0 && i % 500 == 0)
                {
                    Logger.write(id);
                    Logger.write(i);
                    Logger.writeln();
                }
                
            }
            long end = System.currentTimeMillis();
            System.err.println((double) RUN_NUM / ((end - start) / 1000.0));

            NativeTimeStamp.OutputLogRecords();
            Runtime.getRuntime().exec("mv timeRecords.txt timeRecords.1.2.2.128.txt");


            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
