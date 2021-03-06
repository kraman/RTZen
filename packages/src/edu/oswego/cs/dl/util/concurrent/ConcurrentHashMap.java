/*
 * File: ConcurrentHashMap Written by Doug Lea. Adapted from JDK1.2 HashMap.java
 * and Hashtable.java which carries the following copyright: Copyright 1997 by
 * Sun Microsystems, Inc., 901 San Antonio Road, Palo Alto, California, 94303,
 * U.S.A. All rights reserved. This software is the confidential and proprietary
 * information of Sun Microsystems, Inc. ("Confidential Information"). You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Sun.
 * History: Date Who What 26nov2000 dl Created, based on ConcurrentReaderHashMap
 * 12jan2001 dl public release
 */

package edu.oswego.cs.dl.util.concurrent;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A version of Hashtable supporting concurrency for both retrievals and
 * updates:
 * <dl>
 * <dt>Retrievals
 * <dd>Retrievals may overlap updates. (This is the same policy as
 * ConcurrentReaderHashMap.) Successful retrievals using get(key) and
 * containsKey(key) usually run without locking. Unsuccessful retrievals (i.e.,
 * when the key is not present) do involve brief synchronization (locking).
 * Because retrieval operations can ordinarily overlap with update operations
 * (i.e., put, remove, and their derivatives), retrievals can only be guaranteed
 * to return the results of the most recently <em>completed</em> operations
 * holding upon their onset. Retrieval operations may or may not return results
 * reflecting in-progress writing operations. However, the retrieval operations
 * do always return consistent results -- either those holding before any single
 * modification or after it, but never a nonsense result. For aggregate
 * operations such as putAll and clear, concurrent reads may reflect insertion
 * or removal of only some entries.
 * <p>
 * Iterators and Enumerations (i.e., those returned by keySet().iterator(),
 * entrySet().iterator(), values().iterator(), keys(), and elements()) return
 * elements reflecting the state of the hash table at some point at or since the
 * creation of the iterator/enumeration. They will return at most one instance
 * of each element (via next()/nextElement()), but might or might not reflect
 * puts and removes that have been processed since they were created. They do
 * <em>not</em> throw ConcurrentModificationException. However, these
 * iterators are designed to be used by only one thread at a time. Sharing an
 * iterator across multiple threads may lead to unpredictable results if the
 * table is being concurrently modified.
 * <p>
 * <dt>Updates
 * <dd>This class supports a hard-wired preset <em>concurrency
 * level</em> of
 * 32. This allows a maximum of 32 put and/or remove operations to proceed
 * concurrently. This level is an upper bound on concurrency, not a guarantee,
 * since it interacts with how well-strewn elements are across bins of the
 * table. (The preset value in part reflects the fact that even on large
 * multiprocessors, factors other than synchronization tend to be bottlenecks
 * when more than 32 threads concurrently attempt updates.) Additionally,
 * operations triggering internal resizing and clearing do not execute
 * concurrently with any operation.
 * <p>
 * There is <em>NOT</em> any support for locking the entire table to prevent
 * updates. This makes it imposssible, for example, to add an element only if it
 * is not already present, since another thread may be in the process of doing
 * the same thing. If you need such capabilities, consider instead using the
 * ConcurrentReaderHashMap class.
 * </dl>
 * Because of how concurrency control is split up, the size() and isEmpty()
 * methods require accumulations across 32 control segments, and so might be
 * slightly slower than you expect.
 * <p>
 * This class may be used as a direct replacement for java.util.Hashtable in any
 * application that does not rely on the ability to lock the entire table to
 * prevent updates. As of this writing, it performs much faster than Hashtable
 * in typical multi-threaded applications with multiple readers and writers.
 * Like Hashtable but unlike java.util.HashMap, this class does NOT allow
 * <tt>null</tt> to be used as a key or value.
 * <p>
 * Implementation note: A slightly faster implementation of this class will be
 * possible once planned Java Memory Model revisions are in place.
 * <p>[ <a
 * href="http://gee.cs.oswego.edu/dl/classes/edu.oswego/cs/dl/util/concurrent/intro.html">
 * Introduction to this package. </a>]
 */

