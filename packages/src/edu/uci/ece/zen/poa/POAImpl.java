/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.poa;

import javax.realtime.MemoryArea;
import javax.realtime.NoHeapRealtimeThread;
import javax.realtime.PriorityScheduler;
import javax.realtime.ScopedMemory;
import javax.realtime.ImmortalMemory;

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;
import org.omg.RTCORBA.PriorityModel;
import org.omg.RTCORBA.PriorityModelPolicy;
import org.omg.RTCORBA.ThreadpoolPolicy;

//import com.sun.corba.se.internal.POA.Policies;

import edu.uci.ece.zen.orb.ORB;
import edu.uci.ece.zen.orb.ORBImpl;
import edu.uci.ece.zen.orb.PriorityMappingImpl;
import edu.uci.ece.zen.orb.protocol.type.RequestMessage;
import edu.uci.ece.zen.poa.mechanism.ActivationStrategy;
import edu.uci.ece.zen.poa.mechanism.DefaultServantStrategy;
import edu.uci.ece.zen.poa.mechanism.IdAssignmentStrategy;
import edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy;
import edu.uci.ece.zen.poa.mechanism.LifespanStrategy;
import edu.uci.ece.zen.poa.mechanism.RetainStrategy;
import edu.uci.ece.zen.poa.mechanism.SystemIdStrategy;
import edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy;
import edu.uci.ece.zen.poa.mechanism.ServantRetentionStrategy;


//import edu.uci.ece.zen.poa.mechanism.RetainStrategy;
import edu.uci.ece.zen.poa.mechanism.ThreadPolicyStrategy;
import edu.uci.ece.zen.utils.ExecuteInRunnable;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.Queue;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;
import edu.uci.ece.zen.utils.WriteBuffer;

public class POAImpl {

    //////////////////////////////////////////////////////////////////
    //// DATA MEMBERS /////
    /////////////////////////////////////////////////////////////////

    // -- ZEN ORB ---
    MemoryArea thisMemory;

    private edu.uci.ece.zen.orb.ORB orb;

    // --- POA Names relative and Complete Path Names ---
    private int poaId;

    // --- POA Specific references ---
    private POA parent;

    public POA self;

    private java.util.Hashtable theChildren;

    private org.omg.PortableServer.AdapterActivator adapterActivator;

    private org.omg.PortableServer.ServantManager theServantManager;

    private org.omg.PortableServer.Servant theServant = null;

    private POAServerRequestHandler serverRequestHandler;

    private POAImplRunnable poaImplRunnable;

    // --- Mutexes POA and varable specific to the create and destroy ops ---
    private Object createDestroyPOAMutex = new byte[0];

    private boolean disableCreatePOA = false;

    private boolean etherealize;

    // -- Policy List for the POA
    private org.omg.CORBA.Policy[] policyList;
    private org.omg.CORBA.Policy[] clientExposedPolicies;

    // --- State of the POA ---
    private int poaState;

    private int processingState = POA.ACTIVE; // RequestProcessing

    // --- POA Specific strategies ----
    private transient edu.uci.ece.zen.poa.mechanism.LifespanStrategy lifespanStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.ThreadPolicyStrategy threadPolicyStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.IdAssignmentStrategy idAssignmentStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.ServantRetentionStrategy retentionStrategy;

    public transient edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy requestProcessingStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy uniquenessStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.ActivationStrategy activationStrategy;

    private Runnable cachedRunnable;

    POAManager manager;

    int threadPoolId = 0; // this default value means: "use the same thread pool of RootPOA (0)"

    // TODO Just for now the default priority model is server declare,
    // but we have to ask the ORB in order to set this variable properly.
    private PriorityModel priorityModel = PriorityModel.SERVER_DECLARED;
    // TODO Just for now we are using RTJava priorities.
    private int serverPriority = PriorityMappingImpl.toCORBA((short)PriorityScheduler.instance().getNormPriority());

    // --- POA Cached Objects ---
    Queue poaHashMapQueue;

    Queue poaFStringQueue;

    Queue poaIntHolderQueue;

    public POAImpl() {
        poaHashMapQueue = new Queue();
        poaIntHolderQueue = new Queue();
    }

