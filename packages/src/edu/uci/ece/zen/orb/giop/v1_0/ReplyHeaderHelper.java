package edu.uci.ece.zen.orb.giop.v1_0;


import javax.realtime.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.giop.IOP.*;

/**
 * Helper class for : ReplyHeader_1_0
 *
 * @author OpenORB Compiler
 */
public class ReplyHeaderHelper
{

    /**
     * Read ReplyHeader_1_0 from a marshalled stream
     * @param istream the input stream
     * @return the readed ReplyHeader_1_0 value
     */
    public static ReplyHeader read(org.omg.CORBA.portable.InputStream istream)
    {
        ReplyHeader new_one = ReplyHeader.instance();

        new_one.service_context = edu.uci.ece.zen.orb.giop.IOP.ServiceContextListHelper.
                read(istream,FString.instance(new_one.service_context));

        new_one.request_id = istream.read_ulong();

        new_one.reply_status = istream.read_ulong();

        return new_one;
    }

    /**
     * Write ReplyHeader_1_0 into a marshalled stream
     * @param ostream the output stream
     * @param value ReplyHeader_1_0 value
     */
    public static void write(org.omg.CORBA.portable.OutputStream ostream, ReplyHeader value)
    {
        if(ZenProperties.devDbg) System.out.println("writing REPLY 1.0 header");

        //ServiceContextListHelper.write(ostream,value.service_context);

        value.service_context.write(ostream);
        //System.out.println("writing empty service context");
        //ostream.write_ulong(0);

        ostream.write_ulong(value.request_id);
        ostream.write_ulong(value.reply_status);
        //org.omg.GIOP.ReplyStatusType_1_0Helper.write(ostream,value.reply_status);
    }

}
