/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

	package demo.params;

import java.io.*;

import org.omg.CORBA.ORB;
import javax.realtime.*;
import org.omg.PortableServer.*;
import edu.uci.ece.zen.utils.Logger;


/*
 * This client class can be used to test different data types on 
 * RTZen. It sends and receives these data types by calling specific 
 * methods which are implemented in servant calss.
 *  
 * @author Hojjat Jafarpour
 *
 */



public class Client extends RealtimeThread
{
	
	static int[] array = new int[10];

    static
    {
        for (int i = 0; i < array.length; i++)
        {
            array[i] = i ;
        }
    }
    static int id;
    
    
    public static void main(String[] args) throws Exception
    {
        if(args.length > 0)
            runNum = Integer.parseInt(args[0]);
        System.out.println( "=====================Creating RT Thread in client==========================" );
        RealtimeThread rt = (Client) ImmortalMemory.instance().newInstance( Client.class );
        System.out.println( "=====================Starting RT Thread in client==========================" );
        rt.start();
    }

    public Client(){
        //super(null,new LTMemory(3000,300000));
    }

    public static int warmupNum = 5000;
    public static int runNum = 50000;

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
            final ParamTest server = ParamTestHelper.unchecked_narrow(object);
            System.out.println( "===================Connection established...sending request================" );
            //System.out.println( "Servant returned: " + server.getMessage(id , array) );
            
//            System.out.println( "Test for short >> Servant returned : " + server.test_short( (short)id ) );
            
//            System.out.println( "Test for long >> Servant returned: " + server.test_ulonglong( (long)id ) );
            
//            System.out.println( "Test for String >> Servant returned: " + server.test_unbounded_string( "String from Client!!!" ) );
            
//            System.out.println( "Test for char >> Servant returned: " + server.test_char( 'a' ) );
            
//            System.out.println( "Test for float >> Servant returned: " + server.test_float( (float)1.25 ) );
            
//            System.out.println( "Test for boolean >> Servant returned: " + server.test_boolean( true ) );
            
//            TestStruct ts = new TestStruct();
//            ts.a_short = (short)1;
//            ts.a_long = 2;
//            TestStruct test = server.test_struct(ts);
//            System.out.println( "Test for struct >> Servant returned: " + test.a_short +" , "+ test.a_long);
            
//            int[] result = server.test_sequence(array);
//            System.out.println("Test for sequence >> Servant retuened: "+result[0]);
            
//            TestUnion tu = new TestUnion();
//            System.out.println("Test for union >> initial param: "+tu);
//            TestUnion test = server.test_union(tu);
//            System.out.println("Test for union >> Servant retuened: "+test);
            
            demo.params.ParamTestPackage.TestEnum te =demo.params.ParamTestPackage.TestEnum.RTZEN;
            demo.params.ParamTestPackage.TestEnum result = server.test_enum(te);
            System.out.println("Test for enum >> Servant retuened: "+result);
            
            
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
