package edu.uci.ece.zen.orb;

import javax.realtime.*;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.utils.*;

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
        out.init( orb );
        this.giopMajor = giopMajor;
        this.giopMinor = giopMinor;
        int threadJavaPriority = Thread.currentThread().getPriority();
        LaneInfo ln = del.getLane();
        transportScope = ln.transpScope;
        objectKey = ln.getObjectKey();

        //TODO:Assemble and write message header and policies here
        messageId = WaitingStrategy.newMessageId();
        edu.uci.ece.zen.orb.giop.GIOPMessageFactory.constructMessage( this , messageId , out );
        if( ZenProperties.dbg )
            System.err.println( "Message header assembled" );
    }

    /**
     * Client upcall:
     * <p>
     *     <b>Client scope</b> --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */   
    public CDRInputStream invoke(){
        System.out.println( Thread.currentThread() + "In client memory: " + RealtimeThread.getCurrentMemoryArea() );
        out.updateLength();
        if( ZenProperties.dbg )
            System.err.println( "Sending message" );
        MessageComposerRunnable mcr = new MessageComposerRunnable( this );
        ScopedMemory messageScope = orb.getScopedRegion();

        System.out.println( Thread.currentThread() + "ORBImpl perent memory: " + orb.parentMemoryArea );
        System.out.println( Thread.currentThread() + "ORBImpl memory: " + orb.orbImplRegion );
        System.out.println( Thread.currentThread() + "message memory: " + messageScope );

        ExecuteInRunnable erOrbMem = ExecuteInRunnable.instance();
        ExecuteInRunnable erMsgMem = ExecuteInRunnable.instance();

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
        erOrbMem.free();
        erMsgMem.free();
                
        orb.freeScopedRegion( messageScope );
        if( mcr.success )
            return mcr.getReply();
        else
            throw new org.omg.CORBA.TRANSIENT();
    }

    public void registerWaiter(){
        if( ZenProperties.dbg )
            System.err.println( "Waiter registered for req id: " + messageId );
        orb.registerWaiter( messageId );
    }

    public void releaseWaiter(){
        if( ZenProperties.dbg )
            System.err.println( "Waiter released for req id: " + messageId );
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
        System.out.println( Thread.currentThread() + "In memory: " + RealtimeThread.getCurrentMemoryArea() );
        //setup waiting stratergy
        WaitingStrategy waitingStrategy = null;
        if( clr.responseExpected )
            waitingStrategy = new TwoWayWaitingStrategy();
        ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).setPortal( waitingStrategy );
        clr.registerWaiter();
        if( ZenProperties.dbg )
            System.err.println( "Waiter registered" );

        ExecuteInRunnable eir = ExecuteInRunnable.instance();
        SendMessageRunnable smr = SendMessageRunnable.instance();
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
            eir.free();
            clr.out.free();
            smr.free();
            if( ZenProperties.dbg )
                System.err.println( "Message sent" );
        }

        if( waitingStrategy != null ){
            if( ZenProperties.dbg )
                System.err.println( "Waiting for a reply" );
            reply = waitingStrategy.waitForReply();
            clr.releaseWaiter();
            if( ZenProperties.dbg )
                System.err.println( "Got a reply...woohoo: " + reply );
        }
    }

    public CDRInputStream getReply(){ return reply; }
}

class SendMessageRunnable implements Runnable{

    private static Queue sendMessageRunnableCache;
    static{
        try{
            sendMessageRunnableCache = (Queue) ImmortalMemory.instance().newInstance( Queue.class );
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
    
    public static SendMessageRunnable instance(){
        SendMessageRunnable r = (SendMessageRunnable) sendMessageRunnableCache.dequeue();
        if( r == null ){
            try{
                return (SendMessageRunnable) ImmortalMemory.instance().newInstance( SendMessageRunnable.class );
            }catch( Exception e ){
                e.printStackTrace();
            }
        }else
            return r;
        return null;
    }

    private static void release( SendMessageRunnable r ){
        sendMessageRunnableCache.enqueue( r );
    }
    
    WriteBuffer msg;

    public SendMessageRunnable(){}

    /**
     * Client upcall:
     * <p>
     *     Client scope --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt; <b>Message scope/Waiter region</b> --&gt; Transport scope
     * </p>
     */
    public void init( WriteBuffer buffer ){
        this.msg = buffer;
    }

    /**
     * Client upcall:
     * <p>
     *     Client scope --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt; Message scope/Waiter region --&gt; <b>Transport scope</b>
     * </p>
     */
    public void run(){
        edu.uci.ece.zen.orb.transport.Transport trans = (edu.uci.ece.zen.orb.transport.Transport) ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).getPortal();
        trans.send( msg );
    }

    public void free(){
        SendMessageRunnable.release( this );
    }
}
