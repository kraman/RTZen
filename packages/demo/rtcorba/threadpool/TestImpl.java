package demo.rtcorba.threadpool;


/**
 * This class implements the simple Hello World server.
 * @author Angelo Corsaro
 * @version 1.0
 */

public class TestImpl extends testPOA

{
    /**
     * Gets a message from the Hello World Server.
     */
    public int method (int client_id, int iteration){
        //return "Hello To the Zen World!!!";
        //System.out.println("Priority: " + priority);

        return iteration;
    }

    public void shutdown (){}
}

