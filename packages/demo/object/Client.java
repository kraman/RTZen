package demo.object;

import java.io.*;

import org.omg.CORBA.ORB;
import javax.realtime.*;
import org.omg.PortableServer.*;
import edu.uci.ece.zen.utils.Logger;
/**
 * This class implements a simple Client for the Object test
 * demo
 *
 * @author <a href="mailto:yuez@doc.ece.uci.edu">Yue Zhang</a>
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
    public static int warmUp = 10000;
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
            final org.omg.CORBA.Object object1 = orb.string_to_object(ior);
            System.out.println( "===================Connect to TestObject Server1" );
            final TestObject server1 = TestObjectHelper.unchecked_narrow(object1);
            String name1 = server1.getName(); //name1 should be "Mary"
            br.close();

            ior ="";
            iorfile = new File( "ior2.txt" );
            br = new BufferedReader( new FileReader(iorfile) );
            ior = br.readLine();
            System.out.println( "===========================IOR2 read========================================" );
            final org.omg.CORBA.Object object2 = orb.string_to_object(ior);
            System.out.println( "===================Connect to TestObject Server2");
            final TestObject server2 = TestObjectHelper.unchecked_narrow(object2);
            String name2 = server2.getName(); //name2 should be "Roby"

            br.close();

            ior ="";
            iorfile = new File( "ior3.txt" );
            br = new BufferedReader( new FileReader(iorfile) );
            ior = br.readLine();
            System.out.println( "===========================IOR3 read========================================" );
            org.omg.CORBA.Object object3 = orb.string_to_object(ior);
            System.out.println( "===================Connect to HandleObject server==========================" );
            final HandleObject server3 = HandleObjectHelper.unchecked_narrow(object3);
            br.close();
             org.omg.CORBA.ObjectHolder objoutVal = new org.omg.CORBA.ObjectHolder(object2); //objoutVal will be changed to object1 after the echoObject call
             
            System.out.println("======Begin the warmUp loop to test on passing org.omg.CORBA.Object as the parameter======");

            for (int i = 0; i<warmUp; i++){

                //System.out.println("Begin to test on passing org.omg.CORBA.Object as the parameter...");
                org.omg.CORBA.Object objinVal = object1;
                objoutVal.value = object2; //objoutVal will be changed to object1 after the echoObject call
                org.omg.CORBA.Object objretVal = server3.echoObject(objinVal, objoutVal); //objretVal should also be object1 after the echoObject call

                TestObject server4 = TestObjectHelper.unchecked_narrow( objretVal );
                TestObject server5 = TestObjectHelper.unchecked_narrow(objoutVal.value);                    if(server4.getName().equals(name1) && server5.getName().equals(name1)){ //Check if objoutVal and objretVal are set to correct value
                    //System.out.println("Pass the test");
                }
                if(!server4.getName().equals(name1)){ //objretVal is not correct

                    System.out.println("objretVal String Should be "+name1+" but get "+server4.getName());
                }
                if(!server5.getName().equals(name1)){ //objoutVal is not correct
                    System.out.println("objoutVal String Should be "+name1+" but get "+server5.getName());
                }
                objoutVal.value = object2; //objoutVal will be changed to object1 after the echoObject call
            }

            System.out.println("======Begin the benchmark loop to test on passing org.omg.CORBA.Object as the parameter======");

            for (int i = 0; i<runNum; i++){

                //System.out.println("Begin to test on passing org.omg.CORBA.Object as the parameter...");
                org.omg.CORBA.Object objinVal = object1;
                objoutVal.value = object2; //objoutVal will be changed to object1 after the echoObject call
                org.omg.CORBA.Object objretVal = server3.echoObject(objinVal, objoutVal); //objretVal should also be object1 after the echoObject call

                TestObject server4 = TestObjectHelper.unchecked_narrow( objretVal );
                TestObject server5 = TestObjectHelper.unchecked_narrow(objoutVal.value);                    if(server4.getName().equals(name1) && server5.getName().equals(name1)){ //Check if objoutVal and objretVal are set to correct value
                    //System.out.println("Pass the test");
                }
                if(!server4.getName().equals(name1)){ //objretVal is not correct

                    System.out.println("objretVal String Should be "+name1+" but get "+server4.getName());
                }
                if(!server5.getName().equals(name1)){ //objoutVal is not correct
                    System.out.println("objoutVal String Should be "+name1+" but get "+server5.getName());
                }
                objoutVal.value = object2; //objoutVal will be changed to object1 after the echoObject call
            }
            System.exit(0); //shutdown() hasn't been implemented yet
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
