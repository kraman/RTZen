/* --------------------------------------------------------------------------*
 * $Id: POAImpl.java,v 1.4 2004/03/11 19:31:34 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa;


/**
 * The class <code>POA</code> is ZEN specific implementation of the
 * CORBA Portable Object Adapter
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */

// --- OMG Imports---
import org.omg.CORBA.CompletionStatus;

import javax.realtime.*;

import edu.uci.ece.zen.orb.ObjRefDelegate;
import edu.uci.ece.zen.orb.ServerRequest;
import edu.uci.ece.zen.orb.IOR;
import edu.uci.ece.zen.orb.protocols.ProfileList;
import edu.uci.ece.zen.sys.ThreadFactory;
//import edu.uci.ece.zen.util.ExecutorShutdownException;


public class POAImpl extends POA {

     public void setParameters(final edu.uci.ece.zen.orb.ORB orb, final String poaName,final String poaPath, org.omg.CORBA.Policy[] policies, org.omg.PortableServer.POAManager manager, POAServerRequestHandler serverRequestHandler, ActiveDemuxLoc poaDemuxIndex,SynchronizedInt numberOfCurrentRequests, MemoryArea thisMemory)
     {
        System.out.println("OK this is the realtime POA");
     	this.poaId = 1;
     	this.orb = orb;
        this.poaName = poaName;
        this.poaPath = poaPath;
        this.policyList = policies;
        this.poaManager = manager;
        this.serverRequestHandler = serverRequestHandler;
        this.poaDemuxIndex = poaDemuxIndex;
        this.numberOfCurrentRequests = numberOfCurrentRequests;
        this.thisMemory=thisMemory;
        POAHashMap.init();


        // --- Strategies ---
        thisMemory.enter( new Runnable() { public void run() {
        					strategyInit();
                                                } });

    }


 	private void strategyInit()
        {
        try{
        this.threadPolicyStrategy = edu.uci.ece.zen.poa.mechanism.ThreadPolicyStrategy.init(policyList);
        this.idAssignmentStrategy = edu.uci.ece.zen.poa.mechanism.IdAssignmentStrategy.init(policyList);
        this.uniquenessStrategy = edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy.init(policyList);
        this.retentionStrategy = edu.uci.ece.zen.poa.mechanism.ServantRetentionStrategy.init(policyList,
                this.uniquenessStrategy);
        this.lifespanStrategy = edu.uci.ece.zen.poa.mechanism.LifespanStrategy.init(this.policyList);
        this.activationStrategy = edu.uci.ece.zen.poa.mechanism.ActivationStrategy.init(this.policyList,
                this.idAssignmentStrategy, this.retentionStrategy);
        this.requestProcessingStrategy = edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy.init(this.policyList,
                this.retentionStrategy, this.uniquenessStrategy,
                this.threadPolicyStrategy); }
                catch( Exception e) { throw new RuntimeException(); }

        }
    public void handleRequest(final ServerRequest req) {
        // check for the state of the poa? if it is discarding then throw the
        // transient exception...
        validateProcessingState();
        // Check the state of the POAManager. Here the POA is in active state
        POAManager.checkPOAManagerState(this.poaManager);
        // check if the POA has the persistent policy/or the transient
        this.lifespanStrategy.validate(req.getObjectKey());
        req.setHandlers(this, numberOfCurrentRequests,
                this.requestProcessingStrategy);
        try {
            this.orb.getTPHandler(poaId).execute(req);
        } catch (Exception ex) {
            // -- have to send a request not handled to the client here
            // -- Throw a transient exception
            throw new org.omg.CORBA.TRANSIENT("Request cancelled:Executor shut down",
                    3, CompletionStatus.COMPLETED_NO);
        }
    }
    /**
     * Generates the object reference for that particular servant.
     * @param p_servant The servant object.
     * @throws org.omg.PortableServer.POAPackage.ServantNotActive  If the servant passed in is not active.
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy If the policies of the POA dont contain RETAIN and UNIQUE_ID/MYULTIPLE_ID policies.
     * @return The object reference for that particular servant.

     */

