/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

/*******************************************************************************
 * --------------------------------------------------------------------------
 * $Id: SynchronizedInt.java,v 1.5 2003/08/05 23:37:22 nshankar Exp $
 * --------------------------------------------------------------------------
 */

package edu.uci.ece.zen.poa;

/**
 * <code> Synchronized Int </code> is a simple wrapper around the normal integer
 * type that provides methods for the synchronization based on the state of the
 * integer value.
 * 
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna </a>
 * @version 1.0
 */

public class SynchronizedInt {

    // PRE: Should be called from within a Synch Block
    public void increment() {
        ++value;
    }

    /*
     * The method decrements the current number of Active Requests and notifes
     * the wqaiting thereads that there are no more currently active requests
     * for this POA and that the POA may be destroyed.
     */

    /**
     * @param condition
     *            boolean
     */
    public void decrementAndNotifyAll(boolean condition) {
        if (--this.value == 0) {
            synchronized (this) {
                notifyAll();
            }
        }
    }

    /**
     * Return integer value
     * 
     * @return int
     */
    public synchronized int get() {
        return this.value;
    }

    /**
     * Wait for value to drop to zero.
     */
    public synchronized void waitForCompletion() {
        try {
            wait();
        } catch (InterruptedException ex) {
        }
    }

    public void reset() {
        value = 0;
    }

    private int value;
}