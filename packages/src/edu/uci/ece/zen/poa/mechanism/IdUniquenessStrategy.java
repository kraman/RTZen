/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.poa.mechanism;

import org.omg.CORBA.IntHolder;

import edu.uci.ece.zen.poa.POARunnable;

/**
 * The class <code>IdUniquenessStrategy</code> creates the appropriate Strategy: Unique Id or
 * System Id based on the policies specified in the POA.
 * 
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna </a>
 * @version 1.0
 */
public abstract class IdUniquenessStrategy
{

    // --- Ids for the strategies ---
    public static final int UNIQUE_ID = 0;

    public static final int MULTIPLE_ID = 1;

    /**
     * From the policy list create the IdUniquenessStrategy
     * 
     * @param policy Policy[] Policy list
     * @return IdUniquenessStrategy create Id Uniqueness Strategy
     */
    public static IdUniquenessStrategy init(org.omg.CORBA.Policy[] policy, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.NoException;
        if (PolicyUtils.useUniqueIdPolicy(policy))
        {
            return new UniqueIdStrategy();
        } 
        else
        {
            return new MultipleIdStrategy();
        }
    }

    public abstract boolean validate(int policyName);
}

