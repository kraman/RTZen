import java.io.*;
import javax.realtime.*;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;

public class Server extends RealtimeThread
{
    public static void main(String[] args) throws Exception
    {
        RealtimeThread rt = (Server) ImmortalMemory.instance().newInstance( Server.class );
        rt.start();
    }

    public void run()
    {
        try
        {
            ORB zen = ORB.init((String[])null, null);

            POA rootPOA = POAHelper.narrow(zen.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            GroundStationImpl impl = new GroundStationImpl();
            org.omg.CORBA.Object obj = rootPOA.servant_to_reference(impl);
            String ior = zen.object_to_string(obj);

            BufferedWriter bw = new BufferedWriter( new FileWriter("ior.txt") );
            bw.write(ior);
            bw.close();

            System.out.println("Listening for client connections...");

			zen.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
