package edu.uci.ece.zen.orb.transport;

import edu.uci.ece.zen.utils.ActiveDemuxTable;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.orb.ORBImpl;
import edu.uci.ece.zen.orb.ObjectImpl;
import edu.uci.ece.zen.utils.FString;
import org.omg.IOP.TaggedProfile;
import edu.uci.ece.zen.orb.ObjRefDelegate;
import javax.realtime.*;

public abstract class TransportFactory{
    private static ActiveDemuxTable factoryList;

    public static void registerTransport( int tag , Object factory , ScopedMemory orbMemory ){
        try{
            if( factoryList == null ){
                factoryList = (ActiveDemuxTable) ImmortalMemory.instance().newInstance( ActiveDemuxTable.class );
                factoryList.init( 5 );
            }
            factoryList.bind( tag , factory );
        }catch( Exception e ){
            ZenProperties.logger.log( Logger.FATAL , "FATAL: Could not register any transports for ORB." );
        }
    }

    public static TransportFactory getTransport( int tag ){
        return (TransportFactory) factoryList.mapEntry( factoryList.find( tag ) );
    }

    public ORBImpl orbImpl;

    public void init( ORBImpl orbImpl ){
        this.orbImpl = orbImpl;
        internalInit();
    }
    public abstract void internalInit();

    public void createAcceptor( int tag , FString args ){
        getTransport(tag).createAcceptorImpl( args );
    }
    public abstract void createAcceptorImpl( FString args );

    public static void connect( int tag , TaggedProfile profile , ObjectImpl obj , ObjRefDelegate objRefDelegate , boolean isCollocated ){
        getTransport(tag).connectImpl( profile , obj , objRefDelegate , isCollocated );
    }
    public abstract void connectImpl( TaggedProfile profile , ObjectImpl obj , ObjRefDelegate objRefDelegate , boolean isCollocated );
}
