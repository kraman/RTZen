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

public class Peer2
{

    public static void main(String[] args) throws Exception
    {
        Client.orb = ORB.init(args, null);
        Server.zen = Client.orb;
        Server.iorfile = "Peer2ior.txt";
        Server.main(args);

        Client.iorfile = "Peer1ior.txt";
        Client.main(args);
    }
}
