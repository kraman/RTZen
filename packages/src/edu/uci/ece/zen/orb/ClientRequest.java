package edu.uci.ece.zen.orb;

import javax.realtime.*;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.utils.*;
import org.omg.RTCORBA.*;
import org.omg.Messaging.*;
import org.omg.IOP.*;

public class ClientRequest extends org.omg.CORBA.portable.OutputStream{
    public String operation;
    public boolean responseExpected;
    public ObjRefDelegate del;
    public CDROutputStream out;
    public byte giopMajor;
    public byte giopMinor;
    public ScopedMemory transportScope;
    public byte[] objectKey;
    public ORB orb;
    private int messageId;
    public ServiceContext[] contexts;

    /**
     * Client upcall:
     * <p>
     *     <b>Client scope</b> --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public ClientRequest( String operation , boolean responseExpected , byte giopMajor , byte giopMinor , ORB orb , ObjRefDelegate del )
    {
        this.orb = orb;
        this.del = del;
        this.operation = operation;
        this.responseExpected = responseExpected;
        out = CDROutputStream.instance();
        
        if(ZenProperties.devDbg) System.out.println( "1ClientRequest: cur ORBImpl mem (cons, remaining)= " + orb.orbImplRegion.memoryConsumed() + " " + orb.orbImplRegion.memoryRemaining());
        if(ZenProperties.devDbg) System.out.println( "1ClientRequest: cur Client mem (cons, remaining)= " + orb.parentMemoryArea.memoryConsumed() + " " + orb.parentMemoryArea.memoryRemaining());
        
        if(ZenProperties.devDbg) System.out.println( "ClientRequest 1" );
        out.init( orb );
        if(ZenProperties.devDbg) System.out.println( "ClientRequest 2" );
        this.giopMajor = giopMajor;
        this.giopMinor = giopMinor;
        if(ZenProperties.devDbg) System.out.println( "ClientRequest 3" );
        LaneInfo ln = del.getLane();
        if(ZenProperties.devDbg) System.out.println( "ClientRequest 5" );
        transportScope = ln.transpScope;
        if(ZenProperties.devDbg) System.out.println( "ClientRequest 6" );
        objectKey = ln.getObjectKey();
        if(ZenProperties.devDbg) System.out.println( "ClientRequest 7" );

        //TODO:Assemble and write message header and policies here


        if(ZenProperties.devDbg) System.out.println( "2ClientRequest: cur ORBImpl mem (cons, remaining)= " + orb.orbImplRegion.memoryConsumed() + " " + orb.orbImplRegion.memoryRemaining());
        if(ZenProperties.devDbg) System.out.println( "2ClientRequest: cur Client mem (cons, remaining)= " + orb.parentMemoryArea.memoryConsumed() + " " + orb.parentMemoryArea.memoryRemaining());
        if(del.priorityModel == PriorityModel._CLIENT_PROPAGATED && del.serverPriority >= 0){
            contexts = new ServiceContext[1]; //kludge: of course there will be more than just 1
            contexts[0] = new ServiceContext();
            contexts[0].context_id = RTCorbaPriority.value;
            CDROutputStream out1 = CDROutputStream.create(orb);
            short priority = orb.getRTCurrent().the_priority();
            out1.write_short(priority);

            int lim = (int)out1.getBuffer().getLimit();
            contexts[0].context_data = new byte[lim];
            out1.getBuffer().getReadBuffer().readByteArray(contexts[0].context_data, 0 , lim);

            //System.out.println("data " + contexts[0].context_data[0] + "-" + contexts[0].context_data[1] + "-" +
            //                             contexts[0].context_data[2] + "-" + contexts[0].context_data[3]);

            if(ZenProperties.devDbg) System.out.println("CLIENT_PROPAGATED policy -- Sending priority: " + priority);
            out1.free();
        }else{
             contexts = new ServiceContext[0];
        }

        messageId = WaitingStrategy.newMessageId();
        if(ZenProperties.devDbg) System.out.println( "ClientRequest 8" );
        edu.uci.ece.zen.orb.giop.GIOPMessageFactory.constructMessage( this , messageId , out );
        if(ZenProperties.devDbg) System.out.println( "ClientRequest 9" );
        if(ZenProperties.devDbg) System.out.println( "3ClientRequest: cur ORBImpl mem (cons, remaining)= " + orb.orbImplRegion.memoryConsumed() + " " + orb.orbImplRegion.memoryRemaining());
        if(ZenProperties.devDbg) System.out.println( "3ClientRequest: cur Client mem (cons, remaining)= " + orb.parentMemoryArea.memoryConsumed() + " " + orb.parentMemoryArea.memoryRemaining());
    }

    /**
     * Client upcall:
     * <p>
     *     <b>Client scope</b> --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public CDRInputStream invoke(){
        out.updateLength();
        MessageComposerRunnable mcr = new MessageComposerRunnable( this );
        ScopedMemory messageScope = orb.getScopedRegion();

        ExecuteInRunnable erOrbMem = new ExecuteInRunnable();
        ExecuteInRunnable erMsgMem = new ExecuteInRunnable();

        erOrbMem.init( erMsgMem , orb.orbImplRegion );
        erMsgMem.init( mcr, messageScope );

        try{
            if( orb.parentMemoryArea == RealtimeThread.getCurrentMemoryArea() )
                erOrbMem.run();
            else
                orb.parentMemoryArea.executeInArea( erOrbMem );
        }catch( Exception e ){
            ZenProperties.logger.log(
                Logger.SEVERE,
                "edu.uci.ece.zen.orb.ClientRequest",
                "invoke()",
                "Could not invoke remote object due to exception: " + e.toString()
                );
        }

        orb.freeScopedRegion( messageScope );
        if( mcr.success )
            return mcr.getReply();
        else
            throw new org.omg.CORBA.TRANSIENT();
    }

    public void registerWaiter(){
        orb.registerWaiter( messageId );
    }

    public void releaseWaiter(){
        orb.releaseWaiter( messageId );
    }

    //Redirect OutputStream methods to CDROutputStream

    public final void write_octet(final byte value) { out.write_octet(value); }
    public void write_octet_array(byte[] value, int offset, int length) { out.write_octet_array( value , offset , length ); }
    public void write_boolean(boolean value) { out.write_boolean( value ); }
    public void write_boolean_array(boolean[] value, int offset, int length) { out.write_boolean_array( value , offset , length ); }
    public void write_char(char c) { out.write_char( c ); }
    public void write_char_array(char[] value, int offset, int length) { out.write_char_array( value , offset , length ); }
    public void write_string(String s) { out.write_string( s ); }
    public org.omg.CORBA.portable.InputStream create_input_stream() { return out.create_input_stream(); }
    public void write_short(short value) { out.write_short( value ); }
    public void write_short_array(short[] value, int offset, int length) { out.write_short_array( value , offset , length ); }
    public void write_long(int value) { out.write_long( value ); }
    public void write_long_array(int[] value, int offset, int length) { out.write_long_array( value , offset , length ); }
    public void write_wchar(char c) { out.write_wchar( c ); }
    public void write_wchar_array(char[] value, int offset, int length) { out.write_wchar_array( value , offset , length ); }
    public void free() { out.free(); }
    public void write_wstring(String s) { out.write_wstring( s ); }
    public void write_float(float value) { out.write_float( value ); }
    public void write_float_array(float[] value, int offset, int length) { out.write_float_array( value , offset , length ); }
    public void write_double(double value) { out.write_double( value ); }
    public void write_double_array(double[] value, int offset, int length) { out.write_double_array( value , offset , length ); }
    public void write_longlong(long value) { out.write_longlong( value ); }
    public void write_longlong_array(long[] value, int offset, int length) { out.write_longlong_array( value , offset , length ); }
    public void write_ulong(int value) { out.write_ulong( value ); }
    public void write_ulong_array(int[] value, int offset, int length) { out.write_ulong_array( value , offset , length ); }
    public void write_ulonglong(long value) { out.write_ulonglong( value ); }
    public void write_ulonglong_array(long[] value, int offset, int length) { out.write_ulonglong_array( value , offset , length ); }
    public void write_ushort(short value) { out.write_ushort( value ); }
    public void write_ushort_array(short[] value, int offset, int length) { out.write_ushort_array( value , offset , length ); }
    public void write_Object(org.omg.CORBA.Object value) { out.write_Object( value ); }
    public void write_CDROutputStream(CDROutputStream cdr) { out.write_CDROutputStream( cdr ); }
    public void write_Principal(org.omg.CORBA.Principal pr) { out.write_Principal( pr ); }

    public void write_TypeCode(org.omg.CORBA.TypeCode value) { out.write_TypeCode( value ); }
    public void write_any(org.omg.CORBA.Any value) { out.write_any( value ); }
}

class MessageComposerRunnable implements Runnable{
    ClientRequest clr;
    CDRInputStream reply;
    boolean success;

    /**
     * Client upcall:
     * <p>
     *     <b>Client scope</b> --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public MessageComposerRunnable( ClientRequest clr ){
        this.clr = clr;
    }

    /**
     * Client upcall:
     * <p>
     *     Client scope --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt; <b>Message scope/Waiter region</b> --&gt; Transport scope
     * </p>
     */
    public void run(){
        //setup waiting straterg
        WaitingStrategy waitingStrategy = null;
        if( clr.responseExpected )
            waitingStrategy = new TwoWayWaitingStrategy();
        ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).setPortal( waitingStrategy );
        clr.registerWaiter();
        ExecuteInRunnable eir = new ExecuteInRunnable();
        SendMessageRunnable smr = new SendMessageRunnable(clr.transportScope);
        smr.init( clr.out.getBuffer() );
        eir.init( smr , clr.transportScope );
        success = true;
        try{
            clr.orb.orbImplRegion.executeInArea( eir );
        }catch( Exception e ){
            ZenProperties.logger.log(
                Logger.SEVERE,
                "edu.uci.ece.zen.orb.MessageComposerRunnable",
                "run",
                "Could not sent message on transport due to exception: " + e.toString()
                );
            clr.releaseWaiter();
            waitingStrategy = null;
            success = false;
        }finally{
            clr.out.free();
        }

        if( waitingStrategy != null ){
            reply = waitingStrategy.waitForReply();
            clr.releaseWaiter();
        }
    }

    public CDRInputStream getReply(){ return reply; }
}

