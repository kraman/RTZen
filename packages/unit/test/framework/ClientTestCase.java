package test.framework;

import junit.framework.*;
import javax.realtime.*;
import java.io.*;
import org.omg.CORBA.ORB;

abstract public class ClientTestCase extends TestCase{


	protected ServerTestCase server;

	protected ORB orb;

	org.omg.CORBA.Object object;

	protected void setUp(){

		server.setUpCORBA();

		synchronized (server){

			try{

				server.wait();

			}

			catch (InterruptedException e){

				// Repackage the extremely-unlikely-to-occur InterruptedException as a UserException
				// to simplify the method's signature

				throw new NoServant("The server was interrupted during initialization");
			}
		}


		Exception e = server.getException();

		if (e != null){
			e.printStackTrace();
			System.exit(-1);
		}
		else{
			setUpCORBA();
		}
	}

	protected void tearDown() throws UserException{

		shutDownCORBA();
		server.shutDownCORBA();
	}

	protected void setUpCORBA() throws UserException{

		ORB orb = ORB.init((String[])null, null);
		String ior = "";
		File iorfile = new File( "ior.txt" );
		BufferedReader br = new BufferedReader( new FileReader(iorfile) );
		ior = br.readLine();
		object = orb.string_to_object(ior);
	}

	protected org.omg.CORBA.Object getServerObject() throw UserException{

		return object;

	}

	protected void shutDownCORBA() throw UserException{

	}

}




















