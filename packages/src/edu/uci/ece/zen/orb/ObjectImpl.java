package edu.uci.ece.zen.orb;

import javax.realtime.*;

public class ObjectImpl extends org.omg.CORBA.portable.ObjectImpl{
    public org.omg.IOP.IOR ior;
    
    public ObjectImpl( org.omg.IOP.IOR ior )
    {
        this.ior = ior;
    }

    public String[] _ids(){
        //TODO: Return Id's here
        return null;
    }
}


