/* --------------------------------------------------------------------------*
 * $Id: RetainStrategy.java,v 1.1 2003/11/26 22:28:59 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa.mechanism;

import edu.uci.ece.zen.poa.*;


public final class RetainStrategy extends
            ServantRetentionStrategy {
    /**
     * Initialize the Retain Strategy
     * @param uniqueId IdUniquenessStrategy
     */
    public void initialize(IdUniquenessStrategy uniqueId) {
        if (uniqueId.validate(IdUniquenessStrategy.UNIQUE_ID)) {
            this.AOM = (ActiveObjectMap) edu.uci.ece.zen.poa.POAPolicyFactory.createPolicy(edu.uci.ece.zen.poa.ActiveObjectMap.DUAL_MAP);
        } else {
            this.AOM = (ActiveObjectMap) edu.uci.ece.zen.poa.POAPolicyFactory.createPolicy(edu.uci.ece.zen.poa.ActiveObjectMap.SINGLE_MAP);
        }
        // Active Demux Map
        this.activeMap = new ActiveDemuxServantTable();
    }

    /**
     * Return the AOM associated with the Retain-Strategy
     * @return edu.uci.ece.zen.poa.ActiveObjectMap
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */

    public edu.uci.ece.zen.poa.ActiveObjectMap getAOM() throws
                org.omg.PortableServer.POAPackage.WrongPolicy {
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
            byte[] ok)
        throws org.omg.PortableServer.POAPackage.ObjectNotActive,
                org.omg.PortableServer.POAPackage.WrongPolicy {

        org.omg.PortableServer.Servant servant;

        try {
            servant = this.AOM.getServant(ok);
        } catch (Exception ex) {
            throw new org.omg.PortableServer.POAPackage.ObjectNotActive();
        }

        return servant;
    }

   /**
    * Check if the strategy is same as this strategy
    * @param name  policy name
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public void validate(int name) throws
                org.omg.PortableServer.POAPackage.WrongPolicy {
        if (ServantRetentionStrategy.RETAIN != name) {
            throw new org.omg.PortableServer.POAPackage.WrongPolicy();
        }
    }

   /**
    * return the object-id associated with the servant
    * @param servant org.omg.PortableServer.Servant
    * @return edu.uci.ece.zen.poa.ObjectID object-id
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    * @throws org.omg.PortableServer.POAPackage.ServantNotActive
    */
    public int getObjectID( org.omg.PortableServer.Servant servant ,  FString oid_out )
            throws org.omg.PortableServer.POAPackage.WrongPolicy, org.omg.PortableServer.POAPackage.ServantNotActive 
    {
        try {
            if (this.AOM.servantPresent(servant)) {
                return this.AOM.getObjectID(servant);
            }
        } catch (Exception ex) {}
        throw new org.omg.PortableServer.POAPackage.ServantNotActive();
    }

   /**
    * Associate the object-id with the POAHashMap in the AOM
    * @param ok ObjectID
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public void add(byte[] ok,
            edu.uci.ece.zen.poa.POAHashMap map)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
        this.AOM.add(ok, map);
    }

   /**
    * check if servant is presnt in the AOM
    * @param servant org.omg.PortableServer.Servant
    * @return boolean
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public boolean servantPresent(org.omg.PortableServer.Servant servant) throws
                org.omg.PortableServer.POAPackage.WrongPolicy {
        return this.AOM.servantPresent(servant);
    }

   /**
    * Check if the Object id is present in the AOM
    * @param ok ObjectID
    * @return boolean
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public boolean objectIDPresent(byte[] ok) throws
                org.omg.PortableServer.POAPackage.WrongPolicy {
        return this.AOM.objectIDPresent(ok);
    }

    /**
     * <code> deactivateObjectID </code> is used for the purpose of deactivation
     * of Object in the Active Object Map.
     * @param ok spoecifies the Object that needs to be deactivated in the AOM.
     * @exception org.omg.PortableServers.POAPackage.WrongPolicy is thrown if
     * this method is invoked on the Non Retain Strategy.
     */

    public void deactivateObjectID(byte[] ok) throws
                org.omg.PortableServer.POAPackage.WrongPolicy {
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
    public void destroyObjectID(byte[] ok) throws
                org.omg.PortableServer.POAPackage.WrongPolicy {
        this.AOM.destroyObjectID(ok);
    }

   /**
    * Get Hashmap corresponding to the object id
    * @param ok ObjectID
    * @return edu.uci.ece.zen.poa.POAHashMap
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public edu.uci.ece.zen.poa.POAHashMap getHashMap(
            byte[] ok) {
        return this.AOM.getHashMap(ok);
    }

   /**
    * Bind demultiplexing indx
    * @param map POAHashMap
    * @return int slot where bound
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public int bindDemuxIndex(edu.uci.ece.zen.poa.POAHashMap map) throws
                org.omg.PortableServer.POAPackage.WrongPolicy {

        return this.activeMap.bind(map);
    }

   /**
    * Remove the object id from the index
    * @param oid ObjectID
    * @return boolean true if successful else false
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public boolean unbindDemuxIndex(byte[] oid)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
        return this.activeMap.unbind(oid);
    }

   /**
    * return generation count 
    * @param index
    * @return int count
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public int getGenCount(int index)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
        return this.activeMap.getGenCount(index);
    }

   /**
    * locate object id in the AOM
    * @param id ObjectID
    * @return int
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public int find(byte[] id) throws
                org.omg.PortableServer.POAPackage.WrongPolicy {
        return activeMap.find(id);
    }

    protected edu.uci.ece.zen.poa.ActiveObjectMap AOM;
    public ActiveDemuxServantTable activeMap;
}
