/* --------------------------------------------------------------------------*
 * $Id: SingleMap.java,v 1.1 2003/11/26 22:26:35 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa;


/**
 * <code> SingleMap </code> is the AOM associated with the POA that has a MULTIPLE_ID
 * policy. Since there could be multiple Ids that could be associated with one servant,
 * there in only a single HashMap from ObjectIDs to Servants. There is no reverse map
 * from Servants to ObjectIDs.
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 * @see DualMap
 */

import edu.uci.ece.zen.sys.ZenProperties;


public class SingleMap implements ActiveObjectMap {

    private static int initialCapacity;
    private static String name = "poa.aom.size";
    public static int DEFAULT_CAPACITY = 100;

    static {
        try {
            SingleMap.initialCapacity = Integer.parseInt
                    (ZenProperties.getProperty(SingleMap.name));
        } catch (Exception ex) {
            SingleMap.initialCapacity = SingleMap.DEFAULT_CAPACITY;
        }
    }

    /**
     * Map the object id and the POAHashMap in the AOM
     * @param key object id of the servant
     * @param map map representing the servant
     */
    public void add(byte[] key, POAHashMap map) {
        mapByObjectIDs.put(key, map);
    }

    /**
     * Reverse lookup is not possible as there is no one to one association
     * between the POA and the object id.
     * 
     * @param st org.omg.PortableServer.Servant
     * @return ObjectID
     * @throws org.omg.PortableServer.POAPackage.ServantNotActive
     */
    public byte[] getObjectID(org.omg.PortableServer.Servant st)
        throws org.omg.PortableServer.POAPackage.ServantNotActive {

        throw new org.omg.PortableServer.POAPackage.ServantNotActive();
    }

    /**
     * Return hashmpa correponding to the ObjectID
     * @param ok
     * @return POAHashMap
     */
    public POAHashMap getHashMap(byte[] ok) {
        if (this.mapByObjectIDs.containsKey(ok)) {
            return (POAHashMap) mapByObjectIDs.get(ok);
        }
        return null;
    }

    /**
     * Return servant corresponding to the ObjectID
     * @param key ObjectID
     * @return org.omg.PortableServer.Servant
     */
    public org.omg.PortableServer.Servant getServant(byte[] key) {
        return ((POAHashMap) mapByObjectIDs.get(key)).getServant();
    }

    /**
     * Check if servant is present. 
     * @param st org.omg.PortableServer.Servant
     * @return boolean false as reverse lookup is not possible.
     */

    public boolean servantPresent(org.omg.PortableServer.Servant st) {
        return false;
    }

    /**
     * Check if the given object id is present in the AOM
     * @param key ObjectID
     * @return boolean
     */
    public boolean objectIDPresent(byte[] key) {
        return mapByObjectIDs.containsKey(key);
    }

    /**
     * Remove entry from the AOM
     * @param key Object
     */
    public void remove(Object key) {
        if (key instanceof byte[]) {
            mapByObjectIDs.remove((byte[]) key);
        }
    }

    /**
     *
     * @return java.util.Enumeration
     */
    public java.util.Enumeration elements() {
        return mapByObjectIDs.elements();
    }
    /**
     * Remove ObjectID from the AOM
     * @param ok ObjectID
     */

    public void destroyObjectID(byte[] ok) {
        if (this.mapByObjectIDs.containsKey(ok)) {
            org.omg.PortableServer.Servant servant = (org.omg.PortableServer.Servant)
                    this.mapByObjectIDs.get(ok);

            POAHashMap map = (POAHashMap) mapByObjectIDs.get(servant);

            if (map.servicingRequests()) {
                map.waitForDestruction();
            }

            this.remove(ok);
        }
    }

    /* ---------------- Private members ------------------------*/
    private java.util.Hashtable mapByObjectIDs = new java.util.Hashtable(SingleMap.initialCapacity);
}
