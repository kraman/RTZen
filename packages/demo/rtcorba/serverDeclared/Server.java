/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.rtcorba.serverDeclared;


import java.io.*;
import edu.uci.ece.zen.utils.ZenProperties;

import org.omg.CORBA.ORB;
import javax.realtime.*;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;

import org.omg.RTCORBA.RTORB;
import org.omg.RTCORBA.RTORBHelper;

public class Server extends RealtimeThread
{
    String[] args;

    static String ior_output_file1 = "test1.ior";
    static String ior_output_file2 = "test2.ior";
    short poa_priority = 1;
    short object_priority = 2;

    public static void main(String[] args) throws Exception
    {
        System.out.println( "=====================Creating RT Thread in server==========================" );
        Server rt = (Server) ImmortalMemory.instance().newInstance( Server.class );
        System.out.println( "=====================Starting RT Thread in server==========================" );
        rt.init( args );
        rt.start();
    }

    public Server(){
        super(null,null,null,new LTMemory(3000,300000),null,null);
        System.out.println( "====" );
    }

    public void init( String args[] ){
        this.args = args;
    }

    public void run()
    {
        try
        {
            ZenProperties.iiopMinor = 2;
            System.out.println( "=====================Calling ORB Init in server============================" );
            ORB orb = ORB.init( args , null);
            System.out.println( "=====================ORB Init complete in server===========================" );

            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            System.out.println( "=================== RootPOA resolved, starting servant_to_ref ==============" );

            POAManager poaManager = rootPOA.the_POAManager ();

            RTORB rtorb = RTORBHelper.narrow(orb.resolve_initial_references ("RTORB"));
            org.omg.RTCORBA.Current rtcur = org.omg.RTCORBA.CurrentHelper.narrow(orb.resolve_initial_references("RTCurrent"));

            // Create POA with CLIENT_PROPAGATED PriorityModelPolicy,
            // and register Test object with it.
            org.omg.CORBA.Policy [] poa_policy_list = new org.omg.CORBA.Policy[2];

            //NON Standard -- using this until we can get some other stuff working
            ///edu.uci.ece.zen.orb.transport.iiop.Acceptor.enableComponents = true;
            ///edu.uci.ece.zen.orb.transport.iiop.Acceptor.serverPriority = 0;
            ///edu.uci.ece.zen.orb.transport.iiop.Acceptor.priorityModel = org.omg.RTCORBA.PriorityModel.SERVER_DECLARED.value();


            //for standard CORBA, this would be reenabled

            // Create child POA with SERVER_DECLARED PriorityModelPolicy,
            // and MULTIPLE_ID id uniqueness policy (so we can use one
            // servant to create several objects).
            poa_policy_list[0] = rtorb.create_priority_model_policy (org.omg.RTCORBA.PriorityModel.SERVER_DECLARED,poa_priority);
            poa_policy_list[1] = rootPOA.create_id_uniqueness_policy(org.omg.PortableServer.IdUniquenessPolicyValue.MULTIPLE_ID);
            POA childPOA = rootPOA.create_POA ("Child_POA",poaManager,poa_policy_list);

            org.omg.RTPortableServer.POA rtpoa = org.omg.RTPortableServer.POAHelper.narrow(childPOA);

            TestImpl impl = new TestImpl(orb);

            // Create object 1 (it will inherit POA's priority).
            System.out.println("\nActivated object one as ");
            createObject (rtpoa, orb, impl, (short)-1, ior_output_file1);

            // Create object 2 (override POA's priority).
            System.out.println("\nActivated object two as ");
            createObject (rtpoa, orb, impl, object_priority, ior_output_file2);

            // Run ORB Event loop.
            poaManager.activate();

            orb.run();

            System.out.println("Server ORB event loop finished.");

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    void createObject (org.omg.RTPortableServer.POA poa,
                   //ORB orb, org.omg.PortableServer.Servant impl, short priority,
                   ORB orb, TestImpl impl, short priority,
                   String filename)
    {
        try{
            // Register with poa.
            byte [] id; //objectId

            if (priority > -1)
                id = poa.activate_object_with_priority (impl, priority);
            else
                id = poa.activate_object (impl);

            org.omg.CORBA.Object server = poa.id_to_reference (id);

            // Print out the IOR.
            String ior =
                orb.object_to_string (server);

            // Print ior to the file.
            //System.out.println("Running clientPropagated Example server side... ");
            System.out.println( "[Server] " + ior );

            BufferedWriter bw = new BufferedWriter( new FileWriter(filename) );
            bw.write(ior);
            bw.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}


