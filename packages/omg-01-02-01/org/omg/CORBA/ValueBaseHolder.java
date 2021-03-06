/*******************************************************************************
 * *** Copyright (c) 1999 Object Management Group. Unlimited rights to duplicate
 * and use this code are hereby granted provided that this copyright notice is
 * included.
 ******************************************************************************/

package org.omg.CORBA;

final public class ValueBaseHolder implements org.omg.CORBA.portable.Streamable {

    public java.io.Serializable value;

    public ValueBaseHolder() {
    }

    public ValueBaseHolder(java.io.Serializable initial) {
        value = initial;
    }

    public void _read(org.omg.CORBA.portable.InputStream is) {
        value = ValueBaseHelper.read(is);
    }

    public void _write(org.omg.CORBA.portable.OutputStream os) {
        ValueBaseHelper.write(os, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return null;// org.omg.CORBA.ORB.init().get_primitive_tc(TCKind.tk_value);
    }

}