/* --------------------------------------------------------------------------*
 * $Id: ActiveDemuxLoc.java,v 1.1 2003/11/26 22:26:14 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa;

/**
 * Active Demltiplexing Loaction in the Table
 *
 * @author Arvind Krishna
 * @author Krishna Raman
 * @version $Revision: 1.1 $ $Date: 2003/11/26 22:26:14 $
 */

final public class ActiveDemuxLoc {

    public int index; // index into the Map
    public int count; // generation count of the Entry

    public ActiveDemuxLoc(int index, int count) {
        this.index = index;
        this.count = count;
    }
/**
 * Marshall the demux loaction
 * @return byte
 */
    public byte[] marshall() {
        byte[] buffer = new byte[8];
        int nextFreeByte = 0;

        // write the index
        buffer[nextFreeByte++] = (byte) ((index >>> 24) & 0xFF);
        buffer[nextFreeByte++] = (byte) ((index >>> 16) & 0xFF);
        buffer[nextFreeByte++] = (byte) ((index >>> 8) & 0xFF);
        buffer[nextFreeByte++] = (byte) (index & 0xFF);

        // write the generation count
        buffer[nextFreeByte++] = (byte) ((count >>> 24) & 0xFF);
        buffer[nextFreeByte++] = (byte) ((count >>> 16) & 0xFF);
        buffer[nextFreeByte++] = (byte) ((count >>> 8) & 0xFF);
        buffer[nextFreeByte++] = (byte) (count & 0xFF);

        return buffer;

    }
/**
 * Check if two Active Demux Locations are the same
 * @param o The obhect that is to be compared.
 * @return boolean Returns true if the two objects are equal
 */
    public boolean equals(Object o) {
        if (o instanceof ActiveDemuxLoc) {
            ActiveDemuxLoc temp = (ActiveDemuxLoc) o;

            if (index == temp.index && count == temp.count) {
                return true;
            }
        }
        return false;
    }

}