    public POAHashMap getPOAHashMap() {
        POAHashMap ret = (POAHashMap) poaHashMapQueue.dequeue();
        if (ret == null) {
            ret = new POAHashMap();
        }
        return ret;
    }

    public void freePOAHashMap(POAHashMap map) {
        poaHashMapQueue.enqueue(map);
    }

    public FString getFString() {
        FString ret = FString.instance();
        ret.reset();
        return ret;
    }

    public void retFString(FString str) {
        FString.free( str );
    }

    public IntHolder getIntHolder() {
        IntHolder ret = (IntHolder) poaIntHolderQueue.dequeue();
        if (ret == null) {
            ret = new IntHolder();
        }
        return ret;
    }

    public void retIntHolder(IntHolder ih) {
        poaIntHolderQueue.enqueue(ih);
    }

    /**
     * Returns an array with the
     * @return
     */
    public Policy[] getClientExposedPolicies()
    {
        return this.clientExposedPolicies;
    }


    // --- POA Methods ---
    /**
     * Initializes the POAImpl and all mechanisms and policies.
     *
     * @throws InvalidPolicyException
     */
    public void init(ORB orb, POA self, Policy[] policies, POA parent, POAManager manager, POARunnable prun) {
        this.orb = orb;
        this.self = self;
        this.parent = parent;
        this.manager = manager;
        if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("---------------------POAIMpl init:0 ");
        try {
            serverRequestHandler =
                (POAServerRequestHandler) ((ORBImpl) orb.orbImplRegion.getPortal()).getServerRequestHandler();
            if (serverRequestHandler == null) {
                serverRequestHandler =
                    (POAServerRequestHandler) orb.orbImplRegion.newInstance(POAServerRequestHandler.class);
                ((ORBImpl) orb.orbImplRegion.getPortal()).setServerRequestHandler(serverRequestHandler);
            }
        } catch (Exception e1) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "init", e1);
        }
        self.poaDemuxIndex = serverRequestHandler.addPOA(self.poaPath, self);
        self.poaDemuxCount = serverRequestHandler.getPOAGenCount(self.poaDemuxIndex);

        // TODO Extract to a method, FROM HERE

        //make a local copy of the policies
        if (policies != null) {
            this.policyList = new Policy[policies.length];
        }
        else {
            this.policyList = new Policy[0];
        }

        int numOfClientExposedPolicies = 0;
        boolean threadoolPolicyChecked = false;
        boolean priorityModelPolicyChecked = false;
if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("---------------------POAIMpl init:1 ");
        for (int i = 0; i < policyList.length; i++) {
            this.policyList[i] = policies[i].copy();

            if (this.policyList[i] instanceof ThreadpoolPolicy && !threadoolPolicyChecked)
            {
                this.threadPoolId = ((org.omg.RTCORBA.ThreadpoolPolicy) this.policyList[i]).threadpool();
                // TODO Validate the threadPollID here.
                //      Set the serverPriority value properly according to the lanes' priorities
                //      (e.g. to the lanes' lowest priority value )
                // KLUDGE: need to get the lowest priority of the threadpool for the server declated priority
                threadoolPolicyChecked = true;
            }

            if (this.policyList[i] instanceof PriorityModelPolicy && !priorityModelPolicyChecked)
            {
                 this.priorityModel = ((PriorityModelPolicy) this.policyList[i]).priority_model();
                 // Be careful here, we are not mapping CORBA and RTSJ priorities.
                 this.serverPriority = ((PriorityModelPolicy) this.policyList[i]).server_priority();
                 // TODO Validate the value of server priority. It must be consistent with the priority
                 //      of the lanes/threadpool

                 numOfClientExposedPolicies++;
                 priorityModelPolicyChecked = true;
            }
        }
