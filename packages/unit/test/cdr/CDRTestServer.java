package unit.test.cdr;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import javax.realtime.RealtimeThread;
import javax.realtime.ImmortalMemory;
import javax.realtime.LTMemory;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * This class implements a simple Server for the CDR test
 *
 * @author <a href="mailto:yuez@doc.ece.uci.edu">Yue Zhang</a>
 * @version 1.0
 */


public class CDRTestServer extends RealtimeThread{

	public static void main( String[] args){
		try{
			//CDRTestServer rt = (CDRTestServer) (new RealtimeThread(null,null,null,new LTMemory(3000,300000),null,null) );
			RealtimeThread rt = (CDRTestServer) ImmortalMemory.instance().newInstance( CDRTestServer.class );
			rt.start();
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	public void run(){

		try{

			ORB orb = ORB.init((String [])null, null );

			POA rootPOA = POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));
			rootPOA.the_POAManager().activate();

			DataTypesImpl impl = new DataTypesImpl();
			org.omg.CORBA.Object obj = rootPOA.servant_to_reference( impl );


			String ior = orb.object_to_string( obj );
			System.out.println ( "Running CDR test case..." );
			System.out.println ( " [Server] " + ior);


			BufferedWriter bw = new BufferedWriter( new FileWriter ( "ior.txt" ) );
			bw.write( ior );
			bw.close();

			orb.run();
		}
		catch( Exception ex ){
			ex.printStackTrace();
			System.exit( -1 );
		}
	}
}


