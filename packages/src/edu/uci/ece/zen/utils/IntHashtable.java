/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.utils;

import javax.realtime.ImmortalMemory;

import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ZenProperties;

/**
 * This class provides a constant space hashtable for integer values.
 * This hashtable implements linear probing to handle collisions
 * since allocating nodes for chaining causes various memory-related problems.
 * This hashtable doesn't expand when full, so the user may need to know about
 * this limitation.
 * 
 * @author Mark Panahi
 * @author Krishna Raman 
 */
public class IntHashtable {

    /** Table of hash value-objects. */
    int[] keylist;
    int[] valuelist;
    boolean[] collision;
    private int count;

    /**
     * Initialize the hash table and create the hash nodes.
     * 
     * @param limit
     *            The maximum number of values that will be stored in the table.
     */
    public void init(int limit) {
        count = 0;
        try{
            keylist = (int[]) ImmortalMemory.instance().newArray( int.class, limit);
            valuelist = (int[]) ImmortalMemory.instance().newArray( int.class, limit);
            collision = (boolean[]) ImmortalMemory.instance().newArray( boolean.class, limit);
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
        int hash = locate(key);//Math.abs(key) % keylist.length;
        
        if(hash == -1)
            ZenProperties.logger.log(Logger.SEVERE, getClass(), "put", "List full. Cannot add element.");
        else{
            keylist[hash] = key;
            valuelist[hash] = data;
            collision[hash] = true;
        }
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
        int hash = locate(key);//Math.abs(key) % keylist.length;
        return valuelist[hash];
    }
    
    public int get(Object key) {
        return get(key.hashCode());
    }    

    public void clear() {
        ZenProperties.logger.log(Logger.WARN, getClass(), "clear", "Not implemented.");
    }

    protected int locate(int key){
        //System.out.println("initial: " + key);
        // compute an initial hash code
        int hash = Math.abs(key % keylist.length);
        int firstTry = hash;

        while (collision[hash]){
            //System.out.println("probing: " + hash);
            // value located? return the index in table
            if (key == keylist[hash]) 
                return hash;

            // linear probing; other methods would change this line:
            hash = (1+hash)%keylist.length;
            
            if(hash == firstTry)
                return -1;
        }
        //System.out.println("final: " + hash);
        count++;
        return hash;
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
    
    public String toString(){
        String s = "";
        
        for (int i = 0; i < keylist.length; ++i){
            s += "key: " + keylist[i] + " val: " + valuelist[i] + " col: " + collision[i] + "\n";
        }        
        return s;
    }
    
    public static void main(String args[]){
        IntHashtable ih = new IntHashtable();
        ih.init(32);
        for (int i = 0; i < 33; ++i){
            ih.put(i*16, i*16);
        }
        for (int i = 0; i < 32; ++i){
            System.out.println(ih.get(i*16));
        }  
        for (int i = 0; i < 32; ++i){
            ih.put(i*16, i*16);
        }        
        System.out.println(ih.toString());
    }
}
