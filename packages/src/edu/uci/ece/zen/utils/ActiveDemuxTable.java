package edu.uci.ece.zen.utils;

import javax.realtime.MemoryArea;

/**
 * This class provides an Active demultiplexing table. Insertion is O(1) and
 * returns a lookup index. Retrieveing the object with the lookup index is an
 * O(1) operation however, finding the object is still O(n). This class is
 * memory region safe. It internally uses lazy initialization however, memory
 * regions are handled properly.
 * 
 * @author Krishna Raman
 */
public class ActiveDemuxTable {

    /**
     * Table of nodes.
     */
    ActiveDemuxTableNode[] data;

    /**
     * Link list of free nodes. All the nodes in this link list are alrwady
     * allocated in the "data" table.
     */
    ActiveDemuxTableNode freeList;

    /**
     * This method is used to push a freed node onto the freeList. This method
     * is threadsafe.
     * 
     * @param idx
     *            The node to push.
     */
    private void push(ActiveDemuxTableNode idx) {
        synchronized (data) {
            idx.data = null;
            idx.inUse = false;
            idx.genCount++;
            idx.next = freeList;
            freeList = idx;
        }
    }

    /**
     * This method is used to retrieve an unused node from the freeList. This
     * method is thread safe.
     * 
     * @return An integer representing the node from the "data" table to use.
     */
    private int pop() {
        synchronized (data) {
            int idx = freeList.idx;
            freeList = freeList.next;
            data[idx].next = null;
            data[idx].inUse = true;
            return idx;
        }
    }

    /**
     * Constructor to call from a <code>newInstance( &lt;class&gt; )</code>
     * You must call <code>init( &lt;numEntries&gt; )</code> after this method
     * to initialize the ActiveDemuxTable.
     */
    public ActiveDemuxTable() {
    }

    /**
     * Constructor to call when using the <code>new</code> keyword. DO NOT
     * call <code>init( &lt;numEntries&gt; )</code> after this constructor.
     */
    public ActiveDemuxTable(int numEntries) {
        init(numEntries);
    }

    /**
     * This method is used to allocate space in the ActiveDemuxTable. This
     * method is ScopedRegion safe.
     * 
     * @param numEntries
     *            The number of entries to allocate in the table.
     */
    public void init(int numEntries) {
        try {
            MemoryArea mem = MemoryArea.getMemoryArea(this);
            data = (ActiveDemuxTableNode[]) mem.newArray(
                    ActiveDemuxTableNode.class, numEntries);
            for (int i = 0; i < numEntries; i++) {
                data[i] = (ActiveDemuxTableNode) mem
                        .newInstance(ActiveDemuxTableNode.class);
                data[i].idx = i;
                push(data[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Binds an key to an object and returns the active demux location as an
     * integer.
     * 
     * @param key
     *            The key into the ActiveDemuxTable
     * @param data
     *            The object to associate with the key
     * @return The active demux location.
     */
    public int bind(Object key, Object data) {
        int idx = pop();
        this.data[idx].key = key;
        this.data[idx].data = data;
        return idx;
    }

    /**
     * Binds an key to an object and returns the active demux location as an
     * integer.
     * 
     * @param key
     *            The key into the ActiveDemuxTable
     * @param data
     *            The object to associate with the key
     * @return The active demux location.
     */
    public int bind(long primitiveKey, Object data) {
        int idx = pop();
        this.data[idx].primitiveKey = primitiveKey;
        this.data[idx].data = data;
        return idx;
    }

    /**
     * Unbind the key/value at the provided active demux location.
     * 
     * @param idx
     *            The active demux location to unbind.
     */
    public void unbind(int idx) {
        push(data[idx]);
    }

    /**
     * Retrieve the object at the given active demux location.
     * 
     * @param idx
     *            The active demux location to unbind.
     * @return The object at specified active demux location.
     */
    public Object mapEntry(int idx) {
        if (idx < 0 || idx > data.length) return null;
        return data[idx].data;
    }

    /**
     * Retrieve the generation count of the given active demux location.
     * 
     * @param idx
     *            The active demux location to unbind.
     * @return The generation count of the specified location.
     */
    public int getGenCount(int idx) {
        return data[idx].genCount;
    }

    /**
     * Retrieve the active demux location of the provided key. This is a O(n)
     * operation.
     * 
     * @param key
     *            The key into the ActiveDemuxTable
     * @return An integer representing the active demux location.
     */
    public int find(Object key) {
        for (int i = 0; i < data.length; i++) {
            if (data[i].inUse && data[i].key.equals(key)) return i;
        }
        return -1;
    }

    /**
     * Retrieve the active demux location of the provided key. This is a O(n)
     * operation.
     * 
     * @param key
     *            The key into the ActiveDemuxTable
     * @return An integer representing the active demux location.
     */
    public int find(long primitiveKey) {
        for (int i = 0; i < data.length; i++) {
            if (data[i].inUse && data[i].primitiveKey == primitiveKey) return i;
        }
        return -1;
    }
}