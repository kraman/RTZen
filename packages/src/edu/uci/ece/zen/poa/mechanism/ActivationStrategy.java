/* --------------------------------------------------------------------------*
 * $Id: ActivationStrategy.java,v 1.2 2004/03/11 19:31:37 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa.mechanism;


/**
 * The class <code>ActivationStrategy</code> creates either a Implicit/
 * Expicit Activation mechanism for the POA based on the policies specified.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */

import edu.uci.ece.zen.poa.POAPolicyFactory;
import edu.uci.ece.zen.poa.Util;
import edu.uci.ece.zen.sys.ZenProperties;


public abstract class ActivationStrategy {
    // --- Strategy names ---
    protected static final String implicit = "poa.implicitActivation";
    protected static final String explicit = "poa.explicitActivation";

    // --- Initialization Code ----
    static {
        ActivationStrategy.implicitActivation = (ImplicitActivationStrategy)
                POAPolicyFactory.createPolicy(ZenProperties.getProperty(ActivationStrategy.implicit));
        //ActivationStrategy.noImplicitActivation = (ExplicitActivationStrategy)
           //     POAPolicyFactory.createPolicy(ZenProperties.getProperty(ActivationStrategy.explicit));
    }

/**
 *
 * @param policy Policy list
 * @param assignmentStrategy IdAssignmentStrategy
 * @param retentionStrategy ServantRetentionStrategy
 * @return ActivationStrategy
 */
    public static ActivationStrategy init(org.omg.CORBA.Policy[] policy,
            IdAssignmentStrategy assignmentStrategy,
            ServantRetentionStrategy retentionStrategy)
        throws org.omg.PortableServer.POAPackage.InvalidPolicy {

        //if (Util.useImplicitActivationPolicy(policy)) {
           // try {
                // Check if the other policies are Retain and System Id
              //  retentionStrategy.validate(ServantRetentionStrategy.RETAIN);
                //assignmentStrategy.validate(IdAssignmentStrategy.SYSTEM_ID);

                return ActivationStrategy.implicitActivation;
            //} catch (Exception ex) {
               // throw new org.omg.PortableServer.POAPackage.InvalidPolicy();
            //}
        //}

        //return ActivationStrategy.noImplicitActivation;
    }

    public abstract boolean validate(int name);

    // --- Type Values ---
    public static int IMPLICIT_ACTIVATION = 0;
    public static int EXPLICIT_ACTIVATION = 1;

    // --- Singleton references ---
    private static ImplicitActivationStrategy implicitActivation;
    //private static ExplicitActivationStrategy noImplicitActivation;

}

