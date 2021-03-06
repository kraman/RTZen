/*******************************************************************************
 * *** Copyright (c) 1999 Object Management Group. Unlimited rights to duplicate
 * and use this code are hereby granted provided that this copyright notice is
 * included.
 ******************************************************************************/

package org.omg.CORBA;

final public class AnyHolder implements org.omg.CORBA.portable.Streamable {

    public org.omg.CORBA.Any value;

    public AnyHolder() {
    }

    public AnyHolder(org.omg.CORBA.Any initial) {
        value = initial;
    }

    public void _read(org.omg.CORBA.portable.InputStream is) {// ajc value =
        // is.read_any();
    }

    public void _write(org.omg.CORBA.portable.OutputStream os) {// ajc
        // os.write_any(value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return null;// org.omg.CORBA.ORB.init().get_primitive_tc(TCKind.tk_any);
    }

}