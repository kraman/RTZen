package edu.uci.ece.zen.poa.mechanism;

import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.poa.*;
import org.omg.CORBA.IntHolder;

/**
 * The class <code>IdAssignmentStrategy</code> takes care of creating
 * the appropriate instances of SystemId or UniqueIdStrategy based on
 * the policies of the POA.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */
abstract public class IdAssignmentStrategy {

    public static final int NoException = 0;
    public static final int WrongPolicyException = 1;

    /**
     *
     * @param policy org.omg.CORBA.Policy[]
     * @return IdAssignmentStrategy
     */
    public static IdAssignmentStrategy init(org.omg.CORBA.Policy[] policy , IntHolder exceptionValue ) {
        exceptionValue.value = IdAssignmentStrategy.NoException;
        if (Util.useSystemIdPolicy(policy)) {
//            return new SystemIdStrategy();
        } else {
//            return new UserIdStrategy();
        }
        return null;
    }

    /**
     * @throws WrongPolicy
     */
    public abstract void nextId( FString id_out , org.omg.CORBA.IntHolder exceptionValue );
    public abstract boolean isPresent(int PolicyName);
    /**
     * @throws WrongPolicy
     */
    public abstract void validate(int policy , IntHolder exceptionValue );
    public abstract boolean verifyID( FString id );
}

