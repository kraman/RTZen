/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.rtcorba.threadpool;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.omg.PortableServer.*;
import org.omg.CORBA.ORB;
import org.omg.CORBA.*;
import javax.realtime.*;
import org.omg.RTCORBA.*;
import org.omg.Messaging.*;

public class Server
{

    private static org.omg.CORBA.ORB zen;
    private static long nap_time = 1000;
    private static int iorID = 0;
    static int static_threads = 2;
    static int dynamic_threads = 2;

    public static void main(String [] args)
    {
        try
        {
            zen = org.omg.CORBA.ORB.init( (String[])null, null);

            POA root_poa = POAHelper.narrow(zen.resolve_initial_references("RootPOA"));
            //root_poa.the_POAManager().activate();

            RTORB rt_orb = RTORBHelper.narrow(zen.resolve_initial_references ("RTORB"));
            org.omg.RTCORBA.Current current = org.omg.RTCORBA.CurrentHelper.narrow(zen.resolve_initial_references("RTCurrent"));

            POAManager poa_manager = root_poa.the_POAManager();
            poa_manager.activate();

            short default_thread_priority = current.the_priority();

            TestImpl servant = new TestImpl(zen, root_poa, nap_time);
            test t = servant._this ();

            writeIORToFile(t);

            int stacksize = 0;
            boolean allow_request_buffering = false;
            int max_buffered_requests = 0;
            int max_request_buffer_size = 0;

            int threadpool_id_1 =
                rt_orb.create_threadpool (stacksize,
                                   static_threads,
                                   dynamic_threads,
                                   default_thread_priority,
                                   allow_request_buffering,
                                   max_buffered_requests,
                                   max_request_buffer_size);

            Policy threadpool_policy_1 =
                rt_orb.create_threadpool_policy (threadpool_id_1);

            boolean allow_borrowing = false;
            ThreadpoolLane [] lanes = new ThreadpoolLane[1];

            lanes[0].lane_priority = default_thread_priority;
            lanes[0].static_threads = static_threads;
            lanes[0].dynamic_threads = dynamic_threads;

            int threadpool_id_2 =
                rt_orb.create_threadpool_with_lanes (stacksize, lanes,
                                              allow_borrowing,
                                              allow_request_buffering,
                                              max_buffered_requests,
                                              max_request_buffer_size);

            Policy threadpool_policy_2 =
                rt_orb.create_threadpool_policy (threadpool_id_2);

            create_POA_and_register_servant (threadpool_policy_1, "first_poa",
                                         poa_manager, root_poa, zen, rt_orb);

            create_POA_and_register_servant (threadpool_policy_2, "second_poa",
                                         poa_manager, root_poa, zen, rt_orb);

            zen.run();
        }
        catch (java.lang.Exception ie)
        {
            ie.printStackTrace();
        }
    }

    static void create_POA_and_register_servant (Policy threadpool_policy,
                                     String poa_name,
                                     POAManager poa_manager,
                                     POA root_poa,
                                     ORB orb,
                                     RTORB rt_orb) {

        try{
            // Policies for the firstPOA to be created.
            Policy [] policies = new Policy[3];

            // Implicit_activation policy.
            policies[0] = root_poa.create_implicit_activation_policy(ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION);

            // Thread pool policy.
            policies[1] = threadpool_policy;

            // Priority Model policy.
            policies[2] = rt_orb.create_priority_model_policy(PriorityModel.CLIENT_PROPAGATED, (short)0);

            // Create the POA under the RootPOA.
            POA poa = root_poa.create_POA (poa_name, poa_manager, policies);

            // Creation of POAs is over. Destroy the Policy objects.
            /* not sure how this is done in Java
            for (CORBA::ULong i = 0;
               i < policies.length ();
               ++i)
            {
              policies[i]->destroy (ACE_ENV_SINGLE_ARG_PARAMETER);
              ACE_CHECK_RETURN (-1);
            }
    */
            TestImpl servant = new TestImpl(orb, poa, nap_time);
            test t = servant._this();
            writeIORToFile(t);

            //org.omg.CORBA.Object obj = rootPOA.servant_to_reference(servant);


        }catch(Exception e){
            e.printStackTrace();
        }

        // ?? PortableServer::ServantBase_var safe_servant (servant);
        //return result;
    }

    private static void writeIORToFile(test t){
        try
        {
            String ior = zen.object_to_string(t);

            System.out.println("Running Example " + iorID);
            System.out.println( "[Server] " + ior );

            BufferedWriter bw = new BufferedWriter( new FileWriter("ior" + iorID) );
            iorID++;
            bw.write(ior);
            bw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }


}

