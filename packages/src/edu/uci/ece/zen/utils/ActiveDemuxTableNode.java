package edu.uci.ece.zen.utils;

/**
 * Internal node class to hold empty active demux locations.
 * 
 * @author Krishna Raman
 */
public class ActiveDemuxTableNode {

    public ActiveDemuxTableNode() {
    }

    /**
     * The next node in the linked list.
     */
    public ActiveDemuxTableNode next;

    /**
     * The index this node refers to.
     */
    public int idx;

    /**
     * The generation count of this index location.
     */
    public int genCount;

    /**
     * The key to the data stored at this location
     */
    public Object key;

    /**
     * This also stores the key, but only if it is a long value
     */
    public long primitiveKey;

    /**
     * The data to associate with the key.
     */
    public Object data;

    /**
     * Boolean to indicate if this node is in use or not.
     */
    public boolean inUse;
}

