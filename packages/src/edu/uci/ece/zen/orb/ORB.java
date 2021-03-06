/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import java.util.Properties;

import javax.realtime.ImmortalMemory;
import javax.realtime.LTMemory;
import javax.realtime.MemoryArea;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import org.omg.CORBA.Any;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.Environment;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.PolicyCurrent;
import org.omg.CORBA.PolicyError;
import org.omg.CORBA.PolicyManager;
import org.omg.CORBA.Request;
import org.omg.CORBA.ServiceInformationHolder;
import org.omg.CORBA.UNSUPPORTED_POLICY;
import org.omg.CORBA.WrongTransaction;
import org.omg.Messaging.REBIND_POLICY_TYPE;
import org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE;
import org.omg.RTCORBA.RTORB;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import edu.uci.ece.zen.orb.policies.PolicyManagerImpl;
import edu.uci.ece.zen.orb.policies.RebindPolicyImpl;
import edu.uci.ece.zen.orb.policies.SyncScopePolicyImpl;
import edu.uci.ece.zen.poa.TPRunnable;
import edu.uci.ece.zen.utils.ExecuteInRunnable;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.Hashtable;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.Queue;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.net.*;

//import edu.uci.ece.zen.utils.ThreadLocal;

/**
 * @author Alex Potanin (alex@mcs.vuw.ac.nz)
 * @author Krishna Raman
 */

public class ORB extends org.omg.CORBA_2_3.ORB {
    private static Queue unusedFacades;
    private static Hashtable orbTable;
    private static Queue unusedMemoryAreas;
    private static ImmortalMemory imm;
    private static long orbIdSeq;
    private static long scopeMemorySize;
    private static ORB orbSingleton;
    private static int maxSupportedConnections;
    public static String [] endpoints;
    private static RTCurrentRunnable rtrun;

