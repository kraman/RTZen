package edu.uci.ece.zen.orb;

import javax.realtime.*;
import java.util.Vector;
import org.omg.IOP.TaggedProfile;
import edu.uci.ece.zen.orb.transport.Acceptor;

public class AcceptorRegistry{

    private Vector list = new Vector();
    private TaggedProfile[] tpList;

    public TaggedProfile[] getProfiles(byte [] objKey, int objKeyLength){
        tpList = new TaggedProfile[list.size()];

        byte [] tempOKey = new byte[objKeyLength];

        //kludge: assuming objKey.length >= objKeyLength
        System.arraycopy(objKey, 0, tempOKey, 0, objKeyLength);

        for(int i = 0; i < list.size(); ++i){
            ScopedMemory sm = (ScopedMemory)(list.get(i));
            sm.enter(new ARRunnable(i, tempOKey, RealtimeThread.getCurrentMemoryArea()));

        }

        return tpList;
    }
    //
    class ARRunnable implements Runnable{

        private int index;
        MemoryArea ma;
        byte[] objKey;

        public ARRunnable(int ind, byte [] objKey, MemoryArea ma){
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



