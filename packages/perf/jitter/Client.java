package perf.jitter;

import java.io.*;

import org.omg.CORBA.ORB;
import javax.realtime.*;
import org.omg.PortableServer.*;
import perf.TimeStamp.*;

/**
 * This class implements a simple jitter time measurement 
 * 
 *
 * @author <a href="mailto:hschirne@doc.ece.uci.edu">Gunar Schirner</a>
 * @version 1.0
 */

public class Client extends RealtimeThread
{
	public static final int iterations = 10000;

	public static int seqSize = 4;
	public static int testType =1;

    public static void main(String[] args) throws Exception
    {

		/* pass the input parameters */
		if ( args.length == 2 ) {
			Client.testType = Integer.parseInt(args[0]);
			Client.seqSize  = Integer.parseInt(args[1]);
		}

        System.out.println( "=====================Creating RT Thread in client==========================" );
        RealtimeThread rt = (Client) ImmortalMemory.instance().newInstance( Client.class );
        System.out.println( "=====================Starting RT Thread in client==========================" );
        rt.start();
    }

    public Client(){
        //super(null,new LTMemory(3000,300000));
    }

    public void run()
    {
        try
			{
				System.out.println( "=====================Calling ORB Init in client============================" );
				ORB orb = ORB.init((String[])null, null);
				System.out.println( "=====================ORB Init complete in client===========================" );
				String ior = "";
				File iorfile = new File( "ior.txt" );
				BufferedReader br = new BufferedReader( new FileReader(iorfile) );
				ior = br.readLine();
				System.out.println( "===========================IOR read========================================" );
				org.omg.CORBA.Object object = orb.string_to_object(ior);
				System.out.println( "===================Trying to establish connection==========================" );
				HelloWorld server = HelloWorldHelper.unchecked_narrow(object);
				// System.out.println( server.getMsg() );
	

				// allocate the time stamper (to load the native lib)
				NativeTimeStamp rtts = new NativeTimeStamp();
				NativeTimeStamp.Init(1, 20.0);
			
        
				// System.out.println( "====================== Performance warmup =================================" );
				// for( int i=0;i<10000;i++ )
				//server.getMsg();
		
				int i, j, k;
				long start, end;
				if ( testType == 1 ) {
					System.out.println( "====================== Performance benchmark BYTE =========================" );

					byte inOctetSeq[] = new byte[seqSize];
				
					/* preinit the array */
					for ( j = 0; j < seqSize; j++ ) {
						inOctetSeq[j] = (byte) (j % 256);
					}

					// repeat the call 
					start = System.currentTimeMillis();
				
					for(j=0; j<iterations;j++ ) {
						//NativeTimeStamp.RecordTime(20);
						server.putOctetSeq(inOctetSeq);
						NativeTimeStamp.RecordTime(21);
					}
					end = System.currentTimeMillis();

					// keep the old style measurements so we can cross verify the numbers
					System.err.println("Avg latency [ms]: " + ((end-start)/ ((float) iterations)) );
			

					/* now make a test with the sleep functionality */
					/*
					  try {
					  for (int z=0;z<10;z++) {
					  Thread.sleep(1000);
					  NativeTimeStamp.RecordTime(22);
					  }
					  } catch (Exception e) {
					  e.printStackTrace();
					  }
					*/

				} else if (testType == 2) {
				
					System.out.println( "====================== Performance benchmark SHORT ========================" );
					/* short sequence */
					short inShortSeq[] = new short[seqSize];
			
					/* preinit the array */
					for (j = 0; j < seqSize; j++ ) {
						inShortSeq[j] = (short) (j % 65536);
					}
			
					// repeat the call 
					start = System.currentTimeMillis();
			
					for(j=0; j<iterations;j++ ) {
						server.putShortSeq(inShortSeq);
						NativeTimeStamp.RecordTime(21);
					}
					end = System.currentTimeMillis();
			
					// keep the old style measurements so we can cross verify the numbers
					System.err.println("Avg latency [ms]: " + ((end-start)/ ((float) iterations)) );
			
				} else {
					System.out.println("Unknown test type " + testType);
					System.exit(-1);
				}

				NativeTimeStamp.OutputLogRecords();

			}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		System.exit(0);
    }
}
