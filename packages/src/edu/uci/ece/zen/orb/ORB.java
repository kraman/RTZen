package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.utils.*;
import org.omg.CORBA.portable.*;
import java.io.*;
import javax.realtime.*;
import org.omg.RTCORBA.*;
import org.omg.Messaging.*;
import java.util.Properties;
import edu.uci.ece.zen.orb.policies.*;
import org.omg.CORBA.ServiceInformationHolder;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.Context;
import org.omg.CORBA.Environment;
import org.omg.CORBA.Request;
import org.omg.CORBA.WrongTransaction;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.UNSUPPORTED_POLICY;
import org.omg.CORBA.PolicyError;
import org.omg.CORBA.Any;
import org.omg.CORBA.PolicyManager;
import org.omg.CORBA.PolicyCurrent;

/**
 * @author Krishna Raman
 */

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
            for( int i=0;i<20;i++ )
                unusedMemoryAreas.enqueue( new LTMemory( 100 , scopeMemorySize ) );

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
        System.out.println( "======================Locating OBR ID from ZenProperties====================" );
        String orbId = ZenProperties.getORBId( args , props );
        if( orbId == null ){
            orbId = "edu.uci.ece.zen.orb.ORB." + nextOrbId();
        }
        props.setProperty( "org.omg.CORBA.ORBId" , orbId );

        //Return the currusponding ORB
        if( orbId.equals( "" ) ){
            return edu.uci.ece.zen.orb.ORB.orbSingleton;
        }else{
            System.out.println( "======================Trying to locate the orb with that orbid==============" );

            FString fOrbId = new FString( orbId.length() );
            fOrbId.append( orbId );

            edu.uci.ece.zen.orb.ORB retVal = (edu.uci.ece.zen.orb.ORB) orbTable.get( fOrbId );
            if( retVal == null ){
                System.out.println( "======================None found...new orb will mbe made====================" );
                if( unusedFacades.isEmpty() ){
                    throw new RuntimeException( "ORB number limit reached. Cannot create more ORB's. Please increase the number of ORB's in the zen.properties file." );
                }
                ScopedMemory mem = getScopedRegion();
                retVal = (edu.uci.ece.zen.orb.ORB) unusedFacades.dequeue();
                System.out.println( "======================Calling internal init now=============================" );
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
    private ORBImplRunnable orbImplRunnable;
    private ConnectionRegistry connectionRegistry;
    private AcceptorRegistry acceptorRegistry;
    private WaiterRegistry waiterRegistry;
    private Queue executeInRunnableCache;
    //public RTORB rtorb;
    //public PolicyManager policyManager;
    public ScopedMemory[] threadpoolList;
    public edu.uci.ece.zen.poa.POA rootPOA;
    public Object orbRunningLock;

    private FString orbId;

    public ORB(){
        orbImplRunnable = new ORBImplRunnable(orbImplRegion);
        connectionRegistry = new ConnectionRegistry();//KLUDGE:ORB.maxSupportedConnections );
        connectionRegistry.init( 100 );
        acceptorRegistry = new AcceptorRegistry();
        waiterRegistry = new WaiterRegistry();
        waiterRegistry.init( 100 );
        executeInRunnableCache = new Queue();
        //rtorb = new RTORBImpl(this);
        //policyManager = new PolicyManagerImpl(this);
        threadpoolList = new ScopedMemory[10];//KLUDGE: need to set up property for max TPs
        orbId = new FString();
        orbRunningLock = new Integer(0);
        try{
            orbId.init(25);
        }catch( InstantiationException e1 ){
             ZenProperties.logger.log(
                Logger.FATAL,
                "edu.uci.ece.zen.orb.ORB",
                "<init>",
                "Could not initialize ORB facade due to exception: " + e1.toString()
                );
             System.exit(-1);
        }catch( IllegalAccessException e2 ){
             ZenProperties.logger.log(
                Logger.FATAL,
                "edu.uci.ece.zen.orb.ORB",
                "<init>",
                "Could not initialize ORB facade due to exception: " + e2.toString()
                );
             System.exit(-1);
        }
    }

    public ExecuteInRunnable getEIR(){
        ExecuteInRunnable ret = (ExecuteInRunnable) executeInRunnableCache.dequeue();
        if( ret == null ){
            try{
                ret = (ExecuteInRunnable) ImmortalMemory.instance().newInstance( ExecuteInRunnable.class );
            }catch( Exception e ){
                e.printStackTrace();
            }
        }
        return ret;
    }

    public void freeEIR( ExecuteInRunnable r ){
        executeInRunnableCache.enqueue( r );
    }

    private void internalInit( ScopedMemory mem , String orbId , String[] args , Properties props ){
        this.orbId.reset();
        this.orbId.append( orbId );
        System.out.println( "======================Assigning the parent memory area======================" );
        this.parentMemoryArea = RealtimeThread.getCurrentMemoryArea();
        System.out.println( "======================Filing ORBInitRunnable with values====================" );
        ORBInitRunnable orbInitRunnable = new ORBInitRunnable();
        orbInitRunnable.init( args , props , this );
        orbImplRegion = mem;

        ExecuteInRunnable r = new ExecuteInRunnable();

        r.init( orbInitRunnable , mem);
        try{
            System.out.println( "======================Calling ExecuteInRunnable=============================" );
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
    }

    private void isNotDestroyed(){
        if( orbImplRegion == null ){
            throw new org.omg.CORBA.BAD_INV_ORDER("ORB has been destroyed", 4, CompletionStatus.COMPLETED_NO);
        }
    }

    private void isActive(){
        isNotDestroyed();
        //if( !orbImplRunnable.isActive() ){
        //    throw new org.omg.CORBA.BAD_INV_ORDER("ORB has been shutdown", 4, CompletionStatus.COMPLETED_NO);
        //}
    }

    public void set_parameters(String args[], java.util.Properties props) {
        ORBInitRunnable orbInitRunnable = new ORBInitRunnable();
        orbInitRunnable.init( args , props , this );

        ExecuteInRunnable r = new ExecuteInRunnable();
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
    }

    //For Multithreaded ORB's
    public void run(){
        isActive();
        synchronized( orbRunningLock ){
            try{
                orbRunningLock.wait();
            }catch( InterruptedException ie ){
                ie.printStackTrace();
            }
        }
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

        switch(type){
            case SYNC_SCOPE_POLICY_TYPE.value:
                return new SyncScopePolicyImpl(val.extract_short());

            case REBIND_POLICY_TYPE.value:
                return new RebindPolicyImpl(val.extract_short());

            default:
                throw new PolicyError(UNSUPPORTED_POLICY.value);

        }

        //throw new PolicyError(UNSUPPORTED_POLICY.value);
        //throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public String id() {
        return new String( orbId.getTrimData() );
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
        System.out.println( "======================Getting " + object_name + "=============================" );
        if(object_name.equals("RTORB")){
            return getRTORB();
        }else if(object_name.equals("ORBPolicyManager")){
            return getPolicyManager();
        }else if(object_name.equals("PolicyCurrent")){
            PolicyCurrentRunnable prun = new PolicyCurrentRunnable(orbImplRegion);
            internalResolve(prun);
            return prun.val;
        }else if(object_name.equals("RTCurrent")){
            return getRTCurrent();
        }else if(object_name.equals("RootPOA")){
            if( rootPOA == null ){
                rootPOA = edu.uci.ece.zen.poa.POA.instance();
                rootPOA.initAsRootPOA( this );
            }
            return rootPOA;
        }
        //org.omg.CORBA.Object ret = Resolver.resolve( this , object_name );
        //if( ret == null )
            throw new org.omg.CORBA.ORBPackage.InvalidName(object_name + " resolver not implemented");
        //return ret;
        //else if(object_name.equals("RTCurrent"))
            //return policyManager;
    }

    private void internalResolve(Runnable runnable){
        ExecuteInRunnable r = new ExecuteInRunnable();

        r.init(runnable, orbImplRegion);
        try{

            parentMemoryArea.executeInArea( r );
        }catch( Exception e ){
            ZenProperties.logger.log(
                Logger.FATAL,
                "edu.uci.ece.zen.orb.ORB",
                "",
                "" + e.toString()
                );
            System.exit(-1);
        }
    }

    /**
        logic to retrieve policy manger from client memory
    */
    class PolicyManagerRunnable implements Runnable{
        public PolicyManagerImpl val;
        private ScopedMemory sm;
        public PolicyManagerRunnable(ScopedMemory sm){
            this.sm = sm;
        }
        public void run(){
            val = ((ORBImpl)(sm.getPortal())).policyManager;
        }
    }

    public PolicyManager getPolicyManager(){
        PolicyManagerRunnable runnable = new PolicyManagerRunnable(orbImplRegion);
        internalResolve(runnable);
        return runnable.val;
    }

    class RTORBRunnable implements Runnable{
        public RTORBImpl val;
        private ScopedMemory sm;
        public RTORBRunnable(ScopedMemory sm){
            this.sm = sm;
        }
        public void run(){
            val = ((ORBImpl)(sm.getPortal())).rtorb;
        }
    }

    public RTORB getRTORB(){
        RTORBRunnable runnable = new RTORBRunnable(orbImplRegion);
        internalResolve(runnable);
        return runnable.val;
    }

    class RTCurrentRunnable implements Runnable{
        public RTCurrent val;
        public boolean init;
        public ThreadLocal tl;
        private ScopedMemory sm;
        public RTCurrentRunnable(ScopedMemory sm){
            this.sm = sm;
        }
        public void run(){
            if(!init)
                tl = ((ORBImpl)(sm.getPortal())).rtCurrent;
            else
                val = ((ORBImpl)(sm.getPortal())).getRTCurrent();
        }
    }

    public RTCurrent getRTCurrent(){
        RTCurrentRunnable rtrun = new RTCurrentRunnable(orbImplRegion);
        internalResolve(rtrun);
        if(!rtrun.init){
            rtrun.init = true;
            //since ThreadLocal lazily creates the internal hash map every time 
            //get() is called, we have to call it here from client scope and not
            //let it be created lazily from ORB scope
            rtrun.tl.get();
            internalResolve(rtrun);
        }
        return rtrun.val;
    }

    class PolicyCurrentRunnable implements Runnable{
        public PolicyCurrent val;
        private ScopedMemory sm;
        public PolicyCurrentRunnable(ScopedMemory sm){
            this.sm = sm;
        }
        public void run(){
            val = ((ORBImpl)(sm.getPortal())).getPolicyCurrent();
        }
    }


    public String object_to_string(org.omg.CORBA.Object obj) {
        ObjectImpl objectImpl = (ObjectImpl) obj;
        ObjRefDelegate delegate = (ObjRefDelegate) objectImpl._get_delegate();
        return delegate.toString();
    }

    public synchronized org.omg.CORBA.Object string_to_object(String str) {
        org.omg.IOP.IOR ior = IOR.parseString( this , str );
        edu.uci.ece.zen.orb.ObjectImpl objImpl = new ObjectImpl();
        objImpl.init( ior );

        ORBStrToObjRunnable strToObjRunnable = new ORBStrToObjRunnable();
        strToObjRunnable.init( ior , objImpl );
        System.out.println("yue1");
        ExecuteInRunnable r = new ExecuteInRunnable();
        System.out.println("yue2");
        r.init( strToObjRunnable , this.orbImplRegion );
        System.out.println("yue3");
        try{
            parentMemoryArea.executeInArea( r );
        }catch( Exception e ){
            ZenProperties.logger.log(
                Logger.SEVERE,
                "edu.uci.ece.zen.orb.ORB",
                "string_to_object",
                "Could not get object due to exception: " + e.toString()
                );
            e.printStackTrace();
        }
        System.out.println("yue4");
        System.out.println("yue5");
        return objImpl;
    }

    public org.omg.CORBA.portable.OutputStream create_output_stream() {
        CDROutputStream out = CDROutputStream.instance();
        out.init(this);
        return out;
    }

    ///////////////////////////////////////////////////////////////////////////
    ////////////////// Internal helper methods ////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

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
            ZenProperties.logger.log(
                Logger.SEVERE,
                "edu.uci.ece.zen.orb.ORB",
                "getScopedRegion()",
                "Out of memory areas" );
            return null;
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



    public org.omg.CORBA.TypeCode get_primitive_tc(org.omg.CORBA.TCKind tcKind) {
        return edu.uci.ece.zen.orb.TypeCode.lookupPrimitiveTc(tcKind.value());
        // Discarded old implementation:
        //return new edu.uci.ece.zen.orb.TypeCode(tcKind.value());
    }

    public org.omg.CORBA.TypeCode create_struct_tc(String id, String name,
                        org.omg.CORBA.StructMember[] members) {
        return new edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_struct,
                        id, name, members);
    }

    public org.omg.CORBA.TypeCode create_union_tc(String id, String name,
                        org.omg.CORBA.TypeCode discriminator_type,
                        org.omg.CORBA.UnionMember[] members) {
        return new edu.uci.ece.zen.orb.TypeCode(id, name, discriminator_type,
                        members);
    }

    public org.omg.CORBA.TypeCode create_enum_tc(String id, String name,
                        String[] members) {
        return new edu.uci.ece.zen.orb.TypeCode(id, name, members);
    }

    public org.omg.CORBA.TypeCode create_alias_tc(String id, String name,
                        org.omg.CORBA.TypeCode original_type) {
        return new edu.uci.ece.zen.orb.TypeCode(id, name, original_type);
    }

    public org.omg.CORBA.TypeCode create_exception_tc(String id, String name,
                            org.omg.CORBA.StructMember[] members) {
        return new edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_except, id, name, members);
    }

    public org.omg.CORBA.TypeCode create_interface_tc(String id, String name) {
        return new edu.uci.ece.zen.orb.TypeCode(id, name);
    }

    public org.omg.CORBA.TypeCode create_string_tc(int bound) {
        //return new edu.uci.ece.zen.orb.TypeCode(bound);
        return new edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_string);
    }

    public org.omg.CORBA.TypeCode create_wstring_tc(int bound) {
        //return new edu.uci.ece.zen.orb.TypeCode(bound);
        return new edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_wstring);
    }

    public org.omg.CORBA.TypeCode create_sequence_tc(int bound,
                            TypeCode element_type) {
        return new edu.uci.ece.zen.orb.TypeCode(bound, element_type);
    }

    public org.omg.CORBA.TypeCode create_sequence_tc(int bound,
                            org.omg.CORBA.TypeCode element_type) {
        return new edu.uci.ece.zen.orb.TypeCode(bound, element_type);
    }

    public org.omg.CORBA.TypeCode create_recursive_sequence_tc(int bound, int offset) {
        return new edu.uci.ece.zen.orb.TypeCode(bound, offset);
    }

    public org.omg.CORBA.TypeCode create_array_tc(int length, org.omg.CORBA.TypeCode element_type) {
        edu.uci.ece.zen.orb.TypeCode arrayTypecode = new edu.uci.ece.zen.orb.TypeCode(length, element_type);
        arrayTypecode.kind = TCKind._tk_array;

        return arrayTypecode;//new edu.uci.ece.zen.orb.TypeCode(length, element_type);
    }

    public org.omg.CORBA.TypeCode create_native_tc(String id,
                        String name) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.TypeCode create_abstract_interface_tc(String id, String name) {
        return new edu.uci.ece.zen.orb.TypeCode(id, name);
    }

    public org.omg.CORBA.TypeCode create_fixed_tc(short digits, short scale) {
        return new edu.uci.ece.zen.orb.TypeCode(digits, scale);
    }

    public org.omg.CORBA.TypeCode create_value_tc(String id,
                                                String name,
                                                short type_modifier,
                                                org.omg.CORBA.TypeCode concrete_base,
                                                org.omg.CORBA.ValueMember[] members) {
        return new edu.uci.ece.zen.orb.TypeCode (id, name, type_modifier, concrete_base, members);
    }

    public org.omg.CORBA.TypeCode create_recursive_tc(String id) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.TypeCode create_value_box_tc(String id,
                            String name,
                            org.omg.CORBA.TypeCode boxed_type) {
        // Uses the constructor for alias TypeCode, then changes the kind.
        edu.uci.ece.zen.orb.TypeCode newTC = new edu.uci.ece.zen.orb.TypeCode(id, name, boxed_type);
        newTC.kind = org.omg.CORBA.TCKind._tk_value_box;
        return newTC;
    }


    /**
     * Creata a new Any that has its orb reference populated by this
     * orb.
     *
     * @return edu.uci.ece.zen.orb.any.Any using default strategy for
     * implementing Anys, with its orb reference populated by this
     * orb.
    */
    public org.omg.CORBA.Any create_any() {
        return new edu.uci.ece.zen.orb.any.Any(this);
    }


    /** Called in GIOP 1.0 and 1.1 to cancel a request.
     * @param request_id numerical identifier of Request or LocateRequest to cancel
     */
    // LEFT FOR KRISHNA TO IMPLEMENT
    public void cancelRequest(int request_id) {
        throw new org.omg.CORBA.NO_IMPLEMENT("ORB.cancelRequest(int) not implemented");
    }

    /**
     * Given a thread pool ID, this method returns the ScopedMemory region
     * associated with that thread pool.
     */
    public ScopedMemory getThreadPoolRegion( int tpId ){
        return threadpoolList[tpId];
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
            ORBImpl orbImpl = new ORBImpl( args , props , orbFacade );
            curMem.setPortal( orbImpl );
        }else{
            ((ORBImpl)curMem.getPortal()).setProperties( args , props );
        }
    }
}

class ORBStrToObjRunnable implements Runnable{
    org.omg.IOP.IOR ior;

    org.omg.CORBA.portable.ObjectImpl objImpl;

    public ORBStrToObjRunnable(){
    }

    public void init( org.omg.IOP.IOR ior , org.omg.CORBA.portable.ObjectImpl objImpl ){
        this.ior = ior;
        this.objImpl = objImpl;
    }

    public void run(){
        //System.out.println("ORBStrToObjRunnable1");
        ORBImpl orbImpl = ((ORBImpl) ((ScopedMemory)RealtimeThread.getCurrentMemoryArea()).getPortal()  );
        orbImpl.string_to_object( ior , objImpl );
    }
}
