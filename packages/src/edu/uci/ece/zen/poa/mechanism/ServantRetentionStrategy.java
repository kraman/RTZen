/* --------------------------------------------------------------------------*
 * $Id: ServantRetentionStrategy.java,v 1.2 2004/03/11 19:31:37 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa.mechanism;


/**
 * The class <code>ServantRetentionStrategy</code> creates a POA with  
 * either the Retain/Non Retain Strategies based on the policies of the POA
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */



import edu.uci.ece.zen.poa.POAPolicyFactory;
import edu.uci.ece.zen.poa.Util;
import edu.uci.ece.zen.sys.ZenProperties;


public abstract class ServantRetentionStrategy {

    // --- Classpaths for the Strategies ---
    protected static final String retain = "poa.retain";
    protected static final String nonRetain = "poa.nonRetain";

    // --- Initialization Code ----

    /*static {
        ServantRetentionStrategy.nonRetainStrategy = (NonRetainStrategy)
                POAPolicyFactory.createPolicy(ZenProperties.getProperty(ServantRetentionStrategy.nonRetain));

    }*/
    
    /**
     * <code> init </code> builds the appropriate RetentionStrategy based on the
     * Policy of the POA.
     * @param policy is the policy of the POA based on which the Strategies are
     *               constructed.
     * @exception org.omg.PoratableServer.POAPackage.InvalidPolicy if the
     * policies of the POA are in conflict.
     */

    public static ServantRetentionStrategy init(
            org.omg.CORBA.Policy[] policy,
            IdUniquenessStrategy uniquenessStrategy) {
       // if (Util.useRetainPolicy(policy)) {

            RetainStrategy retain;

            retain = (RetainStrategy) POAPolicyFactory.createPolicy(ZenProperties.getProperty(ServantRetentionStrategy.retain));
            retain.initialize(uniquenessStrategy);
            return retain;
        //} else {
           // return ServantRetentionStrategy.nonRetainStrategy;
        //}
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

    

    public abstract org.omg.PortableServer.Servant getServant(
            byte[] ok)
        throws org.omg.PortableServer.POAPackage.ObjectNotActive,
                org.omg.PortableServer.POAPackage.WrongPolicy;

    /**
     * <code> getObjectID </code> returns the ObjectID associated with the
     * Servant in the AOM.
     * @param servant is the Servant who's Object association is returned.
     * @exception org.omg.PortableServer.POAPackage.ServantNotActive is thrown
     * if the Servant is not associted with an Object in the AOM.
     * @exception org.omg.PortableServer.POAPackage.WrongPolicy if invoked with
     * the Non Retain Strategy.
     */

    

    public abstract byte[] getObjectID(

            org.omg.PortableServer.Servant servant)

        throws org.omg.PortableServer.POAPackage.WrongPolicy,
                org.omg.PortableServer.POAPackage.ServantNotActive;

    /**
     * <code> add </code> adds the association between the ObjectID and the
     * Servant in the AOM.
     * @param ok specifes the Object key for the servant.
     * @param map specifes the POAHashmap.
     * @exception org.omg.PortableServer.POAPackage.WrongPolicy is thrown if
     * invoked on the Non Retain Strategy.
     */
    public abstract void add(byte[] ok,
            edu.uci.ece.zen.poa.POAHashMap map)
        throws org.omg.PortableServer.POAPackage.WrongPolicy;

    /**
     * <code> validate </code> checks if the strategy associated is the same as
     * that given in the argument.
     * @param policyName specifies the name of the strategy that is being
     * checked.
     * @exception  org.omg.PortableServer.POAPackage.WrongPolicy is thrown if the
     * validation fails
     */
    public abstract void validate(int policyName) throws
                org.omg.PortableServer.POAPackage.WrongPolicy;

    /**
     * <code> servantPresent </code> chekcs if the servant is present in the AOM.
     * @param servant who's presence is checked in the AOM.
     * @exception  org.omg.PortableServer.POAPackage.WrongPolicy is thrown if
     * invoked on the Non Retain Strategy
     */

    

    public abstract boolean servantPresent(org.omg.PortableServer.Servant servant)
        throws org.omg.PortableServer.POAPackage.WrongPolicy;

    /**
     * <code> objectKeyPresent </code> checks for the presence of the ObjectID
     * in the AOM.
     * @param ok is the ObjectID who's presence is being checked.
     * @exception  org.omg.PortableServer.POAPackage.WrongPolicy is thrown if
     * invoked on the Non Retain Strategy
     */

    public abstract boolean objectIDPresent(byte[]ok)
        throws org.omg.PortableServer.POAPackage.WrongPolicy;

    public abstract edu.uci.ece.zen.poa.POAHashMap getHashMap(byte[] map)
        throws org.omg.PortableServer.POAPackage.WrongPolicy;

    public abstract int bindDemuxIndex(edu.uci.ece.zen.poa.POAHashMap oid) throws
                org.omg.PortableServer.POAPackage.WrongPolicy;

    public abstract boolean unbindDemuxIndex(byte[] oid)
        throws org.omg.PortableServer.POAPackage.WrongPolicy;

    public abstract int getGenCount(int index)
        throws org.omg.PortableServer.POAPackage.WrongPolicy;

    public abstract int find(byte[] id) throws
                org.omg.PortableServer.POAPackage.WrongPolicy;

    // --- Strategy Types ----
    public static final int RETAIN = 0;
    public static final int NON_RETAIN = 1;

    // ---Non RetainStrategy Singleton ---
    //private static NonRetainStrategy nonRetainStrategy;
}

