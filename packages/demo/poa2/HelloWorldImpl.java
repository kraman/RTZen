package demo.poa2;

/**
 * This class implements the simple Hello World server.
 * @author Angelo Corsaro
 * @version 1.0
 */

public class HelloWorldImpl extends HelloWorldPOA
{
    private static int ITER = 1000000;
    private int val;
    
    public HelloWorldImpl(int value)
    {
       this.val = value;    
    }
    
    /**
     * Gets a message from the Hello World Server.
     */
    public int getMessage()
    {
        int j = 0;
        for (int i = 0; i < ITER; i++)
        {
            j++;
        }
        System.out.println(this.val + " WOOHOO! Request got here....now sending back. " + System.currentTimeMillis());
        //return "Hello To the Zen World!!!";
        return (int) j;
    }
}

