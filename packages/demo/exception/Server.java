/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.exception;

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

public class Server extends RealtimeThread
{
    String[] args;
    
    public static void main(String[] args) throws Exception
    {
        //System.out.println( "=====================Creating RT Thread in server==========================" );
        Server rt = (Server) ImmortalMemory.instance().newInstance( Server.class );
        //System.out.println( "=====================Starting RT Thread in server==========================" );
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
            //System.out.println( "=====================Calling ORB Init in server============================" );
            ORB zen = ORB.init( args , null);            
            //System.out.println( "=====================ORB Init complete in server===========================" );

            POA rootPOA = POAHelper.narrow(zen.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();
            //System.out.println( "=================== RootPOA resolved, starting servant_to_ref ==============" );

            HelloWorldImpl impl = new HelloWorldImpl();
            org.omg.CORBA.Object obj = rootPOA.servant_to_reference(impl);
            //System.out.println( "=================== Servant registered, getting IOR ========================" );
            String ior = zen.object_to_string(obj);

            System.out.println("Running Hello World Example... ");
            System.out.println( "[Server] " + ior );

            BufferedWriter bw = new BufferedWriter( new FileWriter("ior.txt") );
            bw.write(ior);
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
