package unit.test.cdr;

import org.omg.CORBA.*;

public class DataTypesImpl extends DataTypesPOA

{

	public short echoShort( short shinVal , org.omg.CORBA.ShortHolder shoutVal) {

		if(shinVal != 100){
			System.out.println("in value wrong in echoShort()");
			System.out.println("The expected value is 100 and the actual value is "+shinVal);
	    }

	    short shretVal = shinVal;
	    shoutVal.value = 102;
	    return shretVal;
    }

    public double echoDouble( double dinVal , org.omg.CORBA.DoubleHolder doutVal)
	{

		    if(dinVal != 100){
						System.out.println("in value wrong in echoDouble()");
						System.out.println("The expected value is 1.0e10 and the actual value is "+dinVal);
				    }

	        double dretVal = dinVal;
	        doutVal.value = 1.0e12;
	        return dretVal;
    }

    public boolean echoBoolean( boolean binVal , org.omg.CORBA.BooleanHolder boutVal)
	    {
			 if(binVal != true){
									System.out.println("in value wrong in echoBoolean()");
									System.out.println("The expected value is true and the actual value is "+binVal);
				    }

	        boolean bretVal = binVal;
	        boutVal.value = false;

	        return bretVal;
	    }

	    public String echoString( String sinVal , org.omg.CORBA.StringHolder soutVal)
		    {

				if(sinVal != true){
													System.out.println("in value wrong in echoString()");
													System.out.println("The expected value is "in String" and the actual value is "+sinVal);
				    }

		        String sretVal = sinVal;
		        soutVal.value = "After";


		        return sretVal;
    }


    public short echoShort( octet oinVal , org.omg.CORBA.ShortHolder ooutVal) {

			if(oinVal != 100){
				System.out.println("in value wrong in echoOctet()");
				System.out.println("The expected value is 100 and the actual value is "+oinVal);
		    }

		    byte oretVal = oinVal;
		    ooutVal.value = (byte)102;
		    return shretVal;
    }


    /*

    public int echoLong( int iinVal , org.omg.CORBA.IntHolder ioutVal){

		(iinVal == 1000000)?:System.out.println("in value wrong in echoLong()");

		int iretVal = iinVal;
		ioutVal.value = 1000002;
		return iretVal;
    }

    public long echoLongLong( long  linVal , org.omg.CORBA.LongHolder loutVal){

		(linVal == 1000000000000)?:System.out.println("in value wrong in echoLongLong()");

		long lretVal = linVal;
		loutVal.value = 1000000000002;
		return lretVal;
	}

	public short echoUShort( int ushinVal , org.omg.CORBA.ShortHolder ushoutVal) {

			(ushinVal == 32768)?:System.out.println("in value wrong in echoUShort()");

		    int ushretVal = ushinVal;
		    ushoutVal.value = 32770;
		    return ushretVal;
    }*/

