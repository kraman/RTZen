/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.poa;

import javax.realtime.ImmortalMemory;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.AdapterActivator;
import org.omg.PortableServer.RequestProcessingPolicy;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;
import org.omg.PortableServer.ThreadPolicy;
import org.omg.PortableServer.ThreadPolicyValue;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.AdapterNonExistent;
import org.omg.PortableServer.POAPackage.NoServant;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongAdapter;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.ServerRequestHandler;
import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.utils.ExecuteInRunnable;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.Hashtable;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.Queue;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;
import edu.uci.ece.zen.orb.CDROutputStream;

/**
 * See section 11.3.9 in the CORBA Sepecification.
 *
 * @author juancol, hojjat
 */
public class POA extends org.omg.CORBA.LocalObject implements org.omg.RTPortableServer.POA//org.omg.PortableServer.POA
{
    private static Queue unusedFacades;

    private static Queue unusedPOARunnables;

    private static ImmortalMemory imm;

    private ORB orb;

    private POA parent;

    public ScopedMemory poaMemoryArea;

    private org.omg.PortableServer.POAManager poaManager;

    private ServerRequestHandler serverRequestHandler;

    private Hashtable theChildren;

    protected SynchronizedInt numberOfCurrentRequests;

    public int poaState;

    protected int poaDemuxIndex;

    protected int poaDemuxCount;

    public int processingState = POA.ACTIVE;

    private AdapterActivator adapterActivator;

    protected FString poaName;

    protected FString poaPath;

    /* Mutexes POA and varable specific to the create and destroy ops */
    private Object createDestroyPOAMutex;

    private boolean disableCreatePOA = false;

    private boolean etherealize;

    /* Constants for the POA Class */
    public static final String rootPoaString = "RootPOA";

    public static final int CREATING = 0;

    public static final int CREATION_COMPLETE = 1;

    public static final int DESTRUCTION_IN_PROGRESS = 3;

    public static final int DESTRUCTION_APPARANT = 4;

    public static final int DESTRUCTION_COMPLETE = 5;

    /* Request Processing States */
    public static final int ACTIVE = 6;

    public static final int DISCARDING = 7;

    public static final int INACTIVE = 8;

    static
    {
        try
        {
            imm = ImmortalMemory.instance();
            //Set up POA Facades
            int numFacades = Integer.parseInt(ZenProperties.getGlobalProperty(
                    "doc.zen.poa.maxNumPOAs", "5")); //TODO put in zen.propeties file.
            unusedFacades = (Queue) imm.newInstance(Queue.class);
            for (int i = 0; i < numFacades; i++)
                unusedFacades.enqueue(imm.newInstance(edu.uci.ece.zen.poa.POA.class));
        }
        catch (Exception e)
        {
            ZenProperties.logger.log(Logger.FATAL, POA.class, "static <init>", e);
            System.exit(-1);
        }
    }


    public static edu.uci.ece.zen.poa.POA instance() {
        edu.uci.ece.zen.poa.POA retVal;
        retVal = (edu.uci.ece.zen.poa.POA) unusedFacades.dequeue();
        return retVal;
    }

    private static void release(edu.uci.ece.zen.poa.POA poa) {
        unusedFacades.enqueue(poa);
    }

    public void free() {
        POA.release(this);
    }

    public void initAsRootPOA(final edu.uci.ece.zen.orb.ORB orb) {

        this.init(orb, rootPoaString, null, null, null);
    }

    /**
     * Constructor
     */
    public POA() {
        theChildren = new Hashtable();
        theChildren.init(Integer.parseInt(ZenProperties.getGlobalProperty("doc.zen.poa.maxNumPOAs", "5")));
        numberOfCurrentRequests = new SynchronizedInt();
        createDestroyPOAMutex = new Integer(0);
        poaName = FString.instance();
        poaPath = FString.instance();
    }

