package edu.uci.ece.zen.poa.mechanism;


import edu.uci.ece.zen.poa.ActiveDemuxServantTable;
import org.omg.CORBA.IntHolder;


public final class RetainStrategy extends
            ServantRetentionStrategy {
    /**
     * Initialize the Retain Strategy
     * @param uniqueId IdUniquenessStrategy
     */
    public void initialize(IdUniquenessStrategy uniqueId) {
        if (uniqueId.validate(IdUniquenessStrategy.UNIQUE_ID)) {
            this.AOM = edu.uci.ece.zen.poa.DualMap();
        } else {
            this.AOM = edu.uci.ece.zen.poa.SingleMap();
        }
        // Active Demux Map
        this.activeMap = new ActiveDemuxServantTable();
    }

    /**
     * Return the AOM associated with the Retain-Strategy
     * @return edu.uci.ece.zen.poa.ActiveObjectMap
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */

    public edu.uci.ece.zen.poa.ActiveObjectMap getAOM()
   {
        return this.AOM;
    }

   /**
    * Return servant associated with the ObjectID
    * @param ok ObjectID
    * @return org.omg.PortableServer.Servant servant
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
    */
    public org.omg.PortableServer.Servant getServant(
            edu.uci.ece.zen.poa.ObjectID ok, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.NoException;

        org.omg.PortableServer.Servant servant;

            servant = this.AOM.getServant(ok, exceptionValue);
            if (exceptionValue.value != 0)
            {
                exceptionValue.value = POARunnable.ObjNotActiveException;
                return null;
            }

        return servant;
    }

   /**
    * Check if the strategy is same as this strategy
    * @param name  policy name
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public void validate(int name, IntHolder exceptionValue
    {
        if (ServantRetentionStrategy.RETAIN != name) {
            exceptionValue.value = POARunnable.WrongPolicyException;
        }
    }
   /**
    * return the object-id associated with the servant
    * @param servant org.omg.PortableServer.Servant
    * @return edu.uci.ece.zen.poa.ObjectID object-id
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    * @throws org.omg.PortableServer.POAPackage.ServantNotActive
    */

    public edu.uci.ece.zen.poa.ObjectID getObjectID(
            org.omg.PortableServer.Servant servant, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.NoException;

            this.AOM.servantPresent(servant, exceptionValue);
            if (exceptionValue.value == 0)
                return this.AOM.getObjectID(servant);
            else
            {
                exceptionValue.value = POARunnable.ServantNotActiveException;
                return null;
            }
    }

   /**
    * Associate the object-id with the POAHashMap in the AOM
    * @param ok ObjectID
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public void add(edu.uci.ece.zen.poa.ObjectID ok,
            edu.uci.ece.zen.poa.POAHashMap map)
    {
        this.AOM.add(ok, map);
    }

   /**
    * check if servant is presnt in the AOM
    * @param servant org.omg.PortableServer.Servant
    * @return boolean
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public boolean servantPresent(org.omg.PortableServer.Servant servant) 
    {
        return this.AOM.servantPresent(servant);
    }

   /**
    * Check if the Object id is present in the AOM
    * @param ok ObjectID
    * @return boolean
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public boolean objectIDPresent(edu.uci.ece.zen.poa.ObjectID ok) 
    {
        return this.AOM.objectIDPresent(ok);
    }

    /**
     * <code> deactivateObjectID </code> is used for the purpose of deactivation
     * of Object in the Active Object Map.
     * @param ok spoecifies the Object that needs to be deactivated in the AOM.
     * @exception org.omg.PortableServers.POAPackage.WrongPolicy is thrown if
     * this method is invoked on the Non Retain Strategy.
     */

    public void deactivateObjectID(edu.uci.ece.zen.poa.ObjectID ok)
    {
        edu.uci.ece.zen.poa.POAHashMap map = this.AOM.getHashMap(ok);

        if (map != null) {
            // edu.uci.ece.zen.orb.Logger.debug("Deactivating the Object Key in the HashMap");
            map.deactivate();
        }
    }

    /**
     * <code> destroyObjectID </code> is used to remove the ObjectID from the
     * AOM. This is different from deactivation in that the former just sets the
     * dirty bit in the ObjectID indicating that the ObjectID has been
     * deactivated but does not remove the association from the AOM.
     * This method is use for removing the association.
     *
     * @param ok specifies the Object that needs to be remvovd.
     * @exception org.omg.PortableServer.POAPackage.WrongPolicy is thrown if
     * invoked with the Non Retain Policy.
     */
    public void destroyObjectID(edu.uci.ece.zen.poa.ObjectID ok)
    {
        this.AOM.destroyObjectID(ok);
    }

   /**
    * Get Hashmap corresponding to the object id
    * @param ok ObjectID
    * @return edu.uci.ece.zen.poa.POAHashMap
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public edu.uci.ece.zen.poa.POAHashMap getHashMap(
            edu.uci.ece.zen.poa.ObjectID ok) {
        return this.AOM.getHashMap(ok);
    }

   /**
    * Bind demultiplexing indx
    * @param map POAHashMap
    * @return int slot where bound
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public int bindDemuxIndex(edu.uci.ece.zen.poa.POAHashMap map)
    {

        return this.activeMap.bind(map);
    }

   /**
    * Remove the object id from the index
    * @param oid ObjectID
    * @return boolean true if successful else false
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public boolean unbindDemuxIndex(edu.uci.ece.zen.poa.ObjectID oid)
   {
        return this.activeMap.unbind(oid);
    }

   /**
    * return generation count 
    * @param index
    * @return int count
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public int getGenCount(int index)
    {
        return this.activeMap.getGenCount(index);
    }

   /**
    * locate object id in the AOM
    * @param id ObjectID
    * @return int
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public int find(edu.uci.ece.zen.poa.ObjectID id)
    {
        return activeMap.find(id);
    }

    protected edu.uci.ece.zen.poa.ActiveObjectMap AOM;
    public ActiveDemuxServantTable activeMap;
}
