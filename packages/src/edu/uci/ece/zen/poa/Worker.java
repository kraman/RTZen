/* --------------------------------------------------------------------------*
 * $Id: Worker.java,v 1.1 2003/11/26 22:26:42 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa;


/**
 * <code> Worker </code> is a single executor that services a request.
 * This is a thread bound executor that maintains an internal thread.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */

import edu.oswego.cs.dl.util.concurrent.Channel;
import edu.uci.ece.zen.sys.ThreadFactory;
import edu.uci.ece.zen.util.ExecutorShutdownException;


public class Worker
    implements edu.uci.ece.zen.util.Executor {

    /**
     * Worker entity in the thread-pool
     * @param handOff_ Channel
     */
    public Worker(Channel handOff_) {
        this.handOff_ = handOff_;
        this.worker = ThreadFactory.createThread(new ExecutorLogic());
        worker.start();
    }
    /**
     * Execute the task i.e. execute the run method in the given thread.
     * @param cmd Runnable
     */

    public void execute(Runnable cmd)
        throws ExecutorShutdownException {
        if (!active) {
            throw new ExecutorShutdownException("ExecutorShutdown");
        }
        cmd.run();
    }
    /**
     * Stop the thread from servicing requests.
     * @param when boolean
     */

    public void shutdown(boolean when) {
        this.active = false;
        // Logger.debug("Shutting down worker:");
        this.worker.interrupt();
    }
    /**
     * Check if the worker is active
     * @return boolean
     */

    private final boolean isActive() {
        return this.active;
    }

    private final Thread worker;
    private volatile boolean active = true;
    private final Channel handOff_;

    class ExecutorLogic implements Runnable {
        public void run() {
            Runnable command = null;

            while (isActive()) {
                // -- Poll for data in the Channel and Process
                try {
                    ((Runnable) handOff_.take()).run();
                } catch (InterruptedException ie) {
                    break;
                }
            }// end while
        }
    }

}
