package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;

public class ObjectImpl extends org.omg.CORBA.portable.ObjectImpl {
    public org.omg.IOP.IOR ior;

    private String[] ids;

    public ObjectImpl() {
        ids = new String[1];
    }

    public void init(org.omg.IOP.IOR ior) {
        this.ior = ior;
        ids[0] = ior.type_id;
        if (ZenBuildProperties.dbgIOR){
            System.out.println("It's in ObjectImpl.init() and the type id is "+ids[0]);
        }
    }

    public String[] _ids() {
        return ids;
    }

    protected void finalize(){
        //System.out.println("WWFinalize called");
        _release();
    }
}
