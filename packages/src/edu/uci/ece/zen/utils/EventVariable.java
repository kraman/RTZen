/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.utils;

/**
 * This class represent an <em>Event Variable</em> that can be used to
 * synchronize concurrent thread on the happening of a given event.
 * 
 * @author <a href="mailto:corsaro@doc.ece.uci.edu">Angelo Corsaro </a>
 * @version 1.0
 */
public class EventVariable {

    private boolean signaled; // = false;

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

    public synchronized void stall(long timeoutMillis)
            throws InterruptedException {
        if (!this.signaled) {
            super.wait(timeoutMillis);
        }
        this.signaled = false;
    }

    public synchronized void stall(long timeoutMillis, int timeoutNanos)
            throws InterruptedException {

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