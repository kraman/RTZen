package edu.uci.ece.zen.orb;

import java.util.Properties;
import edu.uci.ece.zen.utils.*;
import javax.realtime.*;
import org.omg.CORBA.PolicyCurrent;
import edu.uci.ece.zen.orb.policies.*;

public class ORBImpl{
    ZenProperties properties;
    edu.uci.ece.zen.orb.ORB orbFacade;
    ORBImplRunnable orbImplRunnable;
    public Hashtable cachedObjects;
    public ServerRequestHandler serverRequestHandler;
    public ThreadLocal rtCurrent;
    public ThreadLocal policyCurrent;
    public PolicyManagerImpl policyManager;
    public RTORBImpl rtorb;


    public ORBImpl( String args[] , Properties props, edu.uci.ece.zen.orb.ORB orbFacade ){
        System.out.println( "======================In orb impl region===================================" );
        properties = new ZenProperties();
        properties.addPropertiesFromArgs( args );
        properties.addProperties( props );
        this.orbFacade = orbFacade;
        System.out.println( "======================Setting portal variable==============================" );
        orbFacade.orbImplRegion.setPortal( this );
        orbImplRunnable = new ORBImplRunnable(orbFacade.orbImplRegion);
        System.out.println( "======================Creating nhrt sleeper thread=========================" );
        System.out.println( orbFacade.orbImplRegion );
        System.out.println( MemoryArea.getMemoryArea( orbImplRunnable ) );
        System.out.println( NoHeapRealtimeThread.getCurrentMemoryArea() );
        //NoHeapRealtimeThread nhrt = new NoHeapRealtimeThread( null, null,null,(ScopedMemory)orbFacade.orbImplRegion,null,(java.lang.Runnable )orbImplRunnable );
        System.out.println( MemoryArea.getMemoryArea( new Integer(42)) );
        NoHeapRealtimeThread nhrt = new NoHeapRealtimeThread( null,null,null,orbFacade.orbImplRegion,null,orbImplRunnable );
        System.out.println( "======================starting nhrt in orb impl region=====================" );

        nhrt.start();
        cachedObjects = new Hashtable();
        cachedObjects.init(5);

        try{
            rtCurrent = (ThreadLocal)(orbFacade.parentMemoryArea.newInstance( ThreadLocal.class ));
            policyCurrent = (ThreadLocal)(orbFacade.parentMemoryArea.newInstance( ThreadLocal.class ));
            policyManager = (PolicyManagerImpl)(orbFacade.parentMemoryArea.newInstance( PolicyManagerImpl.class ));
            policyManager.init(orbFacade);
            //rtorb = (RTORBImpl)(orbFacade.parentMemoryArea.newInstance( RTORBImpl.class ));
            //rtorb.init(orbFacade);

            cachedObjects.put( "ExecuteInRunnable" , new Queue() );
            cachedObjects.put( "ConnectorRunnable" , new Queue() );
        }catch( Exception e ){
            e.printStackTrace();
        }
    }

    public PolicyCurrent getPolicyCurrent(){
        PolicyCurrentImpl ret = (PolicyCurrentImpl)(policyCurrent.get());

        if(ret == null){
            try{
                ret = (PolicyCurrentImpl)(orbFacade.parentMemoryArea.newInstance( PolicyCurrentImpl.class ));//new RTCurrentImpl(this);
                ret.init(orbFacade);
                policyCurrent.set(ret);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return ret;
    }

    public RTCurrent getRTCurrent(){
        RTCurrent ret = (RTCurrent)(rtCurrent.get());

        if(ret == null){
            try{
                ret = (RTCurrent)(orbFacade.parentMemoryArea.newInstance( RTCurrent.class ));//new RTCurrentImpl(this);
                ret.init(orbFacade);
                rtCurrent.set(ret);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return ret;
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
    private ScopedMemory sm;

    public ORBImplRunnable(ScopedMemory sm){
        active = true;
        this.sm = sm;
    }

    public boolean isActive(){
        return this.active;
    }

    public void setActive( boolean val ){
        this.active = val;
    }

    public void run(){
        //System.out.println("getting portal for: " + NoHeapRealtimeThread.getCurrentMemoryArea().getClass().getName());
        //System.out.println("inner thread: " + Thread.currentThread().toString());
        //ORBImpl orbImpl = (ORBImpl) ((ScopedMemory)NoHeapRealtimeThread.getCurrentMemoryArea()).getPortal();
        ORBImpl orbImpl = (ORBImpl) sm.getPortal();
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
