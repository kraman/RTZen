package edu.uci.ece.zen.orb.policies;

import org.omg.CORBA.PolicyCurrent;
import org.omg.CORBA.Policy;
import org.omg.CORBA.SetOverrideType;
import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;
import org.omg.RTCORBA.*;

/**
 * This class implements the PolicyCurrent
 * @author Mark Panahi
 * @version 1.0
 */

public class PolicyCurrentImpl
    extends org.omg.CORBA.LocalObject
    implements PolicyCurrent
{

    ORB orb;

    public void init(ORB orb){
        //orbMemoryArea = RealtimeThread.getCurrentMemoryArea();
        this.orb = orb;
    }


    /**
     * Operation get_policy_overrides
     */
    public Policy[] get_policy_overrides(int[] ts){

        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * Operation set_policy_overrides
     */
    public void set_policy_overrides(Policy[] policies, SetOverrideType set_add)
        throws org.omg.CORBA.InvalidPolicies{
        throw new org.omg.CORBA.NO_IMPLEMENT();

    }
}