package edu.uci.ece.zen.orb.transport.serial;

import org.omg.IIOP.ProfileBody_1_0;
import org.omg.IIOP.ProfileBody_1_0Helper;
import org.omg.IIOP.ProfileBody_1_1;
import org.omg.IIOP.ProfileBody_1_1Helper;
import org.omg.IIOP.Version;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TAG_SERIAL;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TaggedProfile;
import org.omg.Messaging.PolicyValue;
import org.omg.Messaging.PolicyValueHelper;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.FString;

public class Acceptor extends edu.uci.ece.zen.orb.transport.Acceptor {
    private NativeSerialPort sock = NativeSerialPort.instance();

    public Acceptor(edu.uci.ece.zen.orb.ORB orb, edu.uci.ece.zen.orb.ORBImpl orbImpl) {
        super(orb, orbImpl);
        System.err.println( "Serial transport Acceptor created" );
    }

    protected void accept() {
        //try {
            System.err.println( "Serial transport: accept() " );
            Transport t = new Transport(orb, orbImpl, sock.accept());
            registerTransport(t);
        //} catch (java.io.IOException ioex) {
        //    ZenProperties.logger.log(Logger.WARN, getClass(), "accept", ioex);
        //}
    }

    protected void internalShutdown() {
    }

    protected TaggedProfile getInternalProfile(byte iiopMajorVersion, byte iiopMinorVersion, byte[] objKey) {
        System.err.println( "Serial transport: getInternalProfile() " );
        //FString temp = FString.instance();
        WriteBuffer temp = WriteBuffer.instance();
        temp.setEndian(false);
        temp.writeByte((byte)0);
        temp.writeLong((int)objKey.length);
        temp.writeByteArray(objKey,0,objKey.length);
        TaggedProfile tp = new TaggedProfile();
        tp.tag = TAG_SERIAL.value;   
        tp.profile_data = new byte[(int)temp.getPosition()];//temp.getTrimData();
        ReadBuffer rb = temp.getReadBuffer();
        rb.readByteArray(tp.profile_data,0,(int)temp.getPosition());
        temp.free();
        rb.free();
        
        //FString.free(temp);
/*        
        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);
        //for some reason this already gets written
        out.write_boolean(false); //BIGENDIAN
        out.write_ulong(objKey.length);
        out.write_octet_array(objKey, 0, objKey.length);
        if (ZenProperties.dbg) ZenProperties.logger.log("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&Serial transport: getInternalProfile() obj key array size: " + objKey.length);
        if (ZenProperties.dbg) ZenProperties.logger.log("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&Serial transport: getInternalProfile() obj key marshaled size: " + out.getBuffer().getLimit());
        TaggedProfile tp = new TaggedProfile();
        tp.tag = TAG_SERIAL.value;
        tp.profile_data = new byte[(int)out.getBuffer().getLimit()];
        out.getBuffer().readByteArray(tp.profile_data, 0, (int) out.getBuffer().getLimit());
        out.free();
        
        for(int i = 0 ; i < tp.profile_data.length ; ++i)
            if (ZenProperties.dbg) ZenProperties.logger.log("************************************** SERIAL obj key" + tp.profile_data[i]);
                    
*/        
        return tp;
    }

    private TaggedComponent[] getComponents() {
        return new TaggedComponent[0];
        /*
         * TaggedComponent[] tcarr = new TaggedComponent[1]; //tcarr[0].tag =
         * SERVER_PROTOCOL_POLICY_TYPE.value; tcarr[0] = getPolicyComponent();
         * return tcarr;
         */
    }

    private TaggedComponent getPolicyComponent() {
        TaggedComponent tc = new TaggedComponent();
        tc.tag = org.omg.IOP.TAG_POLICIES.value;

        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);
        out.write_boolean(false); //BIGENDIAN

        PolicyValueHelper.write(out, createServerProtocolPolicyValue());

        tc.component_data = new byte[(int) out.getBuffer().getLimit()];
        out.getBuffer().readByteArray(tc.component_data, 0,
                (int) out.getBuffer().getLimit());
        out.free();

        return tc;
    }

    private PolicyValue createServerProtocolPolicyValue() {
        /*
         * ServerProtocolPolicy spp = ((RTORBImpl)(orb.getRTORB())).spp;
         * PolicyValue pv = new PolicyValue(); pv.ptype = spp.policy_type();
         * CDROutputStream out = CDROutputStream.instance(); out.init(orb);
         * out.write_boolean(false); //BIGENDIAN org.omg.CORBA.Any value =
         * edu.uci.ece.zen.orb.ORB.init().create_any();
         * ServerProtocolPolicyHelper.insert(value, spp); out.write_any(value);
         * pv.pvalue = new byte[(int)out.getBuffer().getLimit()];
         * out.getBuffer().readByteArray(pv.pvalue, 0 ,
         * (int)out.getBuffer().getLimit()); out.free(); return pv;
         */
        return null;
    }
}
