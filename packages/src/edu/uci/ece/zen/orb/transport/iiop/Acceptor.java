package edu.uci.ece.zen.orb.transport.iiop;

import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;
import org.omg.IOP.*;
import org.omg.IIOP.*;
import org.omg.RTCORBA.*;
import org.omg.Messaging.*;

public class Acceptor extends edu.uci.ece.zen.orb.transport.Acceptor{
    private java.net.ServerSocket ssock;

    public Acceptor( edu.uci.ece.zen.orb.ORB orb , edu.uci.ece.zen.orb.ORBImpl orbImpl ){
        super( orb , orbImpl );
        try{
            ssock = new java.net.ServerSocket(0);
        }catch( Exception ex ){
            ZenProperties.logger.log(
                Logger.WARN,
                "edu.uci.ece.zen.orb.transport.iiop.Acceptor",
                "<cinit>",
                "Error binding to post. " + ex.toString() );
        }
    }

    protected void accept(){
        try{
            Transport t = new Transport( orb , orbImpl , ssock.accept() );
            registerTransport( t );
        }catch( java.io.IOException ioex ){
            ZenProperties.logger.log(
                Logger.WARN,
                "edu.uci.ece.zen.orb.transport.iiop.Acceptor",
                "accept",
                "IOException occured " + ioex.toString() );
        }
    }

    protected void internalShutdown(){
    }

    protected TaggedProfile getInternalProfile( byte iiopMajorVersion , byte iiopMinorVersion, byte[] objKey){
        System.out.println("yuez in Acceptor 1");   
        Version version = new Version(iiopMajorVersion, iiopMinorVersion);
        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);
        out.write_boolean(false); //BIGENDIAN
        System.out.println("yuez in Acceptor 2");
        edu.uci.ece.zen.utils.Logger.printThreadStack();
 
        switch(iiopMinorVersion){
            case 0:

             System.out.println("yuez in Acceptor 2.1");

             System.out.println("yuez in Acceptor version "+version);

             System.out.println("yuez in Acceptor, the current memoery is :"+javax.realtime.RealtimeThread.getCurrentMemoryArea());

             System.out.println("yuez in Acceptor, the memory of ssock is "+ javax.realtime.MemoryArea.getMemoryArea(ssock) );
             
             System.out.println("yuez in Acceptor getHostAddress"+ssock.getInetAddress().getHostAddress());

             System.out.println("yuez in Acceptor getLocalPort()"+(short)ssock.getLocalPort());

             System.out.println("yuez in Acceptor objKey"+objKey);    
             
              
             

                ProfileBody_1_0 pb10 = new ProfileBody_1_0(version, ssock.getInetAddress().getHostAddress(), (short)ssock.getLocalPort(), objKey);
                System.out.println("yuez in Acceptor 2.2");
 
               ProfileBody_1_0Helper.write(out, pb10);

            break;
            case 1:
                //org.omg.IOP.TaggedComponent[] components = new org.omg.IOP.TaggedComponent[0];
                //TODO: insert rt policy info and other tagged components

                System.out.println("yuez in Acceptor 2.3");
                 
                ProfileBody_1_1 pb11 = new ProfileBody_1_1(version, ssock.getInetAddress().getHostAddress(), (short)ssock.getLocalPort(), objKey, getComponents());

                System.out.println("yuez in Acceptor 2.4");
                 
                ProfileBody_1_1Helper.write(out, pb11);

            break;
        }

        System.out.println("yuez in Acceptor 3");
         
        
        WriteBuffer outb = out.getBuffer();
        ReadBuffer outrb = outb.getReadBuffer();
        
        TaggedProfile tp = new TaggedProfile();
        tp.tag = TAG_INTERNET_IOP.value;
        tp.profile_data = new byte[(int)outrb.getLimit()];
        outrb.readByteArray(tp.profile_data, 0 , (int)outrb.getLimit());

        out.free();
        outrb.free();

        return tp;
    }

    private TaggedComponent[] getComponents(){
        return new TaggedComponent[0];
/*
        TaggedComponent[] tcarr = new TaggedComponent[1];

        //tcarr[0].tag = SERVER_PROTOCOL_POLICY_TYPE.value;

        tcarr[0] = getPolicyComponent();

        return tcarr;
        */
    }

    private TaggedComponent getPolicyComponent(){
        TaggedComponent tc = new TaggedComponent();
        tc.tag = org.omg.IOP.TAG_POLICIES.value;

        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);
        out.write_boolean(false); //BIGENDIAN

        PolicyValueHelper.write(out, createServerProtocolPolicyValue());

        tc.component_data = new byte[(int)out.getBuffer().getLimit()];
        out.getBuffer().readByteArray(tc.component_data, 0 , (int)out.getBuffer().getLimit());
        out.free();

        return tc;
    }

    private PolicyValue createServerProtocolPolicyValue(){
/*
        ServerProtocolPolicy spp = ((RTORBImpl)(orb.getRTORB())).spp;
        PolicyValue pv = new PolicyValue();
        pv.ptype = spp.policy_type();

        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);
        out.write_boolean(false); //BIGENDIAN

        org.omg.CORBA.Any value = edu.uci.ece.zen.orb.ORB.init().create_any();
        ServerProtocolPolicyHelper.insert(value, spp);
        out.write_any(value);
        pv.pvalue = new byte[(int)out.getBuffer().getLimit()];
        out.getBuffer().readByteArray(pv.pvalue, 0 , (int)out.getBuffer().getLimit());
        out.free();

        return pv;
        */
        return null;
    }
}
