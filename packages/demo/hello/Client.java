/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

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
        if(args.length > 0)
            runNum = Integer.parseInt(args[0]);
        //System.out.println( "=====================Creating RT Thread in client==========================" );
        RealtimeThread rt = (Client) ImmortalMemory.instance().newInstance( Client.class );
        //System.out.println( "=====================Starting RT Thread in client==========================" );
        rt.start();
        rt.join();
        System.exit(0);
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

           //System.out.println( "=====================Calling ORB Init in client============================" );
           ORB orb = ORB.init((String[])null, null);
           //System.out.println( "=====================ORB Init complete in client===========================" );
           String ior = "";
           File iorfile = new File( "ior.txt" );
           BufferedReader br = new BufferedReader( new FileReader(iorfile) );
           ior = br.readLine();
           //System.out.println( "===========================IOR read========================================" );
           org.omg.CORBA.Object object = orb.string_to_object(ior);
           //System.out.println( "===================Trying to establish connection==========================" );
           final HelloWorld server = HelloWorldHelper.unchecked_narrow(object);
           //System.out.println( "===================Connection established...sending request================" );
           System.out.println( "Servant returned: " + server.getMessage() );
            /*
            System.out.println( "====================== Performance warmup =================================" );
            for( int i=0;i<warmupNum;i++ ){
                
                server.getMessage();
                if(i % 100 == 0){        
                    Logger.write(i);
                    Logger.writeln();
                }
            }

            //iSoLeak.IsoLeakHelper.__iSoLeak_beginLeakMeasurement();
            server.beginMeasurement();
            System.out.println( "====================== Performance benchmark ==============================" );
            long start = System.currentTimeMillis();
            for( int i=0;i<runNum;i++ ){
                server.getMessage();
                //sleep(500);
                //sm.enter(r);
                if(i % 500 == 0){
                    Logger.write(i);
                    Logger.writeln();
                }
            }
            long end = System.currentTimeMillis();

            System.err.println( (double)runNum/((end-start)/1000.0));
            */

            server.shutdown();

            server._release();
            object._release();
            orb.shutdown( true );
            System.out.println( "Client thread is exiting" );
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
