package demo.rtcorba.clientPropagated;


/**
 * This class implements the simple Hello World server.
 * @author Angelo Corsaro
 * @version 1.0
 */

public class TestImpl extends TestPOA

{
    /**
     * Gets a message from the Hello World Server.
     */
    public void test_method (short priority)
    //public String getMessage()
    {
        //return "Hello To the Zen World!!!";
        System.out.println("Priority: " + priority);
    }
}

