package edu.uci.ece.zen.orb.transport;

import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;

public abstract class Connector{
    public Connector(){
    }

    public final ScopedMemory connect( String host , short port , edu.uci.ece.zen.orb.ORB orb , ORBImpl orbImpl ){

        Hashtable orbImplCache = orbImpl.cachedObjects;
        Queue eirCache = (Queue)orbImplCache.get( "ExecuteInRunnable" );
        Queue crCache = (Queue)orbImplCache.get( "ConnectorRunnable" );

        ExecuteInRunnable eir = (ExecuteInRunnable) eirCache.dequeue();
        if( eir == null )
            eir = new ExecuteInRunnable();
        ExecuteInRunnable eir2 = (ExecuteInRunnable) eirCache.dequeue();
        if( eir2 == null )
            eir2 = new ExecuteInRunnable();
        ConnectorRunnable connRunnable = (ConnectorRunnable) crCache.dequeue();
        if( connRunnable == null )
            connRunnable = new ConnectorRunnable();
        
        ScopedMemory transportMem = orb.getScopedRegion();
        connRunnable.init( host , port , this , orb );
        eir.init( eir2 , orb.orbImplRegion );
        eir2.init( connRunnable , transportMem );
        try{
            orb.parentMemoryArea.executeInArea( eir );
        }catch( Exception e ){
            e.printStackTrace();
        }
        eirCache.enqueue( eir );
        eirCache.enqueue( eir2 );
        crCache.enqueue( connRunnable );
        return transportMem;
    }

    protected abstract Transport internalConnect( String host , int port , edu.uci.ece.zen.orb.ORB orb , edu.uci.ece.zen.orb.ORBImpl orbImpl );
}
