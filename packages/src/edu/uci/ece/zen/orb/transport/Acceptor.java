package edu.uci.ece.zen.orb.transport;

import javax.realtime.*;
import org.omg.IOP.TaggedProfile;

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

    public TaggedProfile getProfile( byte iiopMajorVersion , byte iiopMinorVersion, byte[] objKey, MemoryArea clientRegion ){
        try{

            ProfileRunnable runnable = (ProfileRunnable)(clientRegion.newInstance( ProfileRunnable.class ));
            runnable.init( iiopMajorVersion , iiopMinorVersion , objKey, this );
            clientRegion.executeInArea( runnable );
            return runnable.getRetVal();

        }catch(IllegalAccessException iae){
            iae.printStackTrace();
        }catch(InstantiationException ie){
            ie.printStackTrace();
        }

        return null;
    }

    protected abstract TaggedProfile getInternalProfile( byte iiopMajorVersion , byte iiopMinorVersion, byte[] objKey);
}

class ProfileRunnable implements Runnable{
    private byte major;
    private byte minor;
    private byte[] objKey;
    private Acceptor acc;

    private TaggedProfile retVal;

    public ProfileRunnable()
    {}

    public void init(byte major , byte minor , byte[] objKey, Acceptor acc){
        this.major = major;
        this.minor = minor;
        this.acc = acc;
        this.objKey = objKey;
    }

    public TaggedProfile getRetVal(){ return retVal; }

    public void run(){

        retVal = acc.getInternalProfile(major, minor, objKey);
    }
}

class AcceptorRunnable implements Runnable{
    private Acceptor acc;
    public AcceptorRunnable( Acceptor acc ){ this.acc=acc; }

    public void run(){
        acc.accept();
    }
}
