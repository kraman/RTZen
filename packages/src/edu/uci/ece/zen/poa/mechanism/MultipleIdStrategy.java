package edu.uci.ece.zen.poa.mechanism; 

import org.omg.CORBA.IntHolder;

public final class MultipleIdStrategy extends
            IdUniquenessStrategy {
    /**
     * check if the strategies value is same as this strategy. 
     * @param policy policy value
     * @return boolean ture if same, else false
     */
    public void  validate(int policy, IntHolder exceptionValue) {
            if (IdUniquenessStrategy.MULTIPLE_ID == policy) {
                exceptionValue.value = POARunnable.NoException;
            } else {
                exceptionValue.value = POARunnable.FalseException;
            }
    }
}