    public org.omg.CORBA.Object servant_to_reference(
            final org.omg.PortableServer.Servant p_servant)
        throws
                org.omg.PortableServer.POAPackage.ServantNotActive,
                org.omg.PortableServer.POAPackage.WrongPolicy {

        //check if this method is being called as a part of an upcall
        org.omg.PortableServer.Current current = null;
        try {
            current = (org.omg.PortableServer.Current) Current.currentInit();
            if (p_servant == current.get_servant()) {
                return current.get_reference();
            }

        } catch (org.omg.PortableServer.CurrentPackage.NoContext ex) {
            //KLUDGE:
            //TODO: Check what is supposed to happen here
            //ex.printStackTrace();
        }

        // method was invoked outside the invocation context: Check if the
        // POA has the RETAIN and UNIQUE_ID in place.
        //NOTE: A ServantNotActive exception was being squelched here.
        byte[]  okey = null;
        byte[] oid = null;

        try{
            oid = this.retentionStrategy.getObjectID(p_servant);
            POAHashMap map = this.retentionStrategy.getHashMap(oid);

            int index = this.retentionStrategy.bindDemuxIndex(map);
            int count = this.retentionStrategy.getGenCount(index);

            // Create the Object Key using the IdHint Strategy
            okey = this.lifespanStrategy.create(this.poaPath, oid,
                    this.poaDemuxIndex, index, count);

            return this.create_reference_with_object_key
                    (okey, p_servant._all_interfaces(this, null)[0]);
        }catch( org.omg.PortableServer.POAPackage.ServantNotActive sna ){
            // Servant is not present and the POA has retain policy: Check if
            // Multiple Id and Implicit Activation are permitted next
            if (this.activationStrategy.validate(edu.uci.ece.zen.poa.mechanism.ActivationStrategy.IMPLICIT_ACTIVATION)
                    || this.uniquenessStrategy.validate
                            (edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy.MULTIPLE_ID)) {
                oid =  ((this.idAssignmentStrategy.nextId()));
                POAHashMap map = POAHashMap.initialize(oid, p_servant);

                this.retentionStrategy.add(oid, map);
                orb.set_delegate (p_servant);

                int index = this.retentionStrategy.bindDemuxIndex(map);
                int genCount = this.retentionStrategy.getGenCount(index);

                // Logger.debug("Servant to ref: serv demux loc: index = " + index
                // + " loc = " + genCount);
                //ActiveDemuxLoc servLoc = new ActiveDemuxLoc(index, genCount);

                okey = this.lifespanStrategy.create(this.poaPath, oid,
                        this.poaDemuxIndex, index,genCount);

                return this.create_reference_with_object_key(okey,
                        p_servant._all_interfaces(this, null)[0]);

            }
            throw sna;
        }

    }
    /**
     * Returns the servant associated with the given object id
     * @param oid The object_is object.
     * @throws org.omg.PortableServer.POAPackage.ObjectNotActive If object indicated by the object_is is not active or the POA has the                      * USE_DEFAULT_SERVANT policy and no default servant has  been registered with the POA this exception is raised.
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy If RETAIN policy nor the USE_DEFAULT_SERVANT policyis present this exception is raised.
     * @return The servant associated with the given object id
     */

    public org.omg.PortableServer.Servant id_to_servant(final byte[] oid)
        throws
                org.omg.PortableServer.POAPackage.ObjectNotActive,
                org.omg.PortableServer.POAPackage.WrongPolicy {
        try {
            byte[] id =  (oid);

            return this.retentionStrategy.getServant(id);
        } catch (Exception ex) {}

        return (org.omg.PortableServer.Servant)
                this.requestProcessingStrategy.getRequestProcessor(edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy.DEFAULT_SERVANT);
    }


    public boolean isDestructionApparent() {
        return (poaState > Util.DESTRUCTION_IN_PROGRESS) ? true : false;
    }

