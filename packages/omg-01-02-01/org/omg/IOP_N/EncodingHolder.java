package org.omg.IOP_N;


/**
 *	Generated from IDL definition of struct "Encoding"
 *	@author IDL compiler 
 */

final public class EncodingHolder
    implements org.omg.CORBA.portable.Streamable {
    public org.omg.IOP_N.Encoding value;

    public EncodingHolder() {}

    public EncodingHolder(org.omg.IOP_N.Encoding initial) {
        value = initial;
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.omg.IOP_N.EncodingHelper.type();
    }

    public void _read(org.omg.CORBA.portable.InputStream _in) {
        value = org.omg.IOP_N.EncodingHelper.read(_in);
    }

    public void _write(org.omg.CORBA.portable.OutputStream _out) {
        org.omg.IOP_N.EncodingHelper.write(_out, value);
    }
}
