/* --------------------------------------------------------------------------*
 * $Id: ActiveObjectMap.java,v 1.1 2003/11/26 22:26:17 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa;


/**
 * This class represents the ActiveObjectMap in the POA. The 
 * structure is as follows : The AOM is an array (implemented using
 * the Vector class) of AOMStructures. To facilitate the various x_to_y
 * methods in the POA, two hash tables map ObjectIDs, Servants into a HashTable.
 * Here are some of the design/implementation notes
 *
 * 1) ObjectIds (represented as a byte-array) are not used directly for
 * hashing because the equalsTo method on arrays compares the references
 * and not the contents.  Hence we wrap the byte-array into an ObjectID
 * that implements the equals-to method the right way.  THe equalTo and 
 * the hash code method are the one that is used by the HasTable class 
 * in Java for accessing hashed entries.  See testHash for details.
 * 2) The above is because ObjectIds coming n from remote clients will not
 * have the same hash-code even though the contents are the same.
 *
 * @author Arvind S. Krishna
 * @version 1.0
 * @see DualMap
 * @see SingleMap
 */

public interface ActiveObjectMap {
    
    /**
     * <code> add </code> to add the association between the ObjectId and the
     * Servant.
     * @param key ObjectID that wraps the ObjectId for the Servant
     * @param map Map entry asspciated with this Object Id
     */
    void add(byte[]key, POAHashMap map);

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
    byte[] getObjectID(org.omg.PortableServer.Servant st)
        throws org.omg.PortableServer.POAPackage.ServantNotActive; 

    /**
     * <code> getHashMap </code> retuns the appropriate HashMap associated
     * with the
     * ObjectID, Null if the ObjectID is not present.
     * @param ok ObjectID whose HashMap is required.
     * @return POAHashMap associated with the ObjectID, Null otherwise.
     */
    POAHashMap getHashMap(byte[] ok);

    /**
     * <code> getServant </code> returns the Servant associated with the ObjectID
     * , Null
     * if the ObjectID is not present in the AOM.
     * @param key the ObjectID associated with the Servant.
     * @return Servant if the key is associated with a Servant, null otherwise
     */
    org.omg.PortableServer.Servant getServant( byte[] key);

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

    boolean objectIDPresent(byte[] key);

    /**
     * <code> remove </code> removes the association between the ObjectID
     * and the Servant
     * in the AOM.
     * @param key key to be removed from the AOM.
     */

    void remove(Object key);

    /**
     * <code> elements </code> returns the Collections view of the AOM.
     * @return List of elements in the AOM.
     */

    java.util.Enumeration elements(); 

    /**
     * <code> destroyObjectID </code> is used to deactivate the ObjectID in the
     * AOM corresponding to the ObjectID specified. The AOM is searched for the
     * ObjectID and the corresponding ObjectID is deactivated.
     * If that  Object Key is servicing requests, then the Thread waits
     * for it to complete servicing the requests.
     * @param ok specifies the objectid that should be deactivated.
     */
    void destroyObjectID(byte[] ok);

    // --Class Paths for the Strings
    String DUAL_MAP = "edu.uci.ece.zen.poa.DualMap";
    String SINGLE_MAP = "edu.uci.ece.zen.poa.SingleMap";
}
