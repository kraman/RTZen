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
import edu.uci.ece.zen.utils.ZenBuildProperties;

import org.omg.RTCORBA.PRIORITY_MODEL_POLICY_TYPE;

public class Acceptor extends edu.uci.ece.zen.orb.transport.Acceptor {
    private java.net.ServerSocket ssock;
    private int threadPoolId;

    public Acceptor(edu.uci.ece.zen.orb.ORB orb,
            edu.uci.ece.zen.orb.ORBImpl orbImpl,
            int threadPoolId) {
        super(orb, orbImpl, threadPoolId);
        try {
            ssock = new java.net.ServerSocket(0, 0, null);

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
        if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("Acceptor version " + version);
        switch (iiopMinorVersion) {
            case 0:
                if (ZenBuildProperties.dbgIOR) {
                    ZenProperties.logger.log("Acceptor, the current memoery is :" + javax.realtime.RealtimeThread .getCurrentMemoryArea());
                    ZenProperties.logger.log("Acceptor, the memory of ssock is " + javax.realtime.MemoryArea .getMemoryArea(ssock));
                    ZenProperties.logger.log("Acceptor getHostAddress" + ssock.getInetAddress().getHostAddress());
                    ZenProperties.logger.log("Acceptor getLocalPort()" + (short) ssock.getLocalPort());
                    ZenProperties.logger.log("Acceptor objKey" + objKey);
                }
                ProfileBody_1_0 pb10 = new ProfileBody_1_0(version, ssock.getInetAddress().getHostAddress(), (short) ssock.getLocalPort(), objKey);
                ProfileBody_1_0Helper.write(out, pb10);
                break;
            case 1:
            case 2:
                //org.omg.IOP.TaggedComponent[] components = new
                // org.omg.IOP.TaggedComponent[0];
                //TODO: insert rt policy info and other tagged components
                ProfileBody_1_1 pb11 = new ProfileBody_1_1(version, ssock.getInetAddress().getHostAddress(), (short) ssock.getLocalPort(), objKey, getComponents());
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

}
