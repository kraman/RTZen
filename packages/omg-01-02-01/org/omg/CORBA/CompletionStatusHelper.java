/* --------------------------------------------------------------------------*
 * $Id: CompletionStatusHelper.java,v 1.2 2004/01/15 18:25:18 kraman Exp $
 *--------------------------------------------------------------------------*/

package org.omg.CORBA;


public final class CompletionStatusHelper {

    public static String id() {
        return "IDL:omg/org/CORBA/CompletionStatus:1.0";
    }

    public static CompletionStatus read(final org.omg.CORBA.portable.InputStream in) {
        return CompletionStatus.from_int(in.read_long());
    }

    public static void write(final org.omg.CORBA.portable.OutputStream out, final CompletionStatus s) {
        out.write_long(s.value());
    }

    // Temporarily aspectized.  Refer to edu.uci.ece.zen.orb.TypeCodeAspect.
    // private static org.omg.CORBA.TypeCode _type = org.omg.CORBA.ORB.init().create_enum_tc(org.omg.CORBA.CompletionStatusHelper.id(),"CompletionStatus",new String[]{"COMPLETED_YES","COMPLETED_NO","COMPLETED_MAYBE"});

}
