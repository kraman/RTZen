package edu.uci.ece.zen.orb;

import javax.realtime.*;

public class ObjectImpl extends org.omg.CORBA.portable.ObjectImpl{
    public org.omg.IOP.IOR ior;
    private String[] ids;
    
    public ObjectImpl( org.omg.IOP.IOR ior )
    {
        this.ior = ior;
        ids = new String[1];
        ids[0] = ior.type_id;
    }

    public String[] _ids(){
        return ids;
    }
}


