package demo.serial_transport;

import java.io.*;
import javax.realtime.*;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;

public class Client extends RealtimeThread
{
    public static void main(String[] args)
    {
        try
        {
            RealtimeThread rt = (Client) ImmortalMemory.instance().newInstance( Client.class );
            rt.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void run()
    {
        try
        {
            ORB orb = ORB.init((String[])null, null);
            File iorfile = new File( "ior.txt" );
            BufferedReader br = new BufferedReader( new FileReader(iorfile) );
            String ior = br.readLine();
            org.omg.CORBA.Object object = orb.string_to_object(ior);
            Airplane server = AirplaneHelper.unchecked_narrow(object);

            WayPoint waypoint = new WayPoint();

            server.sendWayPoint((short)1, waypoint);

            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
