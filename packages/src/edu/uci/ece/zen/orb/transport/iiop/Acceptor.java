package edu.uci.ece.zen.orb.transport.iiop;

import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;
import org.omg.IOP.*;
import org.omg.IIOP.*;

public class Acceptor extends edu.uci.ece.zen.orb.transport.Acceptor{
    private java.net.ServerSocket ssock;

    public Acceptor( edu.uci.ece.zen.orb.ORB orb ){
        super( orb );
        try{
            ssock = new java.net.ServerSocket();
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
            Transport t = new Transport( orb , ssock.accept() );
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

        Version version = new Version(iiopMajorVersion, iiopMinorVersion);

        ProfileBody_1_0 pb10 = new ProfileBody_1_0(version, ssock.getInetAddress().getHostAddress(), (short)ssock.getLocalPort(), objKey);

        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);
        out.write_boolean(false); //BIGENDIAN

        ProfileBody_1_0Helper.write(out, pb10);

        TaggedProfile tp = new TaggedProfile();
        tp.tag = TAG_INTERNET_IOP.value;
        tp.profile_data = new byte[(int)out.getBuffer().getLimit()];
        out.getBuffer().readByteArray(tp.profile_data, 0 , (int)out.getBuffer().getLimit());

        out.free();

        return tp;
    }
}
