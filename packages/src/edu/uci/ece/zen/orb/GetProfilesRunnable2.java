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
import edu.uci.ece.zen.orb.GetProfilesRunnable1;
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

public class GetProfilesRunnable2 implements Runnable{
    
    FString objKey;
    MemoryArea clientArea; 
    POA poa;
    int i;
    CDROutputStream out;

    private static GetProfilesRunnable2 instance;

    public static GetProfilesRunnable2 instance(){
        if(instance == null){
            try{
                instance = (GetProfilesRunnable2) (ImmortalMemory.instance().newInstance(GetProfilesRunnable2.class));
            }catch(Exception e){
                e.printStackTrace();//TODO better error handling
            }
        }
        return instance;
    }        
    
    public GetProfilesRunnable2() {
    }   

    public void init(int i, FString objKey, MemoryArea clientArea, 
            POA poa, CDROutputStream out) {
         this.clientArea = clientArea; this.poa = poa; 
         this.objKey = objKey; this.out = out;
    }

    public void run() {
        if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("GetProfilesRunnable2 6");
        Acceptor acc = (Acceptor)((ScopedMemory) RealtimeThread.getCurrentMemoryArea()).getPortal();
        if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("GetProfilesRunnable2 7");
        TaggedProfile [] tparr = acc.getProfiles((byte) 1, ZenProperties.iiopMinor, 
                objKey.getTrimData(clientArea), clientArea, poa);
        for(int j = 0; j < tparr.length; ++j)
            TaggedProfileHelper.write(out, tparr[j]);
        out.updateProfileLength(tparr.length);
        if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("GetProfilesRunnable2 8, length:" + tparr.length);
        //System.out.println("prof: " + out.toString());
    }    
}
