/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import javax.realtime.ScopedMemory;

/**
 * Based on edu.uci.ece.zen.utils.Hashtable but the keytable is different
 */
public class WaiterRegistry extends edu.uci.ece.zen.utils.ActiveDemuxTable {
    public WaiterRegistry() {
    }

    public void registerWaiter(int key, ScopedMemory mem) {
        super.bind((long) key, mem);
    }

    public ScopedMemory getWaiter(int key) {
        return (ScopedMemory) super.mapEntry(super.find((long) key));
    }

    public void remove(int idx) {
        super.unbind(super.find(idx));
    }
}