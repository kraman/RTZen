package unit.test.cdr;

import org.omg.CORBA.*;
import javax.realtime.RealtimeThread;
import javax.realtime.LTMemory;
import javax.realtime.ImmortalMemory;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * This class implements a simple Client for the CDR test
 *
 * @author <a href="mailto:yuez@doc.ece.uci.edu">Yue Zhang</a>
 * @version 1.0
 */

public class CDRTestClient extends RealtimeThread
{

	private DataTypes stub;

    public static void main( String[] args ){
        try{
            //CDRTestClient rt = (CDRTestClient)( new RealtimeThread (null, null, null, new LTMemory( 3000, 300000), null, null));
            RealtimeThread rt = (CDRTestClient) ImmortalMemory.instance().newInstance( CDRTestClient.class );
            rt.start();
        }
        catch(Exception ex){
            ex.printStackTrace();
            System.exit(-1);
        }

    }

    public void run(){
		try{

			ORB orb = ORB.init((String[]) null, null);

			BufferedReader br = new BufferedReader (new FileReader( "ior.txt" ));
			String ior = br.readLine();

			org.omg.CORBA.Object obj = orb.string_to_object( ior );
			stub = DataTypesHelper.unchecked_narrow( obj );

			testShort();
			//testLong();
			//testLongLong();
			//testUShort();


		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit( -1 );
		}
	}

