package edu.uci.ece.zen.orb.transport.iiop;

import org.omg.IIOP.ProfileBody_1_0;
import org.omg.IIOP.ProfileBody_1_0Helper;
import org.omg.IIOP.ProfileBody_1_1;
import org.omg.IIOP.ProfileBody_1_1Helper;
import org.omg.IIOP.Version;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TaggedProfile;
import org.omg.Messaging.PolicyValue;
import org.omg.Messaging.PolicyValueHelper;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.utils.ZenProperties;

public class Acceptor extends edu.uci.ece.zen.orb.transport.Acceptor {
    private edu.uci.ece.zen.orb.transport.serial.ServerSocket ssock;

    public Acceptor(edu.uci.ece.zen.orb.ORB orb,
            edu.uci.ece.zen.orb.ORBImpl orbImpl) {
        super(orb, orbImpl);
        try {
            ssock = new edu.uci.ece.zen.orb.transport.serial.ServerSocket(0, 0, orb.sockAddr);

        } catch (Exception ex) {
            ZenProperties.logger.log(Logger.WARN,
                    getClass(), "<cinit>",
                    "Error binding to post.", ex);
        }
    }

    protected void accept() {
        try {
            Transport t = new Transport(orb, orbImpl, ssock.accept());
            registerTransport(t);
        } catch (java.io.IOException ioex) {
            ZenProperties.logger.log(Logger.WARN,
                    getClass(), "accept", ioex);
        }
    }

    protected void internalShutdown() {
    }

    protected TaggedProfile getInternalProfile(byte iiopMajorVersion,
            byte iiopMinorVersion, byte[] objKey) {
        Version version = new Version(iiopMajorVersion, iiopMinorVersion);
        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);
        out.write_boolean(false); //BIGENDIAN
        edu.uci.ece.zen.utils.Logger.printThreadStack();

        switch (iiopMinorVersion) {
            case 0:
                if (ZenProperties.devDbg) {
                    ZenProperties.logger.log("yuez in Acceptor 2.1");
                    if (ZenProperties.dbg) ZenProperties.logger.log("yuez in Acceptor version " + version);
                    if (ZenProperties.dbg) ZenProperties.logger.log("yuez in Acceptor, the current memoery is :"
                                    + javax.realtime.RealtimeThread
                                            .getCurrentMemoryArea());
                    if (ZenProperties.dbg) ZenProperties.logger.log("yuez in Acceptor, the memory of ssock is "
                                    + javax.realtime.MemoryArea
                                            .getMemoryArea(ssock));
                    if (ZenProperties.dbg) ZenProperties.logger.log("yuez in Acceptor getHostAddress"
                            + ssock.getInetAddress().getHostAddress());
                    if (ZenProperties.dbg) ZenProperties.logger.log("yuez in Acceptor getLocalPort()"
                            + (short) ssock.getLocalPort());
                    if (ZenProperties.dbg) ZenProperties.logger.log("yuez in Acceptor objKey" + objKey);
                }
                ProfileBody_1_0 pb10 = new ProfileBody_1_0(version, ssock
                        .getInetAddress().getHostAddress(), (short) ssock
                        .getLocalPort(), objKey);
                ProfileBody_1_0Helper.write(out, pb10);

                break;
            case 1:
                //org.omg.IOP.TaggedComponent[] components = new
                // org.omg.IOP.TaggedComponent[0];
                //TODO: insert rt policy info and other tagged components
                ProfileBody_1_1 pb11 = new ProfileBody_1_1(version, ssock
                        .getInetAddress().getHostAddress(), (short) ssock
                        .getLocalPort(), objKey, getComponents());
                ProfileBody_1_1Helper.write(out, pb11);

                break;
        }

        WriteBuffer outb = out.getBuffer();
        ReadBuffer outrb = outb.getReadBuffer();

        TaggedProfile tp = new TaggedProfile();
        tp.tag = TAG_INTERNET_IOP.value;
        tp.profile_data = new byte[(int) outrb.getLimit()];
        outrb.readByteArray(tp.profile_data, 0, (int) outrb.getLimit());

        out.free();
        outrb.free();

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
