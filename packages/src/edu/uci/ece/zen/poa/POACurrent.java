package edu.uci.ece.zen.poa;

import edu.uci.ece.zen.utils.FString;

public class POACurrent extends org.omg.CORBA.LocalObject implements
        org.omg.PortableServer.Current {
    private org.omg.PortableServer.Servant servant;
    private FString okey;
    private org.omg.PortableServer.POA poa;

    /** Creates a new instance of POACurrent */
    public POACurrent() {
    }

    public void init(org.omg.PortableServer.POA poa, FString okey,
            org.omg.PortableServer.Servant servant) {
        this.poa = poa;
        this.okey = okey;
        this.servant = servant;
    }

    public org.omg.PortableServer.Servant get_servant()
            throws org.omg.PortableServer.CurrentPackage.NoContext {
        return this.servant;
    }

    public org.omg.CORBA.Object get_reference()
            throws org.omg.PortableServer.CurrentPackage.NoContext {
        return null;
    }

    public byte[] get_object_id()
            throws org.omg.PortableServer.CurrentPackage.NoContext {
        return okey.getTrimData();
    }

    public org.omg.PortableServer.POA get_POA()
            throws org.omg.PortableServer.CurrentPackage.NoContext {
        return this.poa;
    }
}