	public void testShort(){

		short shinVal = 100;
		ShortHolder shoutVal = new ShortHolder((short)101);
	    short shretVal = stub.echoShort( shinVal, shoutVal);

	    if(shretVal != 100){
		    System.out.println("return value wrong in echoShort()");
		    System.out.println("The expected value is 100 and the actual value is "+shretVal);
	    }

	    if(shoutVal.value != 102){
		    System.out.println("out value wrong in echoShort()");
		    System.out.println("The expected value is 100 and the actual value is "+shoutVal);

	    }
	}
	/*
    public void testLong(){

		int iinVal = 1000000;
		IntHolder ioutVal = new IntHolder(1000001);
	    int iretVal = datatypes.echoLong( iinVal, ioutVal);

	    iretVal == 1000000)?:System.out.println("return value wrong in echoLong()");
        (icoutVal.value == 1000002)?:System.out.println("out value wrong in echoLong()");
    }

    public void testLongLong(){

		long linVal = 1000000000000;
		LongHolder loutVal = new LongHolder(1000000000001);
		long lretVal = datatypes.echoLongLong( linVal, loutVal);


		lretVal == 1000000000000)?:System.out.println("return value wrong in echoLongLong()");
        (lcoutVal.value == 1000000000002)?:System.out.println("out value wrong in echoLongLong()");
    }

    public void testUShort(){

		int ushinVal = 32678;
		UShortHolder ushoutVal = new UShortHolder(32769);
		int ushretVal = datatypes.echoUShort( ushinVal, ushoutVal);
		(ushcretVal == 32768)?:System.out.println("return value wrong in echoUShort()");
		(ushcoutVal.value == 32770)?:System.out.println("out value wrong in echoUShort()");
    }*/

/*
	public void testChar()
    {
        char cinVal = 'X';
        CharHolder coutVal = new CharHolder('B');
        char cretVal = datatypes.echoChar( cinVal, coutVal);

        (cretVal == 'X')?:System.out.println("return value wrong in echoChar()");
        (coutVal.value == 'O')?:System.out.println("out value wrong in echoChar()");
    }

	public void testString()
    {

        String sinVal = "in String";
        StringHolder soutVal = new StringHolder("Before");
        //System.out.println("[client]InValue Sent to Server:"+ sinVal);
        //System.out.println("[client]Out value before = "+ soutVal.value);
        String sretVal = datatypes.echoString( sinVal, soutVal);
		assertEquals("in String", sretVal);
        assertEquals("out Value", soutVal.value);
        //System.out.println("[client]Out value after= "+ soutVal.value);
        //System.out.println("[client]Received Returned Value = "+ sretVal);
    }

	public void testBoolean()
    {

        boolean binVal = true;
        org.omg.CORBA.BooleanHolder boutVal =
                                new org.omg.CORBA.BooleanHolder(true);
        //System.out.println("[client]InValue Sent to Server:"+ binVal);
        //System.out.println("[client]Out value before = "+ boutVal.value);

        boolean bretVal = datatypes.echoBoolean( binVal, boutVal);
		assertEquals(true, bretVal);
        assertEquals(false, boutVal.value);
        //System.out.println("[client]Out value after= "+ boutVal.value);
        //System.out.println("[client]Received Returned Value = "+ bretVal);
    }

	public void testDouble()
    {

        double dinVal = 1.0e10;
        org.omg.CORBA.DoubleHolder doutVal =
                                new org.omg.CORBA.DoubleHolder( 1.2345);
        //System.out.println("[client]InValue Sent to Server:"+ dinVal);
        //System.out.println("[client]Out value before = "+ doutVal.value);
        double dretVal = datatypes.echoDouble( dinVal, doutVal);
		assertEquals(1.0e10, dretVal,0.0001);
        assertEquals(12.3, doutVal.value,0.0001);

        //System.out.println("[client]Out value after= "+ doutVal.value);
        //System.out.println("[client]Received Returned Value = "+ dretVal);
    }

	public void testFloat()
    {

        float finVal = 1.0e10f;
        org.omg.CORBA.FloatHolder foutVal =
                            new org.omg.CORBA.FloatHolder(1.2345f);
        //System.out.println("[client]InValue Sent to Server:"+ finVal);
        //System.out.println("[client]Out value before = "+ foutVal.value);

        float fretVal = datatypes.echoFloat( finVal, foutVal);
		assertEquals(1.0e10f, fretVal,0.0001);
        assertEquals(12.3f, foutVal.value,0.0001);

        //System.out.println("[client]Out value after= "+ foutVal.value);
        //System.out.println("[client]Received Returned Value = "+ fretVal);

    }

	public void testCharSeq()
    {

        char[] cseqinVal = {'A','B'};
        char[] cseqoutArr = {'C','D'};

        test.cdrstreams.DataTypesPackage.charSeqHolder cseqoutVal =
               new test.cdrstreams.DataTypesPackage.charSeqHolder(cseqoutArr);
        //System.out.println("[client]InValue Sent to Server:");
        //System.out.println( cseqinVal[0]+","+cseqinVal[1]);
        //System.out.println("[client]Out value before : ");
        //System.out.println( cseqoutVal.value[0]+","+ cseqoutVal.value[1]);

        char[] cseqretVal = datatypes.echoCharSeq( cseqinVal, cseqoutVal);
        //System.out.println("[client]Out value after: ");
        //System.out.println(cseqoutVal.value[0]+","+cseqoutVal.value[1]);
        //System.out.println("[client]Received Return Value : ");
        //System.out.println( cseqretVal[0]+","+cseqretVal[1]);

		assertEquals(2, cseqretVal.length);
		assertEquals('A', cseqretVal[0]);
        assertEquals('B',cseqretVal[1]);

		assertEquals(2, cseqoutVal.value.length);
        assertEquals('E', cseqoutVal.value[0]);
        assertEquals('F',cseqoutVal.value[1]);

    }

	public void testStringSeq()
    {

        String[] sseqinVal = {"Doc Group","UC Irvine"};
        String[] sseqoutArr = {"out1before","out2before"};

        test.cdrstreams.DataTypesPackage.stringSeqHolder sseqoutVal =
               new test.cdrstreams.DataTypesPackage.stringSeqHolder(sseqoutArr);
        //System.out.println("[client]InValue Sent to Server:");
        //System.out.println( sseqinVal[0]+","+sseqinVal[1]);
        //System.out.println("[client]Out value before : ");
        //System.out.println(sseqoutVal.value[0]+","+sseqoutVal.value[1]);

        String[] sseqretVal = datatypes.echoStringSeq( sseqinVal, sseqoutVal);
        //System.out.println("[client]Out value after: ");
        //System.out.println( sseqoutVal.value[0]+","+sseqoutVal.value[1]);
        //System.out.println("[client]Received Return Value : ");
        //System.out.println( sseqretVal[0]+","+sseqretVal[1]);

		assertEquals(2, sseqretVal.length);
        assertEquals("Doc Group",sseqretVal[0]);
        assertEquals("UC Irvine",sseqretVal[1]);

        assertEquals(2 , sseqoutVal.value.length);
        assertEquals("out1after",sseqoutVal.value[0]);
        assertEquals("out2after",sseqoutVal.value[1]);

    }


	public void testBooleanSeq()
    {

		boolean[] bseqinVal = {true, true};
        boolean[] bseqoutArr = {true, false};

        test.cdrstreams.DataTypesPackage.booleanSeqHolder bseqoutVal =
              new test.cdrstreams.DataTypesPackage.booleanSeqHolder(bseqoutArr);
        //System.out.println("[client]InValue Sent to Server:");
        //System.out.println( bseqinVal[0]+","+bseqinVal[1]);
        //System.out.println("[client]Out value before : ");
        //System.out.println( bseqoutVal.value[0]+","+ bseqoutVal.value[1]);

        boolean[] bseqretVal = datatypes.echoBooleanSeq( bseqinVal, bseqoutVal);
        //System.out.println("[client]Out value after: ");
        //System.out.println(bseqoutVal.value[0]+","+bseqoutVal.value[1]);
        //System.out.println("[client]Received Return Value : ");
        //System.out.println( bseqretVal[0]+","+bseqretVal[1]);

		assertEquals(2, bseqretVal.length);
        assertEquals(true,bseqretVal[0]);
        assertEquals(true,bseqretVal[1]);

        assertEquals(2 , bseqoutVal.value.length);
        assertEquals(false,bseqoutVal.value[0]);
        assertEquals(true,bseqoutVal.value[1]);

    }

	public void testLongSeq()
    {
        int[] iseqinVal = {1234, 2345};
        int[] iseqoutArr = {2345, 3456};

        test.cdrstreams.DataTypesPackage.longSeqHolder iseqoutVal =
               new test.cdrstreams.DataTypesPackage.longSeqHolder(iseqoutArr);
        //System.out.println("[client]InValue Sent to Server:");
        //System.out.println( iseqinVal[0]+","+iseqinVal[1]);
        //System.out.println("[client]Out value before : ");
        //System.out.println( iseqoutVal.value[0]+","+ iseqoutVal.value[1]);

        int[] iseqretVal = datatypes.echoLongSeq( iseqinVal, iseqoutVal);
        //System.out.println("[client]Out value after: ");
        //System.out.println(iseqoutVal.value[0]+","+iseqoutVal.value[1]);
        //System.out.println("[client]Received Return Value : ");
        //System.out.println( iseqretVal[0]+","+iseqretVal[1]);

		assertEquals(2, iseqretVal.length);
        assertEquals(1234,iseqretVal[0]);
        assertEquals(2345,iseqretVal[1]);

        assertEquals(2 , iseqoutVal.value.length);
        assertEquals(4567,iseqoutVal.value[0]);
        assertEquals(-5678,iseqoutVal.value[1]);

    }

	public void testFloatSeq()
    {

        float[] fseqinVal = {1.234f, 2.3456f};
        float[] fseqoutArr = {2.345f, 3.456f};

        test.cdrstreams.DataTypesPackage.floatSeqHolder fseqoutVal =
               new test.cdrstreams.DataTypesPackage.floatSeqHolder(fseqoutArr);
        //System.out.println("[client]InValue Sent to Server:");
        //System.out.println( fseqinVal[0]+","+fseqinVal[1]);
        //System.out.println("[client]Out value before : ");
        //System.out.println( fseqoutVal.value[0]+","+ fseqoutVal.value[1]);

        float[] fseqretVal = datatypes.echoFloatSeq( fseqinVal, fseqoutVal);
        //System.out.println("[client]Out value after: ");
        //System.out.println(fseqoutVal.value[0]+","+fseqoutVal.value[1]);
        //System.out.println("[client]Received Return Value : ");
        //System.out.println( fseqretVal[0]+","+fseqretVal[1]);


		assertEquals(2, fseqretVal.length);
		assertEquals(1.234f, fseqretVal[0],0.0001);
		assertEquals(2.3456f, fseqretVal[1],0.0001);

		assertEquals(2, fseqoutVal.value.length);
        assertEquals(4.345f, fseqoutVal.value[0],0.0001);
        assertEquals(5.456f, fseqoutVal.value[1],0.0001);

    } */


}

