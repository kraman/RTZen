/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.poa2;

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

public class Client1 extends RealtimeThread
{
 
    static String fileName; 
    
    
    public static void main(String[] args) throws Exception
    {
        //if(args.length > 0) 
        //fileName = args[0];
        System.out.println( "=====================Creating RT Thread in client==========================" );
        RealtimeThread rt = (Client1) ImmortalMemory.instance().newInstance( Client1.class );
        System.out.println( "=====================Starting RT Thread in client==========================" );
        rt.start();
    }

    public Client1(){
        //super(null,new LTMemory(3000,300000));
    }

    public static int warmupNum = 5000;
    public static int runNum = 50000;

    public void run()
    {
        try
        {
            System.out.println( "=====================Calling ORB Init in client============================" );
            ORB orb = ORB.init((String[])null, null);
            System.out.println( "=====================ORB Init complete in client===========================" );
            String ior = "";
            //File iorfile = new File(fileName);
            File iorfile = new File("ior1.txt");
            BufferedReader br = new BufferedReader( new FileReader(iorfile) );
            ior = br.readLine();
            System.out.println( "===========================IOR read========================================" );
            org.omg.CORBA.Object object = orb.string_to_object(ior);
            System.out.println( "===================Trying to establish connection==========================" );
            final HelloWorld server = HelloWorldHelper.unchecked_narrow(object);
            System.out.println( server.getMessage1() );

/*
            // Create a scope for running requests in, so that we don't waste the scope we are in.
            ScopedMemory sm = new LTMemory(32000, 100000);
            Runnable r = new Runnable() {
                public void run() {
                    server.getMessage1();
                }
            };
*/
//            System.out.println( "====================== Performance warmup =================================" );
//            for( int i=0;i<warmupNum;i++ ){
//                
//                server.getMessage1();
//                //sleep(1000);
//                
//                //sm.enter(r);
//                if(i % 100 == 0){        
//                    Logger.write(i);
//                    Logger.writeln();
//                }
//            }

            sleep(5000);
            
            System.out.println( "====================== Performance benchmark ==============================" );
            long start = System.currentTimeMillis();
            for( int i=0;i<runNum;i++ ){
                server.getMessage1();
                //System.out.println(System.currentTimeMillis());
                //sleep(10);
                
                //sm.enter(r);
                //if(i % 500 == 0){
                    Logger.write(i);
                    Logger.writeln();
                //}
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
