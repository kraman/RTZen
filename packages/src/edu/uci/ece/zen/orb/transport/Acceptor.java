package edu.uci.ece.zen.orb.transport;

import javax.realtime.MemoryArea;
import javax.realtime.NoHeapRealtimeThread;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;
import edu.uci.ece.zen.orb.ORB;

import org.omg.IOP.TaggedProfile;

import edu.uci.ece.zen.utils.ExecuteInRunnable;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;
import javax.realtime.PriorityParameters;

public abstract class Acceptor {
    protected edu.uci.ece.zen.orb.ORB orb;

    protected edu.uci.ece.zen.orb.ORBImpl orbImpl;

    protected boolean isActive;

    protected AcceptorLogic acceptorLogic;

    protected RealtimeThread acceptorLogicThread;

    public Acceptor(edu.uci.ece.zen.orb.ORB orb,
            edu.uci.ece.zen.orb.ORBImpl orbImpl) {
        this.orb = orb;
        this.orbImpl = orbImpl;
    }

    public final void startAccepting( int priority ) {
        acceptorLogic = new AcceptorLogic(this);
        isActive = true;
        acceptorLogicThread = new NoHeapRealtimeThread( new PriorityParameters(priority) , null, null,
                RealtimeThread.getCurrentMemoryArea(), null, acceptorLogic);
        acceptorLogicThread.start();
    }

    public final void shutdown() {
        isActive = false;
        internalShutdown();
        acceptorLogicThread.interrupt();
        synchronized (acceptorLogic) {
            try {
                acceptorLogic.wait();
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, getClass(), "shutdown", e);
            }
        }
    }

    protected final void registerTransport(Transport t) {
        ((ScopedMemory) RealtimeThread.getCurrentMemoryArea()).setPortal(t);
        RealtimeThread transportThread = new NoHeapRealtimeThread(null, null,
                null, RealtimeThread.getCurrentMemoryArea(), null, t);
        transportThread.start();
    }

    protected abstract void accept();

    protected abstract void internalShutdown();

    private ProfileRunnable prunnable;

    public synchronized TaggedProfile getProfile(byte iiopMajorVersion,
            byte iiopMinorVersion, byte[] objKey, MemoryArea clientRegion) {
        try {
            if (ZenProperties.dbg) ZenProperties.logger.log("Acceptor client region: " + clientRegion);
            edu.uci.ece.zen.utils.Logger.printThreadStack();
            if (prunnable == null) prunnable = new ProfileRunnable();
            prunnable.init(iiopMajorVersion, iiopMinorVersion, objKey, this);
            clientRegion.executeInArea(prunnable);
            return prunnable.getRetVal();
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "getProfile", e);
        }
        return null;
    }

    protected abstract TaggedProfile getInternalProfile(byte iiopMajorVersion,
            byte iiopMinorVersion, byte[] objKey);

    public void finalize() {
        ZenProperties.logger.log("Acceptor region has been GC'd");
    }
}

class AcceptorLogic implements Runnable {
    Acceptor acc;

    public AcceptorLogic(Acceptor acc) {
        this.acc = acc;
    }

    public void run() {
        AcceptRunnable runnable = new AcceptRunnable(acc);
        ExecuteInRunnable eir = new ExecuteInRunnable();
        while (acc.isActive) {
            try {
                ScopedMemory transportMem = ORB.getScopedRegion();
                eir.init(runnable, transportMem);
                acc.orb.orbImplRegion.executeInArea(eir);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, getClass(), "run", e);
            }
        }

        //notify the exit of this thread
        synchronized (this) {
            this.notifyAll();
        }
    }
}

class ProfileRunnable implements Runnable {
    private byte major;

    private byte minor;

    private byte[] objKey;

    private Acceptor acc;

    private TaggedProfile retVal;

    public ProfileRunnable() {
    }

    public void init(byte major, byte minor, byte[] objKey, Acceptor acc) {
        this.major = major;
        this.minor = minor;
        this.acc = acc;
        this.objKey = objKey;
    }

    public TaggedProfile getRetVal() {
        return retVal;
    }

    public void run() {
        retVal = acc.getInternalProfile(major, minor, objKey);
    }
}

class AcceptRunnable implements Runnable {
    private Acceptor acc;

    public AcceptRunnable(Acceptor acc) {
        this.acc = acc;
    }

    public void run() {
        acc.accept();
    }
}
