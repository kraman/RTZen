package edu.uci.ece.zen.orb;

import javax.realtime.*;
import java.util.Vector;
import org.omg.IOP.TaggedProfile;
import edu.uci.ece.zen.orb.transport.Acceptor;
import edu.uci.ece.zen.utils.*;

public class AcceptorRegistry{

    private Vector list = new Vector();

    public TaggedProfile[] getProfiles( FString objKey, MemoryArea clientArea)
            throws IllegalAccessException,InstantiationException,InaccessibleAreaException
    {
        TaggedProfile[] tpList = (TaggedProfile[]) clientArea.newArray(org.omg.IOP.TaggedProfile.class, list.size());
        byte[] tempOKey = objKey.getTrimData( clientArea );
        ARRunnable ar = new ARRunnable();

        for(int i = 0; i < list.size(); ++i){
            ScopedMemory sm = (ScopedMemory)(list.get(i));
            ar.init( i, tempOKey, clientArea , tpList );
            sm.enter( ar );
        }

        return tpList;
    }

    public void addAcceptor(ScopedMemory acceptorArea){
        list.add(acceptorArea);
    }
}

class ARRunnable implements Runnable{

    private int index;
    MemoryArea ma;
    byte[] objKey;
    TaggedProfile[] tpList;

    public ARRunnable(){}

    public void init(int ind, byte [] objKey, MemoryArea clientArea , TaggedProfile[] tpList ){
        index = ind;
        this.ma = clientArea;
        this.objKey = objKey;
        this.tpList = tpList;
    }

    public void run(){
        Acceptor acc = (Acceptor) ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).getPortal();
        tpList[index] = acc.getProfile((byte)1, (byte)0, objKey, ma);
    }
}
