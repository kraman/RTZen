/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.jpl;

import java.io.BufferedWriter;
import java.io.FileWriter;

import javax.realtime.ImmortalMemory;
import javax.realtime.LTMemory;
import javax.realtime.HeapMemory;
import javax.realtime.PriorityScheduler;
import javax.realtime.RealtimeThread;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;
import org.omg.RTCORBA.RTORB;
import org.omg.RTCORBA.RTORBHelper;
import org.omg.RTCORBA.ThreadpoolLane;
import org.omg.RTCORBA.maxPriority;
import org.omg.RTCORBA.minPriority;

/**
 * This class implements a simple CORBA Server.
 *
 * @author Juan Colmenares
 * @author Hojjat Jafarpour
 * @author Mark Panahi
 * @version 1.0
 */

public class Server extends RealtimeThread
{
    private static final int ITERATION_FACTOR_2 = 1;
    private static final int ITERATION_FACTOR_1 = 20;
    public String[] args;
    private static boolean isClientPropagated = true;
    ORB orb;
    static short loPrio = minPriority.value;
    static short hiPrio = maxPriority.value;
    static int staticThreads = 1;

    public static void main(String[] args) throws Exception
    {
        parseCmdLine(args);

        Server rt = (Server) ImmortalMemory.instance().newInstance( Server.class );

        rt.init( args );
        rt.start();
    }

    public Server(){
        super(null,null,null,new LTMemory(3000,300000),null,null);
    }

    public void init( String args[] ){
        this.args = args;
    }

