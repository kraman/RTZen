package org.omg.IOP_N.CodecPackage;

/**
 * Generated from IDL definition of exception "TypeMismatch"
 * 
 * @author IDL compiler
 */

final public class TypeMismatchHolder implements
        org.omg.CORBA.portable.Streamable {
    public org.omg.IOP_N.CodecPackage.TypeMismatch value;

    public TypeMismatchHolder() {
    }

    public TypeMismatchHolder(org.omg.IOP_N.CodecPackage.TypeMismatch initial) {
        value = initial;
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.omg.IOP_N.CodecPackage.TypeMismatchHelper.type();
    }

    public void _read(org.omg.CORBA.portable.InputStream _in) {
        value = org.omg.IOP_N.CodecPackage.TypeMismatchHelper.read(_in);
    }

    public void _write(org.omg.CORBA.portable.OutputStream _out) {
        org.omg.IOP_N.CodecPackage.TypeMismatchHelper.write(_out, value);
    }
}