/* --------------------------------------------------------------------------*
 * $Id: ThreadPolicyStrategy.java,v 1.5 2003/08/05 23:41:31 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa.mechanism;




/**
 * The class <code>ThreadPolicyStrategy</code> is strategy for implementing
 * the Threading-Policy of the POA
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */



import edu.uci.ece.zen.poa.POAPolicyFactory;
import edu.uci.ece.zen.sys.ZenProperties;


abstract public class ThreadPolicyStrategy {

    // -- Strategy Classpaths  ----
    protected static final String singleThreadClasspath = "poa.singleThreaded";
    protected static final String orbControlClasspath = "poa.multiThreaded";

    static {
        ThreadPolicyStrategy.multiThreaded = (OrbControlModelStrategy)
                POAPolicyFactory.createPolicy(ZenProperties.getProperty(ThreadPolicyStrategy.orbControlClasspath));
    }

    public static ThreadPolicyStrategy init(org.omg.CORBA.Policy[] policy , org.omg.CORBA.IntHolder ih ) {

        if (edu.uci.ece.zen.poa.Util.useSingleThreadedPolicy(policy)) {
            return (ThreadPolicyStrategy) edu.uci.ece.zen.poa.POAPolicyFactory.createPolicy(ThreadPolicyStrategy.singleThreadClasspath);
        } else {
            return ThreadPolicyStrategy.multiThreaded;
        }
    }

    public abstract void enter(org.omg.CORBA.portable.InvokeHandler
            invokeHandler);

    public abstract void enter();

    public abstract void exit(org.omg.CORBA.portable.InvokeHandler
            invokeHandler);

    public abstract void exit();

    // --Singleton references ---
    private static OrbControlModelStrategy multiThreaded;
}

