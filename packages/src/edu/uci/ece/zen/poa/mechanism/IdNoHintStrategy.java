/* --------------------------------------------------------------------------*
 * $Id: IdNoHintStrategy.java,v 1.7 2003/09/03 20:44:19 spart Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa.mechanism;


import edu.uci.ece.zen.poa.ObjectKey;


final public class IdNoHintStrategy {

    // Transient No Hint Strategy
    /**
     * Create a transient strategy with no hints
     * @param prefix  char
     * @param objectId byte[]
     * @param time byte[] timestamp
     * @param poaIndex byte[]
     * @return ObjectKey representation
     */
    public static ObjectKey create(char prefix,
                                   byte[] objectId,
                                   byte[] time,
                                   byte[] poaIndex) {
        // length = prefix + length of the time stamp <8 bytes> + hint byte
        int count = 1 + objectId.length + 8 + poaIndex.length + 1;

        byte[] temp = new byte[count];

        int start = 0;

        temp[start++] = (byte) (prefix & 0xFF);
        temp[start++] = (byte) 0; // No hints present

        // write out the time
        System.arraycopy(time, 0, temp, start, time.length);
        start += time.length;

        // write out the active Object Map index
        System.arraycopy(poaIndex, 0, temp, start, poaIndex.length);
        start += poaIndex.length;

        // copy the Object Id
        System.arraycopy(objectId, 0, temp, start, objectId.length);
        return new ObjectKey(temp);
    }

    // For a persistent poa: No Hints present
    /**
     * Persistent Strategy with no hints
     * @param prefix char
     * @param poaPath POAPath Name 
     * @param objectId byte[]
     * @param poaIndex byte[]
     * @return ObjectKey
     */
    public static ObjectKey create(char prefix,
                                   String poaPath,
                                   byte[] time,
                                   byte[] objectId,
                                   byte[] poaIndex) {

        // first get the number of bytes that are needed for creating
        // the object key
        byte[] pathName = poaPath.getBytes();

        // length = prefix + <no of bytes that poaPathName occupies> +
        // poaPathName + length of the time stamp <8 bytes>
        // for a persistent poa; for a transient POA it is minus the
        // poaPathName
        int count = 1 + pathName.length + objectId.length + 8 + 4
                + poaIndex.length + 1;

        int start = 0;
        byte[] temp = new byte[count];

        temp[start++] = (byte) (prefix & 0xFF);
        temp[start++] = (byte) 0; // No hints present
        // write out the time
        System.arraycopy(time, 0, temp, start, time.length);
        start += time.length;
        // write out the active Object Map index
        System.arraycopy(poaIndex, 0, temp, start, poaIndex.length);
        start += poaIndex.length;

        start = write_int(temp, start, pathName.length);
        System.arraycopy(pathName, 0, temp, start, pathName.length);
        start += pathName.length;

        // copy the Object Id
        System.arraycopy(objectId, 0, temp, start, objectId.length);

        return new ObjectKey(temp);

    }

    private static int write_int(byte[]buffer, int start, int value) {
        buffer[start++] = (byte) ((value >>> 24) & 0xFF);
        buffer[start++] = (byte) ((value >>> 16) & 0xFF);
        buffer[start++] = (byte) ((value >>> 8) & 0xFF);
        buffer[start++] = (byte) (value & 0xFF);

        return start;
    }
}
