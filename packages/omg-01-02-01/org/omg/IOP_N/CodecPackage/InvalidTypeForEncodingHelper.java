package org.omg.IOP_N.CodecPackage;

/**
 * Generated from IDL definition of exception "InvalidTypeForEncoding"
 * 
 * @author IDL compiler
 */

public class InvalidTypeForEncodingHelper {
    private static org.omg.CORBA.TypeCode _type = null; // org.omg.CORBA.ORB.init().create_struct_tc(org.omg.IOP_N.CodecPackage.InvalidTypeForEncodingHelper.id(),"InvalidTypeForEncoding",new

    // org.omg.CORBA.StructMember[0]);

    public InvalidTypeForEncodingHelper() {
    }

    public static org.omg.CORBA.TypeCode type() {
        return _type;
    }

    public static String id() {
        return "IDL:omg.org/IOP_N/Codec/InvalidTypeForEncoding:1.0";
    }

    public static org.omg.IOP_N.CodecPackage.InvalidTypeForEncoding read(
            org.omg.CORBA.portable.InputStream in) {
        org.omg.IOP_N.CodecPackage.InvalidTypeForEncoding result = new org.omg.IOP_N.CodecPackage.InvalidTypeForEncoding();

        if (!in.read_string().equals(id())) { throw new org.omg.CORBA.MARSHAL(
                "wrong id"); }
        return result;
    }

    public static void write(org.omg.CORBA.portable.OutputStream out,
            org.omg.IOP_N.CodecPackage.InvalidTypeForEncoding s) {
        out.write_string(id());
    }
}