package edu.uci.ece.zen.orb;

import javax.realtime.*;
import edu.uci.ece.zen.orb.giop.*;
import edu.uci.ece.zen.utils.*;

public class SendMessageRunnable implements Runnable{

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
