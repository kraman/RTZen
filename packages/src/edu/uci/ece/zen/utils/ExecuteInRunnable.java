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
        this.runnable = runnable;
        this.area = area;
    }
    public void run(){
        area.enter( runnable );
    }
    public void free(){
        ExecuteInRunnable.release( this );
    }
}

