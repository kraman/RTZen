/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */


package edu.uci.ece.zen.orb.portableInterceptor;





public class ORBInitInfo extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.ORBInitInfo {
    private int slot_id;
    public ORBInitInfo(edu.uci.ece.zen.orb.ORB orb, InterceptorList list) {
        this.orb = orb;
        this.list = list;
        slot_id = 0;
    }

    public java.lang.String[] arguments() {
//        return orb._args;
        return null;
    }

    public java.lang.String orb_id() {
        return "RTZen";
    }

    public org.omg.IOP.CodecFactory codec_factory() {
        try{

            return org.omg.IOP.CodecFactoryHelper.narrow(orb.resolve_initial_references("CodecFactory"));

        }catch(org.omg.CORBA.ORBPackage.InvalidName iname){
            //iname.printStackTrace();*/
            return null;
        }

    }

    public void register_initial_reference(java.lang.String id, org.omg.CORBA.Object obj)
        throws org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName
    {
//        if (id == null)
//            throw new org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName();
//
//        edu.uci.ece.zen.orb.resolvers.Resolver r = orb.resolverList.find(id);
//
//        if (r == null)
//            throw new org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName("Couldn't register");
//
//        r.setResolver(obj);
    }

    public org.omg.CORBA.Object resolve_initial_references(java.lang.String id)
        throws org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName {
        try {
            return orb.resolve_initial_references(id);
        } catch (org.omg.CORBA.ORBPackage.InvalidName in) {
            throw new org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName(id);
        }
    }

    public void add_client_request_interceptor(org.omg.PortableInterceptor.ClientRequestInterceptor interceptor)
        throws org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName {
        list.add_client_request_interceptor(interceptor);
    }

    public void add_server_request_interceptor(org.omg.PortableInterceptor.ServerRequestInterceptor interceptor)
        throws org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName {
        list.add_server_request_interceptor(interceptor);
    }

    public void add_ior_interceptor(org.omg.PortableInterceptor.IORInterceptor interceptor)
        throws org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName {
        list.add_ior_interceptor(interceptor);
    }

    public int allocate_slot_id() {
        return slot_id++;
    }

    public void register_policy_factory(int type, org.omg.PortableInterceptor.PolicyFactory policy_factory) {}

    private InterceptorList list;
    private edu.uci.ece.zen.orb.ORB orb;
}
