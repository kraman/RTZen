package edu.uci.ece.zen.orb.giop;

import org.omg.CORBA.portable.InvokeHandler;
import org.omg.PortableServer.Servant;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.ResponseHandler;
import edu.uci.ece.zen.orb.giop.type.RequestMessage;
import edu.uci.ece.zen.utils.ExecuteInRunnable;
import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

public class MSGRunnable implements Runnable {
    RequestMessage rm;

    Servant servant;

    CDROutputStream reply;

    ORB orb;

    public MSGRunnable() {
    }

    public void init(RequestMessage rm, Servant servant, CDROutputStream reply,
            ORB orb) {
        this.rm = rm;
        this.servant = servant;
        this.reply = reply;
        this.orb = orb;
    }

    public void run() {
        ResponseHandler rh = new ResponseHandler(orb, rm);

        if (rm.getOperation().equals("_is_a")) {
            boolean _result = servant._is_a(rm.getCDRInputStream()
                    .read_string());
            org.omg.CORBA.portable.OutputStream _output = rh.createReply();
            _output.write_boolean(_result);
            reply = (CDROutputStream) _output;
        } else if (rm.getOperation().equals("_non_existent")) {
            boolean _result = servant._non_existent();
            org.omg.CORBA.portable.OutputStream _output = rh.createReply();
            _output.write_boolean(_result);
            reply = (CDROutputStream) _output;
        } else {
        edu.uci.ece.zen.utils.Logger.printMemStatsImm(324);
           String op = rm.getOperation().toString();
        edu.uci.ece.zen.utils.Logger.printMemStatsImm(325);
           
            reply = (CDROutputStream) ((InvokeHandler) servant)
                    ._invoke(op,
                            (org.omg.CORBA.portable.InputStream) rm
                                    .getCDRInputStream(), rh);
        }
        edu.uci.ece.zen.utils.Logger.printMemStatsImm(326);

        if (rm.getResponseExpected() == 1) {
            reply.updateLength();
            WriteBuffer wb = reply.getBuffer();
            SendRunnable sr = new SendRunnable();
            ExecuteInRunnable eir = new ExecuteInRunnable();
            sr.init(wb);
            eir.init(sr, rm.getTransport());
            try {
                orb.orbImplRegion.executeInArea(eir);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, getClass(), "run", e);
            }
        }
        reply.free();
        //((Transport)( rm.getTransport() ).getPortal()).send(wb);
    }
}

