/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.portableInterceptor;

import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.CORBA.Any;

public class PICurrent extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.Current {
    private Any[] slots;
    private org.omg.CORBA.ORB _orb;

    public PICurrent(org.omg.CORBA.ORB orb, int cap) {
        slots = new Any[cap];
        _orb = orb;
    }

    public Any get_slot(int id)
        throws InvalidSlot {
        if (id >= slots.length || id < 0) {
            throw new InvalidSlot();
        }
        if(slots[id] == null)
            slots[id] = _orb.create_any();
        return slots[id];
    }

    public void set_slot(int id, Any data)
        throws InvalidSlot {
        slots[id] = data;
    }
}
