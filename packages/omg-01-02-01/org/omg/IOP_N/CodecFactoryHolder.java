package org.omg.IOP_N;


/**
 *	Generated from IDL definition of interface "CodecFactory"
 *	@author IDL compiler 
 */

public class CodecFactoryHolder	implements org.omg.CORBA.portable.Streamable {
    public CodecFactory value;
    public CodecFactoryHolder() {}

    public CodecFactoryHolder(CodecFactory initial) {
        value = initial;
    }

    public org.omg.CORBA.TypeCode _type() {
        return CodecFactoryHelper.type();
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = CodecFactoryHelper.read(in);
    }

    public void _write(org.omg.CORBA.portable.OutputStream _out) {
        CodecFactoryHelper.write(_out, value);
    }
}
