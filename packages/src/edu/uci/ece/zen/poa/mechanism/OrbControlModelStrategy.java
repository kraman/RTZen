/* --------------------------------------------------------------------------*
 * $Id: OrbControlModelStrategy.java,v 1.5 2003/08/05 23:37:28 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa.mechanism;


/**
 * The class <code>OrbControlModelStrategy</code> implements the
 * ORBControlModel Policy of the CORBA POA
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */

public final class OrbControlModelStrategy extends ThreadPolicyStrategy {

    /**
     * enter() methods defaults to no-op as the POA is multi-threaded and
     * there is no need to grab a lock.
     * @param invokeHandler org.omg.CORBA.portable.InvokeHandler
     */
    public void enter(org.omg.CORBA.portable.InvokeHandler invokeHandler) {}

    /**
     * executed after return from critical section. Defaults to no-op as
     * POA multi-threaded
     * @param invokeHandler org.omg.CORBA.portable.InvokeHandler
     */
    public void exit(org.omg.CORBA.portable.InvokeHandler invokeHandler) {}

    /**
     * Default synchronization blocks. Specialized in the Single-thread
     * case. Default to no-ops here.
     */
    public void enter() {}

    /**
     * Executed after execution of a block. Default no-op case.
     */
    public void exit() {}
}
