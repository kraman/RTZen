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
    public abstract void setOrb(org.omg.CORBA.ORB orb);
}
