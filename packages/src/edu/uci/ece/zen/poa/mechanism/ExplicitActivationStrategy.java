package edu.uci.ece.zen.poa.mechanism;

public final class ExplicitActivationStrategy extends ActivationStrategy {
    /**
     * check if the strategys int value is same as this strategy
     * 
     * @param name
     *            strategy value
     * @return boolean true if same, else false
     */
    public boolean validate(int name) {
        return (ImplicitActivationStrategy.EXPLICIT_ACTIVATION == name);
    }
}
