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

