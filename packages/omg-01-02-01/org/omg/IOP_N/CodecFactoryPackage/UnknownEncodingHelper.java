package org.omg.IOP_N.CodecFactoryPackage;

/**
 * Generated from IDL definition of exception "UnknownEncoding"
 * 
 * @author IDL compiler
 */

public class UnknownEncodingHelper {
    private static org.omg.CORBA.TypeCode _type = null; // org.omg.CORBA.ORB.init().create_struct_tc(org.omg.IOP_N.CodecFactoryPackage.UnknownEncodingHelper.id(),"UnknownEncoding",new

    // org.omg.CORBA.StructMember[0]);

    public UnknownEncodingHelper() {
    }

    public static org.omg.CORBA.TypeCode type() {
        return _type;
    }

    public static String id() {
        return "IDL:omg.org/IOP_N/CodecFactory/UnknownEncoding:1.0";
    }

    public static org.omg.IOP_N.CodecFactoryPackage.UnknownEncoding read(
            org.omg.CORBA.portable.InputStream in) {
        org.omg.IOP_N.CodecFactoryPackage.UnknownEncoding result = new org.omg.IOP_N.CodecFactoryPackage.UnknownEncoding();

        if (!in.read_string().equals(id())) { throw new org.omg.CORBA.MARSHAL(
                "wrong id"); }
        return result;
    }

    public static void write(org.omg.CORBA.portable.OutputStream out,
            org.omg.IOP_N.CodecFactoryPackage.UnknownEncoding s) {
        out.write_string(id());
    }
}