package edu.uci.ece.zen.services.naming;

import java.io.File;
import java.io.FileOutputStream;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

public class NamingService
{
	public static void main(String[] args)
	{
		ORB orb = edu.uci.ece.zen.orb.ORB.init(args, null);

		try
		{
			System.out.println("Get the root POA");
			POA poa = (POA) orb.resolve_initial_references("RootPOA");

			poa.the_POAManager().activate();
			System.out.println("create and init the impl");
			NamingContextExtImpl impl = new NamingContextExtImpl();

			impl.init(poa);

			System.out.println("write out the IOR");
			org.omg.CORBA.Object obj = poa.servant_to_reference(impl);
			String ior = orb.object_to_string(obj);

			String fileName =
				edu.uci.ece.zen.sys.ZenProperties.getProperty(
					"naming.ior_file.for_writing");
			File iorFile = new File(fileName);
			FileOutputStream ostream = new FileOutputStream(iorFile);

			ostream.write(ior.getBytes());
			ostream.flush();
			ostream.close();

			orb.run();
		}
		catch (Exception e)
		{
			System.out.println("Unable to start naming service.");
			e.printStackTrace();
		}
	}
}