    public void run()
    {
        try
        {
            System.out.println( "[Server] =====================Calling ORB Init in server============================" );
            orb = ORB.init(args , null);
            System.out.println( "[Server] =====================ORB Init complete in server===========================" );

            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            System.out.println( "[Server] =================== RootPOA resolved, starting servant_to_ref ==============" );

            POAManager poaManager = rootPOA.the_POAManager ();
            poaManager.activate();
            System.out.println( "[Server] =================== Activated POA Manager ==============" );

            // Creating a child poa with a threadpool

            RTORB rtorb = RTORBHelper.narrow(orb.resolve_initial_references ("RTORB"));

            //System.out.println("Max prio " + PriorityScheduler.instance().getMaxPriority());
            //System.out.println("Min prio " + PriorityScheduler.instance().getMinPriority());
            //System.out.println("Norm prio " + PriorityScheduler.instance().getNormPriority());
            //short priority = (short) (30 + (PriorityScheduler.getNormPriority(RealtimeThread.currentThread()))) ;
            //short priority = (short) PriorityScheduler.instance().getMaxPriority();
            //System.out.println("Higher priority is: " + priority);

            int threadPoolId;

            if(isClientPropagated){
                System.out.println("[Server] Using client-propagated policy.....");
                Policy[] policy = new Policy[2];
                policy[0] = rtorb.create_priority_model_policy (
                        org.omg.RTCORBA.PriorityModel.CLIENT_PROPAGATED,
                        (short)0);

                ThreadpoolLane[] lanes = new ThreadpoolLane[2];

                //args are: priority, static threads, dynamic threads
                lanes[0] = new ThreadpoolLane(loPrio, staticThreads, 0);
                lanes[1] = new ThreadpoolLane(hiPrio, staticThreads, 0);

                threadPoolId = rtorb.create_threadpool_with_lanes(10, lanes, false, false, 10, 10);
                policy[1] = rtorb.create_threadpool_policy(threadPoolId);

                System.out.println("[Server] Creating a child POA");
                POA childPOA = rootPOA.create_POA("childPOA", poaManager, policy);

                createObj(ITERATION_FACTOR_1, childPOA, "ior1.txt");
                createObj(ITERATION_FACTOR_2, childPOA, "ior2.txt");
            }else{
                System.out.println("[Server] Using server-declared policy.....");
                Policy[] loPolicy = new Policy[2];
                Policy[] hiPolicy = new Policy[2];

                loPolicy[0] = rtorb.create_priority_model_policy (
                        org.omg.RTCORBA.PriorityModel.SERVER_DECLARED,
                        loPrio);
                hiPolicy[0] = rtorb.create_priority_model_policy (
                        org.omg.RTCORBA.PriorityModel.SERVER_DECLARED,
                        hiPrio);

                ThreadpoolLane[] lanes = new ThreadpoolLane[2];

                //args are: priority, static threads, dynamic threads
                lanes[0] = new ThreadpoolLane(loPrio, staticThreads, 0);
                lanes[1] = new ThreadpoolLane(hiPrio, staticThreads, 0);
                threadPoolId = rtorb.create_threadpool_with_lanes(10, lanes, false, false, 10, 10);
                loPolicy[1] = rtorb.create_threadpool_policy(threadPoolId);
                hiPolicy[1] = rtorb.create_threadpool_policy(threadPoolId);

                System.out.println("[Server] Creating a low priority child POA");
                POA lpChildPOA = rootPOA.create_POA("lpChildPOA", poaManager, loPolicy);

                System.out.println("[Server] Creating a high priority child POA");
                POA hpChildPOA = rootPOA.create_POA("hpChildPOA", poaManager, hiPolicy);

                createObj(ITERATION_FACTOR_1, lpChildPOA, "ior1.txt");
                createObj(ITERATION_FACTOR_2, hpChildPOA, "ior2.txt");
            }

            System.out.println( "[Server] RTZen is running ...." );
            orb.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void createObj(int iFactor, POA poa, String iorFile){
        try{
            HelloWorldImpl impl = new HelloWorldImpl(iFactor);
            org.omg.CORBA.Object obj = poa.servant_to_reference(impl);
            System.out.println( "=================== Servant registered, getting IOR ========================" );
            String ior = orb.object_to_string(obj);
            writeIOR(ior, iorFile);
        }catch(Exception e){
            e.printStackTrace();
            System.exit(-1);
        }

    }
    /**
     * Writes the Server ior to file
     */
    private void writeIOR(final String ior, final String filename ) {
        RealtimeThread rt = new RealtimeThread( null, null, null, HeapMemory.instance(), null, new Runnable() {
                public void run() {
                    try {
                        BufferedWriter bw = new BufferedWriter( new FileWriter(filename) );
                        bw.write(ior);
                        bw.close();
                        System.out.println( "[Server] " + ior );
                    }
                    catch ( java.io.IOException ioe ) {
                        System.out.println( "Exception writing " + filename );
                    }
                }
                } );
            rt.start();
        try {
            rt.join();
        }
        catch ( InterruptedException e ) {
        }
    }

    private static void parseCmdLine(String[] args) {

        int i = 0, j;
        String arg;
        char flag;
        boolean vflag = false;
        String outputfile = "";

        if(args.length == 0)
            printUsage();

        while (i < args.length/* && args[i].startsWith("-")*/) {
            arg = args[i++];


            if (arg.equals("-pm")) {
                vflag = true;
                if (i < args.length){
                    if(args[i].equals("sd")){
                        isClientPropagated = false;
                        System.out.println("Setting server-declared policy.....");
                    }else if (args[i].equals("cp")){
                        isClientPropagated = true;
                        System.out.println("Setting client-propagated policy.....");
                    }else{
                        System.err.println("-pm needs an appropriate priority model");
                        printUsage();
                    }
                    i++;
                }else{
                    System.err.println("-pm needs a priority model");
                    printUsage();

                }
            }else if (arg.equals("-lp")) {
                vflag = true;
                if (i < args.length){
                    loPrio = (short)Integer.parseInt(args[i]);
                    if(loPrio < minPriority.value || loPrio > maxPriority.value){
                        System.err.println("lp priority out of range: " + loPrio);
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
                    hiPrio = (short)Integer.parseInt(args[i]);
                    if(hiPrio < minPriority.value || hiPrio > maxPriority.value){
                        System.err.println("hp priority out of range: " + hiPrio);
                        printUsage();
                    }

                    i++;
                }else{
                    System.err.println("-hp needs a value");
                    printUsage();
                }
            }else if (arg.equals("-st")) {
                vflag = true;
                if (i < args.length){
                    staticThreads = Integer.parseInt(args[i]);
                    if(staticThreads < 1 || staticThreads > 10){
                        System.err.println("st out of range: " + staticThreads);
                        printUsage();
                    }

                    i++;
                }else{
                    System.err.println("-st needs a value");
                    printUsage();
                }
            }

            if(!vflag){
                System.err.println("Unrecognized option: " + arg);
                printUsage();
            }

            vflag = false;



    // use this type of check for "wordy" arguments
    /*
            if (arg.equals("-verbose")) {
                System.out.println("verbose mode on");
                vflag = true;
            }

    // use this type of check for arguments that require arguments
            else if (arg.equals("-output")) {
                if (i < args.length)
                    outputfile = args[i++];
                else
                    System.err.println("-output requires a filename");
                if (vflag)
                    System.out.println("output file = " + outputfile);
            }

    // use this type of check for a series of flag arguments
            else {
                for (j = 1; j < arg.length(); j++) {
                    flag = arg.charAt(j);
                    switch (flag) {
                    case 'x':
                        if (vflag) System.out.println("Option x");
                        break;
                    case 'n':
                        if (vflag) System.out.println("Option n");
                        break;
                    default:
                        System.err.println("ParseCmdLine: illegal option " + flag);
                        break;
                    }
                }
            }*/
        }

        System.err.println("lp priority set to: " + loPrio);
        System.err.println("hp priority set to: " + hiPrio);
        System.err.println("static threads set to: " + staticThreads);

    }



    private static void printUsage() {
        System.err.println("Please use the following options:");
        System.err.println("\t-pm cp|sd\tPriority model(pm) of client-propagated(cp) or server declared(sd).");
        System.err.println("\t-lp "+ minPriority.value+"-" + maxPriority.value + "\tPriority of low-priority task. Defaults to CORBA min if not specified.");
        System.err.println("\t-hp "+ minPriority.value+"-" + maxPriority.value + "\tPriority of high-priority task. Defaults to CORBA max if not specified.");
        System.err.println("\t-st 1-10\tNumber of static threads to use. Defaults to 1.");
        System.err.println("\tIMPORTANT: Make sure client priorities match the ones offered by the server!!!");
        System.err.println("\t           Please see client usage parameters.");
        System.exit(-1);
    }

}
