package edu.uci.ece.zen.poa;

import edu.uci.ece.zen.utils.*;
import org.omg.CORBA.IntHolder;

/**
 * Dual Map is used in the case where the POA has UNIQUE_ID Policy,
 * to make operations like servant_to_id and id_to_servant faster there
 * is a reverse map maintained from servant to ObjectID that helps in
 * retrieving the ObjectID associated with the servant.
 */
public class DualMap implements ActiveObjectMap {

    private Hashtable mapByObjectIDs;
    private Hashtable mapByServants;

    public DualMap(){
        mapByObjectIDs = new Hashtable();
        mapByServants = new Hashtable();
        mapByObjectIDs.init( 10 );
        mapByServants.init( 10 );
    }

    /**
     * This method adds the servant to the ActiveObjectMap.
     * @param key Object_id of the object.
     * @param map POAHashMap that contains the object.
     */
    public void add( FString key, POAHashMap map ) {
        mapByObjectIDs.put( key, map );
        mapByServants.put( map.getServant() , key );
    }


    /**
     * This method returns the ObjectID of the given servant.
     * @param st Seravnt.
     * @return ObjectID The objectid of the servant
     */
    public void getObjectID(org.omg.PortableServer.Servant st , FString objectId_out , IntHolder exceptionValue ){
        exceptionValue.value = POARunnable.NoException;
        objectId_out.append( (FString) mapByServants.get(st) );
    }

    // PRE CONDITION:
    // The presence of the ObjectID needs to be checked in the calee
    /**
     * This method returns the POAHashMap indicated by the ObjectID.
     * @param ok
     * @return POAHashMap
     */
    public POAHashMap getHashMap( FString ok ) {
        return (POAHashMap) this.mapByObjectIDs.get(ok);

    }

    /**
     * This method returns the Servant indicated by the ObjectID.
     * @param key
     * @return Servant
     */
    public org.omg.PortableServer.Servant getServant( FString key ) {
        return ((POAHashMap) mapByObjectIDs.get(key)).getServant();
    }

    /**
     * This method returns true if the Servant is present in the table else false.
     * @param st
     * @return boolean
     */
    public boolean servantPresent(org.omg.PortableServer.Servant st) {
        return mapByServants.get(st) != null;
    }

    /**
     * This method returns true if the ObjectID is present in the table else false.
     * @param key
     * @return boolean
     */
    public boolean objectIDPresent( FString key ) {
        return mapByObjectIDs.get(key) != null;
    }

    /**
     * This method removes the Object from the table.
     * @param key Object to be removed.
     */
    public void remove( Object key ) {
        if (key instanceof FString) {
            org.omg.PortableServer.Servant st = getServant((FString) key);

            mapByObjectIDs.remove((FString) key);
            mapByServants.remove(st);
        } else if (key instanceof org.omg.PortableServer.Servant) {
            //FString okey = null;
            //okey = getObjectID((org.omg.PortableServer.Servant) key);

            //mapByObjectIDs.remove(okey);
            //mapByServants.remove((org.omg.PortableServer.Servant) key);
        } else {
            //edu.uci.ece.zen.orb.Logger.error("Wrong key passed to HashTable.remove: " + key);
        }
        //KLUDGE: above
    }

    /**
     * This method destroys the object indicated by the ObjectID
     * @param ok ObjectID of the object to be destroyed.
     */
    public void destroyObjectID( FString ok) {
        if (this.mapByObjectIDs.get(ok) != null) {
            org.omg.PortableServer.Servant servant = (org.omg.PortableServer.Servant) this.mapByObjectIDs.get(ok);
            POAHashMap map = (POAHashMap) mapByServants.get(servant);
            if (map.servicingRequests()) {
                map.waitForDestruction();
            }
            this.remove(ok);
            // edu.uci.ece.zen.orb.Logger.debug("Removing the objectKey from the"
            // + "MapbyObjectIDs");
            this.mapByServants.remove(servant);
            // edu.uci.ece.zen.orb.Logger.debug("Removing Key from the Map");
        }
    }
}
