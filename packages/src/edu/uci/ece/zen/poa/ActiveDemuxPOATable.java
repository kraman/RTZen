/* --------------------------------------------------------------------------*
 * $Id: ActiveDemuxPOATable.java,v 1.1 2003/11/26 22:26:14 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa;


import edu.uci.ece.zen.utils.ZenProperties;

/**
 * Active Demultiplexing Table for all the POAs created in ZEN
 *
 * @author Arvind Krishna
 * @author Nishanth Shankaran
 * @version $Revision: 1.1 $ $Date: 2003/11/26 22:26:14 $
 */


final public class ActiveDemuxPOATable extends edu.uci.ece.zen.utils.Hashtable{

    private static int capacity;
    private static int increment;

    private static String capId = "poa.poa_demux_table.capcity";

    static {
        ActiveDemuxPOATable.capacity = Integer.parseInt(ZenProperties.getGlobalProperty(ActiveDemuxPOATable.capId , "10"));
    }


   /**
    * Constructor for the ActiveDemuxPOATable
    */
    public ActiveDemuxPOATable() {
        super( ActiveDemuxPOATable.capacity );
    }

    /**
     * Bind the POA to a location in the Active Demultiplexing Table.
     * @param oid The object id of the object.
     * @param poa The POA which hosts the object.
     * @return ActiveDemuxLoc The ActiveDemuxLoc object that indicates the location of the object.
     */
    public synchronized ActiveDemuxLoc bind(String oid, POA poa) {

        ActiveDemuxPOATableTableEntry entry = new ActiveDemuxPOATableTableEntry(oid, poa);

        // check if there are any free elements available to recyclye
        if (!freeList.isEmpty()) {
            int place = ((Integer) this.freeList.getFirst()).intValue();

            this.activeMap[place].mapEntry = entry;
            this.activeMap[place].dirty = false;
            return new ActiveDemuxLoc(place, this.activeMap[place].genCount);
        }
        // add it to the next available position
        return this.commitToMap(new ActiveDemuxPOATableNode(entry));
    }

    /**
     * This method returns the location of the object in the POA map if present or else returns -1.
     * @param name The object_id the object.
     * @return int The location of the object.
     */
    public int find(String name) {
        for (int i = 0; i < this.nextAvail; i++) {
            if (this.activeMap[i].mapEntry.name.equals(name)
                    && !this.activeMap[i].dirty) {
                return i;
            }
        }

        return -1;

    }
    /**
     * This method removes the object, if present from the table.
     * @param name the objectid of the object.
     * @return boolean Returns true if the object was preasent and removed else false.
     */
    public synchronized boolean unbind(String name) {
        for (int i = 0; i < this.nextAvail; i++) {
            if (this.activeMap[i].mapEntry.name.equals(name)) {
                this.freeList.addFirst(new Integer(i));
                this.activeMap[i].dirty = true;
                // Done so that if another request comes before reuse
                // the generation count wont match
                this.activeMap[i].genCount++;
                return true;
            }
        }

        return false;
    }

    /**
     * Commit the POA to a loaction in the Active Demux Map
     * @param newNode corresponds to the POA
     * @return ActiveDemuxLoc Location in the map where the node was bound
     */

    protected ActiveDemuxLoc commitToMap(ActiveDemuxPOATableNode newNode) {
        int temp = nextAvail++;

        if (temp > activeMap.length - 1) {
            this.rehash();
        }
        this.activeMap[temp] = newNode;
        return new ActiveDemuxLoc(temp, this.activeMap[temp].genCount);
    }

    /**
     * Resize the map if capacit exceeds
     */

    protected void rehash() {
        // Logger.debug("Rehashing the Active Demux Map");
        ActiveDemuxPOATableNode[] temp = new ActiveDemuxPOATableNode[this.activeMap.length + ActiveDemuxPOATable.increment];

        // copy all the previous elements
        System.arraycopy(this.activeMap, 0, temp, 0, this.activeMap.length);
        this.activeMap = temp;
    }

    /**
     * Returns the genCountpresent at the given location.
     * @param place The location of the object.
     * @return TableEntry The entry at that particular location.
     */
    public ActiveDemuxPOATableTableEntry mapEntry(int place) {
        return this.activeMap[place].mapEntry;
    }

    int getGenCount(int index) {
        return this.activeMap[index].genCount;
    }


    // --- Array Slots for servants activated!
    private ActiveDemuxPOATableNode[] activeMap;
    private java.util.LinkedList freeList;
    private int nextAvail = 0;
}
    class ActiveDemuxPOATableNode {
        public int genCount = 0;
        public ActiveDemuxPOATableTableEntry mapEntry;
        public boolean dirty = false; // signals the removal of this entry

        public ActiveDemuxPOATableNode(ActiveDemuxPOATableTableEntry mapEntry) {
            this.mapEntry = mapEntry;
        }

    }


    class ActiveDemuxPOATableTableEntry {
        public String name;
        public POA poa;

        public ActiveDemuxPOATableTableEntry(String name, POA poa) {
            this.name = name;
            this.poa = poa;
        }
    }

