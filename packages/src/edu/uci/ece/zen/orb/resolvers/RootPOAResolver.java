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
