package perf.jitter;

import java.io.*;

import org.omg.CORBA.ORB;
import javax.realtime.*;
import org.omg.PortableServer.*;
import perf.TimeStamp.*;

/**
 * This class implements a simple jitter time measurement using Ed Pla's timestamp
 * It's special for running RTZen on mock jvm library because jvm doesn't support java-version of NativeTimeStamp 
 * 
 *
 * @author <a href="mailto:hschirne@doc.ece.uci.edu">Gunar Schirner</a>
 * @version 1.0
 */

public class Client_jni extends RealtimeThread
{
	public static final int warmUpIterations =   5000;
	public static final int iterations       = 10000;

	public static int seqSize = 128;
	public static int testType =1;

	public static void main(String[] args) throws Exception
	{

		/* pass the input parameters */
		if ( args.length == 2 ) {
			Client_jni.testType = Integer.parseInt(args[0]);
			Client_jni.seqSize  = Integer.parseInt(args[1]);
                        System.out.println("Test for the sequence size of "+Client_jni.seqSize);
		}

		System.out.println( "=====================Creating RT Thread in client==========================" );
		RealtimeThread rt = (Client_jni) ImmortalMemory.instance().newInstance( Client_jni.class );
		System.out.println( "=====================Starting RT Thread in client==========================" );
		rt.start();
	}

	public Client_jni(){
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
			perf.TimeStamp.NativeTimeStamp rtts = new perf.TimeStamp.NativeTimeStamp();
			NativeTimeStamp.Init(1, 20.0);



			int i, j, k;
			long start, end;
			if ( testType == 0) {
				// calibration loop for the time stamp measurement itself
				System.out.println( "====================== Performance warmup =================================" );
				for( i=0;i<warmUpIterations;i++ )
					NativeTimeStamp.RecordTime(21);

				System.out.println( "====================== Performance benchmark Check Overhead  ====================" );
				// repeat the call 
				start = System.currentTimeMillis();

				// run the time stamp taker in the loop to measure
				// loop and time stamp taker overhead
				NativeTimeStamp.RecordTime(22);
				for(j=0; j<iterations;j++ ) {
					//NativeTimeStamp.RecordTime(20);
					NativeTimeStamp.RecordTime(21);
				}
				NativeTimeStamp.RecordTime(22);
				end = System.currentTimeMillis();

				// keep the old style measurements so we can cross verify the numbers
				System.err.println("Avg latency [ms]: " + ((end-start)/ ((float) iterations)) );

			} else if ( testType == 1 ) {

				byte inOctetSeq[] = new byte[seqSize];

				/* preinit the array */
				for ( j = 0; j < seqSize; j++ ) {
					inOctetSeq[j] = (byte) (j % 256);
				}


				System.out.println( "====================== Performance warmup =================================" );
				for( i=0;i<warmUpIterations;i++ ){
					NativeTimeStamp.RecordTime(21);
					server.putOctetSeq(inOctetSeq);
					NativeTimeStamp.RecordTime(21);
				}

				System.out.println( "====================== Performance benchmark BYTE =========================" );
				// repeat the call 
				start = System.currentTimeMillis();

				for(j=0; j<iterations;j++ ) {
					NativeTimeStamp.RecordTime(22);
					server.putOctetSeq(inOctetSeq);
                                        NativeTimeStamp.RecordTime(22);
					if( j % 100 == 0 ){
						edu.uci.ece.zen.utils.Logger.printMemStats( j );
					}
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

				/* short sequence */
				short inShortSeq[] = new short[seqSize];

				/* preinit the array */
				for (j = 0; j < seqSize; j++ ) {
					inShortSeq[j] = (short) (j % 65536);
				}

				System.out.println( "====================== Performance warmup =================================" );
				for( i=0;i<warmUpIterations;i++ ){
					NativeTimeStamp.RecordTime(21);
					server.putShortSeq(inShortSeq);
					NativeTimeStamp.RecordTime(21);
				}


				System.out.println( "====================== Performance benchmark SHORT ========================" );
				// repeat the call 
				start = System.currentTimeMillis();

				for(j=0; j<iterations;j++ ) {
                                        NativeTimeStamp.RecordTime(22);
					server.putShortSeq(inShortSeq);
					NativeTimeStamp.RecordTime(22);
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
