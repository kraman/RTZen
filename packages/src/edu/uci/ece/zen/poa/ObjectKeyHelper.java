/* --------------------------------------------------------------------------*
 * $Id: ObjectKeyHelper.java,v 1.1 2003/11/26 22:26:22 nshankar Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.poa;




public class ObjectKeyHelper {


    /**
     * Read ObjectKey from stream
     * @param in
     * @return ObjectKey
     */
    public static ObjectKey read(org.omg.CORBA.portable.InputStream in) {

        byte[] contents = null;
        int length = in.read_long();

        // edu.uci.ece.zen.orb.Logger.debug("OK :- read length : " + length);

        contents = new byte[ length ];
        in.read_octet_array(contents, 0, contents.length);
        ObjectKey okey = new ObjectKey(contents);

        // Logger.debug ("Okey read from the stream: " + okey);
        return okey;
    }
    /**
     * Write Object Key to stream
     * @param ok
     * @param out
     *
     */

    public static void write(ObjectKey ok,
            org.omg.CORBA.portable.OutputStream out) {

        byte[] contents = ok.getContents();
        int len = contents.length;

        out.write_long(len);
        out.write_octet_array(contents, 0, len);
    }
}
