/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.octet_test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.realtime.ImmortalMemory;
import javax.realtime.NoHeapRealtimeThread;
import javax.realtime.RealtimeThread;
import javax.realtime.PriorityParameters;
import javax.realtime.PriorityScheduler;

import org.omg.CORBA.ORB;

import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.NativeTimeStamp;
import org.omg.RTCORBA.maxPriority;
import org.omg.RTCORBA.minPriority;

/**
 * This class implements a simple Client for the Hello World CORBA
 * demo
 *
 * @version 1.0
 */

public class Client2 extends NoHeapRealtimeThread
{
    private static final int A_SECOND = 1000;
    private static final int INITIAL_SLEEP = A_SECOND;
    private static final int REQUEST_SLEEP = 10;
    public static final int RUN_NUM = 10000;
    public static final int ARRAY_SIZE = 10;
    public static final int WARM_UP = 3000;
    public static short priority = maxPriority.value;

    static byte[] array1 = new byte[ARRAY_SIZE];

    static
    {
        for (int i = 0; i < array1.length; i++)
        {
            array1[i] = (byte) (i + 10);
        }
    }

    static String fileName;

    static int id;

    public Client2(){
        super( new PriorityParameters( PriorityScheduler.instance().getMaxPriority() ) , RealtimeThread.getCurrentMemoryArea() );
    }

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

            org.omg.RTCORBA.Current rtcur =
                    org.omg.RTCORBA.CurrentHelper.narrow(
                    orb.resolve_initial_references("RTCurrent"));

            rtcur.the_priority(priority);

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
                NativeTimeStamp.RecordTime(22);
                server.getMessage(id, array1);

                NativeTimeStamp.RecordTime(22);

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

            //NativeTimeStamp.OutputLogRecords();
            //Runtime.getRuntime().exec("mv timeRecords.txt timeRecords.1.2.2.128.txt");
            System.out.println( "Client 2 complete" );
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
