/* --------------------------------------------------------------------------*
 * $Id: ActiveDemuxServantTable.java,v 1.2 2004/03/11 19:31:34 nshankar Exp $
 *--------------------------------------------------------------------------*/
package edu.uci.ece.zen.poa;


import edu.uci.ece.zen.utils.ZenProperties;

/**
 * Table for storing Active Demultiplexing Locations for servants activated
 * in the ZEN POA.
 *
 * @author Arvind Krishna
 * @author Nishanth Shankaran
 * @version $Revision: 1.2 $ $Date: 2004/03/11 19:31:34 $
 */

final public class ActiveDemuxServantTable {

    private static int capacity;
    private static int increment;

    private static String capId = "poa.servant_demux_table.capcity";
    private static String inc = "poa.servant_demux_table.increment";

    public static String DEFAULT_CAPACITY = "100";
    public static String DEFAULT_INCREMENT = "250";

    static {
        ActiveDemuxServantTable.capacity = Integer.parseInt(ZenProperties.getGlobalProperty(ActiveDemuxServantTable.capId, ActiveDemuxServantTable.DEFAULT_CAPACITY));
        ActiveDemuxServantTable.increment = Integer.parseInt(ZenProperties.getGLobalProperty(ActiveDemuxServantTable.inc, ActiveDemuxServantTable.DEFAULT_INCREMENT));
    }

    /**
     * Constructor for ActiveDemuxServantTable.
     */
    public ActiveDemuxServantTable() {
        this.activeMap = new Node[ActiveDemuxServantTable.capacity];
        this.freeList = new java.util.LinkedList();
    }

    /**
     * bind the servant to a location in the table.
     * @param map The POAHashMap thats to be added to the table.
     * @return int The location of the POAHashMap in the table.
     */
    public synchronized int bind(POAHashMap map) {

        // check if there are any free elements available to recyclye
        if (!freeList.isEmpty()) {
            int place = ((Integer) this.freeList.getFirst()).intValue();

            this.activeMap[place].mapEntry = map;
            // Logger.debug("ActiveDemux Servant Table: bind index = " + place);
            return place;
        }

        else
        //Right now i am just returning -1. Need to find out wat exception to throw!
        return -1;
        }


    /**
     * This method returns the location of the object in the POA map if present or else returns -1.
     * @param oid The object_id the object.
     * @return int The location of the object.
     */
    public int find(byte[] oid) {
        // Logger.debug("other oid = " + oid.getId());

        for (int i = 0; i < this.nextAvail; i++) {
            // Logger.debug("mapEntry.contents"
            // + this.activeMap[i].mapEntry.objectID());
            if (this.activeMap[i].mapEntry.objectID().equals(oid)
                    && this.activeMap[i].active) {
                // //Logger.debug ("serv demux table find operation:found" + oid);
                // Logger.debug("find index = " + i);
                return i;
            }
        }
        // Logger.debug("Active map did not find servant oid in the map");
        return -1;

    }

    /*
     * remove the object id from the POA map
     * @param oid The objectId
     * @return boolean true if remove was successful
     */
    public synchronized boolean unbind(byte[] oid) {
        for (int i = 0; i < this.nextAvail; i++) {
            if (this.activeMap[i].mapEntry.objectID().equals(oid)) {
                this.freeList.addFirst(new Integer(i));
                this.activeMap[i].active = false;
                this.activeMap[i].genCount++;
                return true;
            }
        }

        return false;
    }

    /**
     Commit the enrty to the table.
     */
     /*
    protected int commitToMap(Node newNode) {
        this.activeMap[nextAvail] = newNode;
        return nextAvail++;
    }
*/
     /**
     * Resize the map if capacit exceeds
     */

/*
    protected void rehash() {
        Node[] temp = new Node[this.activeMap.length + ActiveDemuxServantTable.increment];

        // copy all the previous elements
        System.arraycopy(this.activeMap, 0, temp, 0, this.activeMap.length);
        this.activeMap = temp;
    }
*/
    /**
     * Returns the POAHashMap presnt at the given location
     * @param place The location of the POAHashMap in the table.
     * @return POAHashMap
     */

    public POAHashMap mapEntry(int place) {
        return this.activeMap[place].mapEntry;
    }

    /**
     * Returns the genCountpresnt at the given location
     * @param index The location in the table.
     * @return int
     */

    public int getGenCount(int index) {
        return this.activeMap[index].genCount;
    }

    /**
     * Returns true if the objcect at the given location is active elase false.
     * @param index The location in the table.
     * @return boolean
     */
    public boolean isActive(int index) {
        return this.activeMap[index].active;
    }

    class Node {
        public int genCount = 0;
        public POAHashMap mapEntry;
        public boolean active = true;

        public Node(POAHashMap mapEntry) {
            this.mapEntry = mapEntry;
        }

    }

    // --- Array Slots for servants activated!
    private Node[] activeMap;
    private java.util.LinkedList freeList;
    private int nextAvail = 0;
}
