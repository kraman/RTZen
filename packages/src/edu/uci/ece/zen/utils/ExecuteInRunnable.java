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
        area.enter( runnable );
    }
}

