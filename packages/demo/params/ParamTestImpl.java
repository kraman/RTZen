/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.params;

/*
 * This class implements the methods declared in the idl file to
 * test different data types on RTZen
 * 
 * @author Hojjat Jafarpour
 *
 */

public class ParamTestImpl extends ParamTestPOA
{
	//short test
	// This method receives a short value and returns a short value
	public short test_short(short s1 )
	{
		System.out.println("Short received : "+s1);
		short result = (short)(s1+2);
		return result;
	}
	
	//long test
	//This method receives and sends long values
	public long test_ulonglong (long l)
	{
		System.out.println("Long received : "+l);
		long result = (long)(l+4);
		return result;
	}
	
	// strings unbounded
	public String test_unbounded_string (String str)
	{
		System.out.println("String received : "+str);
		return str;
	}

	//long test
	//This method receives and sends long values
	public char test_char (char ch)
	{
		System.out.println("Char received : "+ch);
		char result = ch;
		return result;
	}
	
	//float test
	public float test_float ( float f)
	{
		System.out.println("Char received : "+f);
		float result = (float)(f+1.25);
		return result;
		
	}
	
	// boolean test
	public boolean test_boolean( boolean b)
	{
		System.out.println("Char received : "+b);
		boolean result = !b;
		return result;

	}

	// struct test
	public TestStruct test_struct( TestStruct ts)
	{
		System.out.println("TestStruct.a_short received : "+ts.a_short);
		System.out.println("TestStruct.a_long received : "+ts.a_long);
		ts.a_short ++;
		ts.a_long ++;
		return ts;

	}
	
	//Sequence test
	public int[] test_sequence(int[] mySeq)
	{
		System.out.println("From client array received, Array[0] ="+mySeq[0]);
		for (int i = 0; i < mySeq.length; i++)
        {
            mySeq[i] = mySeq.length-i;
        }
		return mySeq;
	}
	
	//Union test
	public TestUnion test_union(TestUnion tu)
	{
		tu.val("Changed string val");
		System.out.println("Union ro client: "+tu);
		return tu;
	}
	
	//enum Test
	public demo.params.ParamTestPackage.TestEnum test_enum(demo.params.ParamTestPackage.TestEnum te)
	{
		System.out.println("From client: "+te);
		return te;
	}
	
    /**
     * Gets a message from the Hello World Server.
     */
    public int getMessage(int id , int[] array)
    {
        //System.out.println( "******************  WOOHOO! Request got here....now sending back. ********************" );
        //return "Hello To the Zen World!!!";
        return 42;
    }
}

