package edu.uci.ece.zen.poa;

import javax.realtime.*;

public class Run implements Runnable
{
	Object output;
	public Object[] input;
	int method;
	ScopedMemory thisArea;
        boolean isException;
        Exception ex;

	public Run()
	{
		input = new Object[10];
                isException = false;
	}



	/*public void setInput( Object[] input)
	{
		this.input= input;
	}*/


	public void setMemory()
	{
		thisArea = (ScopedMemory) RealtimeThread.getCurrentMemoryArea();
	}


	private void servant_to_id()
	{
        	try{
		output = ((POAImpl)thisArea.getPortal()).servant_to_id( (org.omg.PortableServer.Servant)input[0]);
                }
                catch(org.omg.PortableServer.POAPackage.ServantNotActive e) {
                isException = true;
                ex = e;
                }
                catch(org.omg.PortableServer.POAPackage.WrongPolicy e) {
                isException = true;
                ex = e;
                }
	}

	private void servant_to_reference()
	{
        	try{
                //System.out.println("Ok going to call poa");
		output = ((POAImpl)thisArea.getPortal()).servant_to_reference( (org.omg.PortableServer.Servant) input[0]);
                //System.out.println("Ok done calling poa");
                }
                catch(org.omg.PortableServer.POAPackage.ServantNotActive e) {
                isException = true;
                ex = e;

                }
                catch(org.omg.PortableServer.POAPackage.WrongPolicy e) {
                isException = true;
                ex = e;

                }


	}

	private void reference_to_servant()
	{
        	try{
		output = ((POAImpl)thisArea.getPortal()).reference_to_servant( (org.omg.CORBA.Object) input[0]);
                }

                catch(org.omg.PortableServer.POAPackage.WrongPolicy e) {
                isException = true;
                ex = e;

                }
                catch(org.omg.PortableServer.POAPackage.ObjectNotActive  e) {
                isException = true;
                ex = e;

                }
                catch(org.omg.PortableServer.POAPackage.WrongAdapter  e) {
                isException = true;
                ex = e;

                }




	}

	private void reference_to_id()
	{
        	try{
		output = ((POAImpl)thisArea.getPortal()).reference_to_id( (org.omg.CORBA.Object) input[0]);
                }

                catch(org.omg.PortableServer.POAPackage.WrongPolicy e) {
                isException = true;
                ex = e;

                }
                catch(org.omg.PortableServer.POAPackage.WrongAdapter e) {
                isException = true;
                ex = e;

                }


	}

	private void id_to_servant()
	{
        	try{
		output = ((POAImpl)thisArea.getPortal()).id_to_servant( (byte[]) input[0]);
                if(output == null) throw new Exception();
                }

                catch(org.omg.PortableServer.POAPackage.WrongPolicy e) {
                isException = true;
                ex = e;

                }
                catch(org.omg.PortableServer.POAPackage.ObjectNotActive e) {
                isException = true;
                ex = e;

                }

                catch(Exception e)
                {
                //System.out.println("In run exception!!!!!!!!!");
                isException = true;
                ex = e;
		}


	}

	private void id_to_reference()
	{
        	try{
		output = ((POAImpl)thisArea.getPortal()).id_to_reference( (byte[]) input[0]);
                }
                catch(org.omg.PortableServer.POAPackage.WrongPolicy e) {
                isException = true;
                ex = e;

                }
                catch(org.omg.PortableServer.POAPackage.ObjectNotActive e) {
                isException = true;
                ex = e;

                }


	}

	private void activate_object()
	{
        	try{
		output = ((POAImpl)thisArea.getPortal()).activate_object( (org.omg.PortableServer.Servant) input[0]);
                }
                catch(org.omg.PortableServer.POAPackage.WrongPolicy e) {
                isException = true;
                ex = e;

                }
                catch(org.omg.PortableServer.POAPackage.ServantAlreadyActive e) {
                isException = true;
                ex = e;

                }

	}


	private void activate_object_with_id()
	{
        	try{
		((POAImpl)thisArea.getPortal()).activate_object_with_id( (byte[]) input[0], (org.omg.PortableServer.Servant) input[1] );
                }
                catch(org.omg.PortableServer.POAPackage.WrongPolicy e) {
                isException = true;
                ex = e;
;
                }
                catch(org.omg.PortableServer.POAPackage.ServantAlreadyActive e) {
                isException = true;
                ex = e;
;
                }
                catch(org.omg.PortableServer.POAPackage.ObjectAlreadyActive  e) {
                isException = true;
                ex = e;
;
                }

	}

