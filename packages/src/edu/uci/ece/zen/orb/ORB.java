package edu.uci.ece.zen.orb;

import org.omg.CORBA.*;
import org.omg.CORBA.portable.*;
import edu.uci.ece.zen.utils.*;
import java.io.*;
import javax.realtime.*;
import org.omg.RTCORBA.*;
import java.util.Properties;
import edu.uci.ece.zen.orb.policies.*;

public class ORB extends org.omg.CORBA_2_3.ORB{
    private static Queue unusedFacades;
    private static Hashtable orbTable;
    private static Queue unusedMemoryAreas;
    private static ImmortalMemory imm;
    private static long orbIdSeq;
    private static long scopeMemorySize;
    private static ORB orbSingleton;
    private static int maxSupportedConnections;

    static{
        try{
            imm = ImmortalMemory.instance();
            //Set up ORB Facades
            int numFacades = Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.orb.maxNumOrbs" , "1" ) );
            unusedFacades = (Queue) imm.newInstance( Queue.class );
            for( int i=0;i<numFacades;i++ )
                unusedFacades.enqueue( imm.newInstance( edu.uci.ece.zen.orb.ORB.class ) );

            //Set up ORB table
            orbTable = (Hashtable) imm.newInstance( Hashtable.class );
            orbTable.init( numFacades );

            //Set up storage for memoryAreas
            unusedMemoryAreas = (Queue) imm.newInstance( Queue.class );
            scopeMemorySize = Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.orb.scopedMemorySize" , "1048576" ));

            //Set up connection registry
            maxSupportedConnections = Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.orb.maxConnectionsPerORB" , "100" ));
        }catch( Exception e ){
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    private static synchronized long nextOrbId(){
        orbIdSeq = (orbIdSeq + 1) % (Long.MAX_VALUE-1);
        return orbIdSeq;
    }

    public static synchronized org.omg.CORBA.ORB init(String[] args, java.util.Properties props) {
        if( props == null )
            props = new Properties();

        //Find the ORBId
        String orbId = ZenProperties.getORBId( args , props );
        if( orbId == null ){
                orbId = "edu.uci.ece.zen.orb.ORB." + nextOrbId();
        }
        props.setProperty( "org.omg.CORBA.ORBId" , orbId );

        //Return the currusponding ORB
        if( orbId.equals( "" ) ){
            return edu.uci.ece.zen.orb.ORB.orbSingleton;
        }else{
            edu.uci.ece.zen.orb.ORB retVal = (edu.uci.ece.zen.orb.ORB) orbTable.get( orbId );
            if( retVal == null ){
                if( unusedFacades.isEmpty() ){
                    throw new RuntimeException( "ORB number limit reached. Cannot create more ORB's. Please increase the number of ORB's in the zen.properties file." );
                }
                ScopedMemory mem = getScopedRegion();
                retVal = (edu.uci.ece.zen.orb.ORB) unusedFacades.dequeue();
                retVal.internalInit( mem , orbId , args , props );
            }
            return retVal;
        }
    }

    /*
    NOTE: Unsupported in RTZen because jRate does not support Applets

    /**
     * Initalizes the ORB and returns a reference to the orb. This method can
     * be called multiple times and must return the same orb reference. This is
     * a special method added to support Java Applets.
     *
     * @param app The applet object to load arguments from.
     * @param props The properties to be used during ORB initialization.
    //* /
    public static org.omg.CORBA.ORB init(java.applet.Applet app,
            java.util.Properties props) {
        ZenProperties.loadProperties(props);
        return ORB.init((String [])null,props);
    }
    */

    public ScopedMemory orbImplRegion;
    public MemoryArea parentMemoryArea;
    private ORBInitRunnable orbInitRunnable;
    private ORBImplRunnable orbImplRunnable;
    private ORBStrToObjRunnable strToObjRunnable;
    private ConnectionRegistry connectionRegistry;
    private AcceptorRegistry acceptorRegistry;
    private WaiterRegistry waiterRegistry;
    private String orbId;

    public ORB(){
        orbInitRunnable = new ORBInitRunnable();
        orbImplRunnable = new ORBImplRunnable();
        strToObjRunnable = new ORBStrToObjRunnable();
        connectionRegistry = new ConnectionRegistry();//KLUDGE:ORB.maxSupportedConnections );
        connectionRegistry.init( 100 );
        acceptorRegistry = new AcceptorRegistry();
        waiterRegistry = new WaiterRegistry();
        waiterRegistry.init( 100 );
    }

    private void internalInit( ScopedMemory mem , String orbId , String[] args , Properties props ){
        this.orbId = orbId;
        this.parentMemoryArea = RealtimeThread.getCurrentMemoryArea();
        orbInitRunnable.init( args , props , this );
        orbImplRegion = mem;

        ExecuteInRunnable r = ExecuteInRunnable.instance();
        r.init( orbInitRunnable , mem);
        try{
            parentMemoryArea.executeInArea( r );
        }catch( Exception e ){
            ZenProperties.logger.log(
                Logger.FATAL,
                "edu.uci.ece.zen.orb.ORB",
                "internalInit",
                "Could not initialize ORB due to exception: " + e.toString()
                );
            System.exit(-1);
        }
        r.free();
    }

    private void isNotDestroyed(){
        if( orbImplRegion == null ){
            throw new org.omg.CORBA.BAD_INV_ORDER("ORB has been destroyed", 4, CompletionStatus.COMPLETED_NO);
        }
    }

    private void isActive(){
        isNotDestroyed();
        if( !orbImplRunnable.isActive() ){
            throw new org.omg.CORBA.BAD_INV_ORDER("ORB has been shutdown", 4, CompletionStatus.COMPLETED_NO);
        }
    }

    public void set_parameters(String args[], java.util.Properties props) {
        orbInitRunnable.init( args , props , this );

        ExecuteInRunnable r = ExecuteInRunnable.instance();
        r.init( orbInitRunnable , this.orbImplRegion );
        try{
            parentMemoryArea.executeInArea( r );
        }catch( Exception e ){
            ZenProperties.logger.log(
                Logger.FATAL,
                "edu.uci.ece.zen.orb.ORB",
                "set_parameters",
                "Could not initialize ORB due to exception: " + e.toString()
                );
            System.exit(-1);
        }
        r.free();
    }

    //For Multithreaded ORB's
    public void run(){
        isActive();

        ExecuteInRunnable r = ExecuteInRunnable.instance();
        r.init( orbImplRunnable , this.orbImplRegion );
        try{
            parentMemoryArea.executeInArea( r );
        }catch( Exception e ){
            ZenProperties.logger.log(
                Logger.FATAL,
                "edu.uci.ece.zen.orb.ORB",
                "set_parameters",
                "Could not run in ORB due to exception: " + e.toString()
                );
            System.exit(-1);
        }
        r.free();
    }

    public void shutdown(boolean wait_for_completion) {
        isActive();
    }

    public boolean work_pending() {
        isActive();
        return false;
    }

    public void perform_work() {
        isActive();
        //do nothing
    }

    public void destroy() {
        isNotDestroyed();
    }

    public boolean get_service_information(short service_type, ServiceInformationHolder service_info) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Policy create_policy(int type, org.omg.CORBA.Any val)
        throws org.omg.CORBA.PolicyError {
/*
        switch(type){
            case PRIORITY_MODEL_POLICY_TYPE.value:
                return new PriorityModelPolicyImpl();
            //break;

            case THREADPOOL_POLICY_TYPE.value:
            //break;

            case SERVER_PROTOCOL_POLICY_TYPE.value:
            //break;

            case CLIENT_PROTOCOL_POLICY_TYPE.value:
            //break;

            case PRIORITY_BANDED_CONNECTION_POLICY_TYPE.value:
            //break;

            default:
                throw new PolicyError(UNSUPPORTED_POLICY.value);


        }
*/
        throw new PolicyError(UNSUPPORTED_POLICY.value);
        //throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public String id() {
        return orbId;
    }

    /**
     *
    // * /
    protected void set_parameters(java.applet.Applet app,
            java.util.Properties props) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }*/

    public String[] list_initial_services() {
        //return Resolver.getResolverStrings();
        return null;
    }

    public org.omg.CORBA.Object resolve_initial_references(String object_name) throws org.omg.CORBA.ORBPackage.InvalidName {
        //return Resolver.resolve( object_name );
        return null;
    }

    public String object_to_string(org.omg.CORBA.Object obj) {
        ObjectImpl objectImpl = (ObjectImpl) obj;
        ObjRefDelegate delegate = (ObjRefDelegate) objectImpl._get_delegate();
        return delegate.toString();
    }

    public synchronized org.omg.CORBA.Object string_to_object(String str) {
        org.omg.IOP.IOR ior = IOR.parseString( this , str );
        org.omg.CORBA.portable.ObjectImpl objImpl = new ObjectImpl( ior );
        strToObjRunnable.init( ior , orbImplRegion , objImpl );

        ExecuteInRunnable r = ExecuteInRunnable.instance();
        r.init( strToObjRunnable , this.orbImplRegion );
        try{
            parentMemoryArea.executeInArea( r );
        }catch( Exception e ){
            ZenProperties.logger.log(
                Logger.SEVERE,
                "edu.uci.ece.zen.orb.ORB",
                "string_to_object",
                "Could not get object due to exception: " + e.toString()
                );
        }
        r.free();

        return strToObjRunnable.getRetVal();
    }

    public org.omg.CORBA.portable.OutputStream create_output_stream() {
        CDROutputStream out = CDROutputStream.instance();
        out.init(this);
        return out;
    }

    ///////////////////////////////////////////////////////////////////////////
    ////////////////// Internal helper methods ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public ScopedMemory getTPHandler( int tpId ){
        //TODO: return thread pool memory region here
        return null;
    }

    public ScopedMemory getWaiterRegion( int key ){
        return waiterRegistry.getWaiter( key );
    }

    public void registerWaiter( int key ){
        try{
            waiterRegistry.registerWaiter( key , (ScopedMemory) RealtimeThread.getCurrentMemoryArea() );
        }catch( Exception e ){
            e.printStackTrace();
        }
    }

    public void releaseWaiter( int key ){
        try{
            waiterRegistry.remove( key );
        }catch( Exception e ){
            e.printStackTrace();
        }
    }

    public static ScopedMemory getScopedRegion(){ 
        ScopedMemory mem = null;
        if( unusedMemoryAreas.isEmpty() ){
            mem = new javax.realtime.LTMemory( scopeMemorySize , scopeMemorySize );
        }else{
            mem = (ScopedMemory) unusedMemoryAreas.dequeue();
        }
        return mem; 
    }

    public static void freeScopedRegion( ScopedMemory sm ){ 
        unusedMemoryAreas.enqueue( sm );
    };

    public ConnectionRegistry getConnectionRegistry(){
        return connectionRegistry;
    }

    public org.omg.CORBA.Object resolveRootPOA(){
        return null;
    }

    public AcceptorRegistry getAcceptorRegistry(){
        return acceptorRegistry;
    }

    ///////////////////////////////////////////////////////////////////////////
    ////////////////// DON'T CARE ABOUT THESE /////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public NVList create_list(int count) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public NVList create_operation_list(org.omg.CORBA.Object oper) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public NamedValue create_named_value(String s, Any any, int flags) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public ExceptionList create_exception_list() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public ContextList create_context_list() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public Context get_default_context() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public Environment create_environment() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void send_multiple_requests_oneway(Request[] req) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void send_multiple_requests_deferred(Request[] req) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public boolean poll_next_response() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public Request get_next_response() throws WrongTransaction {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Current get_current() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}

class ORBInitRunnable implements Runnable{
    String[] args;
    Properties props;
    edu.uci.ece.zen.orb.ORB orbFacade;
    public void init( String[] args , Properties props , edu.uci.ece.zen.orb.ORB orbFacade ){
        this.args = args;
        this.props = props;
        this.orbFacade = orbFacade;
    }

    public void run(){
        ScopedMemory curMem = ((ScopedMemory) RealtimeThread.getCurrentMemoryArea());
        if( curMem.getPortal() == null ){
            curMem.setPortal( new ORBImpl( args , props , orbFacade ) );
        }else{
            ((ORBImpl)curMem.getPortal()).setProperties( args , props );
        }
    }
}

class ORBStrToObjRunnable implements Runnable{
    org.omg.IOP.IOR ior;
    org.omg.CORBA.portable.ObjectImpl objImpl;
    ScopedMemory orbImplRegion;

    org.omg.CORBA.Object retval;

    public ORBStrToObjRunnable(){
    }

    public void init( org.omg.IOP.IOR ior , ScopedMemory orbImplRegion , org.omg.CORBA.portable.ObjectImpl objImpl ){
        this.ior = ior;
        this.orbImplRegion = orbImplRegion;
        this.objImpl = objImpl;
        this.retval = null;
    }

    public org.omg.CORBA.Object getRetVal(){ return retval; }

    public void run(){
        retval = ((ORBImpl) orbImplRegion.getPortal()  ).string_to_object( ior , objImpl );
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

    public void run(){
        ORBImpl orbImpl = (ORBImpl) ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).getPortal();
        synchronized( orbImpl ){
            try{
                orbImpl.wait();
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
