/* --------------------------------------------------------------------------*
 * $Id: DualMap.java,v 1.3 2004/03/11 19:31:34 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa;
import edu.uci.ece.zen.utils.*;
import javax.realtime.*;


public class DualMap implements ActiveObjectMap{

    private static String maxCapacityProperty = "poa.aom.size";
    private static String defaultMaxCapacity = "100";
    private static int maxCapacity;

    static {
        maxCapacity= Integer.parseInt(ZenProperties.getGlobalProperty( maxCapacityProperty, defaultMaxCapacity ));
    }

    Hashtable mapByObjectIDs;
    Hashtable mapByServants;

    DualMap(){
        super();
        mapByObjectIDs = new Hashtable();
        mapByServants = new Hashtable();

        mapByObjectIDs.init( DualMap.maxCapacity );
        mapByServants.init( DualMap.maxCapacity );
    }

    /**
     * This method adds the servant to the ActiveObjectMap.
     * @param key Object_id of the object.
     * @param map POAHashMap that contains the object.
     */
    public void add(byte[] key, POAHashMap map) throws HashtableOverflowException{
        mapByObjectIDs.put(key, map);
        mapByServants.put(map.getServantRegion(), key);
    }


    /**
     * This method returns the ObjectID of the given servant.
     * @param st Seravnt.
     * @return ObjectID The objectid of the servant
     */
    public byte[] getObjectID( ScopedMemory servantRegion ) throws org.omg.PortableServer.POAPackage.ServantNotActive {
        return (byte[]) mapByServants.get(servantRegion);
    }

    // PRE CONDITION:
    // The presence of the ObjectID needs to be checked in the calee
    /**
     * This method returns the POAHashMap indicated by the ObjectID.
     * @param ok
     * @return POAHashMap
     */
    public POAHashMap getHashMap(byte[] ok) {
        return (POAHashMap) mapByObjectIDs.get(ok);
    }

    /**
     * This method returns the Servant indicated by the ObjectID.
     * @param key
     * @return Servant
     */
    public ScopedMemory getServant(byte[] key) {
        return ((POAHashMap) mapByObjectIDs.get(key)).getServantRegion();
    }

    /**
     * This method returns true if the Servant is present in the table else false.
     * @param st
     * @return boolean
     */
    public boolean servantPresent( ScopedMemory st ) {
        return mapByServants.get(st) != null;
    }

    /**
     * This method returns true if the ObjectID is present in the table else false.
     * @param key
     * @return boolean
     */
    public boolean objectIDPresent(byte[] key) {
        return mapByObjectIDs.get(key) != null;
    }

    /**
     * This method removes the Object from the table.
     * @param key Object to be removed.
     */
    public void remove(Object key) {
        if (key instanceof byte[]) {
            ScopedMemory st = getServantRegion((byte[]) key);
            mapByObjectIDs.remove((byte[]) key);
            mapByServants.remove(st);
        } else if (key instanceof ScopedMemory ) {
            byte[] okey = null;

            try {
                okey = getObjectID((ScopedMemory)key);
            } catch (Exception ex) {}
            mapByObjectIDs.remove(okey);
            mapByServants.remove((org.omg.PortableServer.Servant) key);
        } else {
            ZenProperties.logger.log(
                    Logger.SEVERE,
                    "edu.uci.ece.zen.poa.DualMap",
                    "remove(...)",
                    "Wrong key passed to HashTable.remove");
        }
    }

    /**
     * This method destroys the object indicated by the ObjectID
     * @param ok ObjectID of the object to be destroyed.
     */
    public void destroyObjectID(byte[] ok) {
        if (this.mapByObjectIDs.get(ok) != null) {
            org.omg.PortableServer.Servant servant = (org.omg.PortableServer.Servant)
                this.mapByObjectIDs.get(ok);

            POAHashMap map = (POAHashMap) mapByServants.get(servant);

            if (map.servicingRequests()) {
                map.waitForDestruction();
            }

            remove(ok);
            remove(servant);
        }
    }
}
