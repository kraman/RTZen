package edu.uci.ece.zen.orb.giop;

import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.orb.transport.Transport;
import edu.uci.ece.zen.orb.giop.type.RequestMessage;
import org.omg.PortableServer.Servant;
import javax.realtime.*;

public class MSGRunnable implements Runnable{
    RequestMessage rm;
    Servant servant;
    CDROutputStream reply;
    ResponseHandler rh;
    ORB orb;

    public MSGRunnable(){}
    public void init( RequestMessage rm, Servant servant, CDROutputStream reply, ResponseHandler rh, ORB orb ){
        this.rm = rm;
        this.servant = servant;
        this.reply = reply;
        this.rh = rh;
        this.orb = orb;
    }
    public void run(){
       if (rm.getOperation().equals("_is_a") )
        {
            boolean _result = servant._is_a(rm.getCDRInputStream().read_string());
            org.omg.CORBA.portable.OutputStream _output = rh.createReply();
            _output.write_boolean(_result);
            reply = (CDROutputStream) _output;
        }
        else if (rm.getOperation().equals("_non_existent") )
        {
            boolean _result = servant._non_existent();
            org.omg.CORBA.portable.OutputStream _output = rh.createReply();
            _output.write_boolean(_result);
            reply = (CDROutputStream) _output;
        }
        else
        {
            reply = (CDROutputStream)
                ih._invoke(rm.getOperation().toString(),
                        (org.omg.CORBA.portable.InputStream)rm.getCDRInputStream(),
                        rh);
        }

        reply.updateLength();
        WriteBuffer wb = reply.getBuffer();
        SendRunnalbe sr = new SendRunnable();
        ExecuteInRunnable eir = new ExecuteInRunnable();
        sr.init(wb);
        eir.init(sr, rm.getTransport());
        orb.orbImplRegion.executeInArea(eir);
 
        //((Transport)( (ScopedMemory) RealtimeThread.getCurrentMemoryArea()).getPortal()).send(wb);
       
    }
}

