package edu.uci.ece.zen.poa.mechanism;

import edu.uci.ece.zen.utils.ZenProperties;
import org.omg.CORBA.Policy;
import org.omg.CORBA.IntHolder;

/**
 * The class <code>ActivationStrategy</code> creates either a Implicit/
 * Expicit Activation mechanism for the POA based on the policies specified.
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */
public abstract class ActivationStrategy {

    public static final int NoException = 0;
    public static final int InvalidPolicyException = 1;

    // --- Type Values ---
    public static int IMPLICIT_ACTIVATION = 0;
    public static int EXPLICIT_ACTIVATION = 1;


    /**
     *
     * @param policy Policy list
     * @param assignmentStrategy IdAssignmentStrategy
     * @param retentionStrategy ServantRetentionStrategy
     * @return ActivationStrategy
     */
    public static ActivationStrategy init(Policy[] policy, IdAssignmentStrategy assignmentStrategy,
             ServantRetentionStrategy retentionStrategy , IntHolder exceptionValue ){

        exceptionValue.value = ActivationStrategy.NoException;

        if (PolicyUtils.useImplicitActivationPolicy(policy)) {
            // Check if the other policies are Retain and System Id
            retentionStrategy.validate(ServantRetentionStrategy.RETAIN,exceptionValue );

            if( exceptionValue.value != ServantRetentionStrategy.NoException ){
                exceptionValue.value = ActivationStrategy.InvalidPolicyException;
                return null;
            }

            assignmentStrategy.validate(IdAssignmentStrategy.SYSTEM_ID);
            if( exceptionValue.value != IdAssignmentStrategy.NoException ){
                exceptionValue.value = ActivationStrategy.InvalidPolicyException;
                return null;
            }

            return new ImplicitActivationStrategy();
        }else{
            return new ExplicitActivationStrategy();
        }
    }

    public abstract void validate(int name , IntHolder exceptionValue);
}

