package demo.hello;

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

public class Peer1
{

    public static void main(String[] args) throws Exception
    {
        Client.orb = ORB.init((String[])null, null);
        Server.zen = Client.orb;
        Server.iorfile = "Peer1ior.txt";
        Server.main(args);
        HelloWorldImpl.semaphore = new edu.oswego.cs.dl.util.concurrent.Semaphore(1);
        HelloWorldImpl.semaphore.acquire();
        HelloWorldImpl.semaphore.acquire();
        //BufferedReader prompt = new BufferedReader(new InputStreamReader(System.in));
        //prompt.readLine();

        Client.iorfile = "Peer2ior.txt";
        Client.main(args);
    }
}