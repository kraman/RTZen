/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.poa2;

/**
 * This class implements the simple Hello World server.
 * @author Angelo Corsaro
 * @version 1.0
 */

public class HelloWorldImpl extends HelloWorldPOA
{
    private static int ITER = 10000;
    private int val;
    
    public HelloWorldImpl(int value)
    {
       this.val = value;    
    }
    
    /**
     * Gets a message from the Hello World Server.
     */
    public int getMessage1(){
        int j = 0;
        for (int i = 0; i < ITER; i++)
        {
            j++;
        }
        //System.out.println(this.val + " WOOHOO! Request got here....now sending back. " + System.currentTimeMillis());
        //return "Hello To the Zen World!!!";
        return (int)52;
    }
    public int getMessage2(){
       return (int)52;
    }
}