if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("---------------------POAIMpl init:2 ");
        clientExposedPolicies = new Policy[numOfClientExposedPolicies];
        priorityModelPolicyChecked = false;

        for (int i = 0, j = 0; i < policyList.length; i++)
        {
            if (this.policyList[i] instanceof PriorityModelPolicy && !priorityModelPolicyChecked)
            {
                this.clientExposedPolicies[j] = this.policyList[i];
                priorityModelPolicyChecked = true;
                j++;
            }

            if (j == clientExposedPolicies.length) break;
        }

        //init the stratergies
        IntHolder ih = getIntHolder();
        this.threadPolicyStrategy = ThreadPolicyStrategy.init(policyList, ih);
        if (ih.value != 0) {
            retIntHolder(ih);
            prun.exception = POARunnable.InvalidPolicyException;
            return;
        }

        this.idAssignmentStrategy = IdAssignmentStrategy.init(policyList, ih);
        if (ih.value != 0) {
            retIntHolder(ih);
            prun.exception = POARunnable.InvalidPolicyException;
            return;
        }

        this.uniquenessStrategy = IdUniquenessStrategy.init(policyList, ih);
        if (ih.value != 0) {
            retIntHolder(ih);
            prun.exception = POARunnable.InvalidPolicyException;
            return;
        }

        this.retentionStrategy =
            ServantRetentionStrategy.init(policyList, this.uniquenessStrategy, ih);
        if (ih.value != 0) {
            retIntHolder(ih);
            prun.exception = POARunnable.InvalidPolicyException;
            return;
        }

        this.lifespanStrategy = LifespanStrategy.init(this.policyList, ih);
        if (ih.value != 0) {
            retIntHolder(ih);
            prun.exception = POARunnable.InvalidPolicyException;
            return;
        }

        this.activationStrategy =
            ActivationStrategy.init(this.policyList, this.idAssignmentStrategy,
                                    this.retentionStrategy, ih);
        if (ih.value != 0) {
            retIntHolder(ih);
            prun.exception = POARunnable.InvalidPolicyException;
            return;
        }

        this.requestProcessingStrategy =
            RequestProcessingStrategy.init(this.policyList, this.retentionStrategy,
                                           this.uniquenessStrategy, this.threadPolicyStrategy,
                                           this, ih);
        if (ih.value != 0) {
            retIntHolder(ih);
            prun.exception = POARunnable.InvalidPolicyException;
            return;
        }
        retIntHolder(ih);

        // TO HERE

        poaImplRunnable = new POAImplRunnable(self.poaMemoryArea);
        self.poaMemoryArea.setPortal(this);
        NoHeapRealtimeThread nhrt =
            new NoHeapRealtimeThread(null, null, null, self.poaMemoryArea, null, poaImplRunnable);
        ZenProperties.logger.log("======================starting nhrt in poa impl region=====================");

        nhrt.start();
    }

    private int statCount = 0;

    /**
     * Call scoped region graph:
     * <p>
     * Transport thread: <br/>
     * <p>
     * Transport scope --ex in--&gt; ORBImpl scope --&gt; Message --ex in--&gt;
     * ORBImpl scope --&gt; <b>POAImpl region </b> --ex in--&gt; ORBImpl scope
     * --&gt; TP Region
     * </p>
     * TP Thread: <br/>
     * <p>
     * <b>TP Region </b> --ex in--&gt; ORBImpl scope --&gt; Message Region --ex
     * in--&gt; ORBImpl region --&gt; Transport Scope
     * </p>
     * </p>
     */
    TPRunnable tpr = null;  //KLUDGE..leaks memory to IMM, 3am..wll fix later
    public synchronized void handleRequest(RequestMessage req, POARunnable prun) {
    if( tpr == null ){
        try{
            tpr= (TPRunnable) ImmortalMemory.instance().newInstance( TPRunnable.class ); //orb.getTPR();
        }catch( Throwable e ){
            System.out.println( "handleRequest: e" + e );
        }
    }
        IntHolder ih = getIntHolder();
        if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("---------------------handleRequest:0 ");

        // Check the POA's state. if it is discarding then throw the transient exception.
        validateProcessingState(ih);
        if (ih.value != POARunnable.NoException) {
            prun.exception = ih.value;
            retIntHolder(ih);
            if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("handleRequest:0 -- Exception");
            return;
        }
        if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("handleRequest:1 ");
        // Check the state of the POAManager. Here the POA is in active state
        prun.exception = POAManager.checkPOAManagerState(self.the_POAManager());
        if (ih.value != POARunnable.NoException) {
            prun.exception = ih.value;
            retIntHolder(ih);
            if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("handleRequest:1 -- Exception");
            return;
        }
        if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("handleRequest:2 ");
        // check if the POA has the persistent policy/or the transient
        FString objKey = req.getObjectKey();
        this.lifespanStrategy.validate(objKey, ih);
        if (ih.value != POARunnable.NoException) {
            prun.exception = ih.value;
            retIntHolder(ih);
            if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("handleRequest:2 -- Exception");
            return;
        }
        if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("handleRequest:3 ");
        try {
            ScopedMemory tpRegion = this.orb.getThreadPoolRegion(threadPoolId);
            edu.uci.ece.zen.utils.Logger.printThreadStack();

            statCount++;

            if (statCount % ZenBuildProperties.MEM_STAT_COUNT == 0)
                edu.uci.ece.zen.utils.Logger.printMemStats(ZenBuildProperties.dbgPOAScopeId);

            //ExecuteInRunnable eir =
            //    (ExecuteInRunnable) requestScope.newInstance( ExecuteInRunnable.class );
            ExecuteInRunnable eir = orb.getEIR();

            //if(req.getPriority() != (short)serverPriority)
                //if (ZenBuildProperties.dbgPOA) 
                    //ZenProperties.logger.log(Logger.WARN, getClass(), "handleRequest", "server pr != msg pr");

            short pr = 0;
            if( this.priorityModel == PriorityModel.SERVER_DECLARED ){
                pr = (short) this.serverPriority;
                ZenProperties.logger.log( "POAImpl using server declared" );
            }else{
                pr = req.getPriority();//orb.getRTCurrent().the_priority();//req.getPriority();//(short)serverPriority;//
                ZenProperties.logger.log( "POAImpl using client propogated" );
            }

            if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("handleRequest:4  request pr: " + pr + " def. server pr: " + serverPriority);

            tpr.init(self, req, pr);
            eir.init(tpr, tpRegion);
            if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("handleRequest:5 ");
            //HandleRequestRunnable hrr = (HandleRequestRunnable)
            // requestScope.newInstance( HandleRequestRunnable.class );
            //hrr.init( self , req );

            //((ScopedMemory)requestScope).setPo{rtal( hrr );
            req.associatePOA(self);
            //edu.uci.ece.zen.utils.Logger.printMemStatsImm(318);
            orb.orbImplRegion.executeInArea(eir);
            if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("handleRequest:6 ");
            //edu.uci.ece.zen.utils.Logger.printMemStatsImm(319);
            orb.freeEIR(eir);
            if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("handleRequest:7 ");
        } catch (Exception ex) {
            // -- have to send a request not handled to the client here
            // -- Throw a transient exception
            prun.exception = POARunnable.TransientException;
            ZenProperties.logger.log(Logger.WARN, getClass(), "handleRequest", ex);
            return;
        } finally {
            retIntHolder(ih);
        }
     }

        /**
         * Generates the object reference for that particular servant.
         * Activate the servant with the server priority (stablished during POA.init(...)).
         * @param p_servant
         *            The servant object.
         * @throws org.omg.PortableServer.POAPackage.ServantNotActive
         *             If the servant passed in is not active.
         * @throws org.omg.PortableServer.POAPackage.WrongPolicy
         *             If the policies of the POA dont contain RETAIN and
         *             UNIQUE_ID/MYULTIPLE_ID policies.
         * @return The object reference for that particular servant.
         */
        public org.omg.CORBA.Object servant_to_reference(final Servant p_servant,
                MemoryArea clientMemoryArea, POARunnable prun)
        {
            //check if this method is being called as a part of an upcall
            /*
             * KLUDGE: Ignore current for now. org.omg.PortableServer.Current
             * current = null; try { current = (org.omg.PortableServer.Current)
             * Current.currentInit(); if (p_servant == current.get_servant()) {
             * return current.get_reference(); } } catch
             * (org.omg.PortableServer.CurrentPackage.NoContext ex) { //KLUDGE:
             * //TODO: Check what is supposed to happen here //ex.printStackTrace(); } //
             * method was invoked outside the invocation context: Check if the //
             * POA has the RETAIN and UNIQUE_ID in place. //NOTE: A ServantNotActive
             * exception was being squelched here.
             */

            FString okey = FString.instance();//getFString();
            FString oid = getFString();
            IntHolder ih = getIntHolder();
            org.omg.CORBA.Object retVal = null;

            this.retentionStrategy.getObjectID(p_servant, oid, ih);

            switch (ih.value) {
                case POARunnable.ServantNotActiveException:
                    {
                        if (this.activationStrategy.validate(ActivationStrategy.IMPLICIT_ACTIVATION) ||
                            this.uniquenessStrategy.validate(IdUniquenessStrategy.MULTIPLE_ID))
                        {
                            this.idAssignmentStrategy.nextId(oid, ih);
                            if (ih.value != POARunnable.NoException)
                            {
                                prun.exception = ih.value;
                                break;
                            }

                            POAHashMap map = getPOAHashMap();

                            //TODO This should be obtain from the ORB
                            map.init(oid, p_servant, this.serverPriority);

                            this.retentionStrategy.add(oid, map, ih);
                            if (ih.value != POARunnable.NoException) {
                                prun.exception = ih.value;
                                freePOAHashMap(map);
                                break;
                            }
                            //Dont do this right now. will do it later when IOR is being created
                            //orb.set_delegate ( p_servant );

                            int index = this.retentionStrategy.bindDemuxIndex(map, ih);
                            if (ih.value != POARunnable.NoException) {
                                prun.exception = ih.value;
                                freePOAHashMap(map);
                                break;
                            }
                            int genCount = this.retentionStrategy.getGenCount(index, ih);
                            if (ih.value != POARunnable.NoException) {
                                prun.exception = ih.value;
                                freePOAHashMap(map);
                                break;
                            }

                            this.lifespanStrategy.create(self.poaPath, oid,
                                    self.poaDemuxIndex, self.poaDemuxCount, index,
                                    genCount, okey);
                            retVal = this.create_reference_with_object_key(okey,
                                    p_servant._all_interfaces(self, null)[0],
                                    clientMemoryArea);
                            break;
                        }
                        prun.exception = POARunnable.ServantNotActiveException;
                        retIntHolder(ih);
                    }
                    break;
                case POARunnable.WrongPolicyException:
                    prun.exception = POARunnable.WrongPolicyException;
                    retIntHolder(ih);
                    break;
                case POARunnable.NoException: {
                    POAHashMap map = this.retentionStrategy.getHashMap(oid, ih);
                    if (ih.value != POARunnable.NoException) {
                        prun.exception = ih.value;
                        break;
                    }

                    int index = this.retentionStrategy.find(oid, ih);
                    if (ih.value != POARunnable.NoException) {
                        prun.exception = ih.value;
                        break;
                    }
                    int count = this.retentionStrategy.getGenCount(index, ih);
                    if (ih.value != POARunnable.NoException) {
                        prun.exception = ih.value;
                        break;
                    }

                    // Create the Object Key using the IdHint Strategy
                    this.lifespanStrategy.create(self.poaPath, oid,
                        self.poaDemuxIndex, self.poaDemuxCount, index, count, okey);

                    retVal = this.create_reference_with_object_key(okey, p_servant
                            ._all_interfaces(self, null)[0], clientMemoryArea);
                    }
            }
            FString.free(okey);//retFString(okey);
            retFString(oid);
            retIntHolder(ih);
            if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("servant_to_reference " + retVal);
            if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("servant_to_reference client area " + clientMemoryArea);

            //p_servant._set_delegate(((org.omg.CORBA.portable.ObjectImpl)retVal)._get_delegate());

            return retVal;
        }

        public void servant_to_id(Servant servant, MemoryArea mem, POARunnable prun) {
        }

        public void reference_to_servant(org.omg.CORBA.Object obj, MemoryArea mem,
                POARunnable prun) {
        }

        public void references_to_id(org.omg.CORBA.Object obj, MemoryArea mem,
                POARunnable prun) {
        }

        public void id_to_servant(byte[] oid, MemoryArea mem, POARunnable prun) {
        }

        public void id_to_reference(byte[] oid, MemoryArea mem, POARunnable prun) {
        }

        /**
         *
         * @param servant
         * @param mem
         * @param prun
         */
        public void activate_object(Servant servant, MemoryArea mem, POARunnable prun)
        {
           this.activate_object_with_priority(servant, this.serverPriority, mem, prun);
        }

        /**
         *
         * @param servant
         * @param priority
         * @param mem
         * @param prun
         */
        public void activate_object_with_priority(Servant servant, int priority, MemoryArea mem,
                                                  POARunnable prun)
        {
            if (!(idAssignmentStrategy instanceof SystemIdStrategy &&
                    retentionStrategy instanceof RetainStrategy))
              {
                  prun.exception = POARunnable.WrongPolicyException;
                  return;
              }

              IntHolder ih = getIntHolder();
              FString okey = FString.instance();//getFString();
              FString oid = getFString();

              this.retentionStrategy.getObjectID(servant, oid, ih); // We know this is RetainStrategy

              // If the servant is already active, return
              if (ih.value != POARunnable.ServantNotActiveException)
              {
                  prun.exception = POARunnable.SERVANT_ALREADY_ACTIVE;
                  retIntHolder(ih);
                  //retFString(okey);
                  FString.free(okey);
                  retFString(oid);
                  return;
              }

              this.idAssignmentStrategy.nextId(oid, ih);
              if (ih.value != POARunnable.NoException)
              {
                  prun.exception = ih.value;
                  retIntHolder(ih);
                  //retFString(okey);
                  FString.free(okey);
                  retFString(oid);
                  return;
              }

              POAHashMap map = getPOAHashMap();
              map.init(oid, servant, priority);

              this.retentionStrategy.add(oid, map, ih);
              if (ih.value != POARunnable.NoException)
              {
                  prun.exception = ih.value;
                  freePOAHashMap(map);
                  retIntHolder(ih);
                  //retFString(okey);
                  FString.free(okey);
                  retFString(oid);
                  return;
              }

              this.retentionStrategy.bindDemuxIndex(map, ih);
              if (ih.value != POARunnable.NoException)
              {
                  prun.exception = ih.value;
                  freePOAHashMap(map);
                  retIntHolder(ih);
                  //retFString(okey);
                  FString.free(okey);
                  retFString(oid);
                  return;
              }

              retIntHolder(ih);
              //retFString(okey);
              FString.free(okey);
              retFString(oid);
              prun.exception = POARunnable.NoException;

        }

        public void activate_object_with_id(byte[] oid, Servant servant,
                MemoryArea mem, POARunnable prun) {
        }


        public void deactivate_object(byte[] oid, MemoryArea mem, POARunnable prun) {
        }

        public void removeFromParent(POA poa) {
        }

        public void destroy(POA thisPoa) {
        }

        public void id(MemoryArea mem, POARunnable prun) {
        }

        public void get_servant_manager(POARunnable prun) {
        }

        public void set_servant_manager(ServantManager manager, POARunnable prun) {
        }


        public void get_servant(POARunnable prun)
        {
            // TODO test it
            if (!(requestProcessingStrategy instanceof DefaultServantStrategy))
            {
                prun.exception = POARunnable.WrongPolicyException;
                return;
            }

            if (this.theServant == null)
            {
                prun.exception = POARunnable.NoServant;
                return;
            }

            prun.retVal = this.theServant;
        }

        public void set_servant(Servant servant, POARunnable prun)
        {
            // TODO test it
            if (!(requestProcessingStrategy instanceof DefaultServantStrategy))
            {
                prun.exception = POARunnable.WrongPolicyException;
                return;
            }

            this.theServant = servant;
        }

        public void create_reference(String atr, MemoryArea mem, POARunnable prun) {
        }

        public void create_reference_with_id(byte[] oid, String str,
                MemoryArea mem, POARunnable prun) {
        }

        public void policy_list(MemoryArea mem, POARunnable prun) {
        }

        protected void validateProcessingState(IntHolder ih) {
            switch (this.processingState) {
                case POA.DISCARDING:
                    ih.value = POARunnable.TransientException;
                case POA.INACTIVE:
                    ih.value = POARunnable.ObjAdapterException;
            }
            ih.value = POARunnable.NoException;
        }

        private CreateReferenceWithObjectRunnable crwor;

        public synchronized org.omg.CORBA.Object create_reference_with_object_key(
                FString ok, final String intf, MemoryArea clientArea) {
            ZenProperties.logger.log("create_reference_with_object_key 1");
            if (crwor == null) crwor = new CreateReferenceWithObjectRunnable();
            ZenProperties.logger.log("create_reference_with_object_key 2");
            crwor.init(ok, intf, clientArea, orb, threadPoolId, self, this.priorityModel.value() , (short) this.serverPriority );
            ZenProperties.logger.log("create_reference_with_object_key 3");
            try {
                orb.orbImplRegion.executeInArea(crwor);
                return crwor.retVal;
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, getClass(), "create_reference_with_object_key", e);
                return null;
            }
        }

        public edu.uci.ece.zen.poa.mechanism.ThreadPolicyStrategy getThreadPolicyStrategy() {
            return this.threadPolicyStrategy;
        }

        public SynchronizedInt getnumberOfCurrentRequests() {
            return self.numberOfCurrentRequests;
        }

        public void finalize() {
            ZenProperties.logger.log("POAImpl GC'd");
        }

    }

    class CreateReferenceWithObjectRunnable implements Runnable {
        public org.omg.CORBA.Object retVal;

        public FString ok;

        public String intf;

        public MemoryArea ma;

        public ORB orb;

        private POA poa;

        public int tcLen;

        public int threadPoolId;

        public int priorityModel;
        public short objectPriority;

        public CreateReferenceWithObjectRunnable() {
        }

        public void init(FString ok, String intf, MemoryArea ma, ORB orb, int threadPoolId,
                POA poa, int priorityModel , short objectPriority ) {
            this.ok = ok;
            this.intf = intf;
            this.ma = ma;
            this.orb = orb;
            this.poa = poa;
            this.tcLen = 0;
            this.threadPoolId = threadPoolId;
            this.priorityModel = priorityModel;
            this.objectPriority = objectPriority;
        }

        public void run() {
            try {
                retVal = edu.uci.ece.zen.orb.IOR.makeCORBAObject(orb, intf, ok, ma, poa, threadPoolId, priorityModel , objectPriority );
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, getClass(), "run", e);
            }
        }
    }

    class POAImplRunnable implements Runnable {
        private boolean active;

        private ScopedMemory sm;

        public POAImplRunnable(ScopedMemory sm) {
            active = true;
            this.sm = sm;
        }

        public boolean isActive() {
            return this.active;
        }

        public void setActive(boolean val) {
            this.active = val;
        }

        public void run() {
            if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("getting portal for: " + sm);
            if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("inner thread: " + Thread.currentThread().toString());

            POAImpl poaImpl = (POAImpl) sm.getPortal();
            if (ZenBuildProperties.dbgPOA) ZenProperties.logger.log("poa impl is " + poaImpl);
            synchronized (poaImpl) {
                try {
                    while (active) {
                        poaImpl.wait();
                    }
                } catch (InterruptedException ie) {
                    ZenProperties.logger.log(Logger.INFO, getClass(), "run", "ORB is shutting down.");
                }
                active = false;
            }
        }

    }


    // Validate threadpoolid in Init(...)
    // int parentDepth = RealtimeThread.getMemoryAreaStackDepth() - 1;
    // ScopedMemory orbImplMem = (ScopedMemory) RealtimeThread.getOuterMemoryArea(parentDepth);
    // boolean isThreadPoolIDValid;
    // try {
    //     orbImplMem.executeInArea(new Runnable() {
    //      public void run() {
    //              ScopedMemory mem = (ScopedMemory) RealtimeThread.getCurrentMemoryArea();
    //              RTORBImpl orbImpl = (RTORBImpl) mem.getPortal();
    //              isThreadPoolIDValid = orbImpl.validateThreadPoolID();
    //      }
    //   }
    //);
    //                } catch (InaccessibleAreaException e)
    //                {
    //
    //                }
    //
    //                if (isThreadPoolIDValid)
    //                {
    //                 this.threadPoolId = id;
    //                }