    static {
        try {
            try {
                InetAddress addrList[] = InetAddress.getAllByName( InetAddress.getLocalHost().getHostName() );
                endpoints = new String[ addrList.length ];
                for( int i=0;i<addrList.length;i++ )
                    endpoints[i] = addrList[i].getHostAddress();

                //endpoints = new String [] {java.net.InetAddress.getLocalHost().getHostAddress(), "127.0.0.1"};
                if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("local address" + endpoints[0]);
                //sockAddr = new
                // java.net.InetSocketAddress(java.net.InetAddress.getLocalHost().getHostAddress(),0);
                //sockAddr = java.net.InetAddress.getLocalHost();
                //java.net.InetAddress [] arr = java.net.InetAddress.getAllByName( sockAddr.getHostName() );
                //for(int i = 0; i<arr.length; ++i)
                //    System.out.println("ADDR: " + arr[i].toString());
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, ORB.class, "static <init>", e);
            }

//            if(edu.uci.ece.zen.utils.ZenProperties.memDbg)
//                perf.cPrint.nativePrinter.print(0,0,0);

            imm = ImmortalMemory.instance();
            //Set up ORB Facades
            int numFacades = Integer.parseInt(ZenProperties.getGlobalProperty(
                    "doc.zen.orb.maxNumOrbs", "1"));
            unusedFacades = (Queue) imm.newInstance(Queue.class);
            for (int i = 0; i < numFacades; i++)
                unusedFacades.enqueue(imm
                        .newInstance(edu.uci.ece.zen.orb.ORB.class));

            //Set up ORB table
            orbTable = (Hashtable) imm.newInstance(Hashtable.class);
            orbTable.init(numFacades);

            //Set up storage for memoryAreas
            unusedMemoryAreas = (Queue) imm.newInstance(Queue.class);
            scopeMemorySize = Integer.parseInt(ZenProperties.getGlobalProperty("doc.zen.orb.scopedMemorySize", "2097951"));

            int numMemAreas = Integer.parseInt(ZenProperties.getGlobalProperty( "memarea.amount" , "40" ));

            for (int i = 0; i < numMemAreas; i++)
                unusedMemoryAreas.enqueue(new LTMemory(100, scopeMemorySize));

            //Set up connection registry
            maxSupportedConnections = Integer.parseInt(ZenProperties
                    .getGlobalProperty("doc.zen.orb.maxConnectionsPerORB",
                            "100"));


        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, ORB.class, "static <init>", e);
            System.exit(-1);
        }
    }

    private static synchronized long nextOrbId() {
        orbIdSeq = (orbIdSeq + 1) % (Long.MAX_VALUE - 1);
        return orbIdSeq;
    }

    public static synchronized org.omg.CORBA.ORB init(String[] args, java.util.Properties props) {
        if (props == null) props = new Properties();

        //Find the ORBId
        ZenProperties.logger.log("======================Locating OBR ID from ZenProperties====================");
        String orbId = ZenProperties.getORBId(args, props);
        if (orbId == null) {
            orbId = "edu.uci.ece.zen.orb.ORB." + nextOrbId();
        }
        props.setProperty("org.omg.CORBA.ORBId", orbId);

        //Return the corresponding ORB
        if (orbId.equals("")) {
            return edu.uci.ece.zen.orb.ORB.orbSingleton;
        } else {
            ZenProperties.logger.log("======================Trying to locate the orb with that orbid==============");

            FString fOrbId = FString.instance();
            fOrbId.append(orbId);

            edu.uci.ece.zen.orb.ORB retVal = (edu.uci.ece.zen.orb.ORB) orbTable
                    .get(fOrbId);
            if (retVal == null) {
                //TODO: According to Alex, the use of scopes here has to be dealt with from Scoped Types perspective
                ZenProperties.logger.log("======================None found...new orb will mbe made====================");
                if (unusedFacades.isEmpty()) { throw new RuntimeException(
                        "ORB number limit reached. Cannot create more ORB's. Please increase the number of ORB's in the zen.properties file."); }
                ScopedMemory mem = getScopedRegion();
                retVal = (edu.uci.ece.zen.orb.ORB) unusedFacades.dequeue();
                ZenProperties.logger.log("======================Calling internal init now=============================");
                retVal.internalInit(mem, orbId, args, props);
            }
            return retVal;
        }
    }

    public ScopedMemory orbImplRegion;

    public MemoryArea parentMemoryArea;

    private ConnectionRegistry connectionRegistry;
    private AcceptorRegistry acceptorRegistry;
    private WaiterRegistry waiterRegistry;
    private Queue executeInRunnableCache;
    public ScopedMemory[] threadpoolList;
    public edu.uci.ece.zen.poa.POA rootPOA;
    public Object orbRunningLock;
    public RTORBImpl rtorb;
    public NamingContextExt cachedNamingReference;
    private TPRunnable tpr;
    private FString orbId;

    public ORB() {
        //orbImplRunnable = new ORBImplRunnable(orbImplRegion);
        connectionRegistry = new ConnectionRegistry();//KLUDGE:ORB.maxSupportedConnections
        // );
        connectionRegistry.init(100);
        acceptorRegistry = new AcceptorRegistry();
        waiterRegistry = new WaiterRegistry();
        waiterRegistry.init(100);
        executeInRunnableCache = new Queue();
        //rtorb = new RTORBImpl(this);
        rtorb = new RTORBImpl();
        rtorb.init(this);
        //policyManager = new PolicyManagerImpl(this);
        threadpoolList = new ScopedMemory[10];//KLUDGE: need to set up property
        // for max TPs
        orbId = FString.instance();
        orbRunningLock = new Integer(0);
    }

    public ExecuteInRunnable getEIR() {
        ExecuteInRunnable ret = (ExecuteInRunnable) executeInRunnableCache.dequeue();
        if (ret == null) {
            try {
                ret = (ExecuteInRunnable) ImmortalMemory.instance()
                        .newInstance(ExecuteInRunnable.class);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, getClass(), "getEIR", e);
            }
        }
        return ret;
    }

    public void freeEIR(ExecuteInRunnable r) {
        if (ZenBuildProperties.dbgORB) ZenProperties.logger.log("FreeEIR(): EIR from  " + MemoryArea.getMemoryArea(r));
        if(MemoryArea.getMemoryArea(r) != ImmortalMemory.instance()){
            if (ZenBuildProperties.dbgORB) ZenProperties.logger.log("FreeEIR(): EIR not from immortal, but " + MemoryArea.getMemoryArea(r));
            if (ZenBuildProperties.dbgThreadStack) Thread.dumpStack();
        }
        executeInRunnableCache.enqueue(r);
    }

    public TPRunnable getTPR() {
        try {
            if (tpr == null) tpr = (TPRunnable) ImmortalMemory.instance()
                    .newInstance(TPRunnable.class);
            return tpr;
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "getTPR", e);
        }
        return null;
    }

    private void internalInit(ScopedMemory mem, String orbId, String[] args,
            Properties props) {
        this.orbId.reset();
        this.orbId.append(orbId);
        ZenProperties.logger.log("======================Assigning the parent memory area======================");
        this.parentMemoryArea = RealtimeThread.getCurrentMemoryArea();
        ZenProperties.logger.log("======================Filing ORBInitRunnable with values====================");
        ORBInitRunnable orbInitRunnable = new ORBInitRunnable();
        orbInitRunnable.init(args, props, this);
        orbImplRegion = mem;

        executeInORBRegion(orbInitRunnable);

        /*
         * ExecuteInRunnable r = new ExecuteInRunnable(); r.init(
         * orbInitRunnable , mem); try{ System.out.println(
         * "======================Calling
         * ExecuteInRunnable=============================" );
         * parentMemoryArea.executeInArea( r ); }catch( Exception e ){
         * ZenProperties.logger.log( Logger.FATAL, "edu.uci.ece.zen.orb.ORB",
         * "internalInit", "Could not initialize ORB due to exception: " +
         * e.toString() ); System.exit(-1); }
         */
    }

    private void isNotDestroyed() {
        if (orbImplRegion == null) { throw new org.omg.CORBA.BAD_INV_ORDER(
                "ORB has been destroyed", 4, CompletionStatus.COMPLETED_NO); }
    }

    private void isActive() {
        isNotDestroyed();
    }

    public void set_parameters(String args[], java.util.Properties props) {
        ORBInitRunnable orbInitRunnable = new ORBInitRunnable();
        orbInitRunnable.init(args, props, this);

        executeInORBRegion(orbInitRunnable);
    }

    //For Multithreaded ORB's
    public void run() {
        isActive();
        synchronized (orbRunningLock) {
            try {
                orbRunningLock.wait();
            } catch (InterruptedException ie) {
                ZenProperties.logger.log(Logger.WARN, getClass(), "run", ie);
            }
        }
        //System.out.println( "ORB.run() has exited" );
    }

    public void shutdown(boolean wait_for_completion) {
        isActive();
        
        //System.out.println( "Shutting down TP's" );
        ExecuteInRunnable eir = getEIR();
        for( int i=0;i<threadpoolList.length;i++ ){
            if( threadpoolList[i] != null ){
                eir.init( ShutdownRunnable.instance() , threadpoolList[i] );
                executeInORBRegion( eir );
            }
        }
        freeEIR( eir );

        //System.out.println( "Shutting down ORBImpl" );
        ShutdownRunnable osr = ShutdownRunnable.instance();
        executeInORBRegion( osr );
        
        /*Print all active threads* /
        ThreadGroup system = null;
        ThreadGroup tg = Thread.currentThread ().getThreadGroup ();

        while (tg != null)
        {
            system = tg;
            tg = tg.getParent ();
        }

        // Display a list of all system and application threads, and their
        // daemon status

        if (system != null)
        {
            Thread [] thds = new Thread [system.activeCount ()];
            int nthds = system.enumerate (thds);
            for (int i = 0; i < nthds; i++){
                System.out.println (thds [i] + " " + thds [i].isDaemon ());
                thds[i].dumpStack();
            }
        }*/

        //System.out.println( "Shutdown complete" );
        synchronized(orbRunningLock){
            orbRunningLock.notify();
        }
    

        
        
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

    public boolean get_service_information(short service_type,
            ServiceInformationHolder service_info) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Policy create_policy(int type, org.omg.CORBA.Any val)
            throws org.omg.CORBA.PolicyError {

        switch (type) {
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
        return new String(orbId.getTrimData());
    }

    /**
     * // * / protected void set_parameters(java.applet.Applet app,
     * java.util.Properties props) { throw new org.omg.CORBA.NO_IMPLEMENT(); }
     */

    public String[] list_initial_services() {
        //return Resolver.getResolverStrings();
        return null;
    }

    public org.omg.CORBA.Object resolve_initial_references(String object_name)
            throws org.omg.CORBA.ORBPackage.InvalidName {
        if (ZenBuildProperties.dbgORB) ZenProperties.logger.log("======================Getting " + object_name + "=============================");
        if (object_name.equals("RTORB")) {
            return getRTORB();
        } else if (object_name.equals("ORBPolicyManager")) {
            return getPolicyManager();
        } else if (object_name.equals("PolicyCurrent")) {
            PolicyCurrentRunnable prun = new PolicyCurrentRunnable( orbImplRegion);
            executeInORBRegion(prun);
            return prun.val;
        } else if (object_name.equals("RTCurrent")) {
            return getRTCurrent();
        } else if (object_name.equals("RootPOA")) {
            //throw new org.omg.CORBA.NO_IMPLEMENT();

            if (rootPOA == null) {
                rootPOA = edu.uci.ece.zen.poa.POA.instance();
                rootPOA.initAsRootPOA(this);
            }
            return rootPOA;

        }else if(object_name.equals("NameService")){
            return getNaming();
        }

        throw new org.omg.CORBA.ORBPackage.InvalidName(object_name
                + " resolver not implemented");
    }

    /**
     * logic to retrieve policy manger from client memory
     */
    class PolicyManagerRunnable implements Runnable {
        public PolicyManagerImpl val;

        private ScopedMemory sm;

        public PolicyManagerRunnable(ScopedMemory sm) {
            this.sm = sm;
        }

        public void run() {
            val = ((ORBImpl) (sm.getPortal())).policyManager;
        }
    }

    public PolicyManager getPolicyManager() {
        PolicyManagerRunnable runnable = new PolicyManagerRunnable(orbImplRegion);
        executeInORBRegion(runnable);
        return runnable.val;
    }

    public RTORB getRTORB() {
        return rtorb;
    }

    public NamingContextExt getNaming(){
        if (cachedNamingReference == null) {
            if (ZenBuildProperties.dbgORB){
                System.out.println("The location for reading naming service is "+ZenProperties.getGlobalProperty("naming.ior_file.for_reading",""));
            }
            String namingIOR = "";
            try{
                URL namingURL = new URL(ZenProperties.getGlobalProperty("naming.ior_file.for_reading",""));
                File namingIORFile = new File(namingURL.getFile());
                BufferedReader br = new BufferedReader( new FileReader(namingIORFile) );
                namingIOR = br.readLine();
                br.close();
            }
            catch(Exception ex){
                ex.printStackTrace();
                System.exit(-1);
            }

            org.omg.CORBA.Object namingObject = string_to_object(namingIOR);

            cachedNamingReference = NamingContextExtHelper.narrow(namingObject); //Current the ObjRefDelegate._is_a() hasn't been implemented, so it use unchecked_narrow, not narrow
        }

        return cachedNamingReference;  // must return same refrence for each call

    }

    public RTCurrent getRTCurrent() {
        ZenProperties.logger.log("Getting RTCurrent.....");
        /*
        RTCurrentRunnable rtrun = RTCurrentRunnable.instance(parentMemoryArea);
        rtrun.init(orbImplRegion);
        executeInORBRegion(rtrun);
        if (!rtrun.init) {
            rtrun.init = true;
            //since ThreadLocal lazily creates the internal hash map every time
            //get() is called, we have to call it here from client scope and
            // not
            //let it be created lazily from ORB scope
            rtrun.tl.get();
            executeInORBRegion(rtrun);
        }
        return rtrun.val;
        */
        return RTCurrent.instance();
    }

    class PolicyCurrentRunnable implements Runnable {
        public PolicyCurrent val;

        private ScopedMemory sm;

        public PolicyCurrentRunnable(ScopedMemory sm) {
            this.sm = sm;
        }

        public void run() {
            val = ((ORBImpl) (sm.getPortal())).getPolicyCurrent();
        }
    }

    public String object_to_string(org.omg.CORBA.Object obj) {
        ObjectImpl objectImpl = (ObjectImpl) obj;
        ObjRefDelegate delegate = (ObjRefDelegate) objectImpl._get_delegate();
        return delegate.toString();
    }

    public synchronized org.omg.CORBA.Object string_to_object(String str) {
        if (ZenBuildProperties.dbgIOR){
            ZenProperties.logger.log("The String IOR being resolved to an Object is...");
            if(str.equals("")){
                ZenProperties.logger.log("Empty String IOR");
            }else
                ZenProperties.logger.log(str);
        }

        org.omg.IOP.IOR ior = IOR.parseString(this, str);
        edu.uci.ece.zen.orb.ObjectImpl objImpl = new ObjectImpl();
        objImpl.init(ior);

        ORBStrToObjRunnable strToObjRunnable = new ORBStrToObjRunnable();
        strToObjRunnable.init(ior, objImpl);

        executeInORBRegion(strToObjRunnable);
        /*
         * ExecuteInRunnable r = new ExecuteInRunnable(); r.init(
         * strToObjRunnable , this.orbImplRegion ); try{
         * parentMemoryArea.executeInArea( r ); }catch( Exception e ){
         * ZenProperties.logger.log( Logger.SEVERE, "edu.uci.ece.zen.orb.ORB",
         * "string_to_object", "Could not get object due to exception: " +
         * e.toString() ); e.printStackTrace(); }
         */

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

    public ScopedMemory getWaiterRegion(int key) {
        if(ZenBuildProperties.dbgInvocations) {
            ZenProperties.logger.log(getClass()+ " getWaiterRegion() with key: " + key);
        }
        return waiterRegistry.getWaiter(key);
    }

    public void registerWaiter(int key) {
        if(ZenBuildProperties.dbgInvocations) {
            ZenProperties.logger.log(getClass()+ " registerWaiter() with key: " + key);
        }
        try {
            waiterRegistry.registerWaiter(key, (ScopedMemory) RealtimeThread
                    .getCurrentMemoryArea());
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "registerWaiter", e);
        }
    }

    public void releaseWaiter(int key) {
        if(ZenBuildProperties.dbgInvocations) {
            ZenProperties.logger.log(getClass()+ " releaseWaiter() with key: " + key);
        }
        try {
            waiterRegistry.remove(key);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "releaseWaiter", e);
        }
    }

    public static ScopedMemory getScopedRegion() {
        ScopedMemory mem = (ScopedMemory) unusedMemoryAreas.dequeue();
        if ( mem == null) {
            ZenProperties.logger.log(Logger.SEVERE, ORB.class, "getScopedRegion()", "Out of memory areas");
            return null;
        }
        return mem;
    }

    public static void freeScopedRegion(ScopedMemory sm) {
        unusedMemoryAreas.enqueue(sm);
   }

    public ConnectionRegistry getConnectionRegistry() {
        return connectionRegistry;
    }

    public AcceptorRegistry getAcceptorRegistry() {
        return acceptorRegistry;
    }

    public void executeInORBRegion(Runnable runnable) {
        ExecuteInRunnable r = new ExecuteInRunnable();

        r.init(runnable, orbImplRegion);
        try {

            parentMemoryArea.executeInArea(r);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, getClass(), "executeInOBRRegion", e);
            System.exit(-1);
        }
    }

    /**
     * This is used to set up a child region of the ORB region
     */
    public void setUpORBChildRegion(Runnable runnable) {

        ExecuteInRunnable r1 = new ExecuteInRunnable();
        ExecuteInRunnable r2 = new ExecuteInRunnable();
        ScopedMemory sm = getScopedRegion();

        r1.init(r2, orbImplRegion);
        r2.init(runnable, sm);
        try {
            parentMemoryArea.executeInArea(r1);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL,
                    getClass(), "create_threadpool",
                    "Could not create threadpool", e);
            System.err.println("Caught Exception");
            e.printStackTrace();
            System.exit(-1);
        }
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
        //return
        // edu.uci.ece.zen.orb.TypeCode.lookupPrimitiveTc(tcKind.value());
        // Discarded old implementation:
        //return new edu.uci.ece.zen.orb.TypeCode(tcKind.value());
        return null;
    }

    public org.omg.CORBA.TypeCode create_struct_tc(String id, String name,
            org.omg.CORBA.StructMember[] members) {
        //return new
        // edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_struct,
        //                id, name, members);
        return null;
    }

    public org.omg.CORBA.TypeCode create_recursive_struct_tc(String id,
            String name, org.omg.CORBA.StructMember[] members) {
        //return new
        // edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_struct,
        //                id, name, members);
        return null;
    }

    public org.omg.CORBA.TypeCode create_union_tc(String id, String name,
            org.omg.CORBA.TypeCode discriminator_type,
            org.omg.CORBA.UnionMember[] members) {
        //return new edu.uci.ece.zen.orb.TypeCode(id, name, discriminator_type,
        //                members);
        return null;
    }

    public org.omg.CORBA.TypeCode create_enum_tc(String id, String name,
            String[] members) {
        //return new edu.uci.ece.zen.orb.TypeCode(id, name, members);
        return null;
    }

    public org.omg.CORBA.TypeCode create_alias_tc(String id, String name,
            org.omg.CORBA.TypeCode original_type) {
        //return new edu.uci.ece.zen.orb.TypeCode(id, name, original_type);
        return null;
    }

    public org.omg.CORBA.TypeCode create_exception_tc(String id, String name,
            org.omg.CORBA.StructMember[] members) {
        //return new
        // edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_except, id,
        // name, members);
        return null;
    }

    public org.omg.CORBA.TypeCode create_interface_tc(String id, String name) {
        //return new edu.uci.ece.zen.orb.TypeCode(id, name);
        return null;
    }

    public org.omg.CORBA.TypeCode create_string_tc(int bound) {
        //return new edu.uci.ece.zen.orb.TypeCode(bound);
        //return new
        // edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_string);
        return null;
    }

    public org.omg.CORBA.TypeCode create_wstring_tc(int bound) {
        //return new edu.uci.ece.zen.orb.TypeCode(bound);
        //return new
        // edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_wstring);
        return null;
    }

    public org.omg.CORBA.TypeCode create_sequence_tc(int bound,
            org.omg.CORBA.TypeCode element_type) {
        //return new edu.uci.ece.zen.orb.TypeCode(bound, element_type);
        return null;
    }

    public org.omg.CORBA.TypeCode create_recursive_sequence_tc(int bound, int e) {
        //return new edu.uci.ece.zen.orb.TypeCode(bound, element_type);
        return null;
    }

    public org.omg.CORBA.TypeCode create_array_tc(int length,
            org.omg.CORBA.TypeCode element_type) {
        //edu.uci.ece.zen.orb.TypeCode arrayTypecode = new
        // edu.uci.ece.zen.orb.TypeCode(length, element_type);
        //arrayTypecode.kind = TCKind._tk_array;

        //return arrayTypecode;//new edu.uci.ece.zen.orb.TypeCode(length,
        // element_type);
        return null;
    }

    public org.omg.CORBA.TypeCode create_native_tc(String id, String name) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.TypeCode create_abstract_interface_tc(String id,
            String name) {
        //return new edu.uci.ece.zen.orb.TypeCode(id, name);
        return null;
    }

    public org.omg.CORBA.TypeCode create_fixed_tc(short digits, short scale) {
        //return new edu.uci.ece.zen.orb.TypeCode(digits, scale);
        return null;
    }

    public org.omg.CORBA.TypeCode create_value_tc(String id, String name,
            short type_modifier, org.omg.CORBA.TypeCode concrete_base,
            org.omg.CORBA.ValueMember[] members) {
        //return new edu.uci.ece.zen.orb.TypeCode (id, name, type_modifier,
        // concrete_base, members);
        return null;
    }

    public org.omg.CORBA.TypeCode create_recursive_tc(String id) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.TypeCode create_value_box_tc(String id, String name,
            org.omg.CORBA.TypeCode boxed_type) {
        // Uses the constructor for alias TypeCode, then changes the kind.
        //edu.uci.ece.zen.orb.TypeCode newTC = new
        // edu.uci.ece.zen.orb.TypeCode(id, name, boxed_type);
        //newTC.kind = org.omg.CORBA.TCKind._tk_value_box;
        //return newTC;
        return null;
    }

    /**
     * Creata a new Any that has its orb reference populated by this orb.
     *
     * @return edu.uci.ece.zen.orb.any.Any using default strategy for
     *         implementing Anys, with its orb reference populated by this orb.
     */
    public org.omg.CORBA.Any create_any() {
        return null;//new edu.uci.ece.zen.orb.any.Any(this);
    }

    /**
     * Called in GIOP 1.0 and 1.1 to cancel a request.
     *
     * @param request_id
     *            numerical identifier of Request or LocateRequest to cancel
     */
    // LEFT FOR KRISHNA TO IMPLEMENT
    public void cancelRequest(int request_id) {
        throw new org.omg.CORBA.NO_IMPLEMENT(
                "ORB.cancelRequest(int) not implemented");
    }

    /**
     * Given a thread pool ID, this method returns the ScopedMemory region
     * associated with that thread pool.
     */
    public ScopedMemory getThreadPoolRegion(int tpId) {
        return threadpoolList[tpId];
    }

}

