/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import org.omg.RTCORBA.PriorityModel;

import edu.uci.ece.zen.orb.protocol.IOP.ServiceContext;
import edu.uci.ece.zen.utils.ExecuteInRunnable;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;

import edu.uci.ece.zen.utils.Queue;

public class ClientRequest extends org.omg.CORBA.portable.OutputStream {
    public String operation;
    public boolean responseExpected;
    public ObjRefDelegate del;
    public CDROutputStream out;
    public byte giopMajor;
    public byte giopMinor;
    public ScopedMemory transportScope;
    public FString objectKey;
    public ORB orb;
    private int messageId;
    public FString contexts;
    //private static ClientRequest inst;
    private static Queue queue = Queue.fromImmortal();
    private int replyStatus;

    public static ClientRequest instance() {
        ClientRequest cr = (ClientRequest)Queue.getQueuedInstance(ClientRequest.class,queue);
        return cr;
    }

    public ClientRequest(){
        contexts = FString.instance();
    }

    public void finalize(){
        FString.free( contexts );
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
        //TODO: throw appropriate error
        ZenProperties.logger.log("ClientRequest 5");
        transportScope = ln.transpScope;
        ZenProperties.logger.log("ClientRequest 6");
        objectKey = ln.getObjectKey();
        ZenProperties.logger.log("ClientRequest 7");

        if (ZenBuildProperties.dbgInvocations) ZenProperties.logger.log(
                "ClientRequest -- operation: " + operation.toString() +
                " objkey " + objectKey.decode() + " ORD: " + del.id());
        //TODO:Assemble and write message header and policies here
        contexts.reset();
        if (del.priorityModel == PriorityModel._CLIENT_PROPAGATED
                && del.serverPriority >= 0) {
            ZenProperties.logger.log("Sending CLIENT PROPAGATED service context");
            contexts.append(1); //list size
            contexts.append(org.omg.IOP.RTCorbaPriority.value);
            contexts.append(4); //length of data
            contexts.append((int) orb.getRTCurrent().the_priority());
        } else {
            contexts.append(0); //empty list
        }

        messageId = WaitingStrategy.newMessageId();
        ZenProperties.logger.log("ClientRequest 8");
        if(ZenBuildProperties.dbgInvocations)
            ZenProperties.logger.log("ClientRequest messageId:" + messageId);

        edu.uci.ece.zen.orb.protocol.MessageFactory.constructMessage(this,
                messageId, out);
        ZenProperties.logger.log("ClientRequest 9");
        replyStatus = -1;
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
        //out.printWriteBuffer(); This will printout every byte in the OutputStream
        edu.uci.ece.zen.utils.Logger.printMemStatsImm(311);
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
        edu.uci.ece.zen.utils.Logger.printMemStatsImm(312);
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
        edu.uci.ece.zen.utils.Logger.printMemStatsImm(313);
        ZenProperties.logger.log("ClientRequest invoke 3");
        ORB.freeScopedRegion(messageScope);
        //edu.uci.ece.zen.utils.Logger.printMemStatsImm(315);
        orb.freeEIR( erOrbMem );
        orb.freeEIR( erMsgMem );
        //edu.uci.ece.zen.utils.Logger.printMemStatsImm(316);
        CDRInputStream reply = null;
        if (mcr.success){
            //edu.uci.ece.zen.utils.Logger.printMemStatsImm(317);
            reply = mcr.getReply();
            replyStatus = mcr.getReplyStatus();
            //edu.uci.ece.zen.utils.Logger.printMemStatsImm(318);
            mcr.release();
            //edu.uci.ece.zen.utils.Logger.printMemStatsImm(319);
        }else{
            mcr.release();
            throw new org.omg.CORBA.TRANSIENT();
        }
        edu.uci.ece.zen.utils.Logger.printMemStatsImm(3100);
        return reply;
    }

    public int getReplyStatus(){
        return replyStatus;
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
        //out.free();
        queue.enqueue(this);
        //ServiceContext.release(contexts);
        //Thread.dumpStack();
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


