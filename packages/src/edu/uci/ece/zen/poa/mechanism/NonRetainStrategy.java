/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.poa.mechanism;

import org.omg.CORBA.IntHolder;

import edu.uci.ece.zen.poa.POARunnable;
import edu.uci.ece.zen.utils.FString;

public final class NonRetainStrategy extends ServantRetentionStrategy
{
    /**
     * check if strategy same as this strategy
     * 
     * @param name
     *            strategy value
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public void validate(int name, IntHolder exceptionValue)
    {
        if (ServantRetentionStrategy.NON_RETAIN != name)
        {
            exceptionValue.value = POARunnable.WrongPolicyException;
        } else
            exceptionValue.value = POARunnable.NoException;
    }

    /**
     * Return servant associated with AOM
     * 
     * @param ok
     *            edu.uci.ece.zen.poa.ObjectID
     * @return org.omg.PortableServer.Servant
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
     */

    public org.omg.PortableServer.Servant getServant(FString ok,
            IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return null;
    }

    /**
     * Return Objectid associated with the servant
     * 
     * @param servant
     *            org.omg.PortableServer.Servant
     * @return edu.uci.ece.zen.poa.ObjectID
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     * @throws org.omg.PortableServer.POAPackage.ServantNotActive
     */
    public void getObjectID(org.omg.PortableServer.Servant servant,
            FString oid_out, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
    }

    /**
     * Add the ObjectId and servant to the AOM
     * 
     * @param ok
     * @param servant
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public void add(FString ok, edu.uci.ece.zen.poa.POAHashMap servant,
            IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
    }

    /**
     * Check if servant is present in the AOM
     * 
     * @param servant
     *            org.omg.PortableServer.Servant
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public boolean servantPresent(org.omg.PortableServer.Servant servant,
            IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return false;
    }

    /**
     * Check if ObjectId is present
     * 
     * @param ok
     *            edu.uci.ece.zen.poa.ObjectID
     * @return boolean
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public boolean objectIDPresent(FString ok, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return false;
    }

    /**
     * @param ok
     *            edu.uci.ece.zen.poa.ObjectID
     * @return POAHashMap
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public edu.uci.ece.zen.poa.POAHashMap getHashMap(FString ok,
            IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return null;
    }

    /**
     * bind Demultiplexing Index
     * 
     * @param oid
     *            POAHashMap
     * @return int
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public int bindDemuxIndex(edu.uci.ece.zen.poa.POAHashMap oid,
            IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return -1;
    }

    /**
     * Remove demultiplexing index
     * 
     * @param oid
     *            ObjectId
     * @return boolean
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public boolean unbindDemuxIndex(FString oid, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return false;
    }

    /**
     * Return generation count
     * 
     * @param index
     * @return int
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public int getGenCount(int index, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return -1;
    }

    /**
     * find object id in the AOM
     * 
     * @param id
     *            ObjectID
     * @return int
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public int find(FString id, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.WrongPolicyException;
        return -1;
    }

}

