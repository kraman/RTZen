package edu.uci.ece.zen.poa.mechanism; 

public final class UniqueIdStrategy extends IdUniquenessStrategy {
    /**
     * Validate Strategy
     * @param policy policy-type
     * @return boolean true if same, else false
     */
    public boolean validate(int policy) {
        return IdUniquenessStrategy.UNIQUE_ID == policy;
    }
}
