package edu.uci.ece.zen.poa.mechanism;

import org.omg.CORBA.IntHolder;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import edu.uci.ece.zen.poa.POARunnable;
import edu.uci.ece.zen.utils.FString;

/**
 * The class <code>IdAssignmentStrategy</code> takes care of creating the
 * appropriate instances of SystemId or UniqueIdStrategy based on the policies
 * of the POA.
 * 
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna </a>
 * @version 1.0
 */
abstract public class IdAssignmentStrategy {

    // --- Ids for the strategies ---
    public static final int USER_ID = 0;

    public static final int SYSTEM_ID = 1;

    /**
     * @param policy
     *            org.omg.CORBA.Policy[]
     * @return IdAssignmentStrategy
     */
    public static IdAssignmentStrategy init(org.omg.CORBA.Policy[] policy,
            IntHolder exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        if (PolicyUtils.useSystemIdPolicy(policy)) {
            return new SystemIdStrategy();
        } else {
            return new UserIdStrategy();
        }
    }

    /**
     * @throws WrongPolicy
     */
    public abstract void nextId(FString id_out,
            org.omg.CORBA.IntHolder exceptionValue);

    public abstract boolean isPresent(int PolicyName);

    /**
     * @throws WrongPolicy
     */
    public abstract void validate(int policy, IntHolder exceptionValue);

    public abstract boolean verifyID(FString id);
}

