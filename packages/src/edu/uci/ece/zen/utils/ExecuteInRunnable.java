package edu.uci.ece.zen.utils;

import javax.realtime.*;

public class ExecuteInRunnable implements Runnable{
    private static Queue runnableQueue;

    public static ExecuteInRunnable instance(){
        if( runnableQueue == null ){
            try{
                runnableQueue = (Queue) ImmortalMemory.instance().newInstance( Queue.class );
            }catch( Exception e ){
                ZenProperties.logger.log(
                    Logger.FATAL,
                    "edu.uci.ece.zen.utils.ExecuteInRunnable",
                    "instance()",
                    "Exception occured: " + e );
                System.exit(-1);
            }
        }

        Object runnable = runnableQueue.dequeue();
        if( runnable == null )
            try{
                return (ExecuteInRunnable) ImmortalMemory.instance().newInstance( ExecuteInRunnable.class );
            }catch( Exception e ){
                ZenProperties.logger.log(
                    Logger.FATAL,
                    "edu.uci.ece.zen.utils.ExecuteInRunnable",
                    "instance()",
                    "Exception occured: " + e );
                System.exit(-1);
            }
        else
            return (ExecuteInRunnable) runnable;
        return null;
    }

    private static void release( ExecuteInRunnable r ){
        runnableQueue.enqueue( r );
    }
    
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
    public void free(){
        ExecuteInRunnable.release( this );
    }
}

