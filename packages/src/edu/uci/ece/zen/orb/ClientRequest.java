package edu.uci.ece.zen.orb;

import javax.realtime.ImmortalMemory;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import org.omg.RTCORBA.PriorityModel;

import edu.uci.ece.zen.orb.giop.IOP.ServiceContext;
import edu.uci.ece.zen.utils.ExecuteInRunnable;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ZenProperties;

public class ClientRequest extends org.omg.CORBA.portable.OutputStream {
    public String operation;

    public boolean responseExpected;

    public ObjRefDelegate del;

    public CDROutputStream out;

    public byte giopMajor;

    public byte giopMinor;

    public ScopedMemory transportScope;

    public FString objectKey;

    //public byte[] objectKey;
    //public FString objectKey;
    public ORB orb;

    private int messageId;

    public FString contexts;

    private static ClientRequest inst;

    public static ClientRequest instance() {
        if (inst == null) {
            try {
                inst = (ClientRequest) ImmortalMemory.instance().newInstance(
                        ClientRequest.class);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, ClientRequest.class, "instance", e);
            }
        }
        return inst;
    }

    /**
     * Client upcall:
     * <p>
     * <b>Client scope </b> --ex in --&gt; ORB parent scope --&gt; ORB scope
     * --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public void init(String operation, boolean responseExpected,
            byte giopMajor, byte giopMinor, ORB orb, ObjRefDelegate del) {
        //          edu.uci.ece.zen.utils.Logger.printMemStats(310);
        this.orb = orb;
        this.del = del;
        this.operation = operation;
        this.responseExpected = responseExpected;
        out = CDROutputStream.instance();

        ZenProperties.logger.log("ClientRequest 1");
        out.init(orb);
        ZenProperties.logger.log("ClientRequest 2");
        this.giopMajor = giopMajor;
        this.giopMinor = giopMinor;
        ZenProperties.logger.log("ClientRequest 3");
        LaneInfo ln = del.getLane();
        ZenProperties.logger.log("ClientRequest 5");
        transportScope = ln.transpScope;
        ZenProperties.logger.log("ClientRequest 6");
        objectKey = ln.getObjectKey();
        ZenProperties.logger.log("ClientRequest 7");

        //TODO:Assemble and write message header and policies here

        contexts = ServiceContext.instance();

        //contexts.append(0); //empty list
        if (del.priorityModel == PriorityModel._CLIENT_PROPAGATED
                && del.serverPriority >= 0) {

            ZenProperties.logger.log("Sending CLIENT PROPAGATED service context");

            contexts.append(1); //list size

            contexts.append(org.omg.IOP.RTCorbaPriority.value);
            //contexts.append((byte)0); //big endian
            contexts.append(4); //length of data
            contexts.append((int) orb.getRTCurrent().the_priority());
            /*
             * CDROutputStream out1 = CDROutputStream.create(orb); short
             * priority = orb.getRTCurrent().the_priority();
             * out1.write_short(priority); int lim =
             * (int)out1.getBuffer().getLimit();
             * contexts.read(out1.getBuffer().getReadBuffer(), lim);
             * if(ZenProperties.devDbg) System.out.println( "sc data Lim: " +
             * lim); //contexts[0].context_data = new byte[lim];
             * //out1.getBuffer().getReadBuffer().readByteArray(contexts[0].context_data,
             * 0 , lim); //System.out.println("data " +
             * contexts[0].context_data[0] + "-" + contexts[0].context_data[1] +
             * "-" + // contexts[0].context_data[2] + "-" +
             * contexts[0].context_data[3]); if(ZenProperties.devDbg)
             * System.out.println("CLIENT_PROPAGATED policy -- Sending priority: " +
             * priority); out1.free();
             */
            /*
             * { org.omg.IOP.ServiceContext [] contexts1 = new
             * org.omg.IOP.ServiceContext[1]; //kludge: of course there will be
             * more than just 1 contexts1[0] = new org.omg.IOP.ServiceContext();
             * contexts1[0].context_id = RTCorbaPriority.value; CDROutputStream
             * out1 = CDROutputStream.create(orb); short priority =
             * orb.getRTCurrent().the_priority(); out1.write_short(priority);
             * int lim = (int)out1.getBuffer().getLimit();
             * contexts1[0].context_data = new byte[lim];
             * out1.getBuffer().getReadBuffer().readByteArray(contexts1[0].context_data,
             * 0 , lim); //System.out.println("data " +
             * contexts[0].context_data[0] + "-" + contexts[0].context_data[1] +
             * "-" + // contexts[0].context_data[2] + "-" +
             * contexts[0].context_data[3]); if(ZenProperties.devDbg)
             * System.out.println("CLIENT_PROPAGATED policy -- Sending priority: " +
             * priority); out1.free(); CDROutputStream out2 =
             * CDROutputStream.create(orb);
             * org.omg.IOP.ServiceContextListHelper.write(out2,contexts1); byte []
             * test = new byte[(int)out2.getBuffer().getLimit()];
             * out2.getBuffer().getReadBuffer().readByteArray(test, 0 ,
             * (int)out2.getBuffer().getLimit()); System.out.println("what it
             * should be: " + FString.byteArrayToString(test)); }
             */
        } else {
            contexts.append(0); //empty list
            //contexts = new ServiceContext[0];
        }

        messageId = WaitingStrategy.newMessageId();
        ZenProperties.logger.log("ClientRequest 8");
        edu.uci.ece.zen.orb.giop.GIOPMessageFactory.constructMessage(this,
                messageId, out);
        ZenProperties.logger.log("ClientRequest 9");
        //edu.uci.ece.zen.utils.Logger.printMemStats(311);
    }

    /**
     * Client upcall:
     * <p>
     * <b>Client scope </b> --ex in --&gt; ORB parent scope --&gt; ORB scope
     * --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public CDRInputStream invoke() {
        //out.printWriteBuffer(); This will printout every byte in the
        // OutputStream

        //edu.uci.ece.zen.utils.Logger.printMemStats(310);

        ZenProperties.logger.log("ClientRequest invoke 1");
        out.updateLength();
        MessageComposerRunnable mcr = MessageComposerRunnable.instance();
        mcr.init(this);
        ScopedMemory messageScope = ORB.getScopedRegion();

        ExecuteInRunnable erOrbMem = orb.getEIR();//new ExecuteInRunnable();
        ExecuteInRunnable erMsgMem = orb.getEIR();//new ExecuteInRunnable();

        erOrbMem.init(erMsgMem, orb.orbImplRegion);
        erMsgMem.init(mcr, messageScope);
        ZenProperties.logger.log("ClientRequest invoke 2");
        try {
            if (orb.parentMemoryArea == RealtimeThread.getCurrentMemoryArea()) 
                erOrbMem.run();
            else 
                orb.parentMemoryArea.executeInArea(erOrbMem);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.SEVERE,
                    getClass(), "invoke()",
                    "Could not invoke remote object", e);
        }
        //edu.uci.ece.zen.utils.Logger.printMemStats(310);
        ZenProperties.logger.log("ClientRequest invoke 3");
        ORB.freeScopedRegion(messageScope);
        orb.freeEIR( erOrbMem );
        orb.freeEIR( erMsgMem );
        //edu.uci.ece.zen.utils.Logger.printMemStats(311);
        if (mcr.success) return mcr.getReply();
        else throw new org.omg.CORBA.TRANSIENT();
    }

    public void registerWaiter() {
        orb.registerWaiter(messageId);
    }

    public void releaseWaiter() {
        orb.releaseWaiter(messageId);
    }

    //Redirect OutputStream methods to CDROutputStream

    public final void write_octet(final byte value) {
        out.write_octet(value);
    }

    public void write_octet_array(byte[] value, int offset, int length) {
        out.write_octet_array(value, offset, length);
    }

    public void write_boolean(boolean value) {
        out.write_boolean(value);
    }

    public void write_boolean_array(boolean[] value, int offset, int length) {
        out.write_boolean_array(value, offset, length);
    }

    public void write_char(char c) {
        out.write_char(c);
    }

    public void write_char_array(char[] value, int offset, int length) {
        out.write_char_array(value, offset, length);
    }

    public void write_string(String s) {
        out.write_string(s);
    }

    public org.omg.CORBA.portable.InputStream create_input_stream() {
        return out.create_input_stream();
    }

    public void write_short(short value) {
        out.write_short(value);
    }

    public void write_short_array(short[] value, int offset, int length) {
        out.write_short_array(value, offset, length);
    }

    public void write_long(int value) {
        out.write_long(value);
    }

    public void write_long_array(int[] value, int offset, int length) {
        out.write_long_array(value, offset, length);
    }

    public void write_wchar(char c) {
        out.write_wchar(c);
    }

    public void write_wchar_array(char[] value, int offset, int length) {
        out.write_wchar_array(value, offset, length);
    }

    public void free() {
        out.free();
    }

    public void write_wstring(String s) {
        out.write_wstring(s);
    }

    public void write_float(float value) {
        out.write_float(value);
    }

    public void write_float_array(float[] value, int offset, int length) {
        out.write_float_array(value, offset, length);
    }

    public void write_double(double value) {
        out.write_double(value);
    }

    public void write_double_array(double[] value, int offset, int length) {
        out.write_double_array(value, offset, length);
    }

    public void write_longlong(long value) {
        out.write_longlong(value);
    }

    public void write_longlong_array(long[] value, int offset, int length) {
        out.write_longlong_array(value, offset, length);
    }

    public void write_ulong(int value) {
        out.write_ulong(value);
    }

    public void write_ulong_array(int[] value, int offset, int length) {
        out.write_ulong_array(value, offset, length);
    }

    public void write_ulonglong(long value) {
        out.write_ulonglong(value);
    }

    public void write_ulonglong_array(long[] value, int offset, int length) {
        out.write_ulonglong_array(value, offset, length);
    }

    public void write_ushort(short value) {
        out.write_ushort(value);
    }

    public void write_ushort_array(short[] value, int offset, int length) {
        out.write_ushort_array(value, offset, length);
    }

    public void write_Object(org.omg.CORBA.Object value) {
        out.write_Object(value);
    }

    public void write_CDROutputStream(CDROutputStream cdr) {
        out.write_CDROutputStream(cdr);
    }

    public void write_Principal(org.omg.CORBA.Principal pr) {
        out.write_Principal(pr);
    }

    public void write_TypeCode(org.omg.CORBA.TypeCode value) {
        out.write_TypeCode(value);
    }

    public void write_any(org.omg.CORBA.Any value) {
        out.write_any(value);
    }
}

class MessageComposerRunnable implements Runnable {
    ClientRequest clr;

    CDRInputStream reply;

    boolean success;

    private static MessageComposerRunnable inst;

    public static MessageComposerRunnable instance() {
        if (inst == null) {
            try {
                inst = (MessageComposerRunnable) ImmortalMemory.instance()
                        .newInstance(MessageComposerRunnable.class);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.FATAL, MessageComposerRunnable.class, "instance", e);
            }
        }
        return inst;
    }

    /**
     * Client upcall:
     * <p>
     * <b>Client scope </b> --ex in --&gt; ORB parent scope --&gt; ORB scope
     * --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public void init(ClientRequest clr) {
        this.clr = clr;
    }

    /**
     * Client upcall:
     * <p>
     * Client scope --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt;
     * <b>Message scope/Waiter region </b> --&gt; Transport scope
     * </p>
     */
    private int statCount = 0;
    public void run() {
            if (statCount % 100 == 0) {
                edu.uci.ece.zen.utils.Logger.printMemStats(6);
            }
            statCount++;

        //setup waiting straterg
        WaitingStrategy waitingStrategy = null;
        if (clr.responseExpected) {
            //waitingStrategy = TwoWayWaitingStrategy.instance();//new
            // TwoWayWaitingStrategy();
            //TODO: Krishna, make sure this is correct, had to do this to solve
            // mem leak
            waitingStrategy = (WaitingStrategy) (((ScopedMemory) RealtimeThread
                    .getCurrentMemoryArea()).getPortal());
            if (waitingStrategy == null) {
                waitingStrategy = new TwoWayWaitingStrategy();
                ((ScopedMemory) RealtimeThread.getCurrentMemoryArea())
                        .setPortal(waitingStrategy);
            }
            clr.registerWaiter();
        }

        ExecuteInRunnable eir = clr.orb.getEIR();//new ExecuteInRunnable();
        SendMessageRunnable smr = SendMessageRunnable.instance();
        smr.init(clr.transportScope);
        smr.init(clr.out.getBuffer());
        eir.init(smr, clr.transportScope);
        success = true;
        try {
            ZenProperties.logger.log("MCR run 1");
            clr.orb.orbImplRegion.executeInArea(eir);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.SEVERE,
                    getClass(), "run",
                    "Could not sent message on transport", e);
            clr.releaseWaiter();
            waitingStrategy = null;
            success = false;
        } finally {
            clr.out.free();
        }

        if (waitingStrategy != null) {
            reply = waitingStrategy.waitForReply();
            clr.releaseWaiter();
        }
        clr.orb.freeEIR( eir );
    }

    public CDRInputStream getReply() {
        return reply;
    }
}

