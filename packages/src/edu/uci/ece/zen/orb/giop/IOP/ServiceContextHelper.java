package edu.uci.ece.zen.orb.giop.IOP;

/**
 * Helper class for : ServiceContext
 *
 * @author OpenORB Compiler
 */
public class ServiceContextHelper
{
    /**
     * Read ServiceContext from a marshalled stream
     * @param istream the input stream
     * @return the readed ServiceContext value
     */
    public static ServiceContext read(org.omg.CORBA.portable.InputStream istream, int pos)
    {
        ServiceContext new_one = ServiceContext.instance(pos);

        new_one.context_id = org.omg.IOP.ServiceIdHelper.read(istream);

        new_one.context_data_length = istream.read_ulong();

        istream.read_octet_array(new_one.context_data, 0, new_one.context_data_length);

        return new_one;
    }

    /**
     * Write ServiceContext into a marshalled stream
     * @param ostream the output stream
     * @param value ServiceContext value
     */
    public static void write(org.omg.CORBA.portable.OutputStream ostream, ServiceContext value)
    {
        org.omg.IOP.ServiceIdHelper.write(ostream,value.context_id);
        ostream.write_ulong(value.context_data_length);
        ostream.write_octet_array(value.context_data, 0,value.context_data_length);

    }

}
