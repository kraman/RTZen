package edu.uci.ece.zen.poa.mechanism;

import edu.uci.ece.zen.utils.*;

/**
 * The class <code>IdUniquenessStrategy</code> creates
 * the appropriate Strategy: Unique Id or System Id based on the policies
 * specified in the POA.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */
public abstract class IdUniquenessStrategy {

    // --- Ids for the strategies ---
    public static final int UNIQUE_ID = 0;
    public static final int MULTIPLE_ID = 1;

    /**
     * From the policy list create the IdUniquenessStrategy
     * @param policy Policy[] Policy list  
     * @return IdUniquenessStrategy create Id Uniqueness Strategy
     */
    public static IdUniquenessStrategy init(org.omg.CORBA.Policy[] policy, org.omg.CORBA.IntHolder ih ) {
        ih.value = POARunnable.NoException;
        if (Util.useUniqueIdPolicy(policy)) {
//            return new UniqueIdStrategy();
        } else {
//            return new MultipleIdStrategy();
        }
        return null;
    }

    public abstract boolean validate(int policyName);
}