class ORBInitRunnable implements Runnable {
    String[] args;

    Properties props;

    edu.uci.ece.zen.orb.ORB orbFacade;

    public void init(String[] args, Properties props,
            edu.uci.ece.zen.orb.ORB orbFacade) {
        this.args = args;
        this.props = props;
        this.orbFacade = orbFacade;
    }

    public void run() {
        ScopedMemory curMem = ((ScopedMemory) RealtimeThread
                .getCurrentMemoryArea());
        if (curMem.getPortal() == null) {
            ORBImpl orbImpl = new ORBImpl(args, props, orbFacade);
            curMem.setPortal(orbImpl);
        } else {
            ((ORBImpl) curMem.getPortal()).setProperties(args, props);
        }
    }
}

class ORBStrToObjRunnable implements Runnable {
    org.omg.IOP.IOR ior;

    org.omg.CORBA.portable.ObjectImpl objImpl;

    public ORBStrToObjRunnable() {
    }

    public void init(org.omg.IOP.IOR ior,
            org.omg.CORBA.portable.ObjectImpl objImpl) {
        this.ior = ior;
        this.objImpl = objImpl;
    }

    public void run() {
        //System.out.println("ORBStrToObjRunnable1");
        ORBImpl orbImpl = ((ORBImpl) ((ScopedMemory) RealtimeThread
                .getCurrentMemoryArea()).getPortal());
        orbImpl.string_to_object(ior, objImpl);
    }
}

