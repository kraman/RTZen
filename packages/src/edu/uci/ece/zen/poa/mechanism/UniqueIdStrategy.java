/* --------------------------------------------------------------------------*
 * $Id: UniqueIdStrategy.java,v 1.1 2003/11/26 22:29:04 nshankar Exp $
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
