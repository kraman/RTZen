package org.omg.IOP_N;


/**
 *  Generated from IDL definition of alias "EncodingFormat"
 *  @author IDL compiler
 */

public class EncodingFormatHelper {
    private static org.omg.CORBA.TypeCode _type = null;// org.omg.CORBA.ORB.init().create_alias_tc( org.omg.IOP_N.EncodingFormatHelper.id(),"EncodingFormat",org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(2)));
    public EncodingFormatHelper() {}

    public static org.omg.CORBA.TypeCode type() {
        return _type;
    }

    public static String id() {
        return "IDL:omg.org/IOP_N/EncodingFormat:1.0";
    }

    public static short read(org.omg.CORBA.portable.InputStream _in) {
        short _result;

        _result = _in.read_short();
        return _result;
    }

    public static void write(org.omg.CORBA.portable.OutputStream _out, short _s) {
        _out.write_short(_s);
    }
}
