package edu.uci.ece.zen.orb;

import javax.realtime.*;
import java.util.Vector;
import org.omg.IOP.TaggedProfile;
import edu.uci.ece.zen.orb.transport.Acceptor;
import edu.uci.ece.zen.utils.*;

public class AcceptorRegistry{

    private Vector list = new Vector();
    private TaggedProfile[] tpList;

    public TaggedProfile[] getProfiles( FString objKey, MemoryArea clientArea) 
            throws IllegalAccessException,InstantiationException
        {
        tpList = (TaggedProfile[]) clientArea.newArray(org.omg.IOP.TaggedProfile.class, list.size());
        byte [] tempOKey = objKey.getTrimData( clientArea );

        for(int i = 0; i < list.size(); ++i){
            ScopedMemory sm = (ScopedMemory)(list.get(i));
            sm.enter(new ARRunnable(i, tempOKey, clientArea));
        }

        return tpList;
    }
    //
    class ARRunnable implements Runnable{

        private int index;
        MemoryArea ma;
        byte[] objKey;

        public ARRunnable(int ind, byte [] objKey, MemoryArea clientArea){
            index = ind;
            this.ma = ma;
            this.objKey = objKey;
        }

        public void run(){
            Acceptor acc = (Acceptor) ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).getPortal();
            tpList[index] = acc.getProfile((byte)1, (byte)0, objKey, ma);
        }
    }

}



