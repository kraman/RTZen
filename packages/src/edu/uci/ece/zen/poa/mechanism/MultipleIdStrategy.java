package edu.uci.ece.zen.poa.mechanism;

public final class MultipleIdStrategy extends IdUniquenessStrategy {
    /**
     * check if the strategies value is same as this strategy.
     * 
     * @param policy
     *            policy value
     * @return boolean ture if same, else false
     */
    public boolean validate(int policy) {
        return (IdUniquenessStrategy.MULTIPLE_ID == policy);
    }
}
