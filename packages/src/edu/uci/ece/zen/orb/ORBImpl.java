package edu.uci.ece.zen.orb;

import java.util.Properties;
import edu.uci.ece.zen.utils.*;
import javax.realtime.*;

public class ORBImpl{
    ZenProperties properties;
    edu.uci.ece.zen.orb.ORB orbFacade;
    ORBImplRunnable orbImplRunnable;
    public Hashtable cachedObjects;
    
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
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  "OrbImpl.string_to_object 1" );
        ObjRefDelegate delegate = ObjRefDelegate.instance();
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  "OrbImpl.string_to_object 2" );
        delegate.init( ior , (edu.uci.ece.zen.orb.ObjectImpl) objImpl , orbFacade , this );
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  "OrbImpl.string_to_object 3" );
        objImpl._set_delegate( delegate );
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  "OrbImpl.string_to_object 4" );
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
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  "OrbImplRunnable.run 1" );
        ORBImpl orbImpl = (ORBImpl) ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).getPortal();
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  "OrbImplRunnable.run 2 "+orbImpl );
        synchronized( orbImpl ){
            try{
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  "OrbImplRunnable.run 3" );
                while( active ){
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  "OrbImplRunnable.run 3-1" );
                    orbImpl.wait();
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  "OrbImplRunnable.run 3-2" );
                }
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  "OrbImplRunnable.run 4" );
            }catch( InterruptedException ie ){
                ZenProperties.logger.log(
                        Logger.INFO ,
                        "edu.uci.ece.zen.orb.ORBImplRunnable" ,
                        "run()" ,
                        "ORB is shutting down.");
            }
            active=false;
        }
        System.out.println( Thread.currentThread() + " " + RealtimeThread.getCurrentMemoryArea() + " " +  "OrbImplRunnable.run 5" );
    }

}
