package demo.seqtests;

import java.io.*;
import java.util.Arrays;
import org.omg.CORBA.ORB;

/*
* Data type tests for RTZen
* @author shruti
*/

public class Client
{
    public static void main(String[] args)
    {
        byte inOctetSeq[] = new byte[3000];
        short inShortSeq[] = new short[3000];
	int inLongSeq[] = new int[3000];
	String inStringSeq[] = new String[3000];
	double inDoubleSeq[] = new double[3000];
	boolean inBoolSeq[] = new boolean[3000];
	char inCharSeq[] = new char[3000];
	
        try
        {
		
	    for (int i=0;i<2000;i++){
		inStringSeq[i]="I'm super!";
	    }
            ORB orb = ORB.init((String[])null, null);
            String ior = "";

            BufferedReader br = new BufferedReader( new FileReader("ior.txt") );
            ior = br.readLine();

    //        System.out.println("[Client] " + ior);

            org.omg.CORBA.Object object = orb.string_to_object(ior);


            HelloWorld server = HelloWorldHelper.narrow(object);


	  if ( Arrays.equals( inOctetSeq , server.putOctetSeq( inOctetSeq ) ) ) {
		  System.out.println ("Byte Data OK!!");
	  }
	  else System.out.println("Byte Data corrupted");

	  
	  if ( Arrays.equals( inShortSeq , server.putShortSeq( inShortSeq ) ) ) {
		  System.out.println ("Short Data OK!!");
	  }
	  else System.out.println("Short Data corrupted");

	  if ( Arrays.equals( inLongSeq , server.putLongSeq( inLongSeq ) ) ) {
		  System.out.println ("Long Data OK!!");
	  }
	  else System.out.println("Long Data corrupted");
	  
	  
//	  if ( Arrays.equals( inStringSeq , server.putStringSeq( inStringSeq ) ) ) {
//		  System.out.println ("String Data OK!!");
//	  else System.out.println("String Data corrupted");

		/*
		TODO
		String[] throws runtime error.
		Gotta fix this.
		
		 [demo.run] java.lang.NullPointerException
		 [demo.run]     at com.ooc.CORBA.OutputStream.write_string(OutputStream.java:709
		)
		 [demo.run]     at demo.tests.StringSeqHelper.write(StringSeqHelper.java:92)
		 [demo.run]     at demo.tests._HelloWorldStub.putStringSeq(_HelloWorldStub.java:
		186)
		 [demo.run]     at demo.tests.Client.main(Client.java:64)
		
		
		 [demo.run] Java Result: -1
		 */
	  
	  if ( Arrays.equals( inDoubleSeq , server.putDoubleSeq( inDoubleSeq ) ) ) {
		  System.out.println ("Double Data OK!!");
	  }
	  else System.out.println("Double Data corrupted");
	  
	  
	  if ( Arrays.equals( inBoolSeq , server.putBoolSeq( inBoolSeq ) ) ) {
		  System.out.println ("Bool Data OK!!");
	  }
	  else System.out.println("Bool Data corrupted");
	 
	  if ( Arrays.equals( inCharSeq , server.putCharSeq( inCharSeq ) ) ) {
		  System.out.println ("Char Data OK!!");
	  }
	  else System.out.println("Char Data corrupted");
	  
	}
	catch (Exception e)
	{
		e.printStackTrace();
		System.exit(-1);
	}
    }
}
