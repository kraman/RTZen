package edu.uci.ece.zen.orb.transport;

import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;

public abstract class Connector{
    static{
        try{
            Connector.connRunnable = (ConnectorRunnable) ImmortalMemory.instance().newInstance( ConnectorRunnable.class );
        }catch( Exception e ){
            e.printStackTrace();
        }
    }

    public Connector(){
    }

    private static ConnectorRunnable connRunnable;
    public synchronized final ScopedMemory connect( String host , short port , edu.uci.ece.zen.orb.ORB orb ){
        System.out.println( Thread.currentThread() + "transport.Connector.connect 1" );
        ScopedMemory transportMem = orb.getScopedRegion();
        System.out.println( Thread.currentThread() + "transport.Connector.connect 2" );
        Connector.connRunnable.init( host , port , this , orb );
        System.out.println( Thread.currentThread() + "transport.Connector.connect 2-1" );

        ExecuteInRunnable eir = ExecuteInRunnable.instance();
        ExecuteInRunnable eir2 = ExecuteInRunnable.instance();

        System.out.println( Thread.currentThread() + "transport.Connector.connect 2-2" );
        eir.init( eir2 , orb.orbImplRegion );
        eir2.init( Connector.connRunnable , transportMem );
        System.out.println( Thread.currentThread() + "transport.Connector.connect 2-3" );
        try{
        System.out.println( Thread.currentThread() + "transport.Connector.connect 2-4" );
            orb.parentMemoryArea.executeInArea( eir );
        System.out.println( Thread.currentThread() + "transport.Connector.connect 2-5" );
        }catch( Exception e ){
            e.printStackTrace();
        }
        System.out.println( Thread.currentThread() + "transport.Connector.connect 2-6" );
        eir.free();
        eir2.free();
        System.out.println( Thread.currentThread() + "transport.Connector.connect 3" );
        return transportMem;
    }

    protected abstract Transport internalConnect( String host , int port , edu.uci.ece.zen.orb.ORB orb );

}
