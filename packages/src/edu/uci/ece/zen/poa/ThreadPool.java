/* --------------------------------------------------------------------------*
 * $Id: ThreadPool.java,v 1.1 2003/11/26 22:26:35 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa;


import edu.oswego.cs.dl.util.concurrent.Channel;
import edu.uci.ece.zen.util.ExecutorShutdownException;


/**
 * <code> ThreadPool </code> is a pooled executor class in ZEN.
 * The pooled Executor is similar to any other executor and has a
 * run menthod. It hides the fact that there are a pool of executors
 * that service requests. ZENs implementation of the Half-Sync/Half-Async
 * pattern
 * 
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */

public class ThreadPool implements edu.uci.ece.zen.util.Executor {

    public ThreadPool(Channel channel, int maxPoolSize) {
        this.maximumPoolSize_ = maxPoolSize;
        this.handOff_ = channel;
        this.freeList = new java.util.LinkedList();
        this.createThreads(maxPoolSize);
        // this.main = ThreadFactory.createThread (new ExecutorLogic());
        // this.main.start();
        // Logger.debug("Thread Pool started");
    }

    protected void addThread() {
        Worker worker = new Worker(this.handOff_);

        freeList.addFirst(worker);
    }

    protected synchronized void createThreads(int numberOfThreads) {
        for (int i = 0; i < numberOfThreads; ++i) {
            addThread();
        }
    }

    /**
     * Interrupt all threads in the pool, causing them all
     * to terminate. Assuming that executed tasks do not
     * disable (clear) interruptions, each thread will terminate after
     * processing its current task. Threads will terminate
     * sooner if the executed tasks themselves respond to
     * interrupts.
     **/

    protected synchronized final void shutdownImmediate() {

        // this.main.interrupt();

        for (java.util.ListIterator it = freeList.listIterator();
                it.hasNext();) {
            ((Worker) (it.next())).shutdown(true);
        }
        this.cleanup();
    }

    /**
     * Terminate (via interruptAll)  threads after processing elements
     * all currently in queue. Any tasks entered after this point will
     * not be processed. A shut down pool cannot be restarted.
     * This method may block if the task queue is finite and full.
     **/

    protected final void shutdownAfterProcessingCurrentlyQueuedTasks() {

        // Place marker into task queue
        // Logger.debug("Shutting down thread pool after processing all"
        // + "requests");
        try {
            handOff_.put(ENDTASK);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Shutdown the thread-pool called during shutdown of the POA.
     * @param when boolean
     */
    public void shutdown(boolean when) {
        this.active = false;

        if (when) {
            shutdownAfterProcessingCurrentlyQueuedTasks();
        } else {
            shutdownImmediate();
        }
    }

    /**
     * Remove all unprocessed tasks from pool queue, and
     * return them in a java.util.List. It should normally be used only
     * when there are not any active clients of the pool (otherwise
     * you face the possibility that the method will loop
     * pulling out tasks as clients are putting them in.)
     * This method can be useful after
     * shutting down a pool (via interruptAll) to determine
     * whether there are any pending tasks that were not processed.
     * You can then, for example execute all unprocessed commands
     * via code along the lines of:
     * <pre>
     *   List tasks = pool.drain();
     *   for (Iterator it = tasks.iterator(); it.hasNext();)
     *     ( (Runnable)(it.next()) ).run();
     * </pre>
     **/

    public java.util.List drain() {
        boolean wasInterrupted = false;
        java.util.Vector tasks = new java.util.Vector();

        for (;;) {
            try {
                Object x = handOff_.poll(0);

                if (x == null) {
                    break;
                } else {
                    tasks.addElement(x);
                }
            } catch (InterruptedException ex) {
                wasInterrupted = true; // postpone re-interrupt until drained
            }
        }
        if (wasInterrupted) {
            Thread.currentThread().interrupt();
        }
        return tasks;
    }

    protected final void cleanup() {
        // --Drain the queue in the pool..
        this.drain();
        this.freeList = null;
    }

    /**
     * Return if the thread-pool is active.
     * @return boolean
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Execute the given task. 
     * @param request Runnable
     * @throws ExecutorShutdownException When thread-pool has already been
     * shutdown
     */
    public void execute(Runnable request)throws ExecutorShutdownException {
        if (!isActive()) {
            throw new ExecutorShutdownException("Executor Already shutdowm");
        }

        try {
            this.handOff_.put(request);
        } catch (InterruptedException intr) {
            throw new ExecutorShutdownException("Cannot service task,"
                    + "Executor has been shutdown");
        }
    }

    protected volatile int maximumPoolSize_;

    /** Special queue element to signal termination **/
    protected Runnable ENDTASK = new Runnable() {
        public void run() {
            shutdownImmediate();
        }
    };

    /**
     * The channel is used to hand off the command
     * to a thread in the pool
     **/
    protected final Channel handOff_;

    /**
     * Workers list that would service the requests.
     */
    protected java.util.LinkedList freeList;

    protected volatile boolean active = true;

}// End ThreadPool

