/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.utils;

public class ThreadLocal extends Hashtable{
    public ThreadLocal(){}

    public void init( int numThreads ){
        super.init( numThreads );
    }

    public void put( Object obj ){
        put( Thread.currentThread() , obj );
    }

    public Object get(){
        return get( Thread.currentThread() );
    }

    public void release(){
        remove( Thread.currentThread() );
    }
}

