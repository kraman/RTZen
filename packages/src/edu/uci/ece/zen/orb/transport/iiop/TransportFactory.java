package edu.uci.ece.zen.orb.transport.iiop;

import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.orb.ConnectionRegistry;
import edu.uci.ece.zen.orb.CDRInputStream;
import edu.uci.ece.zen.orb.ObjectImpl;
import edu.uci.ece.zen.orb.ObjRefDelegate;
import javax.realtime.*;
import org.omg.IOP.TaggedProfile;

public class TransportFactory extends edu.uci.ece.zen.orb.transport.TransportFactory{
    public void internalInit(){}
    public void createAcceptorImpl( FString args ){
    }
    public void connectImpl( TaggedProfile profile , ObjectImpl obj , ObjRefDelegate objRefDelegate , boolean isCollocated ){
        byte[] data = profile.profile_data;
        CDRInputStream in = CDRInputStream.fromOctetSeq(data, orbImpl.orbFacade );
        byte iiopMinor = data[2];
        FString host = null;
        short port = -1;
        FString object_key = null;

        switch (iiopMinor) {
            case 0: {
                    ZenProperties.logger.log("ObjRefDel processTaggedProfile IIOPv1.0 1");
                    in.read_octet(); //iiop major
                    in.read_octet(); //iiop minor

                    host = in.getBuffer().readFString(true);
                    port = in.read_ushort();
                    object_key = in.getBuffer().readFString(false);
                }
                break;
            case 1:
            case 2: {
                    in.read_octet(); //iiop major
                    in.read_octet(); //iiop minor

                    host = in.getBuffer().readFString(true);
                    port = in.read_ushort();
                    object_key = in.getBuffer().readFString(false);

                    int numComp = in.read_ulong();
                    if (ZenProperties.dbg) ZenProperties.logger.log("number of components: " + numComp);
                    for (int i = 0; i < numComp; ++i) {
                            int ctag = in.read_ulong();
                            if (ZenProperties.dbg) ZenProperties.logger.log("found tag: " + ctag);
                            if (ctag == org.omg.IOP.TAG_ORB_TYPE.value) {
                                int byteLen = in.read_ulong();
                                in.read_boolean(); //endianess
                                int orbType = in.read_ulong();
                                if (ZenProperties.dbg) ZenProperties.logger.log("ORB type: " + orbType);
                            } else if (ctag == org.omg.IOP.TAG_CODE_SETS.value) {
                                //just eat for now
                                int byteLen = in.read_ulong();
                                for(int i1 = 0; i1 < byteLen; ++i1)
                                    in.read_octet();
                            } else if (ctag == org.omg.IOP.TAG_POLICIES.value) {
                                orbImpl.processPolicyTagComponent( in );
                            }else{
                                //just eat if we don't know the type
                                int byteLen = in.read_ulong();
                                for(int i1 = 0; i1 < byteLen; ++i1)
                                    in.read_octet();
                            }
                        }
                    }
                    break;
        }
        if( port != -1 ){
            long connectionKey = ConnectionRegistry.ip2long(host, port);
            ScopedMemory transportScope = orbImpl.orbFacade.getConnectionRegistry().getConnection(connectionKey);

            if (transportScope == null) {
                transportScope = edu.uci.ece.zen.orb.transport.iiop.Connector.instance().connect(host, port, orbImpl.orbFacade, orbImpl);
                orbImpl.orbFacade.getConnectionRegistry().putConnection(connectionKey, transportScope);
                objRefDelegate.addLaneData(
                        javax.realtime.PriorityScheduler.instance().getMinPriority() ,
                        javax.realtime.PriorityScheduler.instance().getMaxPriority() ,
                        transportScope, object_key, edu.uci.ece.zen.orb.protocol.giop.GIOPMessageFactory.class );
            } else {
                if( host != null )
                    FString.free(host);
            }
        }
        in.free();
    }
}
