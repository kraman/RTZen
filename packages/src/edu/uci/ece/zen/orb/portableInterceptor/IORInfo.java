package edu.uci.ece.zen.orb.portableInterceptor;


public class IORInfo extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.IORInfo {
    public org.omg.CORBA.Policy get_effective_policy(int type) {
        return null;
    }

    public void add_ior_component(org.omg.IOP.TaggedComponent component) {
        //profile.addTaggedComponent(component);
    }

    public void add_ior_component_to_profile(org.omg.IOP.TaggedComponent component, int profile_id) {}

    // should be profile list, but we'll assume one iiop profile for now
    //public edu.uci.ece.zen.orb.protocols.Profile profile;
}

