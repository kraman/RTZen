package org.omg.IOP_N.CodecPackage;


/**
 *  Generated from IDL definition of exception "TypeMismatch"
 *  @author IDL compiler
 */

public class TypeMismatchHelper {
    private static org.omg.CORBA.TypeCode _type = null;// org.omg.CORBA.ORB.init().create_struct_tc(org.omg.IOP_N.CodecPackage.TypeMismatchHelper.id(),"TypeMismatch",new org.omg.CORBA.StructMember[0]);
    public TypeMismatchHelper() {}

    public static org.omg.CORBA.TypeCode type() {
        return _type;
    }

    public static String id() {
        return "IDL:omg.org/IOP_N/Codec/TypeMismatch:1.0";
    }

    public static org.omg.IOP_N.CodecPackage.TypeMismatch read(org.omg.CORBA.portable.InputStream in) {
        org.omg.IOP_N.CodecPackage.TypeMismatch result = new org.omg.IOP_N.CodecPackage.TypeMismatch();

        if (!in.read_string().equals(id())) {
            throw new org.omg.CORBA.MARSHAL("wrong id");
        }
        return result;
    }

    public static void write(org.omg.CORBA.portable.OutputStream out, org.omg.IOP_N.CodecPackage.TypeMismatch s) {
        out.write_string(id());
    }
}