	private void deactivate_object()
	{
        	try{
		((POAImpl)thisArea.getPortal()). deactivate_object( (byte[]) input[0]);
                }
                catch(org.omg.PortableServer.POAPackage.WrongPolicy e) {
                isException = true;
                ex = e;
;
                }
                catch(org.omg.PortableServer.POAPackage.ObjectNotActive e) {
                isException = true;
                ex = e;
;
                }

	}


	private void id()
	{
		output = ((POAImpl)thisArea.getPortal()).id ();
	}

	private void get_servant_manager()
	{
        	try{
		output = ((POAImpl)thisArea.getPortal()).get_servant_manager();
                }
                catch(org.omg.PortableServer.POAPackage.WrongPolicy e) {
                isException = true;
                ex = e;
;
                }



	}

	private void set_servant_manager()
	{
        	try{
		((POAImpl)thisArea.getPortal()).set_servant_manager( (org.omg.PortableServer.ServantManager) input[0]);
                }
                catch(org.omg.PortableServer.POAPackage.WrongPolicy e) {
                isException = true;
                ex = e;
;
                }

	}

	private void get_servant()

	{
        	try{
		output = ((POAImpl)thisArea.getPortal()).get_servant();
                }
                catch(org.omg.PortableServer.POAPackage.NoServant e) {
                isException = true;
                ex = e;
;
                }
                catch(org.omg.PortableServer.POAPackage.WrongPolicy e) {
                isException = true;
                ex = e;
;
                }


	}

	private void set_servant()
	{
        	try{
		((POAImpl)thisArea.getPortal()).set_servant( (org.omg.PortableServer.Servant) input[0]);
                }
                catch(org.omg.PortableServer.POAPackage.WrongPolicy e) {
                isException = true;
                ex = e;
;
                }

	}

	private void create_reference()
	{
        	try{
		output = ((POAImpl)thisArea.getPortal()).create_reference( (String) input[0]);
                }
                catch(org.omg.PortableServer.POAPackage.WrongPolicy e) {
		isException = true;
                ex = e;
;
                }

	}


	private void create_reference_with_id()
	{
		output = ((POAImpl)thisArea.getPortal()).create_reference_with_id( (byte[]) input[0], (String) input[1] );
	}

	private void create_reference_with_object_key()
	{
		output = ((POAImpl)thisArea.getPortal()).create_reference_with_object_key( (edu.uci.ece.zen.poa.ObjectKey) input[0], (String) input[1] );
	}


	private void handleRequest()
	{
        	//System.out.println("Ok going to call POA invoke");
		((POAImpl)thisArea.getPortal()).handleRequest( (edu.uci.ece.zen.orb.ServerRequest) input[0]);
                //System.out.println("Ok done call POA invoke");

	}



	public void setType( int no )
	{
		method = no;
	}


	public Object getObject()
	{
		return output;
	}

        public boolean chkException()
        {
        	return isException;
        }
        public Exception getException()
        {
        	return ex;
        }



	public void run()
	{
		//System.out.println("Ok in run with involke value "+  method);

		setMemory();


		switch(method)
		{

			case 0: {
                        			isException = false;
                                                //System.out.println("Ok calling invoke");
						handleRequest();
						break;
					}

			case 1: {
                        			isException = false;
						servant_to_id();
						break;
					}


			case 2 : {
                        			isException = false;
						 servant_to_reference();
                                                 //System.out.println("Ok in run");
						 break;
					 }

			case 3 : {
                        			isException = false;
						 reference_to_servant();
						 break;
					 }

			case 4 : {
                        			isException = false;
						 reference_to_id();
						 break;
					 }

			case 5 : {
                        			isException = false;
						 id_to_servant();
						 break;
					 }

			case 6 : {
                        			isException = false;
						 id_to_reference();
						 break;
					 }
			case 7 : {
                        			isException = false;
						 activate_object();
						 break;
					 }

			case 8 : {
                        			isException = false;
						 activate_object_with_id();
						 break;
					 }

			case 9 : {
                        			isException = false;
						 deactivate_object();
						 break;
					 }

			case 10 :{
                        			isException = false;
						 id();
						 break;
					 }
			case 11 :{
                        			isException = false;
						  get_servant_manager();
						  break;
					 }

			case 12 :{
                        			isException = false;
						 set_servant_manager();
						 break;
					 }

			case 13 :{
                        			isException = false;
						 get_servant();
						 break;
					 }

			case 14 :{
                        			isException = false;
						 set_servant();
						 break;
					 }

			case 15 :{
                        			isException = false;
						 create_reference();
						 break;
					 }

			case 16 :{
                        			isException = false;
						 create_reference_with_id();
						 break;
					 }
                         case 17:{
                         			isException = false;
                                                create_reference_with_object_key();
                                                break;
                                          }



		}
                //System.out.println("Ok exiting run");
	}
}
