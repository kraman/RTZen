/* --------------------------------------------------------------------------*
 * $Id: POAImpl.java,v 1.2 2004/02/25 06:12:43 nshankar Exp $
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
import edu.uci.ece.zen.orb.ior.IOR;
import edu.uci.ece.zen.orb.protocols.ProfileList;
import edu.uci.ece.zen.sys.ThreadFactory;
import edu.uci.ece.zen.util.ExecutorShutdownException;


public class POAImpl extends POA {

/*public POAImpl()
{
	super();
}*/

/*    public static org.omg.CORBA.Object instance(final edu.uci.ece.zen.orb.ORB orb)
        throws org.omg.PortableServer.POAPackage.InvalidPolicy {
        return new POA(orb, "RootPOA", "RootPOA", null, null, null);
    }

    protected POAImpl(final edu.uci.ece.zen.orb.ORB orb,
            final String poaName,
            final String poaPath,
            org.omg.CORBA.Policy[] policies,
            org.omg.PortableServer.POA parent,
            org.omg.PortableServer.POAManager manager)

        throws org.omg.PortableServer.POAPackage.InvalidPolicy {
        this.orb = orb;
        this.poaName = poaName;
        this.parent = (edu.uci.ece.zen.poa.POA) parent;
        this.poaPath = poaPath;

        if (policies != null) {
            this.policyList = new org.omg.CORBA.Policy[policies.length];
            for (int iterator = 0; iterator < policies.length; iterator++) {
                this.policyList[iterator] = policies[iterator].copy();
            }
        }

        if (manager == null) {
            this.poaManager = new edu.uci.ece.zen.poa.POAManager(orb);
        } else {
            this.poaManager = manager;
        }

        // register the POA with the POA Manager
        ((edu.uci.ece.zen.poa.POAManager) poaManager).register(this);

        this.serverRequestHandler = (POAServerRequestHandler) orb.requestHandler();
        // Get the index to the Active Demux Index
        this.poaDemuxIndex = serverRequestHandler.addPOA(poaPath, this);

        theChildren = new java.util.Hashtable();
        numberOfCurrentRequests = new SynchronizedInt();

        // --- Strategies ---
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
                this.threadPolicyStrategy);
        this.poaState = Util.CREATION_COMPLETE;
    }*/

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
        } catch (ExecutorShutdownException ex) {
            // -- have to send a request not handled to the client here
            // -- Throw a transient exception
            throw new org.omg.CORBA.TRANSIENT("Request cancelled:Executor shut down",
                    3, CompletionStatus.COMPLETED_NO);
        }
    }

    /**
     * Generates the object id for that particular servant.
     * @param p_servant The servant object.
     * @throws org.omg.PortableServer.POAPackage.ServantNotActive  If the servant passed in is not active.
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy If the policies of the POA policies do not  contain USE_DEFAULT_SERVANT policy or a            * combination of the RETAIN policy and UNIQUE_ID or IMPLICIT_ACTIVATION policies
     * @return the object id for that particular servant.
     */
    public byte[] servant_to_id(final org.omg.PortableServer.Servant p_servant)
        throws
                org.omg.PortableServer.POAPackage.ServantNotActive,
                org.omg.PortableServer.POAPackage.WrongPolicy {
        try {

            if (p_servant
                    == (org.omg.PortableServer.Servant)
                            this.requestProcessingStrategy.getRequestProcessor(edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy.DEFAULT_SERVANT)) {
                org.omg.PortableServer.Current current = (org.omg.PortableServer.Current) Current.currentInit();

                if (this.theServant == current.get_servant()) {
                    return current.get_object_id();
                }
            }
        } catch (org.omg.PortableServer.CurrentPackage.NoContext ex) {} catch (org.omg.PortableServer.POAPackage.ObjectNotActive ex) {
            throw sna;
        } catch (org.omg.PortableServer.POAPackage.WrongPolicy ex) {}
 byte[] ok = null;

        try {
            ok = this.retentionStrategy.getObjectID(p_servant);
	    return ok;
        } catch (org.omg.PortableServer.POAPackage.ServantNotActive ex) {
            // Creating a new Reference for this ObjectID

            if (this.uniquenessStrategy.validate(edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy.MULTIPLE_ID)
                    && this.activationStrategy.validate(edu.uci.ece.zen.poa.mechanism.ActivationStrategy.IMPLICIT_ACTIVATION)) {
	        ok =  ((this.idAssignmentStrategy.nextId()));
                POAHashMap map = new POAHashMap(ok, p_servant);

                this.retentionStrategy.add(ok, map);
		return ok;

            }
	    else { throw sna;
	         }
        }catch (Exception exc ) { throw sna;}



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
        ObjectKey okey = null;
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
                POAHashMap map = new POAHashMap(oid, p_servant);

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
     * Returns the servant object which is assocuated with that particular object reference.
     * @param reference The object reference object.
     * @throws org.omg.PortableServer.POAPackage.ObjectNotActive  If the object indicated by the reference passed in is not active.
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy If the policies of the POA do not contain RETAIN or USE_DEFAULT_SERVANTpolicies.
     * @throws org.omg.PortableServer.POAPackage.WrongAdapter If the object reference was not created by this POA, the WrongAdapter exception is
     * raised.
     * @return The servant object which is assocuated with that particular object reference.
     */

    public org.omg.PortableServer.Servant reference_to_servant(
            final org.omg.CORBA.Object reference)
        throws org.omg.PortableServer.POAPackage.ObjectNotActive,
                org.omg.PortableServer.POAPackage.WrongPolicy,
                org.omg.PortableServer.POAPackage.WrongAdapter {
        org.omg.PortableServer.Servant servant;

        try {
            this.retentionStrategy.validate(retentionStrategy.RETAIN);
            byte[] oid =  ( this.reference_to_id(reference) ) ;

            // Logger.debug("reference_to_servant: returning the servant in the AOM");
            return this.retentionStrategy.getServant(oid);
        } catch (Exception ex) {}

        // Logger.debug("reference2servant: Returning Default Servant");

        return (org.omg.PortableServer.Servant)
                this.requestProcessingStrategy.getRequestProcessor(edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy.DEFAULT_SERVANT);
    }
    /**
     * Returns the object id for the servant object associated with the reference.
     * @param reference The object reference object.
     * @exception org.omg.PortableServer.POAPackage.WrongAdapter If the object reference was not created by this POA, the WrongAdapter exception is raised.
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy To allow forther extensions.
     * @return The object id for the servant object associated with the reference.
     */

    public byte[] reference_to_id(final org.omg.CORBA.Object reference)
        throws
                org.omg.PortableServer.POAPackage.WrongAdapter,
                org.omg.PortableServer.POAPackage.WrongPolicy {
        ObjRefDelegate delegate = (ObjRefDelegate)
                ((org.omg.CORBA.portable.ObjectImpl) reference)._get_delegate();

        edu.uci.ece.zen.orb.ior.IOR ior = delegate.getIOR();
        edu.uci.ece.zen.orb.protocols.ProfileList list = ior.getProfileList();

        ObjectKey ok = list.getFirstObjectKey();

        if (ok.isPersistent()) {
            if (this.path_name().equals(ok.getPOAPathName())) {
                return ok.getId();
            }
        } else if (this.poaDemuxIndex.equals(ok.poaDemuxIndex())) {
            return ok.getId();
        }

        throw wa;
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

    /**
     * Returns the servant associated with the given object id
     * @param oid The object_is object.
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy If RETAIN policy is not present.
     * @throws org.omg.PortableServer.POAPackage.ObjectNotActive If the Object Id value is not active in the POA.
     * @return The servant associated with the given object id
     */

    public org.omg.CORBA.Object id_to_reference(final byte[] oid)
        throws
                org.omg.PortableServer.POAPackage.ObjectNotActive,
                org.omg.PortableServer.POAPackage.WrongPolicy {
        byte[] objID =  (oid);
        POAHashMap map = null;

        if (this.retentionStrategy.objectIDPresent(objID)) {
            map = this.retentionStrategy.getHashMap(objID);
        } else {
            throw ona;
        }

        // oid has already been activated : check if it is present
        // in the Active Demux Map.
        int index = this.retentionStrategy.find(objID);
        int genCount;
        ObjectKey ok = null;

        if (index == -1) {
            // Ceate an entry in the Demux Map
            index = this.retentionStrategy.bindDemuxIndex(map);
            genCount = this.retentionStrategy.getGenCount(index);
            ok = this.lifespanStrategy.create(this.path_name(), oid,
                    this.poaDemuxIndex, index, genCount);
        } else {
            genCount = this.retentionStrategy.getGenCount(index);
            ok = this.lifespanStrategy.create(this.path_name(), oid,
                    this.poaDemuxIndex, index, genCount);
        }

        ProfileList profiles = ((edu.uci.ece.zen.poa.POAManager) this.poaManager).getAcceptorRegistry().findMatchingProfiles(ok);
        // orb.getAcceptorRegistry().findMatchingProfiles(ok);
        org.omg.PortableServer.Servant st = map.getServant();

        String[] typeIDs = st._all_interfaces(this, oid);

        return edu.uci.ece.zen.orb.ior.IOR.makeCORBAObject(this.orb, profiles,
                st._all_interfaces(this, oid)[0]);
    }
    /**
     * Activates the oeject associated with the given servant and returns the object id of that object.
     * @param p_servant The servant object.
     * @zs org.omg.PortableServer.POAPackage.ServantAlreadyActive If the POA has the UNIQUE_ID policy and the specified servant is already in the Active Object Map.
     * @zs org.omg.PortableServer.POAPackage.WrongPolicy If SYSTEM_ID and RETAIN policies are not present in the POA.
     * @return the object id of that object.
     */

   public byte[] activate_object (org.omg.PortableServer.Servant p_servant)
        throws
	    org.omg.PortableServer.POAPackage.ServantAlreadyActive,
	    org.omg.PortableServer.POAPackage.WrongPolicy
    {
        try {
            this.retentionStrategy.getObjectID (p_servant);
            throw saa;
        }catch (org.omg.PortableServer.POAPackage.ServantNotActive ex) {
            byte[] oid =   ((this.idAssignmentStrategy.nextId()));
            POAHashMap map = new POAHashMap (oid, p_servant);
            this.retentionStrategy.add (oid, map);
            orb.set_delegate (p_servant);
            return oid;
        }
    }

    /**
     * Activates the oeject associated with the given servant and object id
     * @param id The object id object.
     * @param p_servant The servant object.
     * @throws org.omg.PortableServer.POAPackage.ServantAlreadyActive If the POA has the UNIQUE_ID policy and the specified servant is already in the Active Object Map.
     * @exception org.omg.PortableServer.POAPackage.ObjectAlreadyActive If the CORBA object indicated by the Object_Id is active in the POA.
     * @exception org.omg.PortableServer.POAPackage.WrongPolicy If RETAIN policy is not present in the POA.
     */

    public void activate_object_with_id(final byte[] id,
            final org.omg.PortableServer.Servant p_servant)
        throws
                org.omg.PortableServer.POAPackage.ServantAlreadyActive,
                org.omg.PortableServer.POAPackage.ObjectAlreadyActive,
                org.omg.PortableServer.POAPackage.WrongPolicy {
        byte[] oid =  (id);

        // get the Object Key corresponding to this ok in the AOM
        POAHashMap ok = this.retentionStrategy.getHashMap(oid);

        if (ok == null) {
            // Object is not active and hence we can activate !
            // //Logger.debug ("activate_object_with_id: creating new association" +
            // "between servant and id");
            this.retentionStrategy.add(oid, new POAHashMap(oid, p_servant));
            this.orb.set_delegate(p_servant);
            return;
        } else if (ok.isActive()) {
            throw oaa;
        } else {
            // Logger.debug("activate_object_with_id: waiting for okey destruction");
            ok.waitForDestruction();
        }

        POAHashMap map = new POAHashMap(oid, p_servant);

        this.retentionStrategy.add(oid, map);

    }
    /**
     * Deactivate the object associated with the given object id.
     *
     * @param oid The object id object.
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy If RETAIN policy is not present in the POA.
     * @throws org.omg.PortableServer.POAPackage.ObjectNotActive If the CORBA Object indicated by the Object_id is not active in the POA.
     */
    public void deactivate_object(byte[] oid)
        throws
                org.omg.PortableServer.POAPackage.ObjectNotActive,
                org.omg.PortableServer.POAPackage.WrongPolicy {

        final byte[] ok =  (oid);

        if (!this.retentionStrategy.objectIDPresent(ok)) {
            throw ona;
        }
        ((edu.uci.ece.zen.poa.mechanism.RetainStrategy)
                this.retentionStrategy).deactivateObjectID(ok);

        // Recycle the Active Demux Index
        this.retentionStrategy.unbindDemuxIndex(ok);

        final edu.uci.ece.zen.poa.POA poa = this;
        Runnable r1 = new Runnable() {
            public void run() {
                try {
                    org.omg.PortableServer.Servant servant = retentionStrategy.getServant(ok);

                    ((edu.uci.ece.zen.poa.mechanism.RetainStrategy)
                            retentionStrategy).destroyObjectID(ok);

                    if (requestProcessingStrategy instanceof
                            edu.uci.ece.zen.poa.mechanism.ServantActivatorStrategy) {
                        org.omg.PortableServer.ServantActivator activator = (org.omg.PortableServer.ServantActivator)
                                requestProcessingStrategy.getRequestProcessor(requestProcessingStrategy.SERVANT_ACTIVATOR);

                        activator.etherealize(ok, poa, servant, false,
                                true);
                    }
                } catch (Exception ex) {}
            }
        };

        ThreadFactory.createThread(r1).run();

    }

    public java.lang.String the_name() {
        return poaName;
    }
    /**
     * returns the POA's path right from the ROOTPOA
     * @return POA path.
     *
     */

    public java.lang.String path_name() {
        return poaPath;
    }

    /**
     * returns the POA's parent.
     * @return The POA's parent.
     *
     */

    /**
     * returns the POA's id.
     * @return The POA's id.
     *
     */
    public byte[] id() {
        long timeStamp = this.lifespanStrategy.timeStamp();
        StringBuffer buf = new StringBuffer();

        buf.append(timeStamp);
        buf.append("/");
        buf.append(this.poaPath);
        return buf.toString().getBytes();
    }
    /**
     * returns all the childPOAs of the current POA.
     * @return All the childPOAs of the current POA.
     *
     */


    /**
     * returns the POA's POAManager
     * @return The POA's POAManager
     *
     */

    public org.omg.PortableServer.POAManager the_POAManager() {
        return this.poaManager;
    }
    /**
     * returns the POA's AdapterActivator.
     * @return The POA's AdapterActivator.
     *
     */

    public org.omg.PortableServer.AdapterActivator the_activator() {
        return this.adapterActivator;

    }
    /**
     * Assigns the POA's AdapterActivator.
     *
     * @param the_activator AdapterActivator of the POA.
     */

    public void the_activator(
            final org.omg.PortableServer.AdapterActivator the_activator) {
        this.adapterActivator = the_activator;
    }
    /**
     * returns the POA's ServantManager.
     * @return The POA's ServantManager.
     *
     */

    public org.omg.PortableServer.ServantManager get_servant_manager()
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
        try {
            return
                    (org.omg.PortableServer.ServantManager)
                    this.requestProcessingStrategy.getRequestProcessor(edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy.SERVANT_MANAGER);
        } catch (org.omg.PortableServer.POAPackage.ObjectNotActive ex) {
            return null;
        }
    }
    /**
     * Assigns the POA's ServantManager.
     *
     * @param imgr The POA's servantmanager object.
     */

    public void set_servant_manager(
            final org.omg.PortableServer.ServantManager imgr)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
        this.requestProcessingStrategy.setInvokeHandler(imgr);
    }
    /**
     * returns the POA's defaultServant if one is present.
     * @return The POA's defaultServant.
     *
     */

    public org.omg.PortableServer.Servant get_servant()
        throws org.omg.PortableServer.POAPackage.NoServant,
                org.omg.PortableServer.POAPackage.WrongPolicy {
        try {
            return
                    (org.omg.PortableServer.Servant) this.requestProcessingStrategy.getRequestProcessor(edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy.DEFAULT_SERVANT);
        } catch (org.omg.PortableServer.POAPackage.ObjectNotActive ex) {
            throw ns;
        }

    }
    /**
     * Assigns the POA's defaultServant.
     *
     * @param p_servant The POA's defaultServant object.
     */

    public void set_servant(final org.omg.PortableServer.Servant p_servant)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
        this.requestProcessingStrategy.setInvokeHandler(p_servant);
    }

    // Using these methods can lead to the ObjectKey not having any hints for
    // the servant level demux
    //
    /**
     * This operation creates an object reference that encapsulates a POA-generated Object Id value and the specified interface repository id.
     *
     * @param intf The specified repository id, which may be a null string, will become the type_id of the generated object reference.
     * @return The reference to the object.
     */

    public org.omg.CORBA.Object create_reference(final String intf)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
        this.idAssignmentStrategy.validate
                (edu.uci.ece.zen.poa.mechanism.IdAssignmentStrategy.SYSTEM_ID);
        ObjectKey ok = lifespanStrategy.create(this.path_name(),
                this.idAssignmentStrategy.nextId(), this.poaDemuxIndex);

        return this.create_reference_with_object_key(ok, intf);
    }

    /**
     * This operation creates an object reference that encapsulates a POA-generated Object Id value and the specified interface repository id for that         * particular object indicated by the object id.
     *
     * @param intf The specified repository id, which may be a null string, will become the type_id of the generated object reference.
     * @param oid  The object id of the object.
     * @return The reference to the object.
     */
    public org.omg.CORBA.Object create_reference_with_id(final byte[] oid,
            final String intf) {
        String this_poa_name = this.path_name();
        ObjectKey ok = this.lifespanStrategy.create(this.path_name(), oid,
                this.poaDemuxIndex);

        if (!this.idAssignmentStrategy.verifyID(oid)) {
            throw new org.omg.CORBA.BAD_PARAM
                    (14, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }

        return this.create_reference_with_object_key(ok, intf);
    }
    /**
     * This operation returns the polict list for the current POA.
     * @return The polict list for the current POA.
     */

    public org.omg.CORBA.Policy[] policy_list() {
        return this.policyList;
    }

    protected void waitForCompletion() {
        // Logger.debug("Wait for completion" + this.the_name() + ":"
        // + numberOfCurrentRequests);
        numberOfCurrentRequests.waitForCompletion();

    }

    public boolean isDestructionApparent() {
        return (poaState > Util.DESTRUCTION_IN_PROGRESS) ? true : false;
    }

    /*protected final void removePOA(edu.uci.ece.zen.poa.POA poa) {
        this.theChildren.remove(poa.the_name());
    }*/
    /**
     * This method gets the ORB assocuated with the POA.
     * @return The ORB associated with the POA.
     */
    /*public final edu.uci.ece.zen.orb.ORB getORB() {
        return this.orb;
    }*/

    POA getChildPOA(String poaName) {
        POA child = (POA) this.theChildren.get(poaName.trim());

        if (child == null || child.isDestructionApparent()) {
            if (adapterActivator == null) {
                throw new org.omg.CORBA.OBJECT_NOT_EXIST("No adapter activator exists for "
                        + poaName);
            }
            if (((POAManager) poaManager).isDiscarding()) {
                throw new org.omg.CORBA.TRANSIENT("Parent is in discarding state");
            }

            if (((POAManager) poaManager).isInActive()) {
                throw new org.omg.CORBA.OBJ_ADAPTER("Parent POA inactive");
            }

            // one also needs to check for holding state and take action
            if (((POAManager) poaManager).isHolding()) {
                throw new org.omg.CORBA.TRANSIENT("Parent POA in holding state:Cannot Activate Child POA",
                        1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            }

            // The POA is active.. Serailize the calls if Single threaded
            this.threadPolicyStrategy.enter();
            boolean success = the_activator().unknown_adapter(this, poaName);

            this.threadPolicyStrategy.exit();

            if (success) {
                child = (POA) this.theChildren.get(poaName);
                if (child == null) {
                    throw new org.omg.CORBA.INTERNAL("unknown_adapter operation",
                            0, CompletionStatus.COMPLETED_NO);
                }
            }
            throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA activation failed");
        }

        return child;
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
            final ObjectKey ok,
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

        POAHashMap map = new POAHashMap(oID, p_servant);

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
