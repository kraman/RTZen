/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.jpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.realtime.ImmortalMemory;
import javax.realtime.HeapMemory;
import javax.realtime.RealtimeThread;
import org.omg.CORBA.ORB;

import edu.uci.ece.zen.utils.Logger;

import edu.uci.ece.zen.utils.NativeTimeStamp;
import org.omg.RTCORBA.maxPriority;
import org.omg.RTCORBA.minPriority;

/**
 * This class implements a simple CORBA client
 * @version 1.0
 */
public class Client1 extends RealtimeThread
{
    public static ORB sharedOrb;
    private static final int A_SECOND = 1000;
    private static final int INITIAL_SLEEP = A_SECOND;
    private static final int REQUEST_SLEEP = 3;
    public static final int RUN_NUM = 10000;
    public static final int ARRAY_SIZE = 200;
    public static final int WARM_UP = 3000;
    public static final int FACTOR = 2;
    public static short priority = minPriority.value;

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

    public static synchronized ORB getORB(){
       if (sharedOrb == null)
            sharedOrb = ORB.init((String[]) null, null);
       return sharedOrb;
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

    private Object tmp;
    public void allocHeap(){
        try{
            tmp = HeapMemory.instance().newArray( byte.class , 10000 );
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void run()
    {
        try
        {
            System.out.println("==============Calling ORB Init in client 1==============");

            ORB orb = getORB();
            System.out.println("==============ORB Init complete in client 1==============");
            String ior = "";
            File iorfile = new File("ior1.txt"); //new File(fileName);
            id = 1;
            BufferedReader br = new BufferedReader(new FileReader(iorfile));
            ior = br.readLine();
            System.out.println("==============IOR read 1==============");
            org.omg.CORBA.Object object = orb.string_to_object(ior);
            System.out.println("==============Trying to establish connection 1==============");
            final HelloWorld server = HelloWorldHelper.unchecked_narrow(object);

            System.out.println( "===================Trying to initialize the NativeTimeStamp 1================" );
            NativeTimeStamp rtts = new NativeTimeStamp();
            NativeTimeStamp.Init(1, 20.0);
            System.out.println( "===================NativeTimeStamp gets initialized 1================" );

            org.omg.RTCORBA.Current rtcur =
                    org.omg.RTCORBA.CurrentHelper.narrow(
                    orb.resolve_initial_references("RTCurrent"));
            rtcur.the_priority(priority);

            sleep(INITIAL_SLEEP);

            System.out.println("==============Warm Up 1==============");
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
                allocHeap();

            }

            System.out.println("==============Performance benchmark 1==============");
            long start = System.currentTimeMillis();
            for (int i = 0; i< RUN_NUM*FACTOR; i++)
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
                allocHeap();
            }
            long end = System.currentTimeMillis();
            System.err.println((double) RUN_NUM / ((end - start) / 1000.0));

            //NativeTimeStamp.OutputLogRecords();
            //Runtime.getRuntime().exec("mv timeRecords.txt timeRecords.1.1.1.128.txt");
            System.out.println( "Client 1 complete" );
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }



}
