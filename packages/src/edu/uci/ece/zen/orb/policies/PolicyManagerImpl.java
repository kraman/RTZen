package edu.uci.ece.zen.orb.policies;

import org.omg.CORBA.*;

/**
 * This class implements the Policy Manager
 * @author Mark Panahi
 * @version 1.0
 */

public class PolicyManagerImpl
    extends org.omg.CORBA.LocalObject
    implements PolicyManager
{
    private Policy[] policies;

    /**
     * Operation get_policy_overrides
     */
    public Policy[] get_policy_overrides(int[] ts){

        //java.util.Arrays.sort(ts);

        if(ts == null || ts.length == 0)
            return policies;

        int numMatching = 0;
        //I'm assuming that the lists are *very* small here
        //otherwise we shouyld sort both lists first
        for(int j = 0; j < ts.length; ++j){
            for(int i = 0; i < policies.length; ++i){
                if(ts[j] == policies[i].policy_type())
                    numMatching++;//pList[j] = policies[i];
            }
        }

        Policy[] pList = new Policy[numMatching];
        int count = 0;
        for(int j = 0; j < ts.length; ++j){
            for(int i = 0; i < policies.length; ++i){
                if(ts[j] == policies[i].policy_type())
                    pList[count++] = policies[i];
            }
        }

        return pList;
    }

    /**
     * Operation set_policy_overrides
     */
    public void set_policy_overrides(Policy[] policies, SetOverrideType set_add)
        throws org.omg.CORBA.InvalidPolicies{

        if(set_add.value() == SetOverrideType._ADD_OVERRIDE){
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }else{
            this.policies = policies;
        }
    }

}

//class PolicyComparitor