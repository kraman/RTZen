/* --------------------------------------------------------------------------*
 * $Id: ExplicitActivationStrategy.java,v 1.1 2003/11/26 22:28:51 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa.mechanism;


public final class ExplicitActivationStrategy extends ActivationStrategy {

    /**
     * check if the strategys int value is same as this strategy
     * @param name strategy value
     * @return boolean true if same, else false
     */

    public boolean validate(int name) {
        if (ImplicitActivationStrategy.EXPLICIT_ACTIVATION == name) {
            return true;
        } else {
            return false;
        }
    }
}
