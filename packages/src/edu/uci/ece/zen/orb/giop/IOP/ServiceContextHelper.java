package edu.uci.ece.zen.orb.giop.IOP;


import edu.uci.ece.zen.utils.*;
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
    public static FString read(org.omg.CORBA.portable.InputStream istream, FString fs)
    {
        //ServiceContext new_one = ServiceContext.instance(pos);

        int context_id = org.omg.IOP.ServiceIdHelper.read(istream);
        fs.append((long)context_id);

        int context_data_length = istream.read_ulong();
        fs.append((long)context_data_length);

        //istream.read_octet_array(new_one.context_data, 0, new_one.context_data_length);
        fs.read(istream, context_data_length);

        return fs;
    }

    /**
     * Write ServiceContext into a marshalled stream
     * @param ostream the output stream
     * @param value ServiceContext value
     */
    public static void write(org.omg.CORBA.portable.OutputStream ostream, FString value)
    {
        //org.omg.IOP.ServiceIdHelper.write(ostream,value.context_id);
        //ostream.write_ulong(value.context_data_length);
        //ostream.write_octet_array(value.context_data, 0,value.context_data_length);

    }

}