public class ConcurrentHashMap extends AbstractMap implements Map,Cloneable,
        Serializable {

    /*
     * The basic strategy is an optimistic-style scheme based on the guarantee
     * that the hash table and its lists are always kept in a consistent enough
     * state to be read without locking: Read operations first proceed without
     * locking, by traversing the apparently correct list of the apparently
     * correct bin. If an entry is found, but not invalidated (value field
     * null), it is returned. If not found, operations must recheck (after a
     * memory barrier) to make sure they are using both the right list and the
     * right table (which can change under resizes). If invalidated, reads must
     * acquire main update lock to wait out the update, and then re-traverse.
     * All list additions are at the front of each bin, making it easy to check
     * changes, and also fast to traverse. Entry next pointers are never
     * assigned. Remove() builds new nodes when necessary to preserve this.
     * Remove() (also clear()) invalidates removed nodes to alert read
     * operations that they must wait out the full modifications. Locking for
     * puts, removes (and, when necessary gets, etc) is controlled by Segments,
     * each covering a portion of the table. During operations requiring global
     * exclusivity (mainly resize and clear), ALL of these locks are acquired at
     * once. Note that these segments are NOT contiguous -- they are based on
     * the least 5 bits of hashcodes. This ensures that the same segment
     * controls the same slots before and after resizing, which is necessary for
     * supporting concurrent retrievals. This comes at the price of a mismatch
     * of logical vs physical locality, but this seems not to be a performance
     * problem in practice.
     */

    /**
     * The hash table data.
     */
    protected transient Entry[] table;

    /***************************************************************************
     * The number of concurrency control segments. The value can be at most 32
     * since ints are used as bitsets over segments. Emprically, it doesn't seem
     * to pay to decrease it either, so the value should be at least 32. In
     * other words, do not redefine this :-)
     **************************************************************************/

    protected static final int CONCURRENCY_LEVEL = 32;

    /**
     * Bookkeeping for each concurrency control segment. Each segment contains a
     * local count of the number of elements in its region. However, the main
     * use of a Segment is for its lock.
     */
    protected final static class Segment {

        /**
         * The number of elements in this segment's region. It is always updated
         * within synchronized blocks.
         */
        protected int count;

        /**
         * Get the count under synch.
         */
        protected synchronized int getCount() {
            return count;
        }

        /**
         * Force a synchronization
         */
        protected synchronized void synch() {
        }
    }

    /**
     * The array of concurrency control segments.
     */

    protected final Segment[] segments = new Segment[CONCURRENCY_LEVEL];

    /**
     * The default initial number of table slots for this table (32). Used when
     * not otherwise specified in constructor.
     */
    public static int DEFAULT_INITIAL_CAPACITY = 32;

    /**
     * The minimum capacity, used if a lower value is implicitly specified by
     * either of the constructors with arguments. MUST be a power of two.
     */
    private static final int MINIMUM_CAPACITY = 32;

    /**
     * The maximum capacity, used if a higher value is implicitly specified by
     * either of the constructors with arguments. MUST be a power of two <= 1 <
     * <30.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The default load factor for this table (0.75) Used when not otherwise
     * specified in constructor.
     */

    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The load factor for the hash table.
     * 
     * @serial
     */
    protected final float loadFactor;

    /**
     * The table is rehashed when its size exceeds this threshold. (The value of
     * this field is always (int)(capacity * loadFactor).)
     * 
     * @serial
     */
    protected int threshold;

    /**
     * Number of segments voting for resize. The table is doubled when 1/4 of
     * the segments reach threshold. Volatile but updated without synch since
     * this is just a heuristic.
     */

    protected transient volatile int votesForResize;

    /**
     * Return the number of set bits in w. For a derivation of this algorithm,
     * see "Algorithms and data structures with applications to graphics and
     * geometry", by Jurg Nievergelt and Klaus Hinrichs, Prentice Hall, 1993.
     * See also notes by Torsten Sillke at
     * http://www.mathematik.uni-bielefeld.de/~sillke/PROBLEMS/bitcount
     */
    protected static int bitcount(int w) {
        w -= (0xaaaaaaaa & w) >>> 1;
        w = (w & 0x33333333) + ((w >>> 2) & 0x33333333);
        w = w + (w >>> 4) & 0x0f0f0f0f;
        w += w >>> 8;
        w += w >>> 16;
        return w & 0xff;
    }

    /**
     * Returns the appropriate capacity (power of two) for the specified initial
     * capacity argument.
     */
    private int p2capacity(int initialCapacity) {
        int cap = initialCapacity;

        // Compute the appropriate capacity
        int result;

        if (cap > MAXIMUM_CAPACITY || cap < 0) {
            result = MAXIMUM_CAPACITY;
        } else {
            result = MINIMUM_CAPACITY;
            while (result < cap) {
                result <<= 1;
            }
        }
        return result;
    }

    /**
     * Return hash code for Object x. Since we are using power-of-two tables, it
     * is worth the effort to improve hashcode via the same multiplicative
     * scheme as used in IdentityHashMap.
     */
    protected static int hash(Object x) {
        int h = x.hashCode();

        // Multiply by 127 (quickly, via shifts), and mix in some high
        // bits to help guard against bunching of codes that are
        // consecutive or equally spaced.
        return ((h << 7) - h + (h >>> 9) + (h >>> 17));
    }

    /**
     * Constructs a new, empty map with the specified initial capacity and the
     * specified load factor.
     * 
     * @param initialCapacity
     *            the initial capacity. The actual initial capacity is rounded
     *            to the nearest power of two.
     * @param loadFactor
     *            the load factor threshold, used to control resizing. This
     *            value is used in an approximate way: When at least a quarter
     *            of the segments of the table reach per-segment threshold, or
     *            one of the segments itself exceeds overall threshold, the
     *            table is doubled. This will on average cause resizing when the
     *            table-wide load factor is slightly less than the threshold. If
     *            you'd like to avoid resizing, you can set this to a
     *            ridiculously large value.
     * @throws IllegalArgumentException
     *             if the load factor is nonpositive.
     */

    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        if (loadFactor <= 0) { throw new IllegalArgumentException(
                "Illegal Load factor: " + loadFactor); }
        this.loadFactor = loadFactor;
        int cap = p2capacity(initialCapacity);

        table = new Entry[cap];
        threshold = (int) (cap * loadFactor / segments.length) + 1;

        for (int i = 0; i < segments.length; ++i) {
            segments[i] = new Segment();
        }
    }

    /**
     * Constructs a new, empty map with the specified initial capacity and
     * default load factor.
     * 
     * @param initialCapacity
     *            the initial capacity of the ConcurrentHashMap.
     * @throws IllegalArgumentException
     *             if the initial maximum number of elements is less than zero.
     */

    public ConcurrentHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new, empty map with a default initial capacity and default
     * load factor.
     */

    public ConcurrentHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs a new map with the same mappings as the given map. The map is
     * created with a capacity of twice the number of mappings in the given map
     * or 32 (whichever is greater), and a default load factor.
     */

    public ConcurrentHashMap(Map t) {
        this(Math.max(2 * t.size(), 32), DEFAULT_LOAD_FACTOR);
        putAll(t);
    }

    /**
     * Returns the number of key-value mappings in this map.
     * 
     * @return the number of key-value mappings in this map.
     */

    public int size() {
        int c = 0;

        for (int i = 0; i < segments.length; ++i) {
            c += segments[i].getCount();
        }
        return c;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     * 
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */

    public boolean isEmpty() {
        for (int i = 0; i < segments.length; ++i) {
            if (segments[i].getCount() != 0) { return false; }
        }
        return true;
    }

    /**
     * Returns the value to which the specified key is mapped in this table.
     * 
     * @param key
     *            a key in the table.
     * @return the value to which the key is mapped in this table;
     *         <code>null</code> if the key is not mapped to any value in this
     *         table.
     * @exception NullPointerException
     *                if the key is <code>null</code>.
     * @see #put(Object, Object)
     */

    public Object get(Object key) {
        int hash = hash(key); // throws null pointer exception if key null

        /*
         * Start off at the apparently correct bin. If entry is found, we need
         * to check value to make sure it is valid. If not found, we need a
         * barrier to check if we are actually in right bin.
         */

        Segment seg = segments[hash & (CONCURRENCY_LEVEL - 1)];
        Entry[] tab = table;
        int index = hash & (tab.length - 1);
        Entry first = tab[index];
        Entry e = first;

        for (;;) {
            if (e == null) {
                // If key apparently not there, check to
                // make sure this was a valid read
                synchronized (seg) {
                    tab = table;
                }
                if (first == tab[index]) {
                    return null;
                } else {
                    // Wrong list -- must restart traversal at new first
                    e = first = tab[index = hash & (tab.length - 1)];
                }
            } // checking for pointer equality first wins in most applications
            else if (key == e.key || (e.hash == hash && key.equals(e.key))) {
                Object value = e.value;

                if (value != null) { return value; }

                // Entry was invalidated during deletion. But it could
                // have been re-inserted, so we must retraverse.
                synchronized (seg) {
                    tab = table;
                }
                e = first = tab[index = hash & (tab.length - 1)];
            } else {
                e = e.next;
            }
        }
    }

    /**
     * Tests if the specified object is a key in this table.
     * 
     * @param key
     *            possible key.
     * @return <code>true</code> if and only if the specified object is a key
     *         in this table, as determined by the <tt>equals</tt> method;
     *         <code>false</code> otherwise.
     * @exception NullPointerException
     *                if the key is <code>null</code>.
     * @see #contains(Object)
     */

    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    /**
     * Maps the specified <code>key</code> to the specified <code>value</code>
     * in this table. Neither the key nor the value can be <code>null</code>.
     * (Note that this policy is the same as for java.util.Hashtable, but unlike
     * java.util.HashMap, which does accept nulls as valid keys and values.)
     * <p>
     * The value can be retrieved by calling the <code>get</code> method with
     * a key that is equal to the original key.
     * 
     * @param key
     *            the table key.
     * @param value
     *            the value.
     * @return the previous value of the specified key in this table, or
     *         <code>null</code> if it did not have one.
     * @exception NullPointerException
     *                if the key or value is <code>null</code>.
     * @see Object#equals(Object)
     * @see #get(Object)
     */

    public Object put(Object key, Object value) {

        /*
         * Strategy: Start out the same way as get(), trying to find key without
         * lock. If not found, get lock, check that it is correct list, and if
         * so, prepend new node. If not correct, retry. If found, get lock to
         * avoid races with other puts or removes. If the node is in the midst
         * of removal, retry. When the local count exceeds threshold, check to
         * see if other segments have reached threshold, and if so, start global
         * locking/resizing via resize().
         */

        if (value == null) { throw new NullPointerException(); }

        int hash = hash(key);
        Segment seg = segments[hash & (CONCURRENCY_LEVEL - 1)];
        Entry[] tab = table;
        int index = hash & (tab.length - 1);
        Entry first = tab[index];
        Entry e = first;
        int segcount = 0;

        for (;;) {

            if (e == null) {
                synchronized (seg) {
                    tab = table;
                    // make sure we are adding to correct list
                    if (first == tab[index]) {
                        // Add to front of list
                        Entry newEntry = new Entry(hash, key, value, first);

                        tab[index] = newEntry;
                        if ((segcount = ++seg.count) < threshold) {
                            return null;
                        } else {
                            break;
                        } // fall below to call resize outside lock
                    } else { // retry
                        e = first = tab[index = hash & (tab.length - 1)];
                    }
                }
            } else if (key == e.key || (e.hash == hash && key.equals(e.key))) {

                // synch to avoid race with remove and to
                // ensure proper serialization of multiple replaces
                synchronized (seg) {
                    tab = table;
                    Object oldValue = e.value;

                    if (first == tab[index] && oldValue != null) {
                        e.value = value;
                        return oldValue;
                    } else {
                        e = first = tab[index = hash & (tab.length - 1)];
                    }
                }
            } else {
                e = e.next;
            }
        }

        // Reach here only to check if should call resize, outside of seg lock

        int bit = (1 << (hash & (CONCURRENCY_LEVEL - 1)));
        int votes = votesForResize;

        if ((votes & bit) == 0) {
            votes = votesForResize |= bit;
        } // Attempt resize if 1/4 segs vote,
        // or if this seg itself reaches overall threshold.
        // (The latter check is just a safeguard to avoid pathological cases.)
        else if (bitcount(votes) >= CONCURRENCY_LEVEL / 4
                || segcount > CONCURRENCY_LEVEL * threshold) {
            resize(0, 0);
        }

        return null;
    }

    /**
     * Gather all locks in order to call rehash, by recursing withing synch
     * blocks for each segment index. Initial call MUST be resize(0, 0)
     */

    protected void resize(int index, int votes) {
        if (index < segments.length) {
            Segment seg = segments[index];

            synchronized (seg) {
                votes += seg.count / threshold;
                resize(index + 1, votes);
            }
        } else if (votes >= CONCURRENCY_LEVEL / 4) { // avoid false alarm
            rehash();
        }
    }

    /**
     * Rehashes the contents of this map into a new table with a larger
     * capacity.
     */

    protected void rehash() {
        votesForResize = 0; // reset

        Entry[] oldMap = table;
        int oldCapacity = oldMap.length;

        if (oldCapacity >= MAXIMUM_CAPACITY) { return; }

        int newCapacity = oldCapacity << 1;

        Entry[] newMap = new Entry[newCapacity];

        /*
         * We need to guarantee that any existing reads of oldMap can proceed.
         * So we cannot yet null out each oldMap bin. Because we are using
         * power-of-two expansion, the elements from each bin must either stay
         * at same index, or move to oldCapacity+index. We also minimize new
         * node creation by catching cases where old nodes can be reused because
         * their .next fields won't change. (This is checked only for sequences
         * of one and two. It is not worth checking longer ones.)
         */

        for (int i = 0; i < oldCapacity; ++i) {
            Entry l = null;
            Entry h = null;
            Entry e = oldMap[i];

            while (e != null) {
                int hash = e.hash;
                Entry next = e.next;

                if ((hash & oldCapacity) == 0) { // stays at newMap[i]
                    if (l == null
                            && // try to reuse node
                            (next == null || (next.next == null && (next.hash & oldCapacity) == 0))) {
                        l = e;
                        break;
                    }
                    l = new Entry(hash, e.key, e.value, l);
                } else { // moves to newMap[oldCapacity+i]
                    if (h == null
                            && (next == null || (next.next == null && (next.hash & oldCapacity) != 0))) {
                        h = e;
                        break;
                    }
                    h = new Entry(hash, e.key, e.value, h);
                }
                e = next;
            }
            newMap[i] = l;
            newMap[oldCapacity + i] = h;
        }
        table = newMap;
        threshold = (int) (newCapacity * loadFactor / segments.length) + 1;

        // System.out.println("size: " + size() + " cap " + newCapacity);

    }

    /**
     * Removes the key (and its corresponding value) from this table. This
     * method does nothing if the key is not in the table.
     * 
     * @param key
     *            the key that needs to be removed.
     * @return the value to which the key had been mapped in this table, or
     *         <code>null</code> if the key did not have a mapping.
     * @exception NullPointerException
     *                if the key is <code>null</code>.
     */

    public Object remove(Object key) {
        return remove(key, null);
    }

    /**
     * Removes the (key, value) pair from this table. This method does nothing
     * if the key is not in the table, or if the key is associated with a
     * different value. This method is needed by EntrySet.
     * 
     * @param key
     *            the key that needs to be removed.
     * @param value
     *            the associated value. If the value is null, it means "any
     *            value".
     * @return the value to which the key had been mapped in this table, or
     *         <code>null</code> if the key did not have a mapping.
     * @exception NullPointerException
     *                if the key is <code>null</code>.
     */

    protected Object remove(Object key, Object value) {
        int hash = hash(key);
        Segment seg = segments[hash & (CONCURRENCY_LEVEL - 1)];

        /*
         * Removes are fully synchronized on seg because otherwise, they tend to
         * interact badly with ongoing puts. Strategy: Find the entry, then 1.
         * Set value field to null, to force get() to retry 2. Rebuild the list
         * without this entry. All entries following removed node can stay in
         * list, but all preceeding ones need to be cloned. Traversals rely on
         * this strategy to ensure that elements will not be repeated during
         * iteration.
         */

        synchronized (seg) {
            Entry[] tab = table;
            int index = hash & (tab.length - 1);
            Entry first = tab[index];
            Entry e = first;

            for (;;) {
                if (e == null) {
                    return null;
                } else if (key == e.key
                        || (e.hash == hash && key.equals(e.key))) {
                    Object oldValue = e.value;

                    if (value != null && !value.equals(oldValue)) { return null; }
                    e.value = null;
                    seg.count--;

                    Entry head = e.next;

                    for (Entry p = first; p != e; p = p.next) {
                        head = new Entry(p.hash, p.key, p.value, head);
                    }
                    tab[index] = head;
                    return oldValue;
                } else {
                    e = e.next;
                }
            }
        }
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value. Note: This method requires a full internal traversal of
     * the hash table, and so is much slower than method <tt>containsKey</tt>.
     * 
     * @param value
     *            value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     * @exception NullPointerException
     *                if the value is <code>null</code>.
     */

    public boolean containsValue(Object value) {

        if (value == null) { throw new NullPointerException(); }

        for (int s = 0; s < segments.length; ++s) {
            Segment seg = segments[s];
            Entry[] tab;

            synchronized (seg) {
                tab = table;
            }
            for (int i = s; i < tab.length; i += segments.length) {
                for (Entry e = tab[i]; e != null; e = e.next) {
                    Object v = e.value;

                    if (v != null && value.equals(v)) { return true; }
                }
            }
        }
        return false;
    }

    /**
     * Tests if some key maps into the specified value in this table. This
     * operation is more expensive than the <code>containsKey</code> method.
     * <p>
     * Note that this method is identical in functionality to containsValue,
     * (which is part of the Map interface in the collections framework).
     * 
     * @param value
     *            a value to search for.
     * @return <code>true</code> if and only if some key maps to the
     *         <code>value</code> argument in this table as determined by the
     *         <tt>equals</tt> method; <code>false</code> otherwise.
     * @exception NullPointerException
     *                if the value is <code>null</code>.
     * @see #containsKey(Object)
     * @see #containsValue(Object)
     * @see Map
     */

    public boolean contains(Object value) {
        return containsValue(value);
    }

    /**
     * Copies all of the mappings from the specified map to this one. These
     * mappings replace any mappings that this map had for any of the keys
     * currently in the specified Map.
     * 
     * @param t
     *            Mappings to be stored in this map.
     */

    public void putAll(Map t) {
        for (Iterator it = t.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();

            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes all mappings from this map.
     */

    public void clear() {

        /*
         * We don't need all locks at once so long as locks are obtained in low
         * to high order
         */

        for (int s = 0; s < segments.length; ++s) {
            Segment seg = segments[s];

            synchronized (seg) {
                Entry[] tab = table;

                for (int i = s; i < tab.length; i += segments.length) {
                    for (Entry e = tab[i]; e != null; e = e.next) {
                        e.value = null;
                    }
                    tab[i] = null;
                    seg.count = 0;
                }
            }
        }
    }

    /**
     * Returns a shallow copy of this <tt>ConcurrentHashMap</tt> instance: the
     * keys and values themselves are not cloned.
     * 
     * @return a shallow copy of this map.
     */

    public Object clone() {
        try {
            ConcurrentHashMap t = (ConcurrentHashMap) super.clone();

            t.keySet = null;
            t.entrySet = null;
            t.values = null;

            // get current capacity. It is OK if this changes
            // by the time putAll is called. It is only used
            // to help avoid resizings.
            int cap;

            synchronized (segments[0]) {
                cap = table.length;
            }
            t.table = new Entry[cap];

            t.threshold = (int) (cap * loadFactor / segments.length) + 1;
            for (int i = 0; i < t.segments.length; ++i) {
                t.segments[i] = new Segment();
            }

            t.putAll(this);
            return t;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    // Views

    protected transient Set keySet = null;

    protected transient Set entrySet = null;

    protected transient Collection values = null;

    /**
     * Returns a set view of the keys contained in this map. The set is backed
     * by the map, so changes to the map are reflected in the set, and
     * vice-versa. The set supports element removal, which removes the
     * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>,<tt>removeAll</tt>,<tt>retainAll</tt>, and
     * <tt>clear</tt> operations. It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     * 
     * @return a set view of the keys contained in this map.
     */

    public Set keySet() {
        Set ks = keySet;

        if (ks != null) {
            return ks;
        } else {
            return keySet = new AbstractSet() {
                public Iterator iterator() {
                    return new KeyIterator();
                }

                public int size() {
                    return ConcurrentHashMap.this.size();
                }

                public boolean contains(Object o) {
                    return ConcurrentHashMap.this.containsKey(o);
                }

                public boolean remove(Object o) {
                    return ConcurrentHashMap.this.remove(o) != null;
                }

                public void clear() {
                    ConcurrentHashMap.this.clear();
                }
            };
        }
    }

    /**
     * Returns a collection view of the values contained in this map. The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa. The collection supports element removal,
     * which removes the corresponding mapping from this map, via the
     * <tt>Iterator.remove</tt>,<tt>Collection.remove</tt>,
     * <tt>removeAll</tt>,<tt>retainAll</tt>, and <tt>clear</tt>
     * operations. It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     * 
     * @return a collection view of the values contained in this map.
     */

    public Collection values() {
        Collection vs = values;

        if (vs != null) {
            return vs;
        } else {
            return values = new AbstractCollection() {
                public Iterator iterator() {
                    return new ValueIterator();
                }

                public int size() {
                    return ConcurrentHashMap.this.size();
                }

                public boolean contains(Object o) {
                    return ConcurrentHashMap.this.containsValue(o);
                }

                public void clear() {
                    ConcurrentHashMap.this.clear();
                }
            };
        }
    }

    /**
     * Returns a collection view of the mappings contained in this map. Each
     * element in the returned collection is a <tt>Map.Entry</tt>. The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa. The collection supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>,<tt>Collection.remove</tt>,
     * <tt>removeAll</tt>,<tt>retainAll</tt>, and <tt>clear</tt>
     * operations. It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     * 
     * @return a collection view of the mappings contained in this map.
     * @see Map.Entry
     */

    public Set entrySet() {
        Set es = entrySet;

        if (es != null) {
            return es;
        } else {
            return entrySet = new AbstractSet() {
                public Iterator iterator() {
                    return new HashIterator();
                }

                public boolean contains(Object o) {
                    if (!(o instanceof Map.Entry)) { return false; }
                    Map.Entry entry = (Map.Entry) o;
                    Object key = entry.getKey();
                    Object v = ConcurrentHashMap.this.get(key);

                    return v != null && v.equals(entry.getValue());
                }

                public boolean remove(Object o) {
                    if (!(o instanceof Map.Entry)) { return false; }
                    Map.Entry entry = (Map.Entry) o;
                    Object k = entry.getKey();
                    Object v = entry.getValue();

                    return ConcurrentHashMap.this.remove(k, v) != null;
                }

                public int size() {
                    return ConcurrentHashMap.this.size();
                }

                public void clear() {
                    ConcurrentHashMap.this.clear();
                }
            };
        }

    }

    /**
     * Returns an enumeration of the keys in this table.
     * 
     * @return an enumeration of the keys in this table.
     * @see Enumeration
     * @see #elements()
     * @see #keySet()
     * @see Map
     */
    public Enumeration keys() {
        return new KeyIterator();
    }

    /**
     * Returns an enumeration of the values in this table. Use the Enumeration
     * methods on the returned object to fetch the elements sequentially.
     * 
     * @return an enumeration of the values in this table.
     * @see java.util.Enumeration
     * @see #keys()
     * @see #values()
     * @see Map
     */

    public Enumeration elements() {
        return new ValueIterator();
    }

    /**
     * ConcurrentHashMap collision list entry.
     */

    protected static class Entry implements Map.Entry {

        /*
         * The use of volatile for value field ensures that we can detect status
         * changes without synchronization. The other fields are never changed,
         * and are marked as final.
         */

        protected final int hash;

        protected final Object key;

        protected final Entry next;

        protected volatile Object value;

        Entry(int hash, Object key, Object value, Entry next) {
            this.hash = hash;
            this.key = key;
            this.next = next;
            this.value = value;
        }

        // Map.Entry Ops

        public Object getKey() {
            return key;
        }

        /**
         * Get the value. Note: In an entrySet or entrySet.iterator, unless you
         * can guarantee lack of concurrent modification,
         * <tt>getValue</tt> <em>might</em> return null, reflecting the fact
         * that the entry has been concurrently removed. However, there are no
         * assurances that concurrent removals will be reflected using this
         * method.
         * 
         * @return the current value, or null if the entry has been detectably
         *         removed.
         */
        public Object getValue() {
            return value;
        }

        /**
         * Set the value of this entry. Note: In an entrySet or
         * entrySet.iterator), unless you can guarantee lack of concurrent
         * modification, <tt>setValue</tt> is not strictly guaranteed to
         * actually replace the value field obtained via the <tt>get</tt>
         * operation of the underlying hash table in multithreaded applications.
         * If iterator-wide synchronization is not used, and any other
         * concurrent <tt>put</tt> or <tt>remove</tt> operations occur,
         * sometimes even to <em>other</em> entries, then this change is not
         * guaranteed to be reflected in the hash table. (It might, or it might
         * not. There are no assurances either way.)
         * 
         * @param value
         *            the new value.
         * @return the previous value, or null if entry has been detectably
         *         removed.
         * @exception NullPointerException
         *                if the value is <code>null</code>.
         */

        public Object setValue(Object value) {
            if (value == null) { throw new NullPointerException(); }
            Object oldValue = this.value;

            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) { return false; }

            Map.Entry e = (Map.Entry) o;

            if (!key.equals(e.getKey())) { return false; }

            Object v = value;

            return (v == null) ? e.getValue() == null : v.equals(e.getValue());
        }

        public int hashCode() {
            Object v = value;

            return hash ^ ((v == null) ? 0 : v.hashCode());
        }

        public String toString() {
            return key + "=" + value;
        }

    }

    protected class HashIterator implements Iterator,Enumeration {
        protected final Entry[] tab; // snapshot of table

        protected int index; // current slot

        protected Entry entry = null; // current node of slot

        protected Object currentKey; // key for current node

        protected Object currentValue; // value for current node

        protected Entry lastReturned = null; // last node returned by

        // next

        protected HashIterator() {
            // force all segments to synch
            synchronized (segments[0]) {
                tab = table;
            }
            for (int i = 1; i < segments.length; ++i) {
                segments[i].synch();
            }
            index = tab.length - 1;
        }

        public boolean hasMoreElements() {
            return hasNext();
        }

        public Object nextElement() {
            return next();
        }

        public boolean hasNext() {

            /*
             * currentkey and currentValue are set here to ensure that next()
             * returns normally if hasNext() returns true. This avoids surprises
             * especially when final element is removed during traversal --
             * instead, we just ignore the removal during current traversal.
             */

            for (;;) {
                if (entry != null) {
                    Object v = entry.value;

                    if (v != null) {
                        currentKey = entry.key;
                        currentValue = v;
                        return true;
                    } else {
                        entry = entry.next;
                    }
                }

                while (entry == null && index >= 0) {
                    entry = tab[index--];
                }

                if (entry == null) {
                    currentKey = currentValue = null;
                    return false;
                }
            }
        }

        protected Object returnValueOfNext() {
            return entry;
        }

        public Object next() {
            if (currentKey == null && !hasNext()) { throw new NoSuchElementException(); }

            Object result = returnValueOfNext();

            lastReturned = entry;
            currentKey = currentValue = null;
            entry = entry.next;
            return result;
        }

        public void remove() {
            if (lastReturned == null) { throw new IllegalStateException(); }
            ConcurrentHashMap.this.remove(lastReturned.key);
        }

    }

    protected class KeyIterator extends HashIterator {
        protected Object returnValueOfNext() {
            return currentKey;
        }
    }

    protected class ValueIterator extends HashIterator {
        protected Object returnValueOfNext() {
            return currentValue;
        }
    }

    /**
     * Save the state of the <tt>ConcurrentHashMap</tt> instance to a stream
     * (i.e., serialize it).
     * 
     * @serialData An estimate of the table size, followed by the key (Object)
     *             and value (Object) for each key-value mapping, followed by a
     *             null pair. The key-value mappings are emitted in no
     *             particular order.
     */

    protected void writeObject(java.io.ObjectOutputStream s) throws IOException {
        // Write out the loadfactor, and any hidden stuff
        s.defaultWriteObject();

        // Write out capacity estimate. It is OK if this
        // changes during the write, since it is only used by
        // readObject to set initial capacity, to avoid needless resizings.

        int cap;

        synchronized (segments[0]) {
            cap = table.length;
        }
        s.writeInt(cap);

        // Write out keys and values (alternating)
        for (int k = 0; k < segments.length; ++k) {
            Segment seg = segments[k];
            Entry[] tab;

            synchronized (seg) {
                tab = table;
            }
            for (int i = k; i < tab.length; i += segments.length) {
                for (Entry e = tab[i]; e != null; e = e.next) {
                    s.writeObject(e.key);
                    s.writeObject(e.value);
                }
            }
        }

        s.writeObject(null);
        s.writeObject(null);
    }

    /**
     * Reconstitute the <tt>ConcurrentHashMap</tt> instance from a stream
     * (i.e., deserialize it).
     */
    protected void readObject(java.io.ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        // Read in the threshold, loadfactor, and any hidden stuff
        s.defaultReadObject();

        int cap = s.readInt();

        table = new Entry[cap];
        threshold = (int) (cap * loadFactor / segments.length) + 1;
        for (int i = 0; i < segments.length; ++i) {
            segments[i] = new Segment();
        }

        // Read the keys and values, and put the mappings in the table
        for (;;) {
            Object key = s.readObject();
            Object value = s.readObject();

            if (key == null) {
                break;
            }
            put(key, value);
        }
    }

}