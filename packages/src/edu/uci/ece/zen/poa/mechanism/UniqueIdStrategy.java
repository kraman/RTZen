/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

/*******************************************************************************
 * --------------------------------------------------------------------------
 * $Id: UniqueIdStrategy.java,v 1.5 2003/08/05 23:37:28 nshankar Exp $
 * --------------------------------------------------------------------------
 */
package edu.uci.ece.zen.poa.mechanism;

public final class UniqueIdStrategy extends IdUniquenessStrategy {

    /**
     * Validate Strategy
     * 
     * @param policy
     *            policy-type
     * @return boolean true if same, else false
     */
    public boolean validate(int policy) {
        if (IdUniquenessStrategy.UNIQUE_ID == policy) 
        {
            return true;
        } 
        else 
        {
            return false;
        }

    }
}