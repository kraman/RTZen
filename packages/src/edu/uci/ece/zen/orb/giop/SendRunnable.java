package edu.uci.ece.zen.orb.giop;

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

