/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package demo.octet_test;

/**
 * This class implements the simple Hello World server.
 * @version 1.0
 */

public class HelloWorldImpl extends HelloWorldPOA
{
    private static int ITER = 1;
    private int val;
    private int run = 0;

    public HelloWorldImpl(int value)
    {
       this.val = value;    
    }
    
    /**
     * Gets a message from the Hello World Server.
     */
    public int getMessage(int id, byte[] array)
    {
       if (run == 0 && id == 1)
       {
            run++;
//            System.out.print("Array size is:"  + array.length);
//            System.out.print(" Element 0 is:"  + array[0]);
//            System.out.println(" Element in the middle: "  + array[array.length/2]);
//            System.out.println(" LAst Element: "  + array[array.length-1]);
            for (int i = 0; i < array.length; i++)
                System.out.print(i + "=" + array[i] + " ");
            System.out.println("");

        }

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

