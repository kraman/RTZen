package edu.uci.ece.zen.poa;


/**
 * All the Utility functions related to the Policies in the POA are here
 * The static methods in this class are used in the creation of the appropriate
 * strategies in the POA.
 * @author <a href="mailto:krishnaa@uci.edu">Arvind S. Krishna</a>
 * @version 1.0
 */
public class PolicyUtils{

    /**
     * This method returns true if the POA has RETAIN policy.
     * The default policy for any poa is the retain policy.
     * @param policyList The policy list.
     * @return boolean True if the POA has retain policy.
     */
    public static boolean useRetainPolicy(org.omg.CORBA.Policy[] policyList) {
        if (policyList == null) {
            return true;
        }

        for (int i = 0; i < policyList.length; i++) {
            if (policyList[i] instanceof edu.uci.ece.zen.poa.policy.ServantRetentionPolicy) {
                if (((edu.uci.ece.zen.poa.policy.ServantRetentionPolicy) policyList[i]).int_value()
                        == org.omg.PortableServer.ServantRetentionPolicyValue._RETAIN) {
                    System.out.println ("Util found Retain policy");
                    return true;
                } else {
                    System.out.println ("Util found Non-Retain policy");
                    return false;
                }
            }
        }

        // return the default case
        return true;
    }

    /**
     * This methos returns true if the POA has ActiveObjectMapOnly policy.
     * The default case is the AOM hence this function returns false for the default case.
     * @param policyList The policy list.
     * @return boolean True if the POA has AOM policy.
     */
    public static boolean useServantManagerPolicy(org.omg.CORBA.Policy[] policyList) {
        if (policyList == null) {
            return false;
        }

        for (int i = 0; i < policyList.length; i++) {
            if (policyList[i] instanceof edu.uci.ece.zen.poa.policy.RequestProcessingPolicy) {
                if (((edu.uci.ece.zen.poa.policy.RequestProcessingPolicy) policyList[i]).int_value()
                        == org.omg.PortableServer.RequestProcessingPolicyValue._USE_SERVANT_MANAGER) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * This method is to  go through the list of policies that are ther in the list and find if the
     * Single Thread Policy is present.
     * @param policyList  The policy list.
     * @return boolean True if the POA has SingleThreadpolicy.
     */
    public static boolean useSingleThreadedPolicy(org.omg.CORBA.Policy[] policyList) {
        if (policyList == null) {
            return false;
        }

        for (int i = 0; i < policyList.length; i++) {
            if (policyList[i] instanceof edu.uci.ece.zen.poa.policy.ThreadPolicy) {

                if (((edu.uci.ece.zen.poa.policy.ThreadPolicy) policyList[i]).int_value()
                        == org.omg.PortableServer.ThreadPolicyValue._SINGLE_THREAD_MODEL) {

                    return true;
                } else {
                    return false;
                }
            }
        }

        // return the default case in for the Thread Policy in the POA
        return false;
    }

    /**
     * This methos returns true if the POA has UseDefaultServantPlicy.
     * The default case for the poa is AOM so this should return false for that.
     * @param policyList The policy list.
     * @return boolean True if the POA has UseDefaultServantPolicy.
     */
    public static boolean useDefaultServantPolicy(org.omg.CORBA.Policy[] policyList) {
        if (policyList == null) {
            return false;
        }

        for (int i = 0; i < policyList.length; i++) {
            if (policyList[i] instanceof edu.uci.ece.zen.poa.policy.RequestProcessingPolicy) {
                if (((edu.uci.ece.zen.poa.policy.RequestProcessingPolicy) policyList[i]).int_value()
                        == org.omg.PortableServer.RequestProcessingPolicyValue._USE_DEFAULT_SERVANT) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * This methos returns true if the POA has SystemIdPOlicy
     * The default case for the IdAsignment Policy is SystemId. This function should return true for the default case.
     * @param policyList The policy list.
     * @return boolean True if the POA has SystemIdPOlicy.
     */
    public static boolean useSystemIdPolicy(org.omg.CORBA.Policy[] policyList) {
        if (policyList == null) {
            return true;
        }

        for (int i = 0; i < policyList.length; i++) {
            if (policyList[i] instanceof edu.uci.ece.zen.poa.policy.IdAssignmentPolicy) {
                if (((edu.uci.ece.zen.poa.policy.IdAssignmentPolicy) policyList[i]).int_value()
                        == org.omg.PortableServer.IdAssignmentPolicyValue._SYSTEM_ID) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * This methos returns true if the POA has UniqueIdPolicy.
     * The default case for the Iduniqueness Strategy is Unique id.. So this function should return true for the default case.
     * @param policyList The policy list.
     * @return boolean True if the POA has UniqueIdPolicy..
     */
    public static boolean useUniqueIdPolicy(org.omg.CORBA.Policy[] policyList) {
        if (policyList == null) {
            return true;
        }

        for (int i = 0; i < policyList.length; i++) {
            if (policyList[i] instanceof edu.uci.ece.zen.poa.policy.IdUniquenessPolicy) {
                if (((edu.uci.ece.zen.poa.policy.IdUniquenessPolicy) policyList[i]).int_value()
                        == org.omg.PortableServer.IdUniquenessPolicyValue._UNIQUE_ID) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * This methos returns true if the POA has ImplicitActivationPolicy.
     * The default case for the ImplicitActivationStrategy is Implicit_Activation.. So this function should return true for the default case.
     * @param policyList The policy list.
     * @return boolean True if the POA has implicitActivationPolicy.
     */
    public static boolean useImplicitActivationPolicy(org.omg.CORBA.Policy[] policyList) {
        if (policyList == null) {
            return true;
        }

        for (int i = 0; i < policyList.length; i++) {
            if (policyList[i] instanceof edu.uci.ece.zen.poa.policy.ImplicitActivationPolicy) {
                if (((edu.uci.ece.zen.poa.policy.ImplicitActivationPolicy) policyList[i]).int_value()
                        == org.omg.PortableServer.ImplicitActivationPolicyValue._IMPLICIT_ACTIVATION) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return true;

    }


    /**
     * This methos returns true if the POA has Transient.
     * The default policy for a poa is transient policy. hence this function should return true for the default case.

     * @param policyList The policy list.
     * @return boolean True if the POA has Transient.
     */
    public static boolean useTransientPolicy(org.omg.CORBA.Policy[] policyList) {
        if (policyList == null) {
            return true;
        }

        for (int i = 0; i < policyList.length; i++) {
            if (policyList[i] instanceof edu.uci.ece.zen.poa.policy.LifespanPolicy) {
                if (((edu.uci.ece.zen.poa.policy.LifespanPolicy) policyList[i]).int_value()
                        == org.omg.PortableServer.LifespanPolicyValue._TRANSIENT) {

                    return true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }
}
