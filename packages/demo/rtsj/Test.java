/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.rtsj;

import java.io.*;

import org.omg.CORBA.ORB;
import javax.realtime.*;
import org.omg.PortableServer.*;

/**
 * This class implements a simple Server for the Hello World CORBA
 * demo
 *
 * @author <a href="mailto:mpanahi@doc.ece.uci.edu">Mark Panahi</a>
 * @version 1.0
 */

public class Test extends RealtimeThread
{   
    public static void main(String[] args) throws Exception
    {
        System.out.println( "=====================Creating RT Thread in server==========================" );
        RealtimeThread rt = (Test) ImmortalMemory.instance().newInstance( Test.class );
        System.out.println( "=====================Starting RT Thread in server==========================" );
        rt.start();
    }


    public void run(){
        newScope(new R1());

    }

    public static void newScope(Runnable logic){
        ScopedMemory sm = new LTMemory( 100 , 16*1024 );
        sm.enter(logic);
        //System.out.println(r1.s);
    }
}

class R1 implements Runnable{
    public String s;

    public void run(){
        s = "R1";
        System.out.println(s);
        edu.uci.ece.zen.utils.Logger.printThreadStack();
        R2 r2 = new R2();
        r2.parent = (ScopedMemory)RealtimeThread.getCurrentMemoryArea();
        Test.newScope(r2);
        
        //System.out.println(r2.s);

    }
}


class R2 implements Runnable{
    public ScopedMemory parent;

    public void run(){
        String s = new String("R2");
        System.out.println(s);
        System.out.println(" allocated in " + MemoryArea.getMemoryArea(s));
        edu.uci.ece.zen.utils.Logger.printThreadStack();
        
        try{
            ScopedMemory sm = (ScopedMemory)RealtimeThread.getCurrentMemoryArea();
            sm.setPortal(s);
            parent.executeInArea(new R3(sm));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

class R3 implements Runnable{
    public String s;
    public ScopedMemory r2area;

    public R3(ScopedMemory r2area){
        this.r2area = r2area;
    }

    public void run(){
        s = "R3";
        System.out.println(s);
        edu.uci.ece.zen.utils.Logger.printThreadStack();
        Test.newScope(new R4(r2area));
    }
}


class R4 implements Runnable{
    public String s;
    public ScopedMemory r2area;

    public R4(ScopedMemory r2area){
        this.r2area = r2area;
    }


    public void run(){
        s = "R4";
        System.out.println(s);
        System.out.println(r2area);
        System.out.println(r2area.size());
        String s2 = (String)r2area.getPortal();
        System.out.println("weweweqwerwerwer");
      byte[] tmp = new byte[ s2.length() ];
              System.arraycopy( s2.getBytes() , 0 , tmp , 0 , s2.length() );
                      String s3 = new String(tmp);
        //String s3 = new String(s2);
        //System.out.println(s2);
        System.out.println("RRRRRRRRRRRRR");

        try{
            r2area.newInstance(Object.class);
        }catch(Exception e){
            e.printStackTrace();
        }

        //System.out.println("accessing: " + s2 + " allocated in " + MemoryArea.getMemoryArea(s2));
        edu.uci.ece.zen.utils.Logger.printThreadStack();
    }
}


/*
    public void executeInORBRegion(Runnable runnable){
        ExecuteInRunnable r = new ExecuteInRunnable();

        r.init(runnable, orbImplRegion);
        try{

            parentMemoryArea.executeInArea( r );
        }catch( Exception e ){
            ZenProperties.logger.log(
                Logger.FATAL,
                "edu.uci.ece.zen.orb.ORB",
                "",
                e.toString()
                );
            System.exit(-1);
        }
    }
    public void setUpORBChildRegion(Runnable runnable){

        ExecuteInRunnable r1 = new ExecuteInRunnable();
        ExecuteInRunnable r2 = new ExecuteInRunnable();
        ScopedMemory sm = getScopedRegion();

        r1.init( r2 , orbImplRegion );
        r2.init( runnable, sm );
        try{
            parentMemoryArea.executeInArea( r1 );
        }catch( Exception e ){
            ZenProperties.logger.log(
                Logger.FATAL,
                "edu.uci.ece.zen.orb.RTORBImpl",
                "create_threadpool",
                "Could not create threadpool due to exception: " + e.toString()
                );
            System.exit(-1);
        }
    }

    
    public static void main(String[] args) throws Exception
    {
        System.out.println( "=====================Creating RT Thread in server==========================" );
        RealtimeThread rt = (Server) ImmortalMemory.instance().newInstance( Server.class );
        System.out.println( "=====================Starting RT Thread in server==========================" );
        rt.start();
    }

    public Server(){
        //super(null,new LTMemory(3000,300000));
    }

    public void run()
    {
        try
        {
            System.out.println( "=====================Calling ORB Init in server============================" );
            ORB zen = ORB.init((String[])null, null);
            System.out.println( "=====================ORB Init complete in server===========================" );

            POA rootPOA = POAHelper.narrow(zen.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();
            System.out.println( "=================== RootPOA resolved, starting servant_to_ref ==============" );

            HelloWorldImpl impl = new HelloWorldImpl();
            org.omg.CORBA.Object obj = rootPOA.servant_to_reference(impl);
            System.out.println( "=================== Servant registered, getting IOR ========================" );
            String ior = zen.object_to_string(obj);

            System.out.println("Running Hello World Example... ");
            System.out.println( "[Server] " + ior );

            BufferedWriter bw = new BufferedWriter( new FileWriter("ior.txt") );
            bw.write(ior);
            bw.close();

            System.out.println( "============================ ZEN.run() ====================================" );
			zen.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }*/
