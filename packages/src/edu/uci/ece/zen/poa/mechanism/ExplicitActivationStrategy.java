package edu.uci.ece.zen.poa.mechanism;

import org.omg.CORBA.IntHolder;

public final class ExplicitActivationStrategy extends ActivationStrategy {

    /**
     * check if the strategys int value is same as this strategy
     * @param name strategy value
     * @return boolean true if same, else false
     */
    public void validate(int name, IntHolder exceptionValue ) {
        if (ImplicitActivationStrategy.EXPLICIT_ACTIVATION == name) {
            exceptionValue.value = ActivationStrategy.NoException;
        } else {
            exceptionValue.value = ActivationStrategy.InvalidPolicyException;
        }
    }
}
