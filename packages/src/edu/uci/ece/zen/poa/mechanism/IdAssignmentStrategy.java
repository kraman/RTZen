/* --------------------------------------------------------------------------*
 * $Id: IdAssignmentStrategy.java,v 1.2 2004/03/11 19:31:37 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa.mechanism;


/**
 * The class <code>IdAssignmentStrategy</code> takes care of creating
 * the appropriate instances of SystemId or UniqueIdStrategy based on
 * the policies of the POA.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */

import edu.uci.ece.zen.poa.POAPolicyFactory;
import edu.uci.ece.zen.poa.Util;
import edu.uci.ece.zen.sys.ZenProperties;


abstract public class IdAssignmentStrategy {

    // --Classpaths for the strategies ---
    protected static final String systemId = "poa.systemId";
    protected static final String userId = "poa.userId";

    // --- Initialization Code ----
  /*  static {
        IdAssignmentStrategy.userIdStrategy = (UserIdStrategy)
                POAPolicyFactory.createPolicy(ZenProperties.getProperty(IdAssignmentStrategy.userId));
    }*/

/**
 *
 * @param policy org.omg.CORBA.Policy[]
 * @return IdAssignmentStrategy
 */
    public static IdAssignmentStrategy init(org.omg.CORBA.Policy[] policy) {
        //if (Util.useSystemIdPolicy(policy)) {
            return (IdAssignmentStrategy) POAPolicyFactory.createPolicy(ZenProperties.getProperty(IdAssignmentStrategy.systemId));
        //else {
           //eturn IdAssignmentStrategy.userIdStrategy;
        //}
    }

    public abstract byte[] nextId()
        throws org.omg.PortableServer.POAPackage.WrongPolicy;

    public abstract boolean isPresent(int PolicyName);

    public abstract void validate(int policy)
        throws org.omg.PortableServer.POAPackage.WrongPolicy;

    public abstract boolean verifyID(byte[] id);

    // --- Ids for the strategies ---
    public static final int USER_ID = 0;
    public static final int SYSTEM_ID = 1;

    // ---Singleton UserId Strategy ---
    //private static UserIdStrategy userIdStrategy;
}

