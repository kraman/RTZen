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
        
        System.out.println( Thread.currentThread() + "transport.Connector.connect 1" );
        ScopedMemory transportMem = orb.getScopedRegion();
        System.out.println( Thread.currentThread() + "transport.Connector.connect 2" );
        connRunnable.init( host , port , this , orb );
        System.out.println( Thread.currentThread() + "transport.Connector.connect 2-1" );
        eir.init( eir2 , orb.orbImplRegion );
        eir2.init( connRunnable , transportMem );
        System.out.println( Thread.currentThread() + "transport.Connector.connect 2-3" );
        try{
        System.out.println( Thread.currentThread() + "transport.Connector.connect 2-4" );
            orb.parentMemoryArea.executeInArea( eir );
        System.out.println( Thread.currentThread() + "transport.Connector.connect 2-5" );
        }catch( Exception e ){
            e.printStackTrace();
        }
        System.out.println( Thread.currentThread() + "transport.Connector.connect 2-6" );
        eirCache.enqueue( eir );
        eirCache.enqueue( eir2 );
        crCache.enqueue( connRunnable );
        System.out.println( Thread.currentThread() + "transport.Connector.connect 3" );
        return transportMem;
    }

    protected abstract Transport internalConnect( String host , int port , edu.uci.ece.zen.orb.ORB orb );
}
