package demo.hello;

/**
 * This class implements the simple Hello World server.
 * @author Angelo Corsaro
 * @version 1.0
 */

public class HelloWorldImpl extends HelloWorldPOA

{
    /**
     * Gets a message from the Hello World Server.
     */
    public String getMessage()
    {
        return "Hello To the Zen World!!!";
    }
}

