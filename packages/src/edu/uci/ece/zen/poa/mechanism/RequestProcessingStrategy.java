package edu.uci.ece.zen.poa.mechanism;

import edu.uci.ece.zen.poa.*;
import edu.uci.ece.zen.utils.*;

/**
 * The class <code>RequestProcessingStrategy</code> takes care of creating
 * instances of the various strategies based on the policies of the POA
 *
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */
public abstract class RequestProcessingStrategy {

    // --- Type constants ---
    public static int DEFAULT_SERVANT = 0;
    public static int SERVANT_MANAGER = 1;
    public static int ACTIVE_OBJECT_MAP = 2;
    public static int SERVANT_LOCATOR = 3;
    public static int SERVANT_ACTIVATOR = 4;

    /**
     * <code> init <code> creates the appropriate request processing strategy
     * based on the policies specified in the POA.
     * @param policy specifies the policy of the POA for which the strategy is constructed.
     * @param uniquenessStrategy is used by the RequestProcessingStrategy for validation.
     * @param threadStrategy specifies if the POA is Suinglethreaded or not.
     * @param retentionStrategy is used by the RequestProcessingStrategy for validation.
     * @exception org.omg.PortableServer.POAPackage.InvalidPolicy
     * If the policies used to create this POA are in conflict.
     */
    public static RequestProcessingStrategy init( org.omg.CORBA.Policy[] policy, ServantRetentionStrategy retentionStrategy,
            IdUniquenessStrategy uniquenessStrategy, ThreadPolicyStrategy threadStrategy , org.omg.CORBA.IntHolder ih ){

        if (PolicyUtils.useServantManagerPolicy(policy)) {
            try {
                retentionStrategy.validate( retentionStrategy.RETAIN , ih );
                if( ih.value != POARunnable.NoException ){ return; }

                ServantActivatorStrategy activator = Reatin(ServantActivatorStrategy)
                        POAPolicyFactory.createPolicy(ZenProperties.getProperty(RequestProcessingStrategy.servantActivatorPath));

                activator.initialize(retentionStrategy, threadStrategy,
                        uniquenessStrategy);
                return activator;    
            } catch (Exception ex) {
                try {
                    retentionStrategy.validate(retentionStrategy.NON_RETAIN);
                    ServantLocatorStrategy locator = (ServantLocatorStrategy)
                            POAPolicyFactory.createPolicy(ZenProperties.getProperty(RequestProcessingStrategy.servantLocatorPath));

                    return locator;
                } catch (Exception exception) {
                    throw new org.omg.PortableServer.POAPackage.InvalidPolicy();
                }
            }
        }

        if (Util.useDefaultServantPolicy(policy)) {

            if (uniquenessStrategy.validate(IdUniquenessStrategy.UNIQUE_ID)) {
                throw new org.omg.PortableServer.POAPackage.InvalidPolicy();
            }

            try {
                DefaultServantStrategy servant = (DefaultServantStrategy)
                        POAPolicyFactory.createPolicy(ZenProperties.getProperty(RequestProcessingStrategy.defaultServantPath));

                servant.initialize(retentionStrategy, threadStrategy);
                return servant;
            } catch (Exception ex2) {
                throw new org.omg.PortableServer.POAPackage.InvalidPolicy();
            }
        }

        ActiveObjectMapOnlyStrategy aom = (ActiveObjectMapOnlyStrategy)
                POAPolicyFactory.createPolicy(ZenProperties.getProperty
                (RequestProcessingStrategy.aomPath));

        aom.initialize(retentionStrategy, threadStrategy);
        return aom;
    }

    /**
     * <code> setInvokeHandler </code> initialized the appropriate handler that
     * would service the request for the POA. E.g An activeObject Map,
     * Default-Servant or a Servant Manager.
     * @param invokeHandler is used to initialize the handler.
     * @exception org.omg.PortableServer.POAPackage.WrongPolicy is thrown if a
     * wrong handler is passed as an argument. E.g if a Servant is passed
     * when an Active Object Map is expected.
     */
    public abstract void setInvokeHandler(java.lang.Object invokeHandler , org.omg.CORBA.IntHolder ih );
 
    /**
     * <code> handleRequest </code> performs the upcall on the servant,
     * based on the type of strategy that is that is used in the POA.
     * @param request is the ServerRequest from which the operation name and
     * the parameters are demarshalled.
     * @param poa specifies the POA that is invoking the upcall on the servant
     * via its Request Processing Strategy.
     * @param requests gives the current number of requests currently in the POA.
     * @return signifies the successful completion of request/internal errors,
     Forward Request Exceptions
     */
    public abstract int handleRequest(ServerRequest request, edu.uci.ece.zen.poa.POA poa, edu.uci.ece.zen.poa.SynchronizedInt requests); 
}

