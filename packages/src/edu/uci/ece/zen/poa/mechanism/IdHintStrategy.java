/* --------------------------------------------------------------------------*
 * $Id: IdHintStrategy.java,v 1.1 2003/11/26 22:28:54 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa.mechanism;


/**
 * <code> IdHintStrategy </code> creates ObjectKeys with hints for Active
 * Demux index for both the POA and also for Servants. These hints could be
 * embedded for POAs with both Transient and Persistent Strategies. In case
 * of Persistent POAs these are just "hints" and if there is a failure the
 * ORB would need to go thought the normal route and activate the POA hierarchy
 * using the User defined Adapter Activator.
 *
 * In the case of Transient Strategy, the object key contains the Active Demux
 * indices for the POA level and also for the Servant level. These are not just
 * hints per se but if these hints fail then Object Key is deemed to be in
 * active. Thus for the transient case the Object Key is of fixed length if
 * used in conjunction with the SYSTEM ID policy as the ids generated are
 * sequential integers.
 *
 */
import edu.uci.ece.zen.poa.ObjectKey;


final public class IdHintStrategy {


/**
 *
 * @param prefix char
 * @param objectId byte[]
 * @param time byte[]
 * @param poaIndex byte[]
 * @param servIndex byte[]
 * @return ObjectKey
 */
    public static ObjectKey create(char prefix,
            byte[] objectId,
            byte[] time,
            byte[] poaIndex,
            byte[] servIndex) {

        // length = prefix + <no of bytes that poaPathName occupies> +
        // poaPathName + length of the time stamp <8 bytes>
        // for a persistent poa; for a transient POA it is minus the
        // poaPathName
        int count = 1 + objectId.length + time.length + +poaIndex.length
                + servIndex.length + 1;

        byte[] temp = new byte[count];

        int start = 0;

        temp[start++] = (byte) (prefix & 0xFF);
        temp[start++] = (byte) 1; // Hints present
        // write out the time
        System.arraycopy(time, 0, temp, start, time.length);
        start += time.length;
        // write out the active Object Map index
        System.arraycopy(poaIndex, 0, temp, start, poaIndex.length);
        start += poaIndex.length;

        // write out the servant index
        System.arraycopy(servIndex, 0, temp, start, servIndex.length);
        start += servIndex.length;

        // copy the Object Id
        System.arraycopy(objectId, 0, temp, start, objectId.length);
        // Logger.debug("Okey Created: Transient Hint Strategy  = "
        // + new String(temp));
        // Logger.debug("Start = " + start);
        return new ObjectKey(temp);
    }

    // ObjectKey with hints around: Persistent
/**
 *
 * @param prefix char
 * @param path_name String
 * @param time byte[]
 * @param oid byte[]
 * @param poaIndex byte[]
 * @param servIndex byte[]
 * @return ObjectKey
 */
    public static ObjectKey create(char prefix,
            String path_name,
            byte[] time,
            byte[] oid,
            byte[] poaIndex,
            byte[] servIndex) {

        // length = prefix + <no of bytes that poaPathName occupies> +
        // poaPathName + length of the time stamp <8 bytes>
        // for a persistent poa; for a transient POA it is minus the
        // poaPathName

        // first get the number of bytes that are needed for creating
        // the object key
        byte[] pathName = path_name.getBytes();

        int count = 1 + pathName.length + oid.length + 8 + 4 + poaIndex.length
                + servIndex.length + 1;

        byte[] temp = new byte[count];
        int start = 0;

        temp[start++] = (byte) (prefix & 0xFF);
        temp[start++] = (byte) 1; // Hints present
        // write out the time
        System.arraycopy(time, 0, temp, start, time.length);
        start += time.length;
        // write out the POA index
        System.arraycopy(poaIndex, 0, temp, start, poaIndex.length);
        start += poaIndex.length;

        // write out the servant Index
        System.arraycopy(servIndex, 0, temp, start, servIndex.length);
        start += servIndex.length;

        // write the POANanme and string
        start = write_int(temp, start, pathName.length);
        System.arraycopy(pathName, 0, temp, start, pathName.length);
        start += pathName.length;

        // copy the Object Id
        System.arraycopy(oid, 0, temp, start, oid.length);

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
