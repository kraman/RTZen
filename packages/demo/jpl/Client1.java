package demo.jpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.realtime.ImmortalMemory;
import javax.realtime.RealtimeThread;

import org.omg.CORBA.ORB;

import edu.uci.ece.zen.utils.Logger;

/**
 * This class implements a simple CORBA client
 * @version 1.0
 */
public class Client1 extends RealtimeThread
{
    private static final int A_SECOND = 1000;
    private static final int INITIAL_SLEEP = A_SECOND;
    private static final int REQUEST_SLEEP = 100;
    public static final int RUN_NUM = 50000;
    public static final int ARRAY_SIZE = 200;

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
                System.out.println("Usage: Client1 <iorfile> <#id>");
                System.exit(0);
            }

            fileName = args[0];
            id = Integer.parseInt(args[1]);
            System.out.println("==============Creating RT Thread in client ==============");
            RealtimeThread rt = (Client1) ImmortalMemory.instance().newInstance(Client1.class);
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
            ORB orb = ORB.init((String[]) null, null);
            System.out.println("==============ORB Init complete in client==============");
            String ior = "";
            File iorfile = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(iorfile));
            ior = br.readLine();
            System.out.println("==============IOR read==============");
            org.omg.CORBA.Object object = orb.string_to_object(ior);
            System.out.println("==============Trying to establish connection==============");
            final HelloWorld server = HelloWorldHelper.unchecked_narrow(object);

            sleep(INITIAL_SLEEP);

            System.out.println("==============Performance benchmark==============");
            long start = System.currentTimeMillis();
            for (int i = 0; i < RUN_NUM; i++)
            {
                {
                    System.out.print("# ");
                    System.out.println(server.getMessage(id, array1));
                    sleep(REQUEST_SLEEP);
                }

                sleep(REQUEST_SLEEP);

                if (i != 0 && i % 500 == 0)
                {
                    Logger.write(i);
                    Logger.writeln();
                }
            }
            long end = System.currentTimeMillis();
            System.err.println((double) RUN_NUM / ((end - start) / 1000.0));

            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }



}