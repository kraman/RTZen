/* --------------------------------------------------------------------------*
 * $Id: POAPolicyFactory.java,v 1.1 2003/11/26 22:26:32 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa;


/**
 * The class <code>POAPolicyFactory</code> takes care of creating
 * instances for the different policy strategies that govern the
 * behaviour of the POA.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */

public class POAPolicyFactory {

    /**
     * Create an instance of the <code> Policy Object </code> to be used by
     * ZEN and its modules.
     *
     * @return a <code>PolicyStrategy</code> of the type that is specified in the
     * argument.
     */
    public static Object createPolicy(String policyClassName) {
        // edu.uci.ece.zen.orb.Logger.debug ("Creating policy" + policyClassName);
        return createObject(policyClassName);
    }

    /**
     * Create the instance of the <code> PolicyStrategy </code> class using reflection
     * @return the instance of the Strategy created.
     */

    private static Object createObject(String classPath) {
        try {
            Class objectClass = Class.forName(classPath);

            return objectClass.newInstance();
        } catch (Exception ex) {
            edu.uci.ece.zen.orb.Logger.error("Error: Cannot load class"
                    + classPath);
            return null;
        }
    } 

}
