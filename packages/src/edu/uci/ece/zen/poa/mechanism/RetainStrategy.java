package edu.uci.ece.zen.poa.mechanism;

import org.omg.CORBA.IntHolder;

import edu.uci.ece.zen.poa.DualMap;
import edu.uci.ece.zen.poa.POARunnable;
import edu.uci.ece.zen.poa.SingleMap;
import edu.uci.ece.zen.utils.ActiveDemuxTable;
import edu.uci.ece.zen.utils.FString;

/**
 * Implements the strategy corresponding to the CORBA retain policy. 
 */
public final class RetainStrategy extends ServantRetentionStrategy {

    protected edu.uci.ece.zen.poa.ActiveObjectMap AOM;

    public ActiveDemuxTable activeMap;
    
    /**
     * Initialize this strategy
     * @param uniqueId the ID uniqueness strategy
     */
    public void initialize(IdUniquenessStrategy uniqueId) 
    {
        if (uniqueId.validate(IdUniquenessStrategy.UNIQUE_ID)) 
        {
            this.AOM = new DualMap();
        } 
        else 
        {
            this.AOM = new SingleMap();
        }

        // Active Demux Map
        this.activeMap = new ActiveDemuxTable();
        this.activeMap.init(100);
    }

    /**
     * Return the AOM associated with the Retain-Strategy
     * 
     * @return edu.uci.ece.zen.poa.ActiveObjectMap
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */

    public edu.uci.ece.zen.poa.ActiveObjectMap getAOM() {
        return this.AOM;
    }

    /**
     * Return servant associated with the ObjectID
     * 
     * @param ok
     *            ObjectID
     * @return org.omg.PortableServer.Servant servant
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
     */
    public org.omg.PortableServer.Servant getServant(FString ok,
            IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        org.omg.PortableServer.Servant servant;
        servant = this.AOM.getServant(ok);
        return servant;
    }

    /**
     * Check if the strategy is same as this strategy
     * 
     * @param name
     *            policy name
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public void validate(int name, IntHolder exceptionValue) {
        if (ServantRetentionStrategy.RETAIN != name) {
            exceptionValue.value = POARunnable.WrongPolicyException;
        }
    }

    /**
     * return the object-id associated with the servant
     * 
     * @param servant
     *            org.omg.PortableServer.Servant
     * @return edu.uci.ece.zen.poa.ObjectID object-id
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     * @throws org.omg.PortableServer.POAPackage.ServantNotActive
     */

    public void getObjectID(org.omg.PortableServer.Servant servant,
            FString oid_out, IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        oid_out.reset();
        if (this.AOM.servantPresent(servant)) this.AOM.getObjectID(servant,
                oid_out, exceptionValue);
        else exceptionValue.value = POARunnable.ServantNotActiveException;
    }

    /**
     * Associate the object-id with the POAHashMap in the AOM
     * 
     * @param ok
     *            ObjectID
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public void add(FString ok, edu.uci.ece.zen.poa.POAHashMap map,
            IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        this.AOM.add(ok, map);
    }

    /**
     * check if servant is presnt in the AOM
     * 
     * @param servant
     *            org.omg.PortableServer.Servant
     * @return boolean
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public boolean servantPresent(org.omg.PortableServer.Servant servant,
            IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        return this.AOM.servantPresent(servant);
    }

    /**
     * Check if the Object id is present in the AOM
     * 
     * @param ok
     *            ObjectID
     * @return boolean
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public boolean objectIDPresent(FString ok, IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        return this.AOM.objectIDPresent(ok);
    }

    /**
     * <code> deactivateObjectID </code> is used for the purpose of deactivation
     * of Object in the Active Object Map.
     * 
     * @param ok
     *            spoecifies the Object that needs to be deactivated in the AOM.
     * @exception org.omg.PortableServers.POAPackage.WrongPolicy
     *                is thrown if this method is invoked on the Non Retain
     *                Strategy.
     */

    public void deactivateObjectID(FString ok) {
        edu.uci.ece.zen.poa.POAHashMap map = this.AOM.getHashMap(ok);

        if (map != null) {
            // edu.uci.ece.zen.orb.Logger.debug("Deactivating the Object Key in
            // the HashMap");
            map.deactivate();
        }
    }

    /**
     * <code> destroyObjectID </code> is used to remove the ObjectID from the
     * AOM. This is different from deactivation in that the former just sets the
     * dirty bit in the ObjectID indicating that the ObjectID has been
     * deactivated but does not remove the association from the AOM. This method
     * is use for removing the association.
     * 
     * @param ok
     *            specifies the Object that needs to be remvovd.
     * @exception org.omg.PortableServer.POAPackage.WrongPolicy
     *                is thrown if invoked with the Non Retain Policy.
     */
    public void destroyObjectID(FString ok) {
        this.AOM.destroyObjectID(ok);
    }

    /**
     * Get Hashmap corresponding to the object id
     * 
     * @param ok
     *            ObjectID
     * @return edu.uci.ece.zen.poa.POAHashMap
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public edu.uci.ece.zen.poa.POAHashMap getHashMap(FString ok,
            IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        return this.AOM.getHashMap(ok);
    }

    /**
     * Bind demultiplexing indx
     * 
     * @param map
     *            POAHashMap
     * @return int slot where bound
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public int bindDemuxIndex(edu.uci.ece.zen.poa.POAHashMap map,
            IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        return this.activeMap.bind(map.objectID(), map);
    }

    /**
     * Remove the object id from the index
     * 
     * @param oid
     *            ObjectID
     * @return boolean true if successful else false
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public boolean unbindDemuxIndex(FString oid, IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        this.activeMap.unbind(activeMap.find(oid));
        return true;
    }

    /**
     * return generation count
     * 
     * @param index
     * @return int count
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public int getGenCount(int index, IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        return this.activeMap.getGenCount(index);
    }

    /**
     * locate object id in the AOM
     * 
     * @param id
     *            ObjectID
     * @return int
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public int find(FString id, IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        return activeMap.find(id);
    }

}