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

public class LaneInfo {
    public LaneInfo() {
        /*
        try {
            objectKey = new FString();
            objectKey.init(1024);
        } catch (Exception e2) {
            ZenProperties.logger.log(Logger.SEVERE,
                    getClass(), "<init>",
                    "Could not initialize Lane", e2);
        }
        */
    }

    public int minPri;
    public int maxPri;
    public ScopedMemory transpScope;
    public FString objectKey;
    private Class protocolfactory;

    public void init(int minPri, int maxPri, ScopedMemory transpScope, FString objKey, Class pf ) {
        this.minPri = minPri;
        this.maxPri = maxPri;
        this.transpScope = transpScope;
        //objectKey.reset();
        //objectKey.append(objKey, 0, objKey.length);
        this.objectKey = objKey;
        this.protocolfactory = pf;
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
}
