/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.rtcorba.policy;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Server
{

    private static org.omg.CORBA.ORB zen;

    public static void main(String [] args)
    {
        try
        {
            zen = org.omg.CORBA.ORB.init( (String[])null, null);

            POA rootPOA =
                POAHelper.narrow(zen.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            HelloWorldImpl impl = new HelloWorldImpl();
            org.omg.CORBA.Object obj = rootPOA.servant_to_reference(impl);

            String ior = zen.object_to_string(obj);

            System.out.println("Running Hello World Example... ");
            System.out.println( "[Server] " + ior );

            BufferedWriter bw = new BufferedWriter( new FileWriter("ior.txt") );
            bw.write(ior);
            bw.close();

            zen.run();
        }
        catch (java.lang.Exception ie)
        {
            ie.printStackTrace();
        }
    }
}

