package org.omg.IOP_N;


/**
 *  Generated from IDL definition of struct "Encoding"
 *  @author IDL compiler
 */

public class EncodingHelper {
    private static org.omg.CORBA.TypeCode _type = null;// org.omg.CORBA.ORB.init().create_struct_tc(org.omg.IOP_N.EncodingHelper.id(),"Encoding",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("format",org.omg.CORBA.ORB.init().create_alias_tc( org.omg.IOP_N.EncodingFormatHelper.id(),"EncodingFormat",org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(2))),null),new org.omg.CORBA.StructMember("major_version",org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(10)),null),new org.omg.CORBA.StructMember("minor_version",org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(10)),null)});
    public EncodingHelper() {}

    public static org.omg.CORBA.TypeCode type() {
        return _type;
    }

    public static String id() {
        return "IDL:omg.org/IOP_N/Encoding:1.0";
    }

    public static org.omg.IOP_N.Encoding read(org.omg.CORBA.portable.InputStream in) {
        org.omg.IOP_N.Encoding result = new org.omg.IOP_N.Encoding();

        result.format = in.read_short();
        result.major_version = in.read_octet();
        result.minor_version = in.read_octet();
        return result;
    }

    public static void write(org.omg.CORBA.portable.OutputStream out, org.omg.IOP_N.Encoding s) {
        out.write_short(s.format);
        out.write_octet(s.major_version);
        out.write_octet(s.minor_version);
    }
}
