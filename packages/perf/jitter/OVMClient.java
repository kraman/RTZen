package perf.jitter;

import java.io.*;

import org.omg.CORBA.ORB;
import javax.realtime.*;
import org.omg.PortableServer.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * This class implements a simple jitter time measurement 
 * 
 *
 * @author <a href="mailto:yuez@doc.ece.uci.edu">Yue Zhang</a>
 * @version 1.0
 */

public class OVMClient extends RealtimeThread
{
	public static final int warmUpIterations =   1000;
	public static final int iterations       = 1000;

	public static int seqSize = 4; //default value of seqSize, can change by arguments
    
	public static int testType =1; //default value of testType, can change by arguments    

    public static void main(String[] args) throws Exception
    {

		/* pass the input parameters */
		if ( args.length == 2 ) {
			OVMClient.testType = Integer.parseInt(args[0]);
			OVMClient.seqSize  = Integer.parseInt(args[1]);
		}

        System.out.println( "=====================Creating RT Thread in client==========================" );
        RealtimeThread rt = (OVMClient) ImmortalMemory.instance().newInstance( OVMClient.class );
        System.out.println( "=====================Starting RT Thread in client==========================" );
        rt.start();
    }

    public OVMClient(){
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


                AbsoluteTime[] timeStampsArray = new AbsoluteTime[iterations];                         
                javax.realtime.Clock clock = javax.realtime.Clock.getRealtimeClock();			
                RelativeTime frequency = clock.getResolution();
                

		//  System.out.println("The time frequency is "+((RationalTime)frequency).getFrequency());



		int i, j, k;
		long start, end;
		if ( testType == 0) {
			// calibration loop for the time stamp measurement itself
			System.out.println( "====================== Performance warmup =================================" );
			for( i=0;i<warmUpIterations;i++ )
				//NativeTimeStamp.RecordTime(21);

				System.out.println( "====================== Performance benchmark Check Overhead  ====================" );
			// repeat the call 
			start = System.currentTimeMillis();

			// run the time stamp taker in the loop to measure
			// loop and time stamp taker overhead
			//NativeTimeStamp.RecordTime(22);
			for(j=0; j<iterations;j++ ) {
				//NativeTimeStamp.RecordTime(20);
				timeStampsArray[j] = clock.getTime();
				//NativeTimeStamp.RecordTime(21);
			}
			//NativeTimeStamp.RecordTime(22);
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
					for( i=0;i<warmUpIterations;i++ ) {
						server.putOctetSeq(inOctetSeq);
						//NativeTimeStamp.RecordTime(20);
                    }

					System.out.println( "====================== Performance benchmark BYTE =========================" );
					// repeat the call 
					start = System.currentTimeMillis();
				
					for(j=0; j<iterations;j++ ) {
						//NativeTimeStamp.RecordTime(20);
						server.putOctetSeq(inOctetSeq);
                        if( j % 100 == 0 ){
                            edu.uci.ece.zen.utils.Logger.printMemStats( j );
                        }
						//NativeTimeStamp.RecordTime(21);
                        timeStampsArray[j] = clock.getTime();
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
					for( i=0;i<warmUpIterations;i++ )
						server.putShortSeq(inShortSeq);

					System.out.println( "====================== Performance benchmark SHORT ========================" );
					// repeat the call 
					start = System.currentTimeMillis();
			
					for(j=0; j<iterations;j++ ) {
						server.putShortSeq(inShortSeq);
						//NativeTimeStamp.RecordTime(21);
					}
					end = System.currentTimeMillis();
			
					// keep the old style measurements so we can cross verify the numbers
					System.err.println("Avg latency [ms]: " + ((end-start)/ ((float) iterations)) );
			
				} else {
					System.out.println("Unknown test type " + testType);
					System.exit(-1);
				}

				//NativeTimeStamp.OutputLogRecords();
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("timeRecords.txt")));
                for(int m = 0; m<timeStampsArray.length; m++){
                    out.println(m+","+"21,"+timeStampsArray[m]);
                }
                out.flush();
                out.close();     
			}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		System.exit(0);
    }
}
