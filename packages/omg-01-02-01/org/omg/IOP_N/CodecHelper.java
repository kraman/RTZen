package org.omg.IOP_N;


/**
 *  Generated from IDL definition of interface "Codec"
 *  @author IDL compiler
 */

public class CodecHelper {
    public CodecHelper() {}

    public static org.omg.CORBA.TypeCode type() {
        return null;// org.omg.CORBA.ORB.init().create_interface_tc( "IDL:omg.org/IOP_N/Codec:1.0", "Codec");
    }

    public static String id() {
        return "IDL:omg.org/IOP_N/Codec:1.0";
    }

    public static Codec read(org.omg.CORBA.portable.InputStream in) {
        return narrow(in.read_Object());
    }

    public static void write(org.omg.CORBA.portable.OutputStream _out, org.omg.IOP_N.Codec s) {
        _out.write_Object(s);
    }

    public static org.omg.IOP_N.Codec narrow(org.omg.CORBA.Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return (org.omg.IOP_N.Codec) obj;
        } catch (ClassCastException c) {
            if (obj._is_a("IDL:omg.org/IOP_N/Codec:1.0")) {
                org.omg.IOP_N._CodecStub stub;

                stub = new org.omg.IOP_N._CodecStub();
                stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate());
                return stub;
            }
        }
        throw new org.omg.CORBA.BAD_PARAM("Narrow failed");
    }
}
