package org.omg.RTCORBA.RTORBPackage;


/**
 *	Generated from IDL definition of exception "InvalidThreadpool"
 */

public final class InvalidThreadpoolHolder
    implements org.omg.CORBA.portable.Streamable {
    public org.omg.RTCORBA.RTORBPackage.InvalidThreadpool value;

    public InvalidThreadpoolHolder() {}

    public InvalidThreadpoolHolder(final org.omg.RTCORBA.RTORBPackage.InvalidThreadpool initial) {
        value = initial;
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.omg.RTCORBA.RTORBPackage.InvalidThreadpoolHelper.type();
    }

    public void _read(final org.omg.CORBA.portable.InputStream _in) {
        value = org.omg.RTCORBA.RTORBPackage.InvalidThreadpoolHelper.read(_in);
    }

    public void _write(final org.omg.CORBA.portable.OutputStream _out) {
        org.omg.RTCORBA.RTORBPackage.InvalidThreadpoolHelper.write(_out, value);
    }
}
