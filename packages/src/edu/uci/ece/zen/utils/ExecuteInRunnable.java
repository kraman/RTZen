package edu.uci.ece.zen.utils;

import javax.realtime.*;

public class ExecuteInRunnable implements Runnable{
    Runnable runnable;
    MemoryArea area;

    public ExecuteInRunnable(){}
    public void init( Runnable runnable , MemoryArea area ){
        this.runnable = runnable;
        this.area = area;
    }
    public void run(){
        System.out.println("utils.ExecuteInRunnable, the current memory region is "+javax.realtime.RealtimeThread.getCurrentMemoryArea()); 
        area.enter( runnable );
    }
}

