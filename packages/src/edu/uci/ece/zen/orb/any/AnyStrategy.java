/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.any;

/**
 * Used as a base class for individual strategies by the strategy
 * design pattern.
 *
 * <p>
 * This is the base class for the strategies that serve to perform the
 * work of acting as pluggable Anys.
 * 
 * @author Bruce Miller
 * @version $Revision: 1.1 $ $Date: 2004/01/15 18:25:18 $
 * @see edu.uci.ece.zen.orb.any.Any edu.uci.ece.zen.orb.any.Any
 * @see edu.uci.ece.zen.orb.any.pluggable.Any edu.uci.ece.zen.orb.any.pluggable.Any
 * @see edu.uci.ece.zen.orb.any.monolithic.Any edu.uci.ece.zen.orb.any.monolithic.Any
 */
public abstract class AnyStrategy extends org.omg.CORBA.Any {
    public abstract void setOrb(edu.uci.ece.zen.orb.ORB orb);
}