    protected void validateProcessingState() {

        switch (this.processingState) {

        case Util.DISCARDING:
            throw new org.omg.CORBA.TRANSIENT("Destruction of the POA in progress",
                    1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);

        case Util.INACTIVE:
            throw new org.omg.CORBA.OBJ_ADAPTER("POA Manager associated with the POA is Inactive",
                    1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }
    }

    org.omg.CORBA.Object create_reference_with_object_key(
            final byte[]  ok,
            final String intf) {
        edu.uci.ece.zen.orb.protocols.ProfileList list = ((edu.uci.ece.zen.poa.POAManager) this.poaManager).getAcceptorRegistry().findMatchingProfiles(ok);
        // this.orb.getAcceptorRegistry().findMatchingProfiles (ok);

        IOR ior = new IOR(list, intf);

        return edu.uci.ece.zen.orb.ior.IOR.makeCORBAObject(this.orb, ior);
    }

    private void activate_object_with_id_and_return_contents(
            final org.omg.PortableServer.Servant p_servant,
            final byte[] oid)
        throws org.omg.PortableServer.POAPackage.ServantAlreadyActive,
                org.omg.PortableServer.POAPackage.WrongPolicy {
        if (this.retentionStrategy.servantPresent(p_servant)
                && this.uniquenessStrategy.validate(edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy.UNIQUE_ID)) {

            throw saa;
        }

        byte[] oID =  (oid);

        POAHashMap map = POAHashMap.initialize(oID, p_servant);

        this.retentionStrategy.add(oID, map);
    }

    public edu.uci.ece.zen.poa.mechanism.ThreadPolicyStrategy getThreadPolicyStrategy()
    {
    	return this.threadPolicyStrategy;
    }

    public SynchronizedInt getnumberOfCurrentRequests()
    {
    	return numberOfCurrentRequests;
    }

    // //////////////////////////////////////////////////////////////////
    // ////                   DATA MEMBERS                         /////
    // /////////////////////////////////////////////////////////////////
   // -- Exceptions--
   private org.omg.PortableServer.POAPackage.NoServant ns = new org.omg.PortableServer.POAPackage.NoServant();
   private org.omg.PortableServer.POAPackage.ServantAlreadyActive saa = new org.omg.PortableServer.POAPackage.ServantAlreadyActive();
  private org.omg.PortableServer.POAPackage.ObjectAlreadyActive oaa = new org.omg.PortableServer.POAPackage.ObjectAlreadyActive();
   private org.omg.PortableServer.POAPackage.ObjectNotActive ona = new org.omg.PortableServer.POAPackage.ObjectNotActive();
   private org.omg.PortableServer.POAPackage.ServantNotActive sna = new org.omg.PortableServer.POAPackage.ServantNotActive();
   private org.omg.PortableServer.POAPackage.WrongAdapter wa = new org.omg.PortableServer.POAPackage.WrongAdapter();

    // -- ZEN ORB ---
    MemoryArea thisMemory;
    private edu.uci.ece.zen.orb.ORB orb;

    // --- POA Names relative and Complete Path Names ---
    private int poaId;
    private String poaName;
    private String poaPath;

    // --- POA Specific references ---
    private POA                                     parent;
    private java.util.Hashtable                     theChildren;
    private org.omg.PortableServer.POAManager       poaManager;
    private org.omg.PortableServer.AdapterActivator adapterActivator;
    private org.omg.PortableServer.ServantManager   theServantManager;
    private org.omg.PortableServer.Servant          theServant = null;
    private POAServerRequestHandler                 serverRequestHandler;

    // ---  Current Number of request executing in the POA ---
    private SynchronizedInt numberOfCurrentRequests;

    // --- Mutexes POA and varable specific to the create and destroy ops ---
    private Object createDestroyPOAMutex = new byte[0];

    private boolean disableCreatePOA = false;
    private boolean etherealize;

    // -- Policy List for the POA
    private org.omg.CORBA.Policy[] policyList;

    // -- Index into the Active Demux Map
    private ActiveDemuxLoc poaDemuxIndex;

    // --- State of the POA ---
    private int poaState;
    private int processingState = Util.ACTIVE; // RequestProcessing state

    // --- POA Specific strategies ----
    private transient edu.uci.ece.zen.poa.mechanism.LifespanStrategy
            lifespanStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.ThreadPolicyStrategy
            threadPolicyStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.IdAssignmentStrategy
            idAssignmentStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.ServantRetentionStrategy
            retentionStrategy;

    public transient edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy
            requestProcessingStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy
            uniquenessStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.ActivationStrategy
            activationStrategy;
}
