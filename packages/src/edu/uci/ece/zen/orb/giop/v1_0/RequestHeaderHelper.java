package edu.uci.ece.zen.orb.giop.v1_0;

/**
 * Helper class for : RequestHeader_1_0
 *
 * @author OpenORB Compiler
 */
public class RequestHeaderHelper
{
    /**
     * Insert RequestHeader_1_0 into an any
     * @param a an any
     * @param t RequestHeader_1_0 value

    public static void insert(org.omg.CORBA.Any a, org.omg.GIOP.RequestHeader_1_0 t)
    {
        a.insert_Streamable(new org.omg.GIOP.RequestHeader_1_0Holder(t));
    }
*/
    /**
     * Extract RequestHeader_1_0 from an any
     * @param a an any
     * @return the extracted RequestHeader_1_0 value

    public static org.omg.GIOP.RequestHeader_1_0 extract(org.omg.CORBA.Any a)
    {
        if (!a.type().equal(type()))
            throw new org.omg.CORBA.MARSHAL();
        return read(a.create_input_stream());
    }
 */
    //
    // Internal TypeCode value
    //
    //private static org.omg.CORBA.TypeCode _tc = null;
    //private static boolean _working = false;

    /**
     * Return the RequestHeader_1_0 TypeCode
     * @return a TypeCode

    public static org.omg.CORBA.TypeCode type()
    {
        if (_tc == null) {
            synchronized(org.omg.CORBA.TypeCode.class) {
                if (_tc != null)
                    return _tc;
                if (_working)
                    return org.omg.CORBA.ORB.init().create_recursive_tc(id());
                _working = true;
                org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init();
                org.omg.CORBA.StructMember []_members = new org.omg.CORBA.StructMember[6];

                _members[0] = new org.omg.CORBA.StructMember();
                _members[0].name = "service_context";
                _members[0].type = org.omg.IOP.ServiceContextListHelper.type();
                _members[1] = new org.omg.CORBA.StructMember();
                _members[1].name = "request_id";
                _members[1].type = orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_ulong);
                _members[2] = new org.omg.CORBA.StructMember();
                _members[2].name = "response_expected";
                _members[2].type = orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean);
                _members[3] = new org.omg.CORBA.StructMember();
                _members[3].name = "object_key";
                _members[3].type = orb.create_sequence_tc(0,orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_octet));
                _members[4] = new org.omg.CORBA.StructMember();
                _members[4].name = "operation";
                _members[4].type = orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string);
                _members[5] = new org.omg.CORBA.StructMember();
                _members[5].name = "requesting_principal";
                _members[5].type = org.omg.CORBA.OctetSeqHelper.type();
                _tc = orb.create_struct_tc(id(),"RequestHeader_1_0",_members);
                _working = false;
            }
        }
        return _tc;
    }
*/
    /**
     * Return the RequestHeader_1_0 IDL ID
     * @return an ID

    public static String id()
    {
        return _id;
    }

    private final static String _id = "IDL:omg.org/GIOP/RequestHeader_1_0:1.0";
*/



    /**
     * Read RequestHeader_1_0 from a marshalled stream
     * @param istream the input stream
     * @return the readed RequestHeader_1_0 value
     */
    public static RequestHeader read(org.omg.CORBA.portable.InputStream istream)
    {
        RequestHeader new_one = RequestHeader.instance();

        new_one.service_context = edu.uci.ece.zen.orb.giop.IOP.ServiceContextListHelper.
                read(istream,RequestHeader.instance(new_one.service_context));

        new_one.request_id = istream.read_ulong();
        new_one.response_expected = istream.read_boolean();

        new_one.object_key = RequestHeader.instance(new_one.object_key);
        int object_key_length = istream.read_ulong();
        new_one.object_key.append(object_key_length);
        new_one.object_key.read(istream, object_key_length);

        new_one.operation =  RequestHeader.instance(new_one.operation);
        int op_length = istream.read_ulong() - 1;
        new_one.operation.append(op_length);
        new_one.operation.read(istream, op_length);
        istream.read_octet();

        //new_one.operation = istream.read_string();

        //new_one.requesting_principal_length = istream.read_ulong();
        //istream.read_octet_array(new_one.requesting_principal, 0, new_one.requesting_principal_length);

        new_one.requesting_principal =  RequestHeader.instance(new_one.requesting_principal);
        int rp_length = istream.read_ulong();
        new_one.requesting_principal.append(rp_length);
        new_one.requesting_principal.read(istream, rp_length);

        return new_one;
    }

    /**
     * Write RequestHeader_1_0 into a marshalled stream
     * @param ostream the output stream
     * @param value RequestHeader_1_0 value
     */
    public static void write(org.omg.CORBA.portable.OutputStream ostream, RequestHeader value)
    {
        /*
        edu.uci.ece.zen.orb.giop.IOP.ServiceContextListHelper.write(ostream,value.service_context);
        ostream.write_ulong(value.request_id);
        ostream.write_boolean(value.response_expected);
        ostream.write_ulong(value.object_key.length);
        ostream.write_octet_array(value.object_key, 0,value.object_key.length);
        ostream.write_string(value.operation);
        //org.omg.CORBA.OctetSeqHelper.write(ostream,value.requesting_principal);
        ostream.write_ulong(value.requesting_principal_length);
        ostream.write_octet_array(value.requesting_principal, 0,value.requesting_principal_length);
        */
    }

}
