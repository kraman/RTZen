package edu.uci.ece.zen.orb.*;

import javax.realtime.*;
import edu.uci.ece.zen.utils.*;
import java.util.*;

public abstract class Resolver{
    private static java.util.Vector resolverList;

    protected static registerResolver( Resolver resolver ){
        if( resolverList == null )
            resolverList = ImmortalMemory.instance().newInstance( java.util.Vector.class );
        resolverList.add( resolver );
    }

    public static String[] getResolverStrings(){
        String[] ret = new String[ resolver.size() ];
        for( int i=0;i<resolverList.size();i++ )
            ret[i] = (String)((Resolver)resolverList.elementAt(i).toString());
        return ret;
    }

    public static Object resolve( ORB orb , String str ){
        for( int i=0;i<resolverList.size();i++ ){
            Resolver res = ((Resolver)resolverList.elementAt(i));
            if( res.toString().equals( str ) )
                return res.resolve( orb );
        }
        return null;
    }

    private final String resolverString;
    protected Resolver( String resolverString ){

        if ( RealtimeThread.getCurrentMemoryArea() != ImmortalMemory.instance() ){
            ZenProperties.logger.log(
                    Logger.FATAL,
                    "edu.uci.ece.zen.orb.Resolver",
                    "<init>"
                    "Resolver is not allocated in ImmortalMemory" );
            System.exit(-1);
        }
        this.resolverString = resolverString;
    }

    public final String toString(){ return resolverString; }
    public abstract org.omg.CORBA.Object resolve( ORB orb );
}