   /*

    public char echoChar( char inVal , org.omg.CORBA.CharHolder outVal) {
		(inVal == 'X')?:System.out.println("in value wrong in echoChar()");

        char retVal = inVal;
        outVal.value = 'O';
        return retVal;
    }

    public String echoString( String sinVal , org.omg.CORBA.StringHolder soutVal)
    {
		//System.out.println("****CDR Test: String *****");
        String sretVal = sinVal;
        soutVal.value = "out Value";

        //System.out.println("[server]In Value Received:"+ sinVal);
        //System.out.println("[server]Out Value Sent to the Client:"+ soutVal.value);
        //System.out.println("InOut Value:"+ inOutVal.value);
        //System.out.println("[server]Returning Value from server:"+sretVal);
        return sretVal;
    }

    public boolean echoBoolean( boolean binVal , org.omg.CORBA.BooleanHolder boutVal)
    {
        boolean bretVal = binVal;
        boutVal.value = false;

        //System.out.println("[server]In Value Received:"+ binVal);
        //System.out.println("[server]Out Value Sent to the Client:"+ boutVal.value);
        //System.out.println("InOut Value:"+ inOutVal.value);

        //System.out.println("[server]Returning Value from server:"+bretVal);
        return bretVal;
    }

    public double echoDouble( double dinVal , org.omg.CORBA.DoubleHolder doutVal)
    {
        double dretVal = dinVal;
        doutVal.value = 12.3;

        //System.out.println("[server]In Value Received:"+ dinVal);
        //System.out.println("[server]Out Value Sent to the Client:"+ doutVal.value);
        //System.out.println("InOut Value:"+ inOutVal.value);

        //System.out.println("[server]Returning Value from server:"+dretVal);
        return dretVal;
    }

	public float echoFloat( float finVal , org.omg.CORBA.FloatHolder foutVal)
    {

        float fretVal = finVal;
        foutVal.value = 12.3f;
        //System.out.println("[server]In Value Received:"+ finVal);
        //System.out.println("[server]Out Value Sent to the Client:"+ foutVal.value);
        //System.out.println("InOut Value:"+ inOutVal.value);
        //System.out.println("[server]Returning Value from server:"+fretVal);
        return fretVal;
    }







	public byte echoOctet(byte byinVal , org.omg.CORBA.ByteHolder byoutVal)
    {
        System.out.println("****CDR Test: Octet *****");

        byte byretVal = (byte)240;
        byoutVal.value = (byte)0xBC;

        System.out.println("[server]In Value Received:"+ byinVal);
        System.out.println("[server]Out Value Sent to the Client:"+ byoutVal.value);

        //System.out.println("InOut Value:"+ inOutVal.value);

        System.out.println("[server]Returning Value from server:"+byretVal);
        return byretVal;
    }


	public String[] echoStringSeq(String[] sseqinVal , test.cdrstreams.DataTypesPackage.stringSeqHolder sseqoutVal)
    {

		String[] sseqoutArr = {"out1after","out2after"};
		//String[] sseqretVal = {"returned string1","returned string2"};
		String[] sseqretVal = sseqinVal;

        sseqoutVal.value = sseqoutArr;

        //System.out.println("[server]In Value Received:"+ sseqinVal[0]+","+sseqinVal[1]);
        //System.out.println("[server]Out Value Sent to the Client:"+ sseqoutVal.value[0]+","+sseqoutVal.value[1]);

        //System.out.println("[server]Returning Value from server:"+sseqretVal[0]+","+sseqretVal[1]);
        return sseqretVal;
    }

	public char[] echoCharSeq(char[] cseqinVal , test.cdrstreams.DataTypesPackage.charSeqHolder cseqoutVal)
    {

        char[] cseqoutArr = {'E','F'};
		char[] cseqretVal = cseqinVal;

        cseqoutVal.value = cseqoutArr;

        //System.out.println("[server]In Value Received:");
        //System.out.println( cseqinVal[0]+","+cseqinVal[1]);
        //System.out.println("[server]Out Value Sent to the Client:");
        //System.out.println( cseqoutVal.value[0]+","+cseqoutVal.value[1]);
        //System.out.println("[server]Returning Value from server:");
        //System.out.println(cseqretVal[0]+","+cseqretVal[1]);
        return cseqretVal;
    }

	public boolean[] echoBooleanSeq(boolean[] bseqinVal , test.cdrstreams.DataTypesPackage.booleanSeqHolder bseqoutVal)
    {

	    boolean[] bseqoutArr = {false,true};
        //boolean[] bseqretVal = {false,false};
        boolean[] bseqretVal = bseqinVal;

        bseqoutVal.value = bseqoutArr;

        //System.out.println("[server]In Value Received:");
        //System.out.println( bseqinVal[0]+","+bseqinVal[1]);
        //System.out.println("[server]Out Value Sent to the Client:");
        //System.out.println( bseqoutVal.value[0]+","+bseqoutVal.value[1]);

        //System.out.println("[server]Returning Value from server:");
        //System.out.println(bseqretVal[0]+","+bseqretVal[1]);
        return bseqretVal;

    }

	public float[] echoFloatSeq(float[] fseqinVal , test.cdrstreams.DataTypesPackage.floatSeqHolder fseqoutVal)
    {

        float[] fseqoutArr = {4.345f,5.456f};
        float[] fseqretVal = fseqinVal;

        fseqoutVal.value = fseqoutArr;

        //System.out.println("[server]In Value Received:");
        //System.out.println( fseqinVal[0]+","+fseqinVal[1]);
        //System.out.println("[server]Out Value Sent to the Client:");
        //System.out.println( fseqoutVal.value[0]+","+fseqoutVal.value[1]);

        //System.out.println("[server]Returning Value from server:");
        //System.out.println(fseqretVal[0]+","+fseqretVal[1]);
        return fseqretVal;
    }

	public double[] echoDoubleSeq(double[] dseqinVal , test.cdrstreams.DataTypesPackage.doubleSeqHolder dseqoutVal)
	{
        double[] dseqoutArr = {4.345,5.456};
        double[] dseqretVal = {1.0e10, 2.3e10};

        dseqoutVal.value = dseqoutArr;

        System.out.println("[server]In Value Received:");
        System.out.println( dseqinVal[0]+","+dseqinVal[1]);
        System.out.println("[server]Out Value Sent to the Client:");
        System.out.println( dseqoutVal.value[0]+","+dseqoutVal.value[1]);

        System.out.println("[server]Returning Value from server:");
        System.out.println(dseqretVal[0]+","+dseqretVal[1]);
        return dseqretVal;
    }

	public int[] echoLongSeq(int[] iseqinVal , test.cdrstreams.DataTypesPackage.longSeqHolder iseqoutVal)
    {

	    int[] iseqoutArr = {4567,-5678};
        //int[] iseqretVal = {-4321, 5432};
        int[] iseqretVal = iseqinVal;

        iseqoutVal.value = iseqoutArr;

        //System.out.println("[server]In Value Received:");
        //System.out.println( iseqinVal[0]+","+iseqinVal[1]);
        //System.out.println("[server]Out Value Sent to the Client:");
        //System.out.println( iseqoutVal.value[0]+","+iseqoutVal.value[1]);

        //System.out.println("[server]Returning Value from server:");
        //System.out.println(iseqretVal[0]+","+iseqretVal[1]);
        return iseqretVal;

    }*/
}
