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
        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);
        out.write_boolean(false); //BIGENDIAN

        switch(iiopMinorVersion){
            case 0:

                ProfileBody_1_0 pb10 = new ProfileBody_1_0(version, ssock.getInetAddress().getHostAddress(), (short)ssock.getLocalPort(), objKey);
                ProfileBody_1_0Helper.write(out, pb10);

            break;
            case 1:
                org.omg.IOP.TaggedComponent[] components = new org.omg.IOP.TaggedComponent[0];
                //TODO: insert rt policy info and other tagged components
                ProfileBody_1_1 pb11 = new ProfileBody_1_1(version, ssock.getInetAddress().getHostAddress(), (short)ssock.getLocalPort(), objKey, components);
                ProfileBody_1_1Helper.write(out, pb11);

            break;
        }

        TaggedProfile tp = new TaggedProfile();
        tp.tag = TAG_INTERNET_IOP.value;
        tp.profile_data = new byte[(int)out.getBuffer().getLimit()];
        out.getBuffer().readByteArray(tp.profile_data, 0 , (int)out.getBuffer().getLimit());

        out.free();

        return tp;
    }
}
