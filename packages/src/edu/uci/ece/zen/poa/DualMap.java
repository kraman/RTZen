/* --------------------------------------------------------------------------*
 * $Id: DualMap.java,v 1.3 2004/03/11 19:31:34 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa;


/**
 * Dual Map is used in the case where the POA has UNIQUE_ID Policy,
 * to make operations like servant_to_id and id_to_servant faster there
 * is a reverse map maintained from servant to ObjectID that helps in
 * retrieving the ObjectID associated with the servant.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 * @see SingleMap
 */

import edu.uci.ece.zen.utils.*;


public class DualMap implements ActiveObjectMap {

    private static int initialCapacity;
    private static String name = "poa.aom.size";
    public static int DEFAULT_CAPACITY = 100;

    static {
        DualMap.initialCapacity = Integer.parseInt(ZenProperties.getGloablProperty(DualMap.name, DEFAULT_CAPACITY));
    }
    /**
     * This method adds the servant to the ActiveObjectMap.
     * @param key Object_id of the object.
     * @param map POAHashMap that contains the object.
     */

    public void add(byte[] key, POAHashMap map) {
        mapByObjectIDs.put(key, map);
        mapByServants.put(map.getServant(), key);
    }


    /**
     * This method returns the ObjectID of the given servant.
     * @param st Seravnt.
     * @return ObjectID The objectid of the servant
     */

    public byte[] getObjectID(org.omg.PortableServer.Servant st)
        throws org.omg.PortableServer.POAPackage.ServantNotActive {
        return (byte[]) mapByServants.get(st);
    }

    // PRE CONDITION:
    // The presence of the ObjectID needs to be checked in the calee
    /**
     * This method returns the POAHashMap indicated by the ObjectID.
     * @param ok
     * @return POAHashMap
     */
    public POAHashMap getHashMap(byte[] ok) {
        return (POAHashMap) this.mapByObjectIDs.get(ok);

    }

    /**
     * This method returns the Servant indicated by the ObjectID.
     * @param key
     * @return Servant
     */
    public org.omg.PortableServer.Servant getServant(byte[] key) {
        return ((POAHashMap) mapByObjectIDs.get(key)).getServant();
    }

    /**
     * This method returns true if the Servant is present in the table else false.
     * @param st
     * @return boolean
     */
    public boolean servantPresent(org.omg.PortableServer.Servant st) {
        return mapByServants.containsKey(st);

        }

    /**
     * This method returns true if the ObjectID is present in the table else false.
     * @param key
     * @return boolean
     */
    public boolean objectIDPresent(byte[] key) {
        return mapByObjectIDs.containsKey(key);
    }

    /**
     * This method removes the Object from the table.
     * @param key Object to be removed.
     */
    public void remove(Object key) {
        if (key instanceof byte[]) {
            org.omg.PortableServer.Servant st = getServant((byte[]) key);


	    POAHashMap.enqueue( getHashMap( (byte[]) key) );
            mapByObjectIDs.remove((byte[]) key);
            mapByServants.remove(st);
        } else if (key instanceof org.omg.PortableServer.Servant) {
            byte[] okey = null;

            try {
                okey = getObjectID((org.omg.PortableServer.Servant) key);
            } catch (Exception ex) {}
	    POAHashMap.enqueue( getHashMap( (byte[]) okey) );
            mapByObjectIDs.remove(okey);
            mapByServants.remove((org.omg.PortableServer.Servant) key);
        } else {
            ZenProperties.logger.logp(
                Logger.SEVERE;
                "edu.uci.ece.zen.poa.DualMap",
                "remove(...)"
                "Wrong key passed to HashTable.remove");
        }
    }

    /**
     * This method lists all the servants present in the table.
     * @return java.util.Enumeration Enumeration of all the servants.
     */
    public java.util.Enumeration elements() {
        return mapByServants.elements();
    }

    /**
     * This method destroys the object indicated by the ObjectID
     * @param ok ObjectID of the object to be destroyed.
     */
    public void destroyObjectID(byte[] ok) {
        if (this.mapByObjectIDs.containsKey(ok)) {
            org.omg.PortableServer.Servant servant = (org.omg.PortableServer.Servant)
                    this.mapByObjectIDs.get(ok);

            POAHashMap map = (POAHashMap) mapByServants.get(servant);

            if (map.servicingRequests()) {
                map.waitForDestruction();
            }

            this.remove(ok);
            // edu.uci.ece.zen.orb.Logger.debug("Removing the objectKey from the"
            // + "MapbyObjectIDs");
            this.remove(servant);
            // edu.uci.ece.zen.orb.Logger.debug("Removing Key from the Map");

        }
    }

    /* ---------------- Private members ------------------------*/
    private java.util.Hashtable mapByObjectIDs = new java.util.Hashtable(DualMap.initialCapacity);
    private java.util.Hashtable mapByServants = new java.util.Hashtable(DualMap.initialCapacity);
}