    /**
     * Initialization
     *
     * @param orb
     * @param poaName
     * @param policies
     * @param _parent
     * @param manager
     */
    public synchronized void init(final edu.uci.ece.zen.orb.ORB orb, String poaName,
            org.omg.CORBA.Policy[] policies,
            org.omg.PortableServer.POA _parent,
            org.omg.PortableServer.POAManager manager) {

        //TODO improve synchronization

        POA parent = (POA) _parent;
        poaState = POA.CREATING;
        this.orb = orb;
        this.poaMemoryArea = ORB.getScopedRegion();

        this.poaName.reset();
        this.poaName.append(poaName);
        if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("---------------------POAI init:0 ");
        this.poaPath.reset();
        if (parent == null) {
            this.poaPath.append('/');
        } else {
            this.poaPath.append(parent.poaPath.getData(), 0, parent.poaPath.length());
        }
        this.poaPath.append(this.poaName.getData(), 0, this.poaName.length());
        this.poaPath.append('/');
        if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("---------------------POAI init1 ");
        if (manager == null) manager = POAManager.instance();
        this.poaManager = manager;
        ((POAManager) poaManager).register((org.omg.PortableServer.POA) this);

        theChildren.clear();
        numberOfCurrentRequests.reset();
        if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("---------------------POAI init:2 ");
        POARunnable r = new POARunnable(POARunnable.INIT);
        r.addParam(orb);
        r.addParam(this);
        r.addParam(policies);
        r.addParam(parent);
        r.addParam(manager);
        ExecuteInRunnable eir1 = new ExecuteInRunnable();//orb.getEIR();
        eir1.init(r, poaMemoryArea);
        ExecuteInRunnable eir2 = new ExecuteInRunnable();//orb.getEIR();
        eir2.init(eir1, orb.orbImplRegion);
        try {
            orb.parentMemoryArea.executeInArea(eir2);
        } catch (Exception e2) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "init", e2);
        }
        finally {
            //orb.freeEIR(eir1);
            //orb.freeEIR(eir2);
        }
        if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("---------------------POAI init:3 ");
        poaState = POA.CREATION_COMPLETE;
    }


    /**
     * Creates a new POA as a child of the target POA.
     *
     * @throws a new NullPointerException if the adapter_name is null. It pollutes the current
     *             scope.
     */
    public org.omg.PortableServer.POA create_POA(String adapter_name,
                                                 org.omg.PortableServer.POAManager a_POAManager,
                                                 org.omg.CORBA.Policy[] policies)
                                                 throws org.omg.PortableServer.POAPackage.AdapterAlreadyExists,
                                                 org.omg.PortableServer.POAPackage.InvalidPolicy
   {
        // TODO think about allocating an instance of this exception in imm memory;
        if (this.poaState != POA.CREATION_COMPLETE || this.disableCreatePOA)
        {
            throw new BAD_INV_ORDER(17, CompletionStatus.COMPLETED_NO);
        }

        if (adapter_name == null)
        {
            throw new NullPointerException();
        }

        if (adapter_name.equals(rootPoaString))
        {
            throw new IllegalArgumentException("Adapter name cannot be " + rootPoaString);
        }

        if (theChildren.get(adapter_name) != null)
        {
            throw new AdapterAlreadyExists();
        }

        //TODO Policiy validation

        POA childPOA = POA.instance();
        childPOA.init(this.orb, adapter_name, policies, this, a_POAManager);

        theChildren.put(adapter_name, childPOA);

        return childPOA;
    }

    /**
     *
     */
    public org.omg.PortableServer.POA find_POA(final String adapter_name, final boolean activate_it)
                                               throws AdapterNonExistent
    {
        // TODO Support AdapterActivator.

        if (adapter_name == null)
        {   //TODO think about allocating an instance of this exception in imm memory;
            throw new NullPointerException("Adapter name is null.");
        }

        POA poa = (POA) theChildren.get(adapter_name);

        if (poa == null)
        {
            //TODO think about allocating an instance of this exception in imm memory;
            throw new AdapterNonExistent();
        }

        return poa;

//        if( poa != null ) return
//         * (POA) poa; if (activate_it) { boolean temp = false; try{ temp =
//         * the_activator().unknown_adapter(this, adapter_name); } catch (
//         * Exception ex ){ throw new org.omg.CORBA.OBJ_ADAPTER("AdapterActivator
//         * failed to activate POA",1,CompletionStatus.COMPLETED_NO); } if (temp)
//         * return (POA) theChildren.get(adapter_name); } throw new
//         * org.omg.PortableServer.POAPackage.AdapterNonExistent()
//         */
//        throw new org.omg.CORBA.NO_IMPLEMENT()
    }


    /**
     * Call scoped region graph:
     * <p>
     * Transport thread: <br/>
     * <p>
     * Transport scope --ex in--&gt; ORBImpl scope --&gt; <b>Message </b> --ex in--&gt; ORBImpl
     * scope --&gt; POAImpl region --ex in--&gt; ORBImpl scope --&gt; TP Region
     * </p>
     * TP Thread: <br/>
     * <p>
     * <b>TP Region </b> --ex in--&gt; ORBImpl scope --&gt; Message Region --ex in--&gt; ORBImpl
     * region --&gt; Transport Scope
     * </p>
     * </p>
     */
    public void handleRequest(final RequestMessage sreq) {

        edu.uci.ece.zen.orb.transport.Transport transport =
                (edu.uci.ece.zen.orb.transport.Transport) sreq.getTransport().getPortal();
        if (transport.objectTable[0] == null) {
            transport.objectTable[0] = new POARunnable(POARunnable.HANDLE_REQUEST);
            ZenProperties.logger.log("new poa runnable");
        }

        POARunnable r = (POARunnable) transport.objectTable[0];
        r.addParam(sreq);
        r.addParam(RealtimeThread.getCurrentMemoryArea());
        //System.out.println("Inside POAServerRequestHandler.handleRequest()
        // and memoryArea: " + RealtimeThread.getCurrentMemoryArea());

        //edu.uci.ece.zen.utils.Logger.printThreadStack();

        if (transport.objectTable[1] == null) {
            transport.objectTable[1] = new ExecuteInRunnable();
            ZenProperties.logger.log("new EI  runnable");
        }

        ExecuteInRunnable eir1 = (ExecuteInRunnable) transport.objectTable[1];
        eir1.init(r, poaMemoryArea);
        try {
            orb.orbImplRegion.executeInArea(eir1);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "handleRequest", e);
        }
        //TODO: Cant throw exceptions here....marchall the exception into a
        // reply message and send back
        //Look at ResponseHandler
        switch (r.exception) {
            case POARunnable.NoException:
                break;
            case POARunnable.TransientException:
                throw new org.omg.CORBA.TRANSIENT(1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            case POARunnable.ObjAdapterException:
                throw new org.omg.CORBA.OBJ_ADAPTER(1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            case POARunnable.ObjNotExistException:
                throw new org.omg.CORBA.OBJECT_NOT_EXIST(2, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }
    }

    public org.omg.CORBA.Object servant_to_reference(final org.omg.PortableServer.Servant p_servant)
            throws ServantNotActive, WrongPolicy {

        POARunnable r = new POARunnable(POARunnable.SERVANT_TO_REFERENCE); //XXX This is a memory
                                                                           // leak;
        r.addParam(p_servant);
        r.addParam(RealtimeThread.getCurrentMemoryArea());

        if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("POA.servant_to_reference cur mem area: " + RealtimeThread.getCurrentMemoryArea());
        ExecuteInRunnable eir1 = new ExecuteInRunnable();//orb.getEIR();
        eir1.init(r, this.poaMemoryArea);
        ExecuteInRunnable eir2 = new ExecuteInRunnable();//orb.getEIR();
        eir2.init(eir1, orb.orbImplRegion);
        try {
            orb.parentMemoryArea.executeInArea(eir2);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "servant_to_reference", e);
        }
        finally{
            //orb.freeEIR(eir1);
            //orb.freeEIR(eir2);
        }
        switch (r.exception) {
            case POARunnable.NoException:
                break;
            case POARunnable.ServantNotActiveException:
                throw new ServantNotActive();
            case POARunnable.WrongPolicyException:
                throw new WrongPolicy();
        }
        if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("POA.servant_to_reference " + r.retVal);
        return (org.omg.CORBA.Object) r.retVal;
    }


    public byte[] servant_to_id(final Servant p_servant) throws ServantNotActive, WrongPolicy {
        /*
         * POARunnable r = new POARunnable(POARunnable.SERVANT_TO_ID); r.addParam( p_servant );
         * r.addParam( RealtimeThread.getCurrentMemoryArea() ); ExecuteInRunnable eir1 = new
         * ExecuteInRunnable(); eir1.init( poaMemoryArea , r ); ExecuteInRunnable eir2 = new
         * ExecuteInRunnable(); eir2.init( orb.orbImplRegion , eir1 ); try{
         * orb.parentMemoryArea.executeInArea( eir2 ); }catch( Exception e ){ e.printStackTrace(); }
         * switch( r.exception ){ case -1: //no exception break; case 1: throw new
         * ServantNotActive(); case 2: throw new WrongPolicy(); } return (byte[])r.retVal;
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.Servant reference_to_servant(
            final org.omg.CORBA.Object reference) throws ObjectNotActive,
            WrongPolicy, WrongAdapter {
        /*
         * POARunnable r = new POARunnable(POARunnable.REFERENCE_TO_SERVANT); r.addParam( reference );
         * r.addParam( RealtimeThread.getCurrentMemoryArea() ); ExecuteInRunnable eir1 = new
         * ExecuteInRunnable(); eir1.init( poaMemoryArea , r ); ExecuteInRunnable eir2 = new
         * ExecuteInRunnable(); eir2.init( orb.orbImplRegion , eir1 ); try{
         * orb.parentMemoryArea.executeInArea( eir2 ); }catch( Exception e ){ e.printStackTrace(); }
         * switch( r.exception ){ case -1: //no exception break; case 1: throw new
         * ObjectNotActive(); case 2: throw new WrongPolicy(); case 3: throw new WrongAdapter(); }
         * return (Servant)r.retVal;
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public byte[] reference_to_id(final org.omg.CORBA.Object reference)
            throws WrongAdapter, WrongPolicy {
        /*
         * POARunnable r = new POARunnable(POARunnable.REFERENCE_TO_ID); r.addParam( reference );
         * r.addParam( RealtimeThread.getCurrentMemoryArea() ); ExecuteInRunnable eir1 = new
         * ExecuteInRunnable(); eir1.init( poaMemoryArea , r ); ExecuteInRunnable eir2 = new
         * ExecuteInRunnable(); eir2.init( orb.orbImplRegion , eir1 ); try{
         * orb.parentMemoryArea.executeInArea( eir2 ); }catch( Exception e ){ e.printStackTrace(); }
         * switch( r.exception ){ case -1: //no exception break; case 1: throw new WrongAdapter();
         * case 2: throw new WrongPolicy(); } return (byte[])r.retVal;
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.Servant id_to_servant(final byte[] oid)
            throws ObjectNotActive, WrongPolicy {
        /*
         * POARunnable r = new POARunnable(POARunnable.ID_TO_SERVANT); r.addParam( oid );
         * r.addParam( RealtimeThread.getCurrentMemoryArea() ); ExecuteInRunnable eir1 = new
         * ExecuteInRunnable(); eir1.init( poaMemoryArea , r ); ExecuteInRunnable eir2 = new
         * ExecuteInRunnable(); eir2.init( orb.orbImplRegion , eir1 ); try{
         * orb.parentMemoryArea.executeInArea( eir2 ); }catch( Exception e ){ e.printStackTrace(); }
         * switch( r.exception ){ case -1: //no exception break; case 1: throw new
         * ObjectNotActive(); case 2: throw new WrongPolicy(); } return (Servant)r.retVal;
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object id_to_reference(final byte[] oid)
            throws ObjectNotActive, WrongPolicy {
        /*
         * POARunnable r = new POARunnable(POARunnable.ID_TO_REFERENCE); r.addParam( oid );
         * r.addParam( RealtimeThread.getCurrentMemoryArea() ); ExecuteInRunnable eir1 = new
         * ExecuteInRunnable(); eir1.init( poaMemoryArea , r ); ExecuteInRunnable eir2 = new
         * ExecuteInRunnable(); eir2.init( orb.orbImplRegion , eir1 ); try{
         * orb.parentMemoryArea.executeInArea( eir2 ); }catch( Exception e ){ e.printStackTrace(); }
         * switch( r.exception ){ case -1: //no exception break; case 1: throw new
         * ObjectNotActive(); case 2: throw new WrongPolicy(); } return
         * (org.omg.CORBA.Object)r.retVal;
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public byte[] activate_object(org.omg.PortableServer.Servant p_servant)
            throws ServantAlreadyActive, WrongPolicy {

        POARunnable r = new POARunnable(POARunnable.ACTIVATE_OBJECT); // XXX Memory leak.
        r.addParam( p_servant );
        r.addParam(RealtimeThread.getCurrentMemoryArea() );

        ExecuteInRunnable eir1 = new ExecuteInRunnable();//orb.getEIR();
        eir1.init(r, poaMemoryArea);

        ExecuteInRunnable eir2 = new ExecuteInRunnable();//orb.getEIR();
        eir2.init(eir1, orb.orbImplRegion);

        try{
            orb.parentMemoryArea.executeInArea(eir2);
         }
        catch(Exception e)
        {
            ZenProperties.logger.log(Logger.WARN, getClass(), "activate_object", e);
        }
        finally{
            //orb.freeEIR(eir1);
            //orb.freeEIR(eir2);
        }

        switch( r.exception )
        {
            case POARunnable.NoException: //no exception
                break;
             case POARunnable.SERVANT_ALREADY_ACTIVE:
                 throw new ServantAlreadyActive();
             case POARunnable.WrongPolicyException:
                 throw new WrongPolicy();
         }

        return (byte[])r.retVal;
    }

    public void activate_object_with_id(final byte[] id,
            final org.omg.PortableServer.Servant p_servant)
            throws ServantAlreadyActive, ObjectAlreadyActive, WrongPolicy {
        /*
         * POARunnable r = new POARunnable(POARunnable.ACTIVATE_OBJECT_WITH_ID); r.addParam( id );
         * r.addParam( p_servant ); r.addParam( RealtimeThread.getCurrentMemoryArea() );
         * ExecuteInRunnable eir1 = new ExecuteInRunnable(); eir1.init( poaMemoryArea , r );
         * ExecuteInRunnable eir2 = new ExecuteInRunnable(); eir2.init( orb.orbImplRegion , eir1 );
         * try{ orb.parentMemoryArea.executeInArea( eir2 ); }catch( Exception e ){
         * e.printStackTrace(); } switch( r.exception ){ case -1: //no exception break; case 1:
         * throw new ServantAlreadyActive(); case 2: throw new ObjectAlreadyActive(); case 3: throw
         * new WrongPolicy(); }
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void deactivate_object(byte[] oid) throws ObjectNotActive,
            WrongPolicy {
        /*
         * POARunnable r = new POARunnable(POARunnable.DEACTIVATE_OBJECT); r.addParam( oid );
         * r.addParam( RealtimeThread.getCurrentMemoryArea() ); ExecuteInRunnable eir1 = new
         * ExecuteInRunnable(); eir1.init( poaMemoryArea , r ); ExecuteInRunnable eir2 = new
         * ExecuteInRunnable(); eir2.init( orb.orbImplRegion , eir1 ); try{
         * orb.parentMemoryArea.executeInArea( eir2 ); }catch( Exception e ){ e.printStackTrace(); }
         * switch( r.exception ){ case -1: //no exception break; case 1: throw new
         * ObjectNotActive(); case 2: throw new WrongPolicy(); }a
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }



    public void destroy(final boolean etherealize_objects,
            final boolean wait_for_completion) {
        /*
         * if (wait_for_completion) { ThreadSpecificPOACurrent current = POATSS.tss.getCurrent(); if
         * (current != null && ((edu.uci.ece.zen.poa.POA) current.getPOA()).getORB() == this.orb) {
         * throw new org.omg.CORBA.BAD_INV_ORDER("The destroy is unsuccessful as the Current" +
         * "thread is in the InvocationContext", 3, CompletionStatus.COMPLETED_NO); } } synchronized
         * (createDestroyPOAMutex) { this.disableCreatePOA = true; if (((POAManager)
         * poaManager).isInActive()) { this.processingState = POA.INACTIVE; } else {
         * this.processingState = POA.DISCARDING; } // if called multiple_times the first time is
         * the etherealize param if (poaState < POA.DESTRUCTION_IN_PROGRESS) { this.etherealize =
         * etherealize_objects; } this.poaState = POA.DESTRUCTION_IN_PROGRESS; Object[] e =
         * this.the_children.getObjects(); for (int i = 0; i < e.length; i++) { if( e[i] != null )
         * ((POA)e[i]).destroy(etherealize, wait_for_completion); } // Remove the POA from the
         * POAServerRequestHandler and also from the // list of childrenPOA maintained the by Parent
         * if (this.parent != null) { POARunnable r = new
         * POARunnable(POARunnable.REMOVE_FROM_PARENT); r.addParam( poa ); ExecuteInRunnable eir1 =
         * new ExecuteInRunnable(); eir1.init( poaMemoryArea , r ); ExecuteInRunnable eir2 = new
         * ExecuteInRunnable(); eir2.init( orb.orbImplRegion , eir1 ); try{
         * orb.parentMemoryArea.executeInArea( eir2 ); }catch( Exception e ){ e.printStackTrace(); } }
         * this.serverRequestHandler.remove(this); ((edu.uci.ece.zen.poa.POAManager)
         * this.poaManager).unRegister(this); // Clear the list of children for this POA
         * this.theChildren.empty(); // Wait for the Apparent Destruction of the POA if
         * (this.numberOfCurrentRequests.get() != 0) {
         * this.numberOfCurrentRequests.waitForCompletion(); } // At this point the Apparent
         * Destruction of the POA has occured. // Ethrealize the servants for each activeObject in
         * the AOM this.poaState = POA.DESTRUCTION_APPARANT; POARunnable r = new
         * POARunnable(POARunnable.DESTROY); r.addParam( poa ); ExecuteInRunnable eir1 = new
         * ExecuteInRunnable(); eir1.init( poaMemoryArea , r ); ExecuteInRunnable eir2 = new
         * ExecuteInRunnable(); eir2.init( orb.orbImplRegion , eir1 ); try{
         * orb.parentMemoryArea.executeInArea( eir2 ); }catch( Exception e ){ e.printStackTrace(); } //
         * At this point the destruction of the POA has been completed // notify any threads that
         * could be waiting for this POA to be // destroyed this.poaState =
         * POA.DESTRUCTION_COMPLETE; ORB.freeScopedRegion( this.poaMemoryArea ); poaManager.free();
         * this.createDestroyPOAMutex.notifyAll(); } free();
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public ThreadPolicy create_thread_policy(final org.omg.PortableServer.ThreadPolicyValue value)
    {
        if (value == ThreadPolicyValue.SINGLE_THREAD_MODEL)
        {
            return edu.uci.ece.zen.poa.policy.ThreadPolicy.SingleThreaded;
        }
        else if (value == ThreadPolicyValue.ORB_CTRL_MODEL ||
                value == ThreadPolicyValue.MAIN_THREAD_MODEL)
        {
            return edu.uci.ece.zen.poa.policy.ThreadPolicy.OrbControlled;
        }

        throw new IllegalArgumentException(); // this should never happen.
    }

    public org.omg.PortableServer.LifespanPolicy create_lifespan_policy(
            final org.omg.PortableServer.LifespanPolicyValue value) {
        /*
         * return this.serverRequestHandler.create_lifespan_policy(value);
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.IdUniquenessPolicy create_id_uniqueness_policy(
            final org.omg.PortableServer.IdUniquenessPolicyValue value) {
        /*
         * return this.serverRequestHandler.create_id_uniqueness_policy(value);
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.IdAssignmentPolicy create_id_assignment_policy(
            final org.omg.PortableServer.IdAssignmentPolicyValue value) {
        /*
         * return this.serverRequestHandler.create_id_assignment_policy(value);
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.ImplicitActivationPolicy create_implicit_activation_policy(
            final org.omg.PortableServer.ImplicitActivationPolicyValue value) {
        /*
         * return this.serverRequestHandler.create_implicit_activation_policy(value);
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.ServantRetentionPolicy create_servant_retention_policy(
            final org.omg.PortableServer.ServantRetentionPolicyValue value) {
        /*
         * return this.serverRequestHandler.create_servant_retention_policy(value);
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public RequestProcessingPolicy create_request_processing_policy(final RequestProcessingPolicyValue value)
    {
        if (value == RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY)
        {
            return edu.uci.ece.zen.poa.policy.RequestProcessingPolicy.AOM;
        }
        else if (value == RequestProcessingPolicyValue.USE_DEFAULT_SERVANT)
        {
            return edu.uci.ece.zen.poa.policy.RequestProcessingPolicy.DefaultServant;
        }
        else if (value == RequestProcessingPolicyValue.USE_SERVANT_MANAGER)
        {
            return edu.uci.ece.zen.poa.policy.RequestProcessingPolicy.ServantManager;
        }

        throw new IllegalArgumentException(); // this should never happen.
    }

    public java.lang.String the_name() {
        return new String(poaName.getTrimData());
    }

    public java.lang.String path_name() {
        return new String(poaPath.getTrimData());
    }

    public org.omg.PortableServer.POA the_parent() {
        return parent;
    }

    public byte[] id() {
        POARunnable r = new POARunnable(POARunnable.ID);
        r.addParam(RealtimeThread.getCurrentMemoryArea());
        ExecuteInRunnable eir1 = orb.getEIR();
        eir1.init(r, poaMemoryArea);
        ExecuteInRunnable eir2 = orb.getEIR();
        eir2.init(eir1, orb.orbImplRegion);
        try {
            orb.parentMemoryArea.executeInArea(eir2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            orb.freeEIR(eir1);
            orb.freeEIR(eir2);
        }
        return (byte[]) r.retVal;
    }

    public org.omg.PortableServer.POA[] the_children() {
        /*
         * Object[] objs = theChildren.getObjects(); POA[] poas = new POA[objs.length]; for( int
         * i=0;i <objs.length;i++ ) poas[i] = (POA) objs[i]; return poas;
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.POAManager the_POAManager() {
        return this.poaManager;
    }

    public org.omg.PortableServer.AdapterActivator the_activator() {
        /*
         * return this.adapterActivator;
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void the_activator(
            final org.omg.PortableServer.AdapterActivator the_activator) {
        /*
         * this.adapterActivator = the_activator;
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public ServantManager get_servant_manager() throws WrongPolicy {
        /*
         * POARunnable r = getPOARunnable(); r.init( POARunnable.GET_SERVANT_MANAGER , null , null ,
         * null , null , null , null , null ); poaMemoryArea.enter(r); Exception ex =
         * r.getException(); org.omg.PortableServer.ServantManager retVal =
         * (org.omg.PortableServer.ServantManager) r.getRetVal(); returnPOARunnable( r ); if( ex !=
         * null ) throw ex; return retVal;
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void set_servant_manager(
            final org.omg.PortableServer.ServantManager imgr)
            throws org.omg.PortableServer.POAPackage.WrongPolicy {
        /*
         * POARunnable r = getPOARunnable(); r.init( POARunnable.SET_SERVANT_MANAGER , imgr , null ,
         * null , null , null , null , null ); poaMemoryArea.enter(r); Exception ex =
         * r.getException(); returnPOARunnable( r ); if( ex != null ) throw ex;
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.Servant get_servant() throws NoServant, WrongPolicy
    {
        // TODO Test it.
        POARunnable r =  new POARunnable(POARunnable.GET_SERVANT);

        // jumping to PoaImpl scope
        ExecuteInRunnable eir1 = orb.getEIR();
        eir1.init(r, poaMemoryArea);
        ExecuteInRunnable eir2 = orb.getEIR();
        eir2.init(eir1, orb.orbImplRegion);
        try
        {
            orb.parentMemoryArea.executeInArea(eir2);
        }
        catch (Exception e2) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "get_servant", e2);
        }
        finally {
            orb.freeEIR(eir1);
            orb.freeEIR(eir2);
        }

        switch (r.exception) {
            case POARunnable.NoException:
                break;
            case POARunnable.NoServant:
                throw new NoServant();
            case POARunnable.WrongPolicyException:
                throw new WrongPolicy();
            default:
                if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("Unexpected exception in " + getClass() +".get_servant.");
        }

       if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("POA.get_servant" + r.retVal);
       return (Servant) r.retVal;
    }


    public void set_servant(final org.omg.PortableServer.Servant p_servant) throws WrongPolicy
    {
        if (p_servant == null)
        {
            // TODO check about exception and memory scopes. We think that the sooner checking
            // parameters and throwing exception the better.
            throw new NullPointerException(); // p_servant is null;
        }

        // TODO Test it.
        POARunnable r =  new POARunnable(POARunnable.SET_SERVANT); // XXX Memory leak
        r.addParam(p_servant);

        ExecuteInRunnable eir1 = orb.getEIR();
        eir1.init(r, poaMemoryArea);
        ExecuteInRunnable eir2 = orb.getEIR();
        eir2.init(eir1, orb.orbImplRegion);
        try
        {
            orb.parentMemoryArea.executeInArea(eir2);
        }
        catch (Exception e2)
        {
            ZenProperties.logger.log(Logger.WARN, getClass(), "set_servant", e2);
        }
        finally{
            orb.freeEIR(eir1);
            orb.freeEIR(eir2);
        }

        if (r.exception == POARunnable.WrongPolicyException)
        {
           throw new WrongPolicy();
        }
        else if (r.exception != POARunnable.NoException)
        {
            if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("Unexpected exception in " + getClass() +".set_servant.");
        }
    }

    public org.omg.CORBA.Object create_reference(final String intf)
            throws org.omg.PortableServer.POAPackage.WrongPolicy {
        /*
         * org.omg.CORBA.Object retVal; POARunnable r = getPOARunnable(); r.init(
         * POARunnable.CREATE_REFERENCE , intf , curMemArea , null , null , null , null , null );
         * poaMemoryArea.getCurrentMemoryArea(); Exception ex = r.getException(); retVal =
         * r.getRetVal(); returnPOARunnable( r ); if( ex != null ) throw ex; return retVal;
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object create_reference_with_id(final byte[] oid,
            final String intf) {
        /*
         * org.omg.CORBA.Object retVal; POARunnable r = getPOARunnable(); r.init(
         * POARunnable.CREATE_REFERENCE_WITH_ID , intf , oid , curMemArea , null , null , null ,
         * null ); poaMemoryArea.getCurrentMemoryArea(); Exception ex = r.getException(); retVal =
         * r.getRetVal(); returnPOARunnable( r ); if( ex != null ) throw ex; return retVal;
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Policy[] policy_list() {
        /*
         * org.omg.CORBA.Policy[] retVal; POARunnable r = getPOARunnable(); r.init(
         * POARunnable.GET_POLICY_LIST , RealtimeThread.getCurrentMemoryArea() , null , null , null ,
         * null , null , null ); poaMemoryArea.getCurrentMemoryArea(); Exception ex =
         * r.getException(); retVal = r.getRetVal(); returnPOARunnable( r ); if( ex != null ) throw
         * ex; return retVal;
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    protected void waitForCompletion() {
        numberOfCurrentRequests.waitForCompletion();
    }

    public final edu.uci.ece.zen.orb.ORB getORB() {
        return this.orb;
    }

    POA getChildPOA(String poaName) {
        /*
         * POA child = (POA) this.theChildren.get(poaName); if (child == null || child.getStatus() ==
         * POA.DESTRUCTION_APPARANT) { if (adapterActivator == null) { if( POA.objNotExistException ==
         * null ){ POA.objNotExistException = ImmortalMemory.instance().newInstance(
         * OBJECT_NOT_EXIST.class ); } throw POA.objNotExistException; } if (((POAManager)
         * poaManager).isDiscarding()) { if( POA.parentDiscardTransientException == null ){
         * POA.parentDiscardTransientException = ImmortalMemory.instance().newInstance(
         * ParentDiscardTRANSIENT.class ); } throw parentDiscardTransientException; } if
         * (((POAManager) poaManager).isInActive()) { throw new org.omg.CORBA.OBJ_ADAPTER("Parent
         * POA inactive"); } // one also needs to check for holding state and take action if
         * (((POAManager) poaManager).isHolding()) { throw new org.omg.CORBA.TRANSIENT("Parent POA
         * in holding state:Cannot Activate Child POA", 1,
         * org.omg.CORBA.CompletionStatus.COMPLETED_NO); } // The POA is active.. Serailize the
         * calls if Single threaded childMemory.enter(new Runnable(){ public void run(){
         * (((POAImpl)childMemory.getPortal()).getThreadPolicyStrategy()).enter(); } });
         * //this.threadPolicyStrategy.enter(); boolean success =
         * the_activator().unknown_adapter(this, poaName); childMemory.enter(new Runnable() { public
         * void run(){ ( (POAImpl)childMemory.getPortal()).getThreadPolicyStrategy().exit();} });
         * //this.threadPolicyStrategy.exit(); if (success) { child = (POA)
         * this.theChildren.get(poaName); if (child == null) { throw new
         * org.omg.CORBA.INTERNAL("unknown_adapter operation", 0, CompletionStatus.COMPLETED_NO); } }
         * throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA activation failed"); } return child;
         */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Activates an CORBA object
     */
    public byte[] activate_object_with_priority(Servant servant, short priority)
        throws ServantAlreadyActive, WrongPolicy
    {
        POARunnable r = new POARunnable(POARunnable.ACTIVATE_OBJECT_WITH_PRIORITY); // XXX Memory leak.
        r.addParam(servant);
        r.addParam(new Short(priority)); // XXX Memory leak
        r.addParam(RealtimeThread.getCurrentMemoryArea());

        // TODO Extract into a method from HERE
        ExecuteInRunnable eir1 = orb.getEIR();
        eir1.init(r, poaMemoryArea);

        ExecuteInRunnable eir2 = orb.getEIR();
        eir2.init(eir1, orb.orbImplRegion);

        try{
            orb.parentMemoryArea.executeInArea(eir2);
         }
        catch(Exception e)
        {
            ZenProperties.logger.log(Logger.WARN, getClass(), "activate_object_with_priority", e);
        }
        finally{
            orb.freeEIR(eir1);
            orb.freeEIR(eir2);
        }


        // TODO to HERE
//
//        switch( r.exception )
//        {
//            case POARunnable.NoException: //no exception
//                break;
//             case POARunnable.SERVANT_ALREADY_ACTIVE:
//                 throw new ServantAlreadyActive();
//             case POARunnable.WrongPolicyException:
//                 throw new WrongPolicy();
//         }
//
//        return (byte[])r.retVal;

        return null;
    }

    /* (non-Javadoc)
     * @see org.omg.RTPortableServer.POAOperations#create_reference_with_priority(java.lang.String, short)
     */
    public org.omg.CORBA.Object create_reference_with_priority(String arg0, short arg1) throws WrongPolicy
    {
        // TODO Auto-generated method stub
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /* (non-Javadoc)
     * @see org.omg.RTPortableServer.POAOperations#create_reference_with_id_and_priority(byte[], java.lang.String, short)
     */
    public org.omg.CORBA.Object create_reference_with_id_and_priority(byte[] arg0, String arg1, short arg2) throws WrongPolicy
    {
        // TODO Auto-generated method stub
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }



    /* (non-Javadoc)
     * @see org.omg.RTPortableServer.POAOperations#activate_object_with_id_and_priority(byte[], org.omg.PortableServer.Servant, short)
     */
    public void activate_object_with_id_and_priority(byte[] arg0, Servant arg1, short arg2) throws ServantAlreadyActive, ObjectAlreadyActive, WrongPolicy
    {
        // TODO Auto-generated method stub
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Returns the policies that are exposed to the client.
     * To store this references may cause IllegalAccessError. They come from POAImpl scope.
     * @return policies exposed to the client.
     */
    public /*Policy[]*/ CDROutputStream getClientExposedPolicies(short priority)
    {
        POARunnable r = new POARunnable(POARunnable.GET_CLIENT_EXPOSED_POLICIES);
        r.setPriority(priority);
        executeInPOAMemoryArea(r);
        //return (Policy[]) r.retVal;
        return (CDROutputStream)r.retVal;
    }

    /**
     *
     * @param r a POARunnable object
     */
    private void executeInPOAMemoryArea(POARunnable r)
    {
        ExecuteInRunnable eir1 = new ExecuteInRunnable();//TODO orb.getEIR();
        eir1.init(r, poaMemoryArea);
        ExecuteInRunnable eir2 = new ExecuteInRunnable();//TODO orb.getEIR();
        eir2.init(eir1, orb.orbImplRegion);

        try {
            orb.parentMemoryArea.executeInArea(eir2);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "executeInPOAMemoryArea", e);
        }
        finally{
            //orb.freeEIR(eir1);
            //orb.freeEIR(eir2);
        }
    }

}

