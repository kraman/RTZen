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
import javax.realtime.RealtimeThread;
import javax.realtime.NoHeapRealtimeThread;

import org.omg.CORBA.ORB;

import edu.uci.ece.zen.utils.Logger;

import edu.uci.ece.zen.utils.NativeTimeStamp;
import org.omg.RTCORBA.maxPriority;
import org.omg.RTCORBA.minPriority;

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
        parseCmdLine(args);
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


    private static void parseCmdLine(String[] args) {

        int i = 0, j;
        String arg;
        char flag;
        boolean vflag = false;
        boolean def = false;
        String outputfile = "";

        if(args.length == 0)
            printUsage();

        while (i < args.length && !def/* && args[i].startsWith("-")*/) {
            arg = args[i++];


            if (arg.equals("-lp")) {
                vflag = true;
                if (i < args.length){
                    Client1.priority = (short)Integer.parseInt(args[i]);
                    if(Client1.priority < minPriority.value || Client1.priority > maxPriority.value){
                        System.err.println("lp priority out of range: " + Client1.priority);
                        printUsage();
                    }

                    i++;
                }else{
                    System.err.println("-lp needs a value");
                    printUsage();
                }
            }else if (arg.equals("-hp")) {
                vflag = true;
                if (i < args.length){
                    Client2.priority = (short)Integer.parseInt(args[i]);
                    if(Client2.priority < minPriority.value || Client2.priority > maxPriority.value){
                        System.err.println("hp priority out of range: " + Client2.priority);
                        printUsage();
                    }

                    i++;
                }else{
                    System.err.println("-hp needs a value");
                    printUsage();
                }
            }else if (arg.equals("-d")) {
                vflag = true;
                System.err.println("Using default priorities.");
                Client1.priority = minPriority.value;
                Client2.priority = maxPriority.value;
                def = true;
                //i++;
            }

            if(!vflag){
                System.err.println("Unrecognized option: " + arg);
                printUsage();
            }

            vflag = false;

        }

        System.err.println("lp Client set to: " + Client1.priority);
        System.err.println("hp Client set to: " + Client2.priority);
    }

    private static void printUsage() {
        System.err.println("Please use the following options:");
        System.err.println("\t-lp "+ minPriority.value+"-" + maxPriority.value + "\tPriority of low-priority client. Defaults to CORBA min if not specified.");
        System.err.println("\t-hp "+ minPriority.value+"-" + maxPriority.value + "\tPriority of high-priority client. Defaults to CORBA max if not specified.");
        System.err.println("\t-d \t\tUse defaults for both.");
        System.exit(-1);
    }
}
