package edu.uci.ece.zen.poa.mechanism;

import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.poa.*;

/**
 * The class <code>ServantRetentionStrategy</code> creates a POA with  
 * either the Retain/Non Retain Strategies based on the policies of the POA
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */
public abstract class ServantRetentionStrategy {

    // --- Strategy Types ----
    public static final int RETAIN = 0;
    public static final int NON_RETAIN = 1;

    /**
     * <code> init </code> builds the appropriate RetentionStrategy based on the
     * Policy of the POA.
     * @param policy is the policy of the POA based on which the Strategies are
     *               constructed.
     * @exception org.omg.PoratableServer.POAPackage.InvalidPolicy if the
     * policies of the POA are in conflict.
     */
    public static ServantRetentionStrategy init( org.omg.CORBA.Policy[] policy, IdUniquenessStrategy uniquenessStrategy, org.omg.CORBA.IntHolder ih ) {
        if (PolicyUtils.useRetainPolicy(policy)) {
//            RetainStrategy retain = new RetainStrategy();
//            retain.initialize(uniquenessStrategy , ih );
//            return retain;
        } else {
//            return new NonRetainStrategy();
        }
        return null;
    }

    /**
     * <code> getServant </code> returns the Servant associated with the
     * ObjectID in the AOM.
     * @param ok specifies the ObjectID for which the associated servant is
     * needed.
     * @exception org.omg.PortableServer.POAPackage.ObjectNotActive is thrown if
     * there is no Servant associated with this Object Key.
     * @exception org.omg.PortableServer.POAPackage.WrongPolicy is thrown by the
     * Non Retain Strategy if this operation is invoke on it.
     */
    public abstract org.omg.PortableServer.Servant getServant( FString ok , org.omg.CORBA.IntHolder ih );

    /**
     * <code> getObjectID </code> returns the ObjectID associated with the
     * Servant in the AOM.
     * @param servant is the Servant who's Object association is returned.
     * @exception org.omg.PortableServer.POAPackage.ServantNotActive is thrown
     * if the Servant is not associted with an Object in the AOM.
     * @exception org.omg.PortableServer.POAPackage.WrongPolicy if invoked with
     * the Non Retain Strategy.
     */
    public abstract void getObjectID( org.omg.PortableServer.Servant servant , FString oid_out , org.omg.CORBA.IntHolder ih );

    /**
     * <code> add </code> adds the association between the ObjectID and the
     * Servant in the AOM.
     * @param ok specifes the Object key for the servant.
     * @param map specifes the POAHashmap.
     * @exception org.omg.PortableServer.POAPackage.WrongPolicy is thrown if
     * invoked on the Non Retain Strategy.
     */
    public abstract void add( FString ok, edu.uci.ece.zen.poa.POAHashMap map );

    /**
     * <code> validate </code> checks if the strategy associated is the same as
     * that given in the argument.
     * @param policyName specifies the name of the strategy that is being
     * checked.
     * @exception  org.omg.PortableServer.POAPackage.WrongPolicy is thrown if the
     * validation fails
     */
    public abstract void validate(int policyName , org.omg.CORBA.IntHolder ih );

    /**
     * <code> servantPresent </code> chekcs if the servant is present in the AOM.
     * @param servant who's presence is checked in the AOM.
     * @exception  org.omg.PortableServer.POAPackage.WrongPolicy is thrown if
     * invoked on the Non Retain Strategy
     */
    public abstract boolean servantPresent(org.omg.PortableServer.Servant servant , org.omg.CORBA.IntHolder ih );

    /**
     * <code> objectKeyPresent </code> checks for the presence of the ObjectID
     * in the AOM.
     * @param ok is the ObjectID who's presence is being checked.
     * @exception  org.omg.PortableServer.POAPackage.WrongPolicy is thrown if
     * invoked on the Non Retain Strategy
     */
    public abstract boolean objectIDPresent( FString ok , org.omg.CORBA.IntHolder ih );
    public abstract edu.uci.ece.zen.poa.POAHashMap getHashMap( FString map , org.omg.CORBA.IntHolder ih )
    public abstract int bindDemuxIndex( edu.uci.ece.zen.poa.POAHashMap oid , org.omg.CORBA.IntHolder ih );
    public abstract boolean unbindDemuxIndex( FString oid , org.omg.CORBA.IntHolder ih );
    public abstract int getGenCount(int index , org.omg.CORBA.IntHolder ih );
    public abstract int find( FString id , org.omg.CORBA.IntHolder ih );
}

