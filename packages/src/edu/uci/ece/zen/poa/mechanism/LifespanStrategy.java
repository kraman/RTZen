/* --------------------------------------------------------------------------*
 * $Id: LifespanStrategy.java,v 1.3 2004/03/11 19:31:37 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa.mechanism;


/**
 * The class <code>LifespanStrategy</code> creates a Persistent or
 * a Transient Strategy based on the Policies in the POA.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */


// --- ZEN Imports ---
import edu.uci.ece.zen.poa.ActiveDemuxLoc;
import edu.uci.ece.zen.poa.ActiveDemuxLocOperations;
import edu.uci.ece.zen.poa.POAPolicyFactory;
import edu.uci.ece.zen.poa.Util;
import edu.uci.ece.zen.sys.ZenProperties;


abstract public class LifespanStrategy {

    public static String eternal = "poa.eternal";
    public static String ephemeral = "poa.ephemeral";

    // -- Initialization Code ---
   /* static {
        LifespanStrategy.persistentStrategy = (PersistentStrategy)
                POAPolicyFactory.createPolicy(ZenProperties.getProperty(LifespanStrategy.eternal));
    }*/

    /**
     * <code> init </code> creates either a Persistent/Transient Strategy
     * based on the policy of the POA.
     * @param policy specifies the Policy of the POA.
     *
     */
    public static LifespanStrategy init(org.omg.CORBA.Policy[] policy) {

        //if (Util.useTransientPolicy(policy)) {
            return (LifespanStrategy) POAPolicyFactory.createPolicy(ZenProperties.getProperty(LifespanStrategy.ephemeral));
       // }
        //return LifespanStrategy.persistentStrategy;
    }

    /**
     * <code> timeStamp </code> returns the Time Stamp associated in the ObjectKey.
     *
     */

    abstract public long timeStamp();

    /** <code> create </code> creates the appropriate ObjectKey based on the
     * Strategy loaded, i.e. either a persistent object key or a transient
     * object key.
     * @param path_name specifies the complete POA Path Name of the POA.
     * @param oid specifies the Object Id of the Object Key
     * @param poaLoc ActiveDemuxLoc
     */

    abstract public byte[] create(String path_name,
            byte[] oid,
            ActiveDemuxLoc poaLoc);

    abstract public byte[]  create(String path_name,
            byte[] oid,
            ActiveDemuxLoc poaLoc,
            int index, int count);

    /**
     * <code> isValid </code> checks for the validity of the Object Key.
     * Primarily used in the case of transient POA's. Validation based
     * on comparison of the time-stamps.
     * @param objectKey is the Object Key who's freshness is checked.
     * @exception org.omg.CORBA.OBJECT_NOT_EXISTS if the test fails.
     */
    
    abstract public int validate(byte[] objectKey) throws org.omg.CORBA.OBJECT_NOT_EXIST;

    // --Persistent Strategy Singleton ---
//    protected static PersistentStrategy persistentStrategy;
}

