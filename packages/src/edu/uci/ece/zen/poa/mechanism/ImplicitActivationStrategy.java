package edu.uci.ece.zen.poa.mechanism;

import org.omg.CORBA.IntHolder;

public final class ImplicitActivationStrategy extends ActivationStrategy {

    /**
     * Check if the strategy value is same as this strategys value. Usually
     * used to check if two strategies are the same
     * @param name Strategies integer value
     * @return boolean true if same, else false.
     */
    public void validate(int name, IntHolder exceptionValue ) {
        if (ActivationStrategy.IMPLICIT_ACTIVATION == name) {
            exceptionValue.value = ActivationStrategy.NoException;
        } else {
            exceptionValue.value = ActivationStrategy.InvalidPolicyException;
        }
    }

}
