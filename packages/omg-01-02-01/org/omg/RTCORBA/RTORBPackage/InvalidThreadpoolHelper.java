package org.omg.RTCORBA.RTORBPackage;


/**
 *  Generated from IDL definition of exception "InvalidThreadpool"
 */

public final class InvalidThreadpoolHelper {
    private static org.omg.CORBA.TypeCode _type = null;// org.omg.CORBA.ORB.init().create_exception_tc( org.omg.RTCORBA.RTORBPackage.InvalidThreadpoolHelper.id(),"InvalidThreadpool",new org.omg.CORBA.StructMember[0]);

    public static org.omg.CORBA.TypeCode type() {
        return _type;
    }

    public static String id() {
        return "IDL:omg.org/RTCORBA/RTORB/InvalidThreadpool:1.0";
    }

    public static org.omg.RTCORBA.RTORBPackage.InvalidThreadpool read(final org.omg.CORBA.portable.InputStream in) {
        org.omg.RTCORBA.RTORBPackage.InvalidThreadpool result = new org.omg.RTCORBA.RTORBPackage.InvalidThreadpool();

        if (!in.read_string().equals(id())) {
            throw new org.omg.CORBA.MARSHAL("wrong id");
        }
        return result;
    }

    public static void write(final org.omg.CORBA.portable.OutputStream out, final org.omg.RTCORBA.RTORBPackage.InvalidThreadpool s) {
        out.write_string(id());
    }
}
