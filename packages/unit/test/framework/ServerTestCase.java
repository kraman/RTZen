package test.framework;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.*;
import java.io.*;
import javax.realtime.*;

abstract public class ServerTestCase extends RealtimeThread {

	protected ORB orb;

	protected POA rootPOA;

	org.omg.CORBA.Object obj;

	protected Exception e;

	public ServerTestCase(){

		super(null,null,null,new LTMemory(3000,300000),null,null);

	}

	public void run(){
		try{

			setUpCORBA();

			synchronized (this){
				notifyAll();
			}

			orb.run();
		}
		catch(Exception ex){

			this.e = ex;
		}
	}

	protected void setUpCORBA() throw UserException{

		orb = ORB.init((String[])null, null);

		rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

		rootPOA.the_POAManager().activate();

	}


	protected registerServant( Servant servantImpl ){

		obj = rootPOA.servant_to_reference(servantImpl);

		String ior = orb.object_to_string(obj);

		BufferedWriter bw = new BufferedWriter( new FileWriter("ior.txt") );

		bw.write(ior);

		bw.close();

	}

	protected void shutDownCORBA throw UserException{

	}

	public Exception getException(){

		return e;

	}


	public void setException(Exception ex){

		this.e = e;

	}
}










