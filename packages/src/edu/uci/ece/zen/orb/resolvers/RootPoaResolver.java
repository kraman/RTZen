package edu.uci.ece.zen.orb.resolvers;

import javax.realtime.*;
import edu.uci.ece.zen.utils.*;
import java.util.*;
import edu.uci.ece.zen.orb.*;

public class RootPoaResolver extends Resolver{
    static{
        Resolver r = ImmortalMemory.instance().newInstance( RootPoaResolver.class );
        Resolver.registerResolver( this );
    }
    
    protected class RootPoaResolver(){
        super( "RootPOA" );
    }

    public org.omg.CORBA.Object resolve( ORB orb ){
        return orb.resolveRootPOA();
    }
}
