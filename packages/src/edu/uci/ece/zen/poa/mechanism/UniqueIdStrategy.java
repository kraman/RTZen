/* --------------------------------------------------------------------------*
 * $Id: UniqueIdStrategy.java,v 1.5 2003/08/05 23:37:28 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa.mechanism; 


public final class UniqueIdStrategy extends IdUniquenessStrategy {
    
    /**
     * Validate Strategy
     * @param policy policy-type
     * @return boolean true if same, else false
     */
    public boolean validate(int policy) {
        if (IdUniquenessStrategy.UNIQUE_ID == policy) {
            return true;
        } else {
            return false;
        }
        
    }
}
