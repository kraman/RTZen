package org.omg.IOP_N.CodecPackage;

/**
 * Generated from IDL definition of exception "FormatMismatch"
 * 
 * @author IDL compiler
 */

final public class FormatMismatchHolder implements
        org.omg.CORBA.portable.Streamable {
    public org.omg.IOP_N.CodecPackage.FormatMismatch value;

    public FormatMismatchHolder() {
    }

    public FormatMismatchHolder(
            org.omg.IOP_N.CodecPackage.FormatMismatch initial) {
        value = initial;
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.omg.IOP_N.CodecPackage.FormatMismatchHelper.type();
    }

    public void _read(org.omg.CORBA.portable.InputStream _in) {
        value = org.omg.IOP_N.CodecPackage.FormatMismatchHelper.read(_in);
    }

    public void _write(org.omg.CORBA.portable.OutputStream _out) {
        org.omg.IOP_N.CodecPackage.FormatMismatchHelper.write(_out, value);
    }
}