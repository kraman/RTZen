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
    public static void create(char prefix,
                                   FString objectId,
                                   FString time,
                                   FString poaIndex,
                                   FString okey_out) {
        // length = prefix + length of the time stamp <8 bytes> + hint byte

        okey_out.reset();


        okey_out.append((byte) (prefix & 0xFF));
        okey_out.append((byte) 0); // No hints present

        // write out the time
        okey_out.append(time.getData(), 0, time.length());

        // write out the active Object Map index
        okey_out.append(poaIndex.getData(), 0, poaIndex.length()); 

        // copy the Object Id
        okey_out.append(objectId.getData(), 0, objectId.length()); 
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
    public static void create(char prefix,
                                   FString poaPath,
                                   FString time,
                                   FString objectId,
                                   FString poaIndex,
                                   FString okey_out) {

        // first get the number of bytes that are needed for creating
        // the object key

        // length = prefix + <no of bytes that poaPathName occupies> +
        // poaPathName + length of the time stamp <8 bytes>
        // for a persistent poa; for a transient POA it is minus the
        // poaPathName

        
        okey_out.reset();
        okey_out.append((byte) (prefix & 0xFF));
        okey_out.append( (byte) 0); // No hints present
        // write out the time
        okey_out.append(time.getData(), 0, time.length()); 
        // write out the active Object Map index
        okey_out.append(poaIndex.getData(), 0, poaIndex.length()); 

        write_int(okey_out, poaPath.length());
        okey_out.append(poaName.getData(), 0, poaName.length());

        // copy the Object Id
        okey_out.append(objectId.getData(), 0, objectId.length()); 

    }

    private static void write_int(byte[] okey_out, int value) {
        okey_out.append( (byte) ((value >>> 24) & 0xFF));
        okey_out.append( (byte) ((value >>> 16) & 0xFF));
        okey_out.append( (byte) ((value >>> 8) & 0xFF));
        okey_out.append( (byte) (value & 0xFF));

    }
}
