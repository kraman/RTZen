/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

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