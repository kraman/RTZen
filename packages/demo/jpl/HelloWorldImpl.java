/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.jpl;

/**
 * This class implements the simple Hello World server.
 * @version 1.0
 */

public class HelloWorldImpl extends HelloWorldPOA
{
    private static int ITER = 1;
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
         
        for (int i = 0; i<array.length; i++){
            array[i] = array[i]*array[i]*array[i];
        }
        */
        
        //System.out.println(id + " Request got here ! ...now sending it back. " + array[0]);
        return id;
    }
}

