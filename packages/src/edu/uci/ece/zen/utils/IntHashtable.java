package edu.uci.ece.zen.utils;

import javax.realtime.ImmortalMemory;

import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ZenProperties;

/**
 * This class provides a constant space hashtable which is Scoped Memory safe.
 * 
 * @author Krishna Raman
 */
public class IntHashtable {

    /** Table of hash value-objects. */
    int[] keylist;
    int[] valuelist;
    boolean[] collision;

    /**
     * Initialize the hash table and create the hash nodes.
     * 
     * @param limit
     *            The maximum number of values that will be stored in the table.
     */
    public void init(int limit) {
        try{
            keylist = (int[]) ImmortalMemory.instance().newArray(
                    int.class, limit);
                    
            valuelist = (int[]) ImmortalMemory.instance().newArray(
            int.class, limit);
            
            collision = (boolean[]) ImmortalMemory.instance().newArray(
            boolean.class, limit);
        }catch(Exception e){
            e.printStackTrace();//TODO
        }
    }

    /**
     * Associate the key with the data in the hash table.
     * 
     * @param key
     *            The key into the hashtable.
     * @param data
     *            The data to associate with the key.
     */
    public void put(int key, int data) {
        int hash = Math.abs(key) % keylist.length;
        
        if(collision[hash] && key != keylist[hash])
            ZenProperties.logger.log(Logger.FATAL, getClass(), "put", "Collision has occurred.");
        
        keylist[hash] = key;
        valuelist[hash] = data;
        collision[hash] = true;
    }

    public void put(Object key, int data) {
        put(key.hashCode(), data);
    }

    /**
     * Lookup the key in the hashtable.
     * 
     * @param key
     *            Key to look up.
     * @return The object associated with the key or null.
     */
    public int get(int key) {
        int hash = Math.abs(key) % keylist.length;
        return valuelist[hash];
    }
    
    public int get(Object key) {
        return get(key.hashCode());
    }    

    public void clear() {
    }

    /**
     * Remove the key association from the hash table.
     * 
     * @param key
     *            The key to remove.
    
    public void remove(Object key) {
        int hash = key.hashCode() % list.length;
        synchronized (list) {
            HashNode prev = null;
            for (HashNode i = list[hash]; i != null; prev = i, i = i.next) {
                if (i.key.equals(key)) {
                    if (prev == null) {
                        HashNode tmp = i;
                        list[hash] = i.next;
                        push(tmp);
                    } else {
                        HashNode tmp = i;
                        prev.next = i.next;
                        push(tmp);
                    }
                }
            }
        }
    } */
}
