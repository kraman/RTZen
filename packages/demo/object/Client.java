package demo.object;

import java.io.*;

import org.omg.CORBA.ORB;
import javax.realtime.*;
import org.omg.PortableServer.*;
import edu.uci.ece.zen.utils.Logger;
/**
 * This class implements a simple Client for the Test Object
 * demo
 *
 * @author <a href="mailto:mpanahi@doc.ece.uci.edu">Mark Panahi</a>
 * @version 1.0
 */

public class Client extends RealtimeThread
{
    public static void main(String[] args) throws Exception
    {
        if(args.length > 0)
            runNum = Integer.parseInt(args[0]);
        System.out.println( "=====================Creating RT Thread in client==========================" );
        RealtimeThread rt = (Client) ImmortalMemory.instance().newInstance( Client.class );
        System.out.println( "=====================Starting RT Thread in client==========================" );
        rt.start();
    }
/*
    public Client(){
        //super(null,new LTMemory(3000,300000));
    }
*/
    public static int runNum = 10000;

    public void run()
    {
        try
        {

            System.out.println( "=====================Calling ORB Init in client============================" );
            ORB orb = ORB.init((String[])null, null);
            System.out.println( "=====================ORB Init complete in client===========================" );
            String ior = "";
            File iorfile = new File( "ior1.txt" );
            BufferedReader br = new BufferedReader( new FileReader(iorfile) );
            ior = br.readLine();
            System.out.println( "===========================IOR1 read========================================" );
            org.omg.CORBA.Object object1 = orb.string_to_object(ior);
            System.out.println( "===================Connect to TestObject Server1" );

            final TestObject server1 = TestObjectHelper.unchecked_narrow(object1);
            String name1 = server1.getName();
            br.close();

            ior ="";
            iorfile = new File( "ior2.txt" );
            br = new BufferedReader( new FileReader(iorfile) );
            ior = br.readLine();
            System.out.println( "===========================IOR2 read========================================" );
            org.omg.CORBA.Object object2 = orb.string_to_object(ior);
            System.out.println( "===================Connect to TestObject Server2");
            final TestObject server2 = TestObjectHelper.unchecked_narrow(object2);
             String name2 = server2.getName();
             
            br.close();

            ior ="";
            iorfile = new File( "ior3.txt" );
            br = new BufferedReader( new FileReader(iorfile) );
            ior = br.readLine();
            System.out.println( "===========================IOR3 read========================================" );
            org.omg.CORBA.Object object3 = orb.string_to_object(ior);
            System.out.println( "===================Connect to Exchange Object==========================" );
            final ExchangeObject server3 = ExchangeObjectHelper.unchecked_narrow(object3);
            br.close();

            for (int i = 0; i<runNum; i++){

                System.out.println("Test pass org.omg.CORBA.Object as the parameter...");


                org.omg.CORBA.Object objinVal = object1;
                org.omg.CORBA.ObjectHolder objoutVal = new org.omg.CORBA.ObjectHolder(object2);

                //System.out.println("The obj is "+obj);
                //System.out.println("The objoutVal is "+objoutVal);
                org.omg.CORBA.Object objretVal = server3.exchange(objinVal, objoutVal);

                TestObject server4 = TestObjectHelper.unchecked_narrow( objretVal );TestObject server5 = TestObjectHelper.unchecked_narrow(objoutVal.value);                    if(server4.getName().equals(name1) && server5.getName().equals(name1)){
                    System.out.println("Pass the test");
                }
                if(!server4.getName().equals(name1)){

                    System.out.println("objretVal String Should be "+name1+" but get "+server4.getName());
                }
                if(!server5.getName().equals(name1)){
                    System.out.println("objoutVal String Should be "+name1+" but get "+server5.getName());
                }


            }






                            System.exit(0);
                            }
                            catch (Exception e)
                            {
                            e.printStackTrace();
                            System.exit(-1);
                            }
                            }
                            }
