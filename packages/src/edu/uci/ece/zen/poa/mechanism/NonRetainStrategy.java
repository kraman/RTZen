/* --------------------------------------------------------------------------*
 * $Id: NonRetainStrategy.java,v 1.1 2003/11/26 22:28:55 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa.mechanism;


public final class NonRetainStrategy extends
            ServantRetentionStrategy {

    /**
     * check if strategy same as this strategy
     * @param name strategy value
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public void validate(int name) throws
                org.omg.PortableServer.POAPackage.WrongPolicy {
        if (ServantRetentionStrategy.NON_RETAIN != name) {
            throw new org.omg.PortableServer.POAPackage.WrongPolicy();
        }
    }

   /**
    * Return servant associated with AOM
    * @param ok edu.uci.ece.zen.poa.ObjectID
    * @return org.omg.PortableServer.Servant
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
    */

    public org.omg.PortableServer.Servant getServant(
            byte[] ok)
        throws org.omg.PortableServer.POAPackage.ObjectNotActive,
                org.omg.PortableServer.POAPackage.WrongPolicy {
        throw new org.omg.PortableServer.POAPackage.WrongPolicy();
    }

    /**
     * Return Objectid associated with the servant
     * @param servant org.omg.PortableServer.Servant
     * @return edu.uci.ece.zen.poa.ObjectID
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     * @throws org.omg.PortableServer.POAPackage.ServantNotActive
     */
    public byte[] getObjectID(
            org.omg.PortableServer.Servant servant)
        throws org.omg.PortableServer.POAPackage.WrongPolicy,
                org.omg.PortableServer.POAPackage.ServantNotActive {
        throw new org.omg.PortableServer.POAPackage.WrongPolicy();
    }

   /**
    * Add the ObjectId and servant to the AOM
    * @param ok
    * @param servant
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public void add(byte[] ok,
            edu.uci.ece.zen.poa.POAHashMap servant)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
        throw new org.omg.PortableServer.POAPackage.WrongPolicy();
    }

   /**
    * Check if servant is present in the AOM
    * @param servant org.omg.PortableServer.Servant
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public boolean servantPresent(org.omg.PortableServer.Servant servant)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
        throw new org.omg.PortableServer.POAPackage.WrongPolicy();
    }

   /**
    * Check if ObjectId is present
    * @param ok edu.uci.ece.zen.poa.ObjectID
    * @return boolean
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public boolean objectIDPresent(byte[] ok) throws
                org.omg.PortableServer.POAPackage.WrongPolicy {
        throw new org.omg.PortableServer.POAPackage.WrongPolicy();
    }

   /**
    *
    * @param ok edu.uci.ece.zen.poa.ObjectID
    * @return POAHashMap
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public edu.uci.ece.zen.poa.POAHashMap getHashMap
            (byte[]  ok)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
        throw new org.omg.PortableServer.POAPackage.WrongPolicy();
    }

   /**
    * bind Demultiplexing Index
    * @param oid POAHashMap
    * @return int
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public int bindDemuxIndex(edu.uci.ece.zen.poa.POAHashMap oid) throws
                org.omg.PortableServer.POAPackage.WrongPolicy {
        throw new org.omg.PortableServer.POAPackage.WrongPolicy();
    }

   /**
    * Remove demultiplexing index
    * @param oid ObjectId
    * @return boolean
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public boolean unbindDemuxIndex(byte[]oid)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
        throw new org.omg.PortableServer.POAPackage.WrongPolicy();
    }

   /**
    * Return generation count 
    * @param index
    * @return int
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public int getGenCount(int index)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
        throw new org.omg.PortableServer.POAPackage.WrongPolicy();
    }

   /**
    * find object id in the AOM
    * @param id ObjectID
    * @return int
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */

    public int find(byte[] id) throws
                org.omg.PortableServer.POAPackage.WrongPolicy {
        throw new org.omg.PortableServer.POAPackage.WrongPolicy();
    }

}

