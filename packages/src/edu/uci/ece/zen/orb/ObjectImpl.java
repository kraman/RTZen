/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;

public class ObjectImpl extends org.omg.CORBA.portable.ObjectImpl {
    public org.omg.IOP.IOR ior;

    private String[] ids;

    public ObjectImpl() {
        ids = new String[1];
    }

    public void init(org.omg.IOP.IOR ior) {
        this.ior = ior;
        ids[0] = ior.type_id;
        if (ZenBuildProperties.dbgIOR){
            ZenProperties.logger.log("It's in ObjectImpl.init() and the type id is "+ids[0]);
        }
    }

    public String[] _ids() {
        return ids;
    }

    protected void finalize(){
        if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("ObjectImpl being finalized with type id: " +ids[0]);
        _release();
    }
}
