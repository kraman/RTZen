/* --------------------------------------------------------------------------*
 * $Id: IdUniquenessStrategy.java,v 1.1 2003/11/26 22:28:55 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa.mechanism;


/**
 * The class <code>IdUniquenessStrategy</code> creates
 * the appropriate Strategy: Unique Id or System Id based on the policies
 * specified in the POA.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */


import edu.uci.ece.zen.poa.POAPolicyFactory;
import edu.uci.ece.zen.poa.Util;
import edu.uci.ece.zen.sys.ZenProperties;


public abstract class IdUniquenessStrategy {

    // ---- Strategy Classpaths ----
    protected static final String uniqueIdPath = "poa.uniqueId";
    protected static final String multipleIdPath = "poa.multipleId";

    // ---- Initialization Code ----
    static {

        IdUniquenessStrategy.uniqueId = (UniqueIdStrategy)
                POAPolicyFactory.createPolicy(ZenProperties.getProperty(IdUniquenessStrategy.uniqueIdPath));

        IdUniquenessStrategy.multipleId = (MultipleIdStrategy)
                POAPolicyFactory.createPolicy(ZenProperties.getProperty(IdUniquenessStrategy.multipleIdPath));
    }

    /**
     * From the policy list create the IdUniquenessStrategy
     * @param policy Policy[] Policy list  
     * @return IdUniquenessStrategy create Id Uniqueness Strategy
     */
    public static IdUniquenessStrategy init(org.omg.CORBA.Policy[] policy) {
        if (Util.useUniqueIdPolicy(policy)) {
            return IdUniquenessStrategy.uniqueId;
        } else {
            return IdUniquenessStrategy.multipleId;
        }
    }

    public abstract boolean validate(int  policyName);

    // --- Ids for the strategies ---
    public static final int UNIQUE_ID = 0;
    public static final int MULTIPLE_ID = 1;

    // ---Singleton Strategies ---
    private static MultipleIdStrategy multipleId;
    private static UniqueIdStrategy   uniqueId;
}