class RTCurrentRunnable implements Runnable {
    public RTCurrent val;
    public boolean init;
    public ThreadLocal tl;
    private ScopedMemory sm;
    private static RTCurrentRunnable instance;

    public static RTCurrentRunnable instance(MemoryArea parentMemoryArea){
        if(instance == null){
            try{
                instance = (RTCurrentRunnable) (parentMemoryArea.newInstance(RTCurrentRunnable.class));
            }catch(Exception e){
                e.printStackTrace();//TODO better error handling
            }
        }

        return instance;
    }

    private RTCurrentRunnable(){
    }

    public void init(ScopedMemory sm) {
        this.sm = sm;
    }

    public void run() {
        ZenProperties.logger.log("======================RTCurrentRunnable1");
        Logger.printThreadStack();
        if (!init) {
            ZenProperties.logger.log("======================RTCurrentRunnable2");
            Object o = sm.getPortal();
            ZenProperties.logger.log("======================RTCurrentRunnable2.5");
            tl = ((ORBImpl) o).rtCurrent;

            //tl = ((ORBImpl) (sm.getPortal())).rtCurrent;
        } else {
            ZenProperties.logger.log("======================RTCurrentRunnable3");
            val = ((ORBImpl) (sm.getPortal())).getRTCurrent();
        }
    }
}
