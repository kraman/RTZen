/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import javax.realtime.InaccessibleAreaException;
import javax.realtime.MemoryArea;
import javax.realtime.ImmortalMemory;
import javax.realtime.ScopedMemory;
import javax.realtime.RealtimeThread;

import org.omg.CORBA.Policy;

import edu.uci.ece.zen.poa.POA;
import edu.uci.ece.zen.utils.ByteArrayCache;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;
import edu.uci.ece.zen.utils.ThreadPool;
import org.omg.IOP.TaggedProfile;
import org.omg.IOP.TaggedProfileHelper;

import edu.uci.ece.zen.orb.transport.Acceptor;


public class IORRunnable implements Runnable {

    FString objKey;
    MemoryArea clientArea; 
    POA poa;
    org.omg.IOP.IOR ior;
    CDROutputStream out;
    int priorityModel;
    short objectPriority;

    private static IORRunnable instance;

    public static IORRunnable instance(){
        if(instance == null){
            try{
                instance = (IORRunnable) (ImmortalMemory.instance().newInstance(IORRunnable.class));
            }catch(Exception e){
                e.printStackTrace();//TODO better error handling
            }
        }
        return instance;
    }

    public IORRunnable() {
    }

    public void init(FString objKey, MemoryArea clientArea, 
            POA poa, CDROutputStream out, int priorityModel , short objectPriority ) {

         if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("IORRunnable 2");
        this.clientArea = clientArea;
         if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("IORRunnable 3");
        this.poa = poa;
         if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("IORRunnable 4");
        this.ior = ior;
        this.objKey = objKey;
        this.out = out;
        this.priorityModel = priorityModel;
        this.objectPriority = objectPriority;
    }

    public void run() {
        ThreadPool tp = (ThreadPool)((ScopedMemory) RealtimeThread.getCurrentMemoryArea()).getPortal();
        tp.getProfiles(objKey, clientArea, poa, out, priorityModel , objectPriority );
    }
}
