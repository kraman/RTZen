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
    private org.omg.CORBA.Object tempobj;

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
            tempobj = obj;
			stub = DataTypesHelper.unchecked_narrow( obj );

			testShort();
			testDouble();

			testBoolean();
			testString();
			testOctet();
			testOctetSeq();
			testStructSeq();
			testonewayShort();
			//testShort();                        
			//testObject();
			testLongLong();



		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit( -1 );
		}
	}

	public void testShort(){
        System.out.println("Test Short");

		short shinVal = 100;
		ShortHolder shoutVal = new ShortHolder((short)101);
		short shretVal = stub.echoShort( shinVal, shoutVal);

		if(shretVal != 100){
			System.out.println("return value wrong in echoShort()");
			System.out.println("The expected value is 100 and the actual value is "+shretVal);
		}

		if(shoutVal.value != 102){
			System.out.println("out value wrong in echoShort()");
			System.out.println("The expected value is 102 and the actual value is "+shoutVal.value);

		}
	}
	/*
	   public void testLongLong(){

	   long llinVal = (long)Math.pow((double)2,(double)36);
	   LongHolder lloutVal = new LongHolder((long)Math.pow((double)2,(double)37));
	   long llretVal = stub.echoLongLong( llinVal, lloutVal);

	   if(llretVal != (long)Math.pow((double)2,(double)36)){
	   System.out.println("return value wrong in echoLongLong()");
	   System.out.println("The expected value is"+(long)Math.pow((double)2,(double)36)+ " and the actual value is "+llretVal);
	   }

	   if(lloutVal.value != (long)Math.pow((double)2,(double)38)){
	   System.out.println("out value wrong in echoLongLong()");
	   System.out.println("The expected value is"+(long)Math.pow((double)2,(double)38)+" and the actual value is "+lloutVal.value);

	   }
	   }
	 */
	public void testLongLong(){

         System.out.println("Test LongLong");
         

		long llinVal = (long)1;
		LongHolder lloutVal = new LongHolder((long)2);
		long llretVal = stub.echoLongLong( llinVal, lloutVal);

		if(llretVal != (long)1){
			System.out.println("return value wrong in echoLongLong()");
			System.out.println("The expected value is"+(long)1+" The actual value is "+llretVal);
		}

		if(lloutVal.value != (long)3){
			System.out.println("out value wrong in echoLongLong()");
			System.out.println("The expected value is"+(long)3+" the actual value is "+lloutVal.value);

		}
	}





	public void testDouble()
	{
        System.out.println("Test Double");        

		double dinVal = 1.123456789;
		org.omg.CORBA.DoubleHolder doutVal =
			new org.omg.CORBA.DoubleHolder( 2.123456789);
		double dretVal = stub.echoDouble( dinVal, doutVal);


		if(dretVal != 1.123456789){
			System.out.println("return value wrong in echoDouble()");
			System.out.println("The expected value is 1.123456789 and the actual value is "+dretVal);
		}

		if(doutVal.value != 3.123456789){
			System.out.println("out value wrong in echoDouble()");
			System.out.println("The expected value is 3.123456789 and the actual value is "+doutVal.value);

		}
	}

	public void testBoolean(){
        System.out.println("Test Boolean");        

		boolean binVal = true;
		org.omg.CORBA.BooleanHolder boutVal =
			new org.omg.CORBA.BooleanHolder(true);
		boolean bretVal = stub.echoBoolean( binVal, boutVal);
		if(bretVal != true){
			System.out.println("return value wrong in echoBoolean()");
			System.out.println("The expected value is true and the actual value is "+bretVal);
		}

		if(boutVal.value != false){
			System.out.println("out value wrong in echoBoolean()");
			System.out.println("The expected value is false and the actual value is "+boutVal.value);

		}

	}


	public void testString()
	{

        System.out.println("Test String");
        

		String sinVal = "in String";
		StringHolder soutVal = new StringHolder("Before");
		String sretVal = stub.echoString( sinVal, soutVal);


		if(!sretVal.equals("in String")){
			System.out.println("return value wrong in echoString()");
			System.out.println("The expected value is \"in String\" and the actual value is "+sretVal);
		}

		if(!soutVal.value.equals("After")){
			System.out.println("out value wrong in echoString()");
			System.out.println("The expected value is \"After\" and the actual value is "+soutVal.value);

		}



	}


	public void testOctet(){
        System.out.println("Test Octet");        

		byte oinVal = 100;
		ByteHolder ooutVal = new ByteHolder((byte)101);
		byte oretVal = stub.echoOctet( oinVal, ooutVal);

		if(oretVal != 100){
			System.out.println("return value wrong in echoOctet()");
			System.out.println("The expected value is 100 and the actual value is "+oretVal);
		}

		if(ooutVal.value != 102){
			System.out.println("out value wrong in echoOctet()");
			System.out.println("The expected value is 102 and the actual value is "+ooutVal.value);

		}
	}

	public void testOctetSeq()
	{
        System.out.println("Test OctetSeq");
        
		byte[] oseqinVal = {100, 101};
		byte[] oseqoutArr = {102, 103, 104};

		unit.test.cdr.DataTypesPackage.octetSeqHolder oseqoutVal =
			new unit.test.cdr.DataTypesPackage.octetSeqHolder(oseqoutArr);

		byte[] oseqretVal = stub.echoOctetSeq( oseqinVal, oseqoutVal);

		if(oseqretVal.length != 2){
			System.out.println("return value length wrong in testOctetSeq");
			System.out.println("The expected lenght is 2 and the actual lenght is "+oseqretVal.length);
		}

		if(oseqretVal[0] != 100){
			System.out.println("return value wrong in testOctetSeq");
			System.out.println("The expected value at positon 0 is 100 and the actual lenght is "+oseqretVal[0]);
		}

		if(oseqretVal[1] != 101){
			System.out.println("return value wrong in testOctetSeq");
			System.out.println("The expected value at positon 1 is 101 and the actual lenght is "+oseqretVal[1]);
		}

		if(oseqoutVal.value.length != 3){
			System.out.println("out value length wrong in testOctetSeq");
			System.out.println("The expected lenght is 3 and the actual lenght is "+oseqoutVal.value.length);
		}

		if(oseqoutVal.value[0] != 105){

			System.out.println("out value wrong in testOctetSeq");
			System.out.println("The expected value at positon 0 is 105 and the actual lenght is "+oseqoutVal.value[0]);
		}

		if(oseqoutVal.value[1] != 106){
			System.out.println("out value wrong in testOctetSeq");
			System.out.println("The expected value at positon 1 is 106 and the actual lenght is "+oseqoutVal.value[1]);
		}

		if(oseqoutVal.value[2] != 107){
			System.out.println("out value wrong in testOctetSeq");
			System.out.println("The expected value at positon 2 is 107 and the actual lenght is "+oseqoutVal.value[2]);
		}
	}

	public void testStructSeq(){
        System.out.println("Test StructSeq");
        

		unit.test.cdr.DataTypesPackage.str str1= new unit.test.cdr.DataTypesPackage.str((byte)100,true);
		unit.test.cdr.DataTypesPackage.str str2= new unit.test.cdr.DataTypesPackage.str((byte)101,true);

		unit.test.cdr.DataTypesPackage.str[] strseqinVal = new unit.test.cdr.DataTypesPackage.str[2];
		strseqinVal[0] = str1;
		strseqinVal[1] = str2;

		unit.test.cdr.DataTypesPackage.str str3= new unit.test.cdr.DataTypesPackage.str((byte)102,false);
		unit.test.cdr.DataTypesPackage.str str4= new unit.test.cdr.DataTypesPackage.str((byte)103,false);

		unit.test.cdr.DataTypesPackage.str[] strseqArr = new unit.test.cdr.DataTypesPackage.str[2];
		strseqArr[0] = str3;
		strseqArr[1] = str4;

		unit.test.cdr.DataTypesPackage.strSeqHolder strseqoutVal=  new unit.test.cdr.DataTypesPackage.strSeqHolder(strseqArr);

		unit.test.cdr.DataTypesPackage.str[] strseqretVal = stub.echoStructSeq( strseqinVal, strseqoutVal);

		if(strseqretVal.length != 2){
			System.out.println("return value length wrong in echoStructSeq");
			System.out.println("The expected lenght is 2 and the actual lenght is "+strseqretVal.length);
		}

		if(strseqretVal[0].a != 100){
			System.out.println("return value wrong in echoStructSeq");
			System.out.println("The expected a value at positon 0 is 100 and the actual value is "+strseqretVal[0].a);
		}

		if(strseqretVal[0].b != true){
			System.out.println("return value wrong in echoStructSeq");
			System.out.println("The expected b value at positon 0 is true and the actual value is "+strseqretVal[0].b);
		}

		if(strseqretVal[1].a != 101){
			System.out.println("return value wrong in echoStructSeq");
			System.out.println("The expected a value at positon 1 is 101 and the actual value is "+strseqretVal[1].a);
		}

		if(strseqretVal[1].b != true){
			System.out.println("return value wrong in echoStructSeq");
			System.out.println("The expected b value at positon 1 is true and the actual value is "+strseqretVal[1].b);
		}

		if(strseqoutVal.value.length != 2){
			System.out.println("out value length wrong in echoStructSeq");
			System.out.println("The expected lenght is 2 and the actual lenght is "+strseqoutVal.value.length);
		}


		if(strseqoutVal.value[0].a != 104){
			System.out.println("out value wrong in echoStructSeq");
			System.out.println("The expected a value at positon 0 is 104 and the actual value is "+strseqoutVal.value[0].a);
		}

		if(strseqoutVal.value[0].b != true){
			System.out.println("out value wrong in echoStructSeq");
			System.out.println("The expected b value at positon 0 is true and the actual value is "+strseqoutVal.value[0].b);
		}

		if(strseqoutVal.value[1].a != 105){
			System.out.println("out value wrong in echoStructSeq");
			System.out.println("The expected a value at positon 1 is 105 and the actual value is "+strseqoutVal.value[1].a);
		}

		if(strseqoutVal.value[1].b != true){
			System.out.println("out value wrong in echoStructSeq");
			System.out.println("The expected b value at positon 1 is true and the actual value is "+strseqoutVal.value[1].b);
		}
	}

	public void testonewayShort(){

        System.out.println("Test onewayShort");   


		short shinVal = 100;

		stub.onewayShort( shinVal);


		return;


	}

	public void testObject(){
        System.out.println("Test Object");
        

		org.omg.CORBA.Object objinVal = tempobj;
		org.omg.CORBA.ObjectHolder objoutVal = new org.omg.CORBA.ObjectHolder(tempobj);

		org.omg.CORBA.Object objretVal = stub.echoObject(objinVal, objoutVal);

		return;

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


