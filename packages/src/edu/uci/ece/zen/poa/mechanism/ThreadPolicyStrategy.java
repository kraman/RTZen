package edu.uci.ece.zen.poa.mechanism;

import edu.uci.ece.zen.poa.*;
import edu.uci.ece.zen.utils.*;

/**
 * The class <code>ThreadPolicyStrategy</code> is strategy for implementing
 * the Threading-Policy of the POA
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */
abstract public class ThreadPolicyStrategy {
    public static ThreadPolicyStrategy init(org.omg.CORBA.Policy[] policy , org.omg.CORBA.IntHolder ih ) {
        if ( PolicyUtils.useSingleThreadedPolicy(policy)) {
            return (ThreadPolicyStrategy) new SingleThreadModelStrategy();
        } else {
            return new OrbControlModelStrategy();
        }
    }

    public abstract void enter(org.omg.CORBA.portable.InvokeHandler invokeHandler);
    public abstract void enter();
    public abstract void exit(org.omg.CORBA.portable.InvokeHandler invokeHandler);
    public abstract void exit();
}

