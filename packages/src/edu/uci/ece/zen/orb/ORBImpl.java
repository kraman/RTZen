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
    public ServerRequestHandler serverRequestHandler;
    public ThreadLocal rtCurrent;
    public ThreadLocal policyCurrent;
    public PolicyManagerImpl policyManager;
    public RTORBImpl rtorb;

    public Queue eirCache;
    public Queue crCache;   //ConnectorRunnable cache

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
        try{
            rtCurrent = (ThreadLocal)(orbFacade.parentMemoryArea.newInstance( ThreadLocal.class ));
            //rtCurrent = new ThreadLocal();
            policyCurrent = (ThreadLocal)(orbFacade.parentMemoryArea.newInstance( ThreadLocal.class ));
            policyManager = (PolicyManagerImpl)(orbFacade.parentMemoryArea.newInstance( PolicyManagerImpl.class ));
            policyManager.init(orbFacade);
            rtorb = (RTORBImpl)(orbFacade.parentMemoryArea.newInstance( RTORBImpl.class ));
            rtorb.init(orbFacade);

            rtorb.create_threadpool (0,//stacksize,
                                   10,//static_threads,
                                   0,//dynamic_threads,
                                   (short)RealtimeThread.NORM_PRIORITY,//default_thread_priority,
                                   false,//allow_request_buffering,
                                   0,//max_buffered_requests,
                                   0//max_request_buffer_size
                                   );
        }catch( Exception e ){
            e.printStackTrace();
        }

        eirCache = new Queue();
        crCache = new Queue();
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
        //System.out.println("ORBImpl string_to_object 1");
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
        System.out.println("getting portal for: " + sm );
        System.out.println("inner thread: " + Thread.currentThread().toString());

        ORBImpl orbImpl = (ORBImpl) sm.getPortal();
        System.out.println( "orb impl is " + orbImpl );
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
        synchronized( orbImpl.orbFacade.orbRunningLock ){
            orbImpl.orbFacade.orbRunningLock.notifyAll();
        }
    }
}

class AcceptorCreatorRunnable implements Runnable{
    ORB orb;
    ORBImpl orbImpl;
    short acceptorPriority;

    ScopedMemory retVal;

    public AcceptorCreatorRunnable( ORB orb , ORBImpl orbImpl ){
        this.orb = orb;
        this.orbImpl = orbImpl;
    }

    public void init( short acceptorPriority ){
        this.acceptorPriority = acceptorPriority;
    }

    public void run(){

    }
}
