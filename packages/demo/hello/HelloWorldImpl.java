package demo.hello;

/**
 * This class implements the simple Hello World server.
 * @author Angelo Corsaro
 * @version 1.0
 */

public class HelloWorldImpl extends HelloWorldPOA
{
     public static edu.oswego.cs.dl.util.concurrent.Semaphore semaphore;
    /**
     * Gets a message from the Hello World Server.
     */
    public void aa()
    {
        System.out.println( "******************  WOOHOO! Request got here1 ********************" );
        if(semaphore!=null) semaphore.release();
        System.out.println( "******************  WOOHOO! Request got here2 ********************" );
        //return "Hello To the Zen World!!!";
        //return 42;
    }
}

