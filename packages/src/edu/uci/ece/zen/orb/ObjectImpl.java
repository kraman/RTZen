package edu.uci.ece.zen.orb;

public class ObjectImpl extends org.omg.CORBA.portable.ObjectImpl {
    public org.omg.IOP.IOR ior;

    private String[] ids;

    public ObjectImpl() {
        ids = new String[1];
    }

    public void init(org.omg.IOP.IOR ior) {
        this.ior = ior;
        ids[0] = ior.type_id;
    }

    public String[] _ids() {
        return ids;
    }
}

