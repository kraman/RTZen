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
 * This class implements a simple CORBA client
 * @version 1.0
 */
public class Client3 extends RealtimeThread
{
    private static final String IOR1 = "ior1.txt";
    private static final String IOR2 = "ior2.txt";

    /**
     * Main function.
     */
    public static void main(String[] args)
    {
        try
        {
            RealtimeThread rt1 = (Client1) ImmortalMemory.instance().newInstance(Client1.class);
            NoHeapRealtimeThread rt2 = (Client2) ImmortalMemory.instance().newInstance(Client2.class);

            rt1.start();
            rt2.start();
            rt2.join();
            rt1.join();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
        NativeTimeStamp.OutputLogRecords();
    }
}
