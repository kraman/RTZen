/* --------------------------------------------------------------------------*
 * $Id: MultipleIdStrategy.java,v 1.1 2003/11/26 22:28:55 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa.mechanism; 


public final class MultipleIdStrategy extends
            IdUniquenessStrategy {
    /**
     * check if the strategies value is same as this strategy. 
     * @param policy policy value
     * @return boolean ture if same, else false
     */
    public boolean validate(int policy) {
            if (IdUniquenessStrategy.MULTIPLE_ID == policy) {
                return true;
            } else {
                return false;
            }
    }
}
