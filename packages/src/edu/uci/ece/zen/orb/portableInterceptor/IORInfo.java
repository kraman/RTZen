/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

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

