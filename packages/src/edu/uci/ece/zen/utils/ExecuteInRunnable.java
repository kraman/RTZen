package edu.uci.ece.zen.utils;

import javax.realtime.MemoryArea;

/**
 * This class is used to jump between memory regions. It is used when the
 * program needs to jump to a parent memory region and then enter a sibling. To
 * do this, the ExecuteInRunnable is initialized with the next memory region and
 * the runnable object to run in it. The ExecuteInRunnable is run in the parent
 * memory region and then the ExecuteInRunnable invokes the program provided
 * runnable object in the next memory region.
 * 
 * @author Krishna Raman
 */
public class ExecuteInRunnable implements Runnable {
    /** The runnable to invoke in the next memory region */
    Runnable runnable;

    /** The next memory area to enter */
    MemoryArea area;

    public ExecuteInRunnable() {
    }

    public void init(Runnable runnable, MemoryArea area) {
        this.runnable = runnable;
        this.area = area;
    }

    public void run() {
	try{
            //System.out.println( "utils.ExecuteInRunnable, the current" + " memory region is " + javax.realtime.RealtimeThread.getCurrentMemoryArea());
	    area.enter(runnable);
	}catch(Throwable ex){
            System.out.println( "jumping to area " + area + " and running " + runnable );
	    System.err.println("Exception occured");
	    ex.printStackTrace();
	}
    }
}

