/* --------------------------------------------------------------------------*
 * $Id: EventVariable.java,v 1.1 2004/02/05 01:11:56 kraman Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.utils;


/**
 * This class represent an <em>Event Variable</em> that can be used to
 * synchronize concurrent thread on the happening of a given event.
 *
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro</a>
 * @version 1.0
 */

public class EventVariable {

    private boolean signaled;   // = false;

    public EventVariable() {
        this.signaled = false;
    }

    public EventVariable(boolean signal) {
        signaled = signal;
    }
    
    public synchronized void stall() throws InterruptedException {
        if (!this.signaled) {
            this.wait();
        }
        this.signaled = false;
    }

    public synchronized void stall(long timeoutMillis) throws InterruptedException {
        if (!this.signaled) {
            super.wait(timeoutMillis);
        }
        this.signaled = false;
    }

    public synchronized void stall(long timeoutMillis, int timeoutNanos) throws
                InterruptedException {

        if (!this.signaled) {
            this.wait(timeoutMillis, timeoutNanos);
        }
        this.signaled = false;
    }

    public synchronized void signal() {
        this.signaled = true;
        this.notify();
    }

    public synchronized void broadCastSignal() {
        this.signaled = true;
        this.notifyAll();
    }
}
