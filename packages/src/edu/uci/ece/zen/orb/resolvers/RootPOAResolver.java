/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.resolvers;

import javax.realtime.*;
import edu.uci.ece.zen.utils.*;
import java.util.*;
import edu.uci.ece.zen.orb.*;

public class RootPOAResolver extends Resolver{
    static{
        Resolver r = ImmortalMemory.instance().newInstance( RootPoaResolver.class );
        Resolver.registerResolver( this );
    }
    
    protected class RootPOAResolver(){
        super( "RootPOA" );
    }

    public org.omg.CORBA.Object resolve( ORB orb ){
        POA rootPoa = edu.uci.ece.zen.poa.POA.instance();
        rootPoa.initAsRootPOA( orb );
        if( orb.getRootPOA() == null )
            orb.setRootPOA(  );
        return orb.getRootPOA();
    }
}
