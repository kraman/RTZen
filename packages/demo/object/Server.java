/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.object;

import java.io.*;

import org.omg.CORBA.ORB;
import javax.realtime.*;
import org.omg.PortableServer.*;

/**
 * This class implements a simple Server for the Test Object
 * demo
 *
 * @author <a href="mailto:yuez@doc.ece.uci.edu">Yue Zhang</a>
 * @version 1.0
 */

public class Server extends RealtimeThread
{
    public static void main(String[] args) throws Exception
    {
        //System.out.println( "=====================Creating RT Thread in server==========================" );
        RealtimeThread rt = (Server) ImmortalMemory.instance().newInstance( Server.class );
        //System.out.println( "=====================Starting RT Thread in server==========================" );
        rt.start();
    }
    /*
    public Server(){
        super(null,null,null,new LTMemory(3000,300000),null,null);
    }
    */

    public void run()
    {
        try
        {
            //System.out.println( "=====================Calling ORB Init in server============================" );
            ORB zen = ORB.init((String[])null, null);
            //System.out.println( "=====================ORB Init complete in server===========================" );

            POA rootPOA = POAHelper.narrow(zen.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();
            //System.out.println( "=================== RootPOA resolved, starting servant_to_ref ==============" );

            TestObjectImpl impl1 = new TestObjectImpl("Mary");
            TestObjectImpl impl2 = new TestObjectImpl("Roby");
            HandleObjectImpl impl3 = new HandleObjectImpl();

            org.omg.CORBA.Object obj1 = rootPOA.servant_to_reference(impl1);
            org.omg.CORBA.Object obj2 = rootPOA.servant_to_reference(impl2);
            org.omg.CORBA.Object obj3 = rootPOA.servant_to_reference(impl3);
            
/* this stuff was for testing read and write buffer capacities
            final edu.uci.ece.zen.orb.CDROutputStream out = 
                edu.uci.ece.zen.orb.CDROutputStream.create((edu.uci.ece.zen.orb.ORB)zen);
            ScopedMemory sm1 = new LTMemory(1024*1024, 1024*1024 );  
    
            ScopedMemory sm2 = new LTMemory(1024*1024, 1024*1024 );
    
            final byte [] barr = new byte [] {1,2,3,1,2,3,1,2,3,1,2,3};
            final edu.uci.ece.zen.utils.ReadBuffer readBuf = edu.uci.ece.zen.utils.ReadBuffer.instance();
            readBuf.init();
        
            Runnable r1 = new Runnable() {
                public void run() {
                    for(long i = 0; i < 1000; ++i){
                        readBuf.writeByteArray(barr, 0, barr.length);
                        out.write_long((int)i);    
                    }
                     
                }
            };
            
            for(int k = 0; k < 1000; ++k){
                sm1.enter(r1);
                sm2.enter(r1);
                
            }

            //System.out.println("WORKS");
*/            

            //System.out.println( "=================== Servant registered, getting IOR ========================" );
            String ior1 = zen.object_to_string(obj1);
            String ior2 = zen.object_to_string(obj2);
            String ior3 = zen.object_to_string(obj3);
            

            System.out.println("Running Test Object Example... ");
            System.out.println( "[TestObject Server1] " + ior1 );
            System.out.println( "[TestObject Server2] " + ior2 );
            System.out.println( "[HandleObject Server] " + ior3 );      
            

            BufferedWriter bw = new BufferedWriter( new FileWriter("ior1.txt") );
            bw.write(ior1);            
            bw.close();

            bw = new BufferedWriter( new FileWriter("ior2.txt") );
            bw.write(ior2);
            bw.close();

            bw = new BufferedWriter( new FileWriter("ior3.txt") );
            bw.write(ior3);
            bw.close();


            //System.out.println( "============================ ZEN.run() ====================================" );
			zen.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
