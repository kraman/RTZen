package edu.uci.ece.zen.orb.protocol;

import org.omg.CORBA.portable.InvokeHandler;
import org.omg.PortableServer.Servant;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.ResponseHandler;
import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.utils.ExecuteInRunnable;
import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;
import edu.uci.ece.zen.utils.Logger;

/**
 * @author Krishna Raman
 * @author Mark Panahi
 *
 * Logic to make invocations on the servant and send back the reponse if expected.
 * Also handles special operations like _is_a and _non_existent.
 */

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
        try{

            //edu.uci.ece.zen.utils.Logger.printMemStatsImm(322);
            ResponseHandler rh = new ResponseHandler(orb, rm);
            //edu.uci.ece.zen.utils.Logger.printMemStatsImm(323);

            ///// Parse service context here

            FString contexts = rm.getServiceContexts();

            if (ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("MSGRunnable REQUEST SC: " + contexts.decode());

            ReadBuffer rb = contexts.toReadBuffer();

            //if (ZenBuildProperties.dInvocationsbg) System.out.println("#############REPLY RB: " + rb.toString());

            int size = rb.readLong();

            if(ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("MSGRunnable REPLY CONTEXT size: " + size);
            //orb.getRTCurrent().the_priority((short) javax.realtime.PriorityScheduler.instance().getNormPriority()); //kludge
            for(int i = 0; i < size; ++i){

                int id = rb.readLong();
                if(ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("MSGRunnable REPLY CONTEXT id: " + id);

                if(id == org.omg.IOP.RTCorbaPriority.value){
                    if(ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("MSGRunnable REPLY CONTEXT id:RTCorbaPriority");
                    //if(ZenBuildProperties.dbgInvocations) System.out.println("MSGRunnable CUR thread priority: " + orb.getRTCurrent().the_priority());

                    rb.readLong(); //eat length

                    short priority = (short)rb.readLong();

                    if(ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("MSGRunnable RECEIVED thread priority: " + priority);

                    rm.setPriority(priority);
                    orb.getRTCurrent().the_priority(priority);

                    if(ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("MSGRunnable NEW thread priority: " + orb.getRTCurrent().the_priority());

                } else{ // just eat
                    if(ZenBuildProperties.dbgInvocations) ZenProperties.logger.log("MSGRunnable Skipping unknown service context " + id);
                    int byteLen = rb.readLong();
                    for(int i1 = 0; i1 < byteLen; ++i1)
                        rb.readByte();
                }
            }

            rb.free();
            /////////// end parse service context

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

                /*
                   if(ZenBuildProperties.dbgInvocations) {
                   ZenProperties.logger.log("wbdbg__Servant2: " + servant.toString() + " id: " + rm.getRequestId() + " reply: " + reply.toString());
                   ZenProperties.logger.log("wbdbg__Servant2: " + servant.toString() + " id: " + rm.getRequestId() + " wb: " + wb.toString());
                   }*/
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
            rm.free();
            //((Transport)( rm.getTransport() ).getPortal()).send(wb);
        }catch(Throwable ex){
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}

