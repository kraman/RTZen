package edu.uci.ece.zen.poa;

import org.omg.CORBA.IntHolder;

import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.Hashtable;

/**
 * <code> SingleMap </code> is the AOM associated with the POA that has a
 * MULTIPLE_ID policy. Since there could be multiple Ids that could be
 * associated with one servant, there in only a single HashMap from ObjectIDs to
 * Servants. There is no reverse map from Servants to ObjectIDs.
 * 
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna </a>
 * @version 1.0
 * @see DualMap
 */
public class SingleMap implements ActiveObjectMap {

    private Hashtable mapByObjectIDs;

    public SingleMap() {
        mapByObjectIDs = new Hashtable();
        mapByObjectIDs.init(10);
    }

    /**
     * Map the object id and the POAHashMap in the AOM
     * 
     * @param key
     *            object id of the servant
     * @param map
     *            map representing the servant
     */
    public void add(FString key, POAHashMap map) {
        mapByObjectIDs.put(key, map);
    }

    /**
     * Reverse lookup is not possible as there is no one to one association
     * between the POA and the object id.
     * 
     * @param st
     *            org.omg.PortableServer.Servant
     * @return ObjectID
     * @throws org.omg.PortableServer.POAPackage.ServantNotActive
     */
    public void getObjectID(org.omg.PortableServer.Servant st,
            FString objectId_out, IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.ServantNotActiveException;
    }

    /**
     * Return hashmpa correponding to the ObjectID
     * 
     * @param ok
     * @return POAHashMap
     */
    public POAHashMap getHashMap(FString ok) {
        return (POAHashMap) (this.mapByObjectIDs.get(ok));
    }

    /**
     * Return servant corresponding to the ObjectID
     * 
     * @param key
     *            ObjectID
     * @return org.omg.PortableServer.Servant
     */
    public org.omg.PortableServer.Servant getServant(FString key) {
        return ((POAHashMap) mapByObjectIDs.get(key)).getServant();
    }

    /**
     * Check if servant is present.
     * 
     * @param st
     *            org.omg.PortableServer.Servant
     * @return boolean false as reverse lookup is not possible.
     */

    public boolean servantPresent(org.omg.PortableServer.Servant st) {
        return false;
    }

    /**
     * Check if the given object id is present in the AOM
     * 
     * @param key
     *            ObjectID
     * @return boolean
     */
    public boolean objectIDPresent(FString key) {
        return mapByObjectIDs.get(key) != null;
    }

    /**
     * Remove entry from the AOM
     * 
     * @param key
     *            Object
     */
    public void remove(Object key) {
        if (key instanceof FString) {
            mapByObjectIDs.remove((FString) key);
        }
    }

    /**
     * Remove ObjectID from the AOM
     * 
     * @param ok
     *            ObjectID
     */
    public void destroyObjectID(FString ok) {
        if (this.mapByObjectIDs.get(ok) != null) {
            org.omg.PortableServer.Servant servant = (org.omg.PortableServer.Servant) this.mapByObjectIDs
                    .get(ok);
            POAHashMap map = (POAHashMap) mapByObjectIDs.get(servant);
            if (map.servicingRequests()) {
                map.waitForDestruction();
            }
            this.remove(ok);
        }
    }
}