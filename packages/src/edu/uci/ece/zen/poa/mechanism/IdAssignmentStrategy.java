package edu.uci.ece.zen.poa.mechanism;

/**
 * The class <code>IdAssignmentStrategy</code> takes care of creating
 * the appropriate instances of SystemId or UniqueIdStrategy based on
 * the policies of the POA.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */
abstract public class IdAssignmentStrategy {
    /**
     * @param policy org.omg.CORBA.Policy[]
     * @return IdAssignmentStrategy
     */
    public static IdAssignmentStrategy init(org.omg.CORBA.Policy[] policy) {
        if (PolicyUtils.useSystemIdPolicy(policy))
            return (IdAssignmentStrategy) new edu.uci.ece.zen.poa.mechanism.SystemIdStrategy();
        else
            return (IdAssignmentStrategy) new edu.uci.ece.zen.poa.mechanism.UserIdStrategy();
    }

    /*
     * Returns the next Id or null if the policy does not support it.
     */
    public abstract void nextId( FString id_out );
    public abstract boolean isPresent(int PolicyName);
    public abstract boolean validate(int policy);
    public abstract boolean verifyID( FString id_in );

    // --- Ids for the strategies ---
    public static final int USER_ID = 0;
    public static final int SYSTEM_ID = 1;
}

