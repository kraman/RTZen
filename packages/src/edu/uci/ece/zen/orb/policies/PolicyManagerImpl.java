package edu.uci.ece.zen.orb.policies;

import org.omg.CORBA.PolicyManager;
import org.omg.CORBA.Policy;
import org.omg.CORBA.SetOverrideType;
import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;
import org.omg.RTCORBA.*;

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
    //private MemoryArea orbMemoryArea;
    ORB orb;

    public PolicyManagerImpl(ORB orb){
        //orbMemoryArea = RealtimeThread.getCurrentMemoryArea();
        this.orb = orb;
    }

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

        for(int i = 0; i < policies.length; ++i){
            if(policies[i] instanceof ClientProtocolPolicy){
                Protocol [] protocols = ((ClientProtocolPolicy)policies[i]).protocols();
                for(int j = 0; j < protocols.length; ++j){
                    if(protocols[j].protocol_type == org.omg.IOP.TAG_INTERNET_IOP.value)
                        setTCPProps(protocols[j]);

                }

            }else if(policies[i] instanceof ServerProtocolPolicy){
                throw new org.omg.CORBA.NO_IMPLEMENT();
            }




        }
    /*
        if(set_add.value() == SetOverrideType._ADD_OVERRIDE){
            //throw new org.omg.CORBA.NO_IMPLEMENT();
        }else{  // if(set_add.value() == SetOverrideType._SET_OVERRIDE){
            //this.policies = policies;
            //this.policies = orbMemoryArea.newArray(Policy.class, policies.length);
        }
        */
    }

    private void setTCPProps(Protocol protocol){

        TCPProtocolProperties tcpPP = (TCPProtocolProperties)(protocol.transport_protocol_properties); //kludge

/*
        if(policy.policy_type() == SERVER_PROTOCOL_POLICY_TYPE.value){
            ServerProtocolPolicyImpl spol = (ServerProtocolPolicyImpl)policy;
            tcpPP = (TCPProtocolProperties)(spol.protocols()[0].transport_protocol_properties); //kludge
        }else if(policy.policy_type() == CLIENT_PROTOCOL_POLICY_TYPE.value){
            ClientProtocolPolicyImpl cpol = (ClientProtocolPolicyImpl)policy;
            tcpPP = (TCPProtocolProperties)(cpol.protocols()[0].transport_protocol_properties); //kludge
        }else{
            ZenProperties.logger.log(
                            Logger.FATAL, "wrong policy",
                            " ", " ");
        }
*/
        send_buffer_size = tcpPP.send_buffer_size();
        recv_buffer_size = tcpPP.recv_buffer_size();
        keep_alive = tcpPP.keep_alive();
        dont_route = tcpPP.dont_route();
        no_delay = tcpPP.no_delay();
/*

        ExecuteInRunnable r1 = new ExecuteInRunnable();
        TCPProtocolPropertiesRunnable tcprun = new TCPProtocolPropertiesRunnable();
        //tcprun.init(
        r1.init(tcprun, orb.orbImplRegion);
        try{
            orb.parentMemoryArea.executeInArea(r1);
        }catch( Exception e ){
            ZenProperties.logger.log(
                Logger.FATAL, "edu.uci.ece.zen.orb.RTORBImpl",
                " ", " " + e.toString() );
            System.exit(-1);
        }
*/
    }
    //tcp protocol properties
    public int send_buffer_size;
    public int recv_buffer_size;
    public boolean keep_alive;
    public boolean dont_route;
    public boolean no_delay;

    class TCPProtocolPropertiesRunnable implements Runnable{

        //tcp protocol properties
        public int send_buffer_size;
        public int recv_buffer_size;
        public boolean keep_alive;
        public boolean dont_route;
        public boolean no_delay;

        public void init(int send_buffer_size, int recv_buffer_size, boolean keep_alive, boolean dont_route, boolean no_delay){
            this.send_buffer_size = send_buffer_size;
            this.recv_buffer_size = recv_buffer_size;
            this.keep_alive = keep_alive;
            this.dont_route = dont_route;
            this.no_delay = no_delay;
        }

        public void run(){
            /*
            RTORBImpl.this.send_buffer_size = send_buffer_size;
            RTORBImpl.this.recv_buffer_size = recv_buffer_size;
            RTORBImpl.this.keep_alive = keep_alive;
            RTORBImpl.this.dont_route = dont_route;
            RTORBImpl.this.no_delay = no_delay;
            */
        }
    }
}

//class PolicyComparitor
