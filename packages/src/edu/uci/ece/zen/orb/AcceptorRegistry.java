package edu.uci.ece.zen.orb;

import java.util.Vector;

import javax.realtime.InaccessibleAreaException;
import javax.realtime.MemoryArea;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import org.omg.IOP.TaggedProfile;

import edu.uci.ece.zen.orb.transport.Acceptor;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ZenProperties;

/**
 * This is a registry for all Acceptor objects that are created for incomming
 * connections. Current this class uses a java.util.Vector class to store the
 * acceptors but this will replaced later. Also, there is currently no way to
 * remove an acceptor once it has been added.
 *
 * @author Krishna Raman
 */
public class AcceptorRegistry {
    /** A normal java.util.Vector to store the acceptor objects. */
    private Vector list = new Vector();

    /**
     * Function to return all transport profiles for acceptors stored in this
     * registry. The profile objects are created in the client area that is
     * passed in.
     *
     * @param objKey
     *            The object key to embed in the profile.
     * @param clientArea
     *            The memory area to create the profiles in.
     * @return A array containing the list of transport profiles.
     */
    public TaggedProfile[] getProfiles(FString objKey, MemoryArea clientArea)
            throws IllegalAccessException, InstantiationException,
            InaccessibleAreaException {
        TaggedProfile[] tpList = (TaggedProfile[]) clientArea.newArray(
                org.omg.IOP.TaggedProfile.class, list.size());
        byte[] tempOKey = objKey.getTrimData(clientArea);
        //System.out.println( "okey fstr len: " + objKey.length() + " barray len: " + tempOKey.length );
        ARRunnable ar = new ARRunnable();

        for (int i = 0; i < list.size(); ++i) {
            ScopedMemory sm = (ScopedMemory) (list.get(i));
            ar.init(i, tempOKey, clientArea, tpList);
            sm.enter(ar);
        }

        return tpList;
    }

    /**
     * This method adds the acceptor to the registry.
     */
    public void addAcceptor(ScopedMemory acceptorArea) {
        list.add(acceptorArea);
    }

    /**
     * Method to shutdown all acceptors.
     */
    public void shutdown() {
        ARShutdownRunnable r = new ARShutdownRunnable();

        for (int i = 0; i < list.size(); ++i) {
            ScopedMemory sm = (ScopedMemory) (list.get(i));
            //sm.enter( ar );
            ORB.freeScopedRegion(sm);
        }
    }
}

class ARShutdownRunnable {
    public ARShutdownRunnable() {
    }

    public void run() {
        Acceptor acc = (Acceptor) ((ScopedMemory) RealtimeThread
                .getCurrentMemoryArea()).getPortal();
        acc.shutdown();
    }
}

class ARRunnable implements Runnable {

    private int index;

    MemoryArea ma;

    byte[] objKey;

    TaggedProfile[] tpList;

    public ARRunnable() {
    }

    public void init(int ind, byte[] objKey, MemoryArea clientArea,
            TaggedProfile[] tpList) {
        index = ind;
        this.ma = clientArea;
        this.objKey = objKey;
        this.tpList = tpList;
    }

    public void run() {
        Acceptor acc = (Acceptor) ((ScopedMemory) RealtimeThread
                .getCurrentMemoryArea()).getPortal();
        tpList[index] = acc.getProfile((byte) 1, ZenProperties.iiopMinor, objKey, ma);
    }
}
