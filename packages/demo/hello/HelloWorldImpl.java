/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

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
    public int getMessage()
    {
        //System.out.println( "******************  WOOHOO! Request got here....now sending back. ********************" );
        //return "Hello To the Zen World!!!";
        return 42;
    }
}

