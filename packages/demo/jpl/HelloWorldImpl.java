package demo.jpl;

/**
 * This class implements the simple Hello World server.
 * @version 1.0
 */

public class HelloWorldImpl extends HelloWorldPOA
{
    //private static int ITER = 100;
    private int val;
    
    public HelloWorldImpl(int value)
    {
       this.val = value;    
    }
    
    /**
     * Gets a message from the Hello World Server.
     */
    public int getMessage(int id, int[] array)
    {
        /*
           for (int i = 0; i < this.val * ITER; i++)
           {
           int j = i % array.length; 
           array[j] = id;
           }
         */
        for (int i = 0; i<array.length; i++){
            array[i] = array[i]*array[i]*array[i];
        }
        
        //System.out.println(id + " Request got here ! ...now sending it back. " + array[0]);
        return id;
    }
}

