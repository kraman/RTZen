package org.omg.IOP_N;


/**
 *  Generated from IDL definition of alias "TaggedComponentSeq"
 *  @author IDL compiler
 */

public class TaggedComponentSeqHelper {
    private static org.omg.CORBA.TypeCode _type = null;// org.omg.CORBA.ORB.init().create_alias_tc( org.omg.IOP_N.TaggedComponentSeqHelper.id(),"TaggedComponentSeq",org.omg.CORBA.ORB.init().create_sequence_tc(0, org.omg.CORBA.ORB.init().create_struct_tc(org.omg.IOP.TaggedComponentHelper.id(),"TaggedComponent",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("tag",org.omg.CORBA.ORB.init().create_alias_tc( org.omg.IOP.ComponentIdHelper.id(),"ComponentId",org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(5))),null),new org.omg.CORBA.StructMember("component_data",org.omg.CORBA.ORB.init().create_sequence_tc(0, org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(10))),null)})));
    public TaggedComponentSeqHelper() {}

    public static org.omg.CORBA.TypeCode type() {
        return _type;
    }

    public static String id() {
        return "IDL:omg.org/IOP_N/TaggedComponentSeq:1.0";
    }

    public static org.omg.IOP.TaggedComponent[] read(org.omg.CORBA.portable.InputStream _in) {
        org.omg.IOP.TaggedComponent[] _result;
        int _l_result0 = _in.read_long();

        _result = new org.omg.IOP.TaggedComponent[_l_result0];
        for (int i = 0; i < _result.length; i++) {
            _result[i] = org.omg.IOP.TaggedComponentHelper.read(_in);
        }

        return _result;
    }

    public static void write(org.omg.CORBA.portable.OutputStream _out, org.omg.IOP.TaggedComponent[] _s) {

        _out.write_long(_s.length);
        for (int i = 0; i < _s.length; i++) {
            org.omg.IOP.TaggedComponentHelper.write(_out, _s[i]);
        }

    }
}
