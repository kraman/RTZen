package edu.uci.ece.zen.orb.giop;

import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.orb.transport.Transport;
import edu.uci.ece.zen.orb.giop.type.RequestMessage;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;
import org.omg.PortableServer.Servant;
import javax.realtime.*;
import org.omg.CORBA.portable.InvokeHandler;

public class MSGRunnable implements Runnable{
    RequestMessage rm;
    Servant servant;
    CDROutputStream reply;
    ORB orb;

    public MSGRunnable(){}
    public void init( RequestMessage rm, Servant servant, CDROutputStream reply, ORB orb ){
        this.rm = rm;
        this.servant = servant;
        this.reply = reply;
        this.orb = orb;
    }
    public void run(){
       ResponseHandler rh = new ResponseHandler( orb , rm );
        
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
            ((InvokeHandler)servant)._invoke(rm.getOperation().toString(), (org.omg.CORBA.portable.InputStream)rm.getCDRInputStream(), rh);
        }

        reply.updateLength();
        WriteBuffer wb = reply.getBuffer();
        SendRunnable sr = new SendRunnable();
        ExecuteInRunnable eir = new ExecuteInRunnable();
        sr.init(wb);
        eir.init(sr, rm.getTransport());
        try{
            orb.orbImplRegion.executeInArea(eir);
        }catch( Exception e ){
            e.printStackTrace();
        }
        //((Transport)( (ScopedMemory) RealtimeThread.getCurrentMemoryArea()).getPortal()).send(wb);
    }
}
