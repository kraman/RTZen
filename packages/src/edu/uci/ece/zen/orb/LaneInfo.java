/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import javax.realtime.ScopedMemory;

import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ExecuteInRunnable;
import edu.uci.ece.zen.utils.Queue;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ZenProperties;

public class LaneInfo {
    public LaneInfo() {
    }

    public int minPri;
    public int maxPri;
    public ScopedMemory transpScope;
    public FString objectKey;
    private Class protocolfactory;
    private ORB orb;

    public void init(int minPri, int maxPri, ScopedMemory transpScope, FString objKey, Class pf , ORB orb ) {
        this.minPri = minPri;
        this.maxPri = maxPri;
        this.transpScope = transpScope;
        //objectKey.reset();
        //objectKey.append(objKey, 0, objKey.length);
        this.objectKey = objKey;
        this.protocolfactory = pf;
        this.orb = orb;
    }

    /*
     * public byte[] getObjectKey(){ return objectKey.getTrimData(); }
     */
    public FString getObjectKey() {
        return objectKey;
    }

    public Class getProtocolFactory(){
        return protocolfactory;
    }

    public void releaseTransport(){
        ExecuteInRunnable eir = orb.getEIR();
        ExecuteInRunnable eir2 = orb.getEIR();

        eir.init(eir2, orb.orbImplRegion);
        eir2.init( ShutdownRunnable.instance() , transpScope);

        try {
            orb.parentMemoryArea.executeInArea(eir);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "releaseTransport", e);
        }

        orb.freeEIR( eir );
        orb.freeEIR( eir2 );
    }
}
