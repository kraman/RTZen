package edu.uci.ece.zen.poa.mechanism;


// --- ZEN Imports ---
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.poa.*;

// --- OMG Imports ---
import org.omg.CORBA.IntHolder;

/**
 * The class <code>LifespanStrategy</code> creates a Persistent or
 * a Transient Strategy based on the Policies in the POA.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */


abstract public class LifespanStrategy {
    /**
     * <code> init </code> creates either a Persistent/Transient Strategy
     * based on the policy of the POA.
     * @param policy specifies the Policy of the POA.
     *
     */
    public static LifespanStrategy init(org.omg.CORBA.Policy[] policy , IntHolder excetpionValue ) {
        excetpionValue.value = POARunnable.NoException;

        if (PolicyUtils.usetransientpolicy(policy)) { 
//            return new TransientStrategy(); 
        }else{
//            return new PersistentStrategy();
        }
        return null;
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
    abstract public void create(FString path_name, FString oid, ActiveDemuxLoc poaLoc , FString okey_out );
    abstract public void create(FString path_name, FString oid, int poaLocIndex , int poaLocCount , int servLocIndex , int servLocCount , FString okey_out );

    /**
     * <code> isValid </code> checks for the validity of the Object Key.
     * Primarily used in the case of transient POA's. Validation based
     * on comparison of the time-stamps.
     * @param objectKey is the Object Key who's freshness is checked.
     * @exception org.omg.CORBA.OBJECT_NOT_EXISTS if the test fails.
     */
    abstract public void validate( FString objKey , IntHolder excetpionValue );
}

