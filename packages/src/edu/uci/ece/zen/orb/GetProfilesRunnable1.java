/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import javax.realtime.NoHeapRealtimeThread;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;
import javax.realtime.InaccessibleAreaException;
import javax.realtime.MemoryArea;
import javax.realtime.ImmortalMemory;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.CDRInputStream;
import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.PriorityMappingImpl;
import edu.uci.ece.zen.poa.POA;
import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.orb.transport.iiop.AcceptorRunnable;
import edu.uci.ece.zen.orb.transport.Acceptor;
import edu.uci.ece.zen.poa.HandleRequestRunnable;
import org.omg.IOP.TaggedProfile;
import org.omg.IOP.TaggedProfileHelper;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;
import org.omg.RTCORBA.ThreadpoolLane;
import org.omg.RTCORBA.ThreadpoolLanesHelper;
import edu.uci.ece.zen.utils.*;

public class GetProfilesRunnable1 implements Runnable{
    
    ScopedMemory accArea; 
    FString objKey;
    MemoryArea clientArea; 
    POA poa;
    int i;
    CDROutputStream out;
    
    private static GetProfilesRunnable1 instance;

    public static GetProfilesRunnable1 instance(){
        if(instance == null){
            try{
                instance = (GetProfilesRunnable1) (ImmortalMemory.instance().newInstance(GetProfilesRunnable1.class));
            }catch(Exception e){
                e.printStackTrace();//TODO better error handling
            }
        }
        return instance;
    }    

    public GetProfilesRunnable1() {
    }

    public void init(int i, ScopedMemory accArea, FString objKey, MemoryArea clientArea, 
            POA poa, CDROutputStream out) {
         this.i = i; this.accArea = accArea; this.clientArea = clientArea;
         this.poa = poa; this.objKey = objKey; this.out = out;
    }

    public void run() {
        if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("GetProfilesRunnable1 6");
        GetProfilesRunnable2 gpr2 = GetProfilesRunnable2.instance();//new GetProfilesRunnable2();//TODO -- static? per TP?
        gpr2.init(i, objKey, clientArea, poa, out); 
        if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("GetProfilesRunnable1 7");
        accArea.enter(gpr2);
    }    
}

