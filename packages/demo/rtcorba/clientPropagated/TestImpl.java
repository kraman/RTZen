package demo.rtcorba.clientPropagated;


/**
 * This class implements the RTCORBA client propagated
    priority demo server from TAO.
 * @author Mark Panahi
 * @version 1.0
 */

public class TestImpl extends TestPOA

{
    /**
     *
     */
    public void test_method (short priority)
    {
        System.out.println("Priority: " + priority);
    }
}

