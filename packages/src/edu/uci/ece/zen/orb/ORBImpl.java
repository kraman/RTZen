package edu.uci.ece.zen.orb;

import java.util.Properties;
import edu.uci.ece.zen.utils.*;
import javax.realtime.*;

public class ORBImpl{
    ZenProperties properties;
    edu.uci.ece.zen.orb.ORB orbFacade;
    ORBImplRunnable orbImplRunnable;
    public Hashtable cachedObjects;
    public ServerRequestHandler serverRequestHandler;
    
    public ORBImpl( String args[] , Properties props, edu.uci.ece.zen.orb.ORB orbFacade ){
        properties = new ZenProperties();
        properties.addPropertiesFromArgs( args );
        properties.addProperties( props );
        this.orbFacade = orbFacade;
        ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).setPortal( this );
        orbImplRunnable = new ORBImplRunnable();
        NoHeapRealtimeThread nhrt = new NoHeapRealtimeThread( orbImplRunnable );
        nhrt.start();

        cachedObjects = new Hashtable();
        cachedObjects.init(5);
        try{
            cachedObjects.put( "ExecuteInRunnable" , new Queue() );
            cachedObjects.put( "ConnectorRunnable" , new Queue() );
        }catch( Exception e ){
            e.printStackTrace();
        }
    }

    protected void handleInvocation(){
    }

    protected void setProperties( String[] args , Properties props ){
    }

    public void registerTransport( ScopedMemory mem ){
    }

    public void string_to_object( org.omg.IOP.IOR ior , org.omg.CORBA.portable.ObjectImpl objImpl ){
        ObjRefDelegate delegate = ObjRefDelegate.instance();
        delegate.init( ior , (edu.uci.ece.zen.orb.ObjectImpl) objImpl , orbFacade , this );
        objImpl._set_delegate( delegate );
    }

    public void setServerRequestHandler( ServerRequestHandler handler ){
        serverRequestHandler = handler;
    }

    public ServerRequestHandler getServerRequestHandler(){
        return serverRequestHandler;
    }
}

class ORBImplRunnable implements Runnable{
    private boolean active;

    public ORBImplRunnable(){
        active = true;
    }

    public boolean isActive(){
        return this.active;
    }

    public void setActive( boolean val ){
        this.active = val;
    }

    public void run(){
        ORBImpl orbImpl = (ORBImpl) ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).getPortal();
        synchronized( orbImpl ){
            try{
                while( active ){
                    orbImpl.wait();
                }
            }catch( InterruptedException ie ){
                ZenProperties.logger.log(
                        Logger.INFO ,
                        "edu.uci.ece.zen.orb.ORBImplRunnable" ,
                        "run()" ,
                        "ORB is shutting down.");
            }
            active=false;
        }
    }

}
