package org.omg.IOP_N;


/**
 *	Generated from IDL definition of interface "Codec"
 *	@author IDL compiler 
 */

public class CodecHolder	implements org.omg.CORBA.portable.Streamable {
    public Codec value;
    public CodecHolder() {}

    public CodecHolder(Codec initial) {
        value = initial;
    }

    public org.omg.CORBA.TypeCode _type() {
        return CodecHelper.type();
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = CodecHelper.read(in);
    }

    public void _write(org.omg.CORBA.portable.OutputStream _out) {
        CodecHelper.write(_out, value);
    }
}
