package edu.uci.ece.zen.poa.mechanism;

import org.omg.CORBA.IntHolder;

public final class NonRetainStrategy extends
            ServantRetentionStrategy {

    /**
     * check if strategy same as this strategy
     * @param name strategy value
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public void validate(int name, IntHolder exceptionValue) 
     {
        if (ServantRetentionStrategy.NON_RETAIN != name) {
            exceptionValue.value = POARunnable.WrongPolicyException;
        }
        else
            exceptionValue.value = POARunnable.NoException;
    }

   /**
    * Return servant associated with AOM
    * @param ok edu.uci.ece.zen.poa.ObjectID
    * @return org.omg.PortableServer.Servant
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
    */

    public org.omg.PortableServer.Servant getServant(
            edu.uci.ece.zen.poa.ObjectID ok, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
    }

    /**
     * Return Objectid associated with the servant
     * @param servant org.omg.PortableServer.Servant
     * @return edu.uci.ece.zen.poa.ObjectID
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     * @throws org.omg.PortableServer.POAPackage.ServantNotActive
     */
    public edu.uci.ece.zen.poa.ObjectID getObjectID(
            org.omg.PortableServer.Servant servanti, IntHolder exceptionValue)
    {
         exceptionValue.value = POARunnable.WrongPolicyException;
    }

   /**
    * Add the ObjectId and servant to the AOM
    * @param ok
    * @param servant
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public void add(edu.uci.ece.zen.poa.ObjectID ok,
            edu.uci.ece.zen.poa.POAHashMap servant, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
    }

   /**
    * Check if servant is present in the AOM
    * @param servant org.omg.PortableServer.Servant
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public boolean servantPresent(org.omg.PortableServer.Servant servant, IntHolder exceptionValue)
   {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return null;
    }

   /**
    * Check if ObjectId is present
    * @param ok edu.uci.ece.zen.poa.ObjectID
    * @return boolean
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public boolean objectIDPresent(edu.uci.ece.zen.poa.ObjectID ok, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return null;
    }

   /**
    *
    * @param ok edu.uci.ece.zen.poa.ObjectID
    * @return POAHashMap
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public edu.uci.ece.zen.poa.POAHashMap getHashMap
            (edu.uci.ece.zen.poa.ObjectID ok, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return null;
    }

   /**
    * bind Demultiplexing Index
    * @param oid POAHashMap
    * @return int
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public int bindDemuxIndex(edu.uci.ece.zen.poa.POAHashMap oid, IntHolder exceptionValue
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return null;
    }

   /**
    * Remove demultiplexing index
    * @param oid ObjectId
    * @return boolean
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public boolean unbindDemuxIndex(edu.uci.ece.zen.poa.ObjectID oid, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return null;
    }

   /**
    * Return generation count 
    * @param index
    * @return int
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */
    public int getGenCount(int index, IntHolder exceptionValue)
     {
         exceptionValue.value = POARunnable.WrongPolicyException;
         return null;
    }

   /**
    * find object id in the AOM
    * @param id ObjectID
    * @return int
    * @throws org.omg.PortableServer.POAPackage.WrongPolicy
    */

    public int find(edu.uci.ece.zen.poa.ObjectID id, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return null;
    }

}

