package edu.uci.ece.zen.orb;

import javax.realtime.*;
import java.util.Vector;
import org.omg.IOP.TaggedProfile;
import edu.uci.ece.zen.orb.transport.Acceptor;

public class AcceptorRegistry{

    private Vector list = new Vector();
    private TaggedProfile[] tpList;

    public TaggedProfile[] getProfiles(){
        tpList = new TaggedProfile[list.size()];

        for(int i = 0; i < list.size(); ++i){
            ScopedMemory sm = (ScopedMemory)(list.get(i));
            sm.enter(new ARRunnable(i,RealtimeThread.getCurrentMemoryArea()));

        }

        return tpList;
    }

    class ARRunnable implements Runnable{

        private int index;
        MemoryArea ma;

        public ARRunnable(int ind, MemoryArea ma){
            index = ind;
            this.ma = ma;

        }

        public void run(){
            Acceptor acc = (Acceptor) ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).getPortal();
            tpList[index] = acc.getProfile((byte)1, (byte)0, ma);
        }
    }

}



