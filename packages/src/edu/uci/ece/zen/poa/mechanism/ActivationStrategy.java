package edu.uci.ece.zen.poa.mechanism;

import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.poa.*;
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
                exceptionValue.value = ServantRetentionStrategy.InvalidPolicyException;
                return null;
            }

            assignmentStrategy.validate(IdAssignmentStrategy.SYSTEM_ID,exceptionValue);
            if( exceptionValue.value != IdAssignmentStrategy.NoException ){
                exceptionValue.value = IdAssignmentStrategy.InvalidPolicyException;
                return null;
            }

//            return new ImplicitActivationStrategy();
        }else{
//            return new ExplicitActivationStrategy();
        }
        return null;
    }

    public abstract boolean validate(int name);
}

