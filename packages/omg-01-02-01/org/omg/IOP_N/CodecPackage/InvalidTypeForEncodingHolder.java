package org.omg.IOP_N.CodecPackage;

/**
 * Generated from IDL definition of exception "InvalidTypeForEncoding"
 * 
 * @author IDL compiler
 */

final public class InvalidTypeForEncodingHolder implements
        org.omg.CORBA.portable.Streamable {
    public org.omg.IOP_N.CodecPackage.InvalidTypeForEncoding value;

    public InvalidTypeForEncodingHolder() {
    }

    public InvalidTypeForEncodingHolder(
            org.omg.IOP_N.CodecPackage.InvalidTypeForEncoding initial) {
        value = initial;
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.omg.IOP_N.CodecPackage.InvalidTypeForEncodingHelper.type();
    }

    public void _read(org.omg.CORBA.portable.InputStream _in) {
        value = org.omg.IOP_N.CodecPackage.InvalidTypeForEncodingHelper
                .read(_in);
    }

    public void _write(org.omg.CORBA.portable.OutputStream _out) {
        org.omg.IOP_N.CodecPackage.InvalidTypeForEncodingHelper.write(_out,
                value);
    }
}