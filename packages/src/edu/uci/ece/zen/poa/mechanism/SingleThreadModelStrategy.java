/*******************************************************************************
 * --------------------------------------------------------------------------
 * $Id: SingleThreadModelStrategy.java,v 1.7 2003/08/05 23:37:28 nshankar Exp $
 * --------------------------------------------------------------------------
 */

package edu.uci.ece.zen.poa.mechanism;

/**
 * The class <code>SingleThreadModelStrategy</code> implements the
 * SingleThreadPolicy of the CORBA POA.
 * 
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna </a>
 * @version 1.0
 */

import edu.oswego.cs.dl.util.concurrent.Semaphore;

public final class SingleThreadModelStrategy extends ThreadPolicyStrategy {

    /**
     * Enter block after grabbing mutex. Since multiple POAs can share enter()
     * synchronizes unsing the <code> 
     * SingleThreadModelStrategy.class </code>
     * 
     * @param invokeHandler
     *            request handler
     */
    public synchronized void enter(
            org.omg.CORBA.portable.InvokeHandler invokeHandler) {
        if (servantTable.contains(invokeHandler)) {
            synchronized (SingleThreadModelStrategy.class) {
                try {
                    // edu.uci.ece.zen.orb.Logger.debug("SingleThreadedPOA:invokehandler
                    // Shared, Thread waiting for"
                    // + "Completion of request");
                    SingleThreadModelStrategy.class.wait();

                } catch (InterruptedException ex) {
                }
            }
        }
    }

    /**
     * Exit from the synchronized block
     * 
     * @param invokeHandler
     *            request handler
     */
    public synchronized void exit(
            org.omg.CORBA.portable.InvokeHandler invokeHandler) {
        synchronized (SingleThreadModelStrategy.class) {
            servantTable.remove(invokeHandler);
            SingleThreadModelStrategy.class.notifyAll();
        }
    }

    /**
     * Binary semaphore based synchronized block
     */

    public void enter() {
        try {
            this.binarySemaphore.acquire();
        } catch (InterruptedException ex) {
        }
    }

    /**
     * Release binary semaphore permit
     */
    public void exit() {
        this.binarySemaphore.release();
    }

    private final static java.util.Vector servantTable = new java.util.Vector();

    // --Binary Semaphore ---
    private final Semaphore binarySemaphore = new Semaphore(1);

}

