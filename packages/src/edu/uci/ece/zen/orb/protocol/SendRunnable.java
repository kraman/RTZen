/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.protocol;

import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.orb.transport.Transport;
import edu.uci.ece.zen.utils.WriteBuffer;

public class SendRunnable implements Runnable {
    WriteBuffer wb;

    public SendRunnable() {
    }

    public void init(WriteBuffer wb) {
        this.wb = wb;
    }

    public void run() {
        ((Transport) ((ScopedMemory) RealtimeThread.getCurrentMemoryArea())
                .getPortal()).send(wb);

    }
}

