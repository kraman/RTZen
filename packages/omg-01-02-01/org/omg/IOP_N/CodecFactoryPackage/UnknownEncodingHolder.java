package org.omg.IOP_N.CodecFactoryPackage;


/**
 *	Generated from IDL definition of exception "UnknownEncoding"
 *	@author IDL compiler 
 */

final public class UnknownEncodingHolder
    implements org.omg.CORBA.portable.Streamable {
    public org.omg.IOP_N.CodecFactoryPackage.UnknownEncoding value;

    public UnknownEncodingHolder() {}

    public UnknownEncodingHolder(org.omg.IOP_N.CodecFactoryPackage.UnknownEncoding initial) {
        value = initial;
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.omg.IOP_N.CodecFactoryPackage.UnknownEncodingHelper.type();
    }

    public void _read(org.omg.CORBA.portable.InputStream _in) {
        value = org.omg.IOP_N.CodecFactoryPackage.UnknownEncodingHelper.read(_in);
    }

    public void _write(org.omg.CORBA.portable.OutputStream _out) {
        org.omg.IOP_N.CodecFactoryPackage.UnknownEncodingHelper.write(_out,
                value);
    }
}
