/* --------------------------------------------------------------------------*
 * $Id: ActiveObjectMap.java,v 1.4 2003/07/30 19:32:12 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa;

import edu.uci.ece.zen.utils.*;
import org.omg.CORBA.IntHolder;

/**
 * This class represents the ActiveObjectMap in the POA. The 
 * structure is as follows : The AOM is an array (implemented using
 * the Vector class) of AOMStructures. To facilitate the various x_to_y
 * methods in the POA, two hash tables map ObjectIDs, Servants into a HashTable.
 * Here are some of the design/implementation notes
 */
public interface ActiveObjectMap {
    /**
     * <code> add </code> to add the association between the ObjectId and the
     * Servant.
     * @param key ObjectID that wraps the ObjectId for the Servant
     * @param map Map entry asspciated with this Object Id
     */
    void add( FString key, POAHashMap map);

    /**
     * <code> getObjectID </code> returns the ObjectID associated with the
     * corresponding
     * servant. Exception is raised if there is no association present.
     * @param st whose corresponding ObjectID is required.
     * @return ObjectID if the Servant has an ObjectID in the AOM, Null
     * otherwise.
     * @throws org.omg.PortableServer.POAPackage.ServantNotActive if there
     * is only a single
     * Map present.
     */
    void getObjectID(org.omg.PortableServer.Servant st , FString objectId_out , IntHolder exceptionValue );

    /**
     * <code> getHashMap </code> retuns the appropriate HashMap associated
     * with the
     * ObjectID, Null if the ObjectID is not present.
     * @param ok ObjectID whose HashMap is required.
     * @return POAHashMap associated with the ObjectID, Null otherwise.
     */
    POAHashMap getHashMap( FString ok );

    /**
     * <code> getServant </code> returns the Servant associated with the ObjectID
     * , Null
     * if the ObjectID is not present in the AOM.
     * @param key the ObjectID associated with the Servant.
     * @return Servant if the key is associated with a Servant, null otherwise
     */
    org.omg.PortableServer.Servant getServant( FString key ); 

    /**
     * <code> servantPresent </code> checks if there is a Servant Associated
     * with the
     * corresponding ObjectID in the AOM.
     * @param st whose ObjectID is needed.
     * @return true if there is a Servant Present false otherwise.
     */
    boolean servantPresent(org.omg.PortableServer.Servant st);

    /**
     * <code> objectKeyPresent </code> checks if the ObjectID is present in the
     * AOM
     * @param key ObjectID whose presence is to be checked.
     * @return true if the ObjectID is present, false otherwise.
     */

    boolean objectIDPresent( FString key );

    /**
     * <code> remove </code> removes the association between the ObjectID
     * and the Servant
     * in the AOM.
     * @param key key to be removed from the AOM.
     */

    void remove( FString key );

    /**
     * <code> destroyObjectID </code> is used to deactivate the ObjectID in the
     * AOM corresponding to the ObjectID specified. The AOM is searched for the
     * ObjectID and the corresponding ObjectID is deactivated.
     * If that  Object Key is servicing requests, then the Thread waits
     * for it to complete servicing the requests.
     * @param ok specifies the objectid that should be deactivated.
     */
    void destroyObjectID( FString ok );
}
