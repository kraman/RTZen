package edu.uci.ece.zen.orb.transport;

import javax.realtime.*;

public abstract class Acceptor{
    protected edu.uci.ece.zen.orb.ORB orb;
    protected boolean isActive;
    
    public Acceptor( edu.uci.ece.zen.orb.ORB orb ){
        this.orb = orb;
    }

    public final void startAccepting(){
        AcceptorRunnable runnable = new AcceptorRunnable( this );
        while( isActive ){
            ScopedMemory transportMem = orb.getScopedRegion();
            transportMem.enter( runnable );
        }
    }

    public final void shutdown( boolean waitForCompletion ){
        //waitForCompletion;  //currently ignored. always waiting for completion.
        isActive = false;
        internalShutdown();
    }

    protected final void registerTransport( Transport t ){
        ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).setPortal( t );
        NoHeapRealtimeThread transportThread = new NoHeapRealtimeThread(t);
        transportThread.start();
    }

    protected abstract void accept();
    protected abstract void internalShutdown();
    public abstract org.omg.CORBA.portable.IDLEntity getProfile( byte iiopMajorVersion , byte iiopMinorVersion );
}

class AcceptorRunnable implements Runnable{
    private Acceptor acc;
    public AcceptorRunnable( Acceptor acc ){ this.acc=acc; }

    public void run(){
        acc.accept();
    }
}
