package edu.uci.ece.zen.utils;

import javax.realtime.*;

public class ExecuteInRunnable implements Runnable{
    Runnable runnable;
    MemoryArea area;

    public ExecuteInRunnable(){}
    public void init( Runnable runnable , MemoryArea area ){
        System.out.println( Thread.currentThread() + "ExecuteInRunnable.init 1" );
        this.runnable = runnable;
        System.out.println( Thread.currentThread() + "ExecuteInRunnable.init 2" );
        this.area = area;
        System.out.println( Thread.currentThread() + "ExecuteInRunnable.init 3" );
    }
    public void run(){
        System.out.println( Thread.currentThread() + "In memory: " + RealtimeThread.getCurrentMemoryArea() );
        System.out.println( Thread.currentThread() + "ExecuteInRunnable.run 1" );
        area.enter( runnable );
        System.out.println( Thread.currentThread() + "ExecuteInRunnable.run 2" );
    }
}

