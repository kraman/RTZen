package edu.uci.ece.zen.orb.giop;

import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.orb.transport.Transport;
import javax.realtime.*;

public class SendRunnable implements Runnable{
    WriteBuffer wb;
    
    public SendRunnable(){}
    public void init( WriteBuffer wb){
        this.wb = wb;
    }
    public void run(){
        ((Transport)( (ScopedMemory) RealtimeThread.getCurrentMemoryArea()).getPortal()).send(wb);
   
    }
}

