package demo.seqtests;

import java.io.*;
import org.omg.PortableServer.*;


/*
* Data type tests for RTZen
* @author shruti
*/

public class Server
{
    public static void main(String [] args)
    {
        try
        {
            org.omg.CORBA.ORB zen = org.omg.CORBA.ORB.init( (String[])null, null);

            POA rootPOA = POAHelper.narrow(zen.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            HelloWorldImpl impl = new HelloWorldImpl();
            org.omg.CORBA.Object obj = rootPOA.servant_to_reference(impl);

            String ior = zen.object_to_string(obj);

            System.out.println("Running Data Types Tests... ");
            //System.out.println("[Server] " + ior);

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

