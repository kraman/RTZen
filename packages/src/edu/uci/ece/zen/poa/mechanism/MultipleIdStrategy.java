/* --------------------------------------------------------------------------*
 * $Id: MultipleIdStrategy.java,v 1.5 2003/08/05 23:37:28 nshankar Exp $
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
