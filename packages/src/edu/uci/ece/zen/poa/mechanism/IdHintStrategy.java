package edu.uci.ece.zen.poa.mechanism;

import edu.uci.ece.zen.utils.FString;

/**
 * <code> IdHintStrategy </code> creates ObjectKeys with hints for Active Demux
 * index for both the POA and also for Servants. These hints could be embedded
 * for POAs with both Transient and Persistent Strategies. In case of Persistent
 * POAs these are just "hints" and if there is a failure the ORB would need to
 * go thought the normal route and activate the POA hierarchy using the User
 * defined Adapter Activator. In the case of Transient Strategy, the object key
 * contains the Active Demux indices for the POA level and also for the Servant
 * level. These are not just hints per se but if these hints fail then Object
 * Key is deemed to be in active. Thus for the transient case the Object Key is
 * of fixed length if used in conjunction with the SYSTEM ID policy as the ids
 * generated are sequential integers.
 */
final public class IdHintStrategy {
    /**
     * @param prefix
     *            char
     * @param objectId
     *            FString
     * @param time
     *            FString
     * @param poaIndex
     *            FString
     * @param servIndex
     *            FString
     * @return ObjectKey
     */
    public static void create(char prefix, FString objectId, FString time,
            int poaIndex, int poaGenCount, int servIndex, int servGenCount,
            FString ok_out) {

        // length = prefix + <no of bytes that poaPathName occupies> +
        // poaPathName + length of the time stamp <8 bytes>
        // for a persistent poa; for a transient POA it is minus the
        // poaPathName

        ok_out.reset();
        ok_out.append((byte) (prefix & 0xFF));
        ok_out.append((byte) 1);
        // write out the time
        ok_out.append(time.getData(), 0, time.length());
        // write out the active Object Map index
        write_int(ok_out, poaIndex);
        write_int(ok_out, poaGenCount);

        // write out the servant index
        write_int(ok_out, servIndex);
        write_int(ok_out, servGenCount);

        // copy the Object Id
        ok_out.append(objectId.getData(), 0, objectId.length());
        // Logger.debug("Okey Created: Transient Hint Strategy = "
        // + new String(temp));
        // Logger.debug("Start = " + start);
    }

    // ObjectKey with hints around: Persistent
    /**
     * @param prefix
     *            char
     * @param path_name
     *            String
     * @param time
     *            byte[]
     * @param oid
     *            byte[]
     * @param poaIndex
     *            byte[]
     * @param servIndex
     *            byte[]
     * @return ObjectKey
     */
    public static void create(char prefix, FString path_name, FString time,
            FString oid, int poaIndex, int poaGenCount, int servIndex,
            int servGenCount, FString objKey_out) {

        // length = prefix + <no of bytes that poaPathName occupies> +
        // poaPathName + length of the time stamp <8 bytes>
        // for a persistent poa; for a transient POA it is minus the
        // poaPathName

        // first get the number of bytes that are needed for creating
        // the object key
        objKey_out.reset();
        objKey_out.append((byte) (prefix & 0xFF));
        objKey_out.append((byte) 1);

        // write out the time
        objKey_out.append(time.getData(), 0, time.length());

        // write out the active Object Map index
        write_int(objKey_out, poaIndex);
        write_int(objKey_out, poaGenCount);

        // write out the servant index
        write_int(objKey_out, servIndex);
        write_int(objKey_out, servGenCount);

        // write the POANanme and string
        write_int(objKey_out, path_name.length());
        objKey_out.append(path_name.getData(), 0, path_name.length());

        // copy the Object Id
        objKey_out.append(oid.getData(), 0, oid.length());
    }

    private static void write_int(FString okey_out, int value) {
        okey_out.append((byte) ((value >>> 24) & 0xFF));
        okey_out.append((byte) ((value >>> 16) & 0xFF));
        okey_out.append((byte) ((value >>> 8) & 0xFF));
        okey_out.append((byte) (value & 0xFF));
    }
}
