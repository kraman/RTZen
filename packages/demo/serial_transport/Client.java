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
            GroundStation server = GroundStationHelper.unchecked_narrow(object);

            WayPoint[] noFlyZone = new WayPoint[4];

            for (int i = 0; i < noFlyZone.length; i++)
            {
                noFlyZone[i] = new WayPoint();
                noFlyZone[i].altitude = (byte) (41 + i);
                noFlyZone[i].latitude = 80000 + i;
                noFlyZone[i].longitude = -90000 - i;
            }

            for (int i = 1; i <= 1; i++)
            {
                for (int j = 0; j < noFlyZone.length; j++)
                {
                    System.out.println("Sending no fly zone: alt=" + noFlyZone[j].altitude + " lat=" + noFlyZone[j].latitude + " lon=" + noFlyZone[j].longitude);
                }
                server.sendWayPt((short)i, noFlyZone);
            }

            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
