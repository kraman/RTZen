package demo.exception;

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
    public int getMessage() throws TestException
    {
        throw new TestException( 42 );
    }
}

