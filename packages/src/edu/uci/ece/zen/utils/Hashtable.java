package edu.uci.ece.zen.utils;

/** This class provides a constant space hashtable which is Scoped Memory safe.
 * @author Krishna Raman
 */
public class Hashtable{

    /** The hashtable uses linear chaining and this class provides a link list
     * node.
     */
    class HashNode{
        Object key;
        Object data;

        HashNode next;
    }
    
    /** Table of hash value-objects. */
    HashNode[] list;

    /** Linked list of unused hash nodes. */
    HashNode unusedHashNodes;

    /** Adds a hash node to the begining of the unused node list. 
     * @param h The hash node to return to the unused list.
     */
    private void push( HashNode h ){
        h.next = unusedHashNodes;
        unusedHashNodes = h;
    }

    /** Get a hash node from the begining of the unused node list. */
    private HashNode pop(){
        synchronized( list ){
            HashNode tmp = unusedHashNodes;
            unusedHashNodes = unusedHashNodes.next;
            return tmp;
        }
    }
    
    /** Initialize the hash table and create the hash nodes. 
     * @param limit The maximum number of values that will be stored in the table.
     */
    public void init( int limit ){
        list = new HashNode[limit];
        for( int i=0;i<limit;i++ ){
            push( new HashNode() );
        }
    }
    
    /** Associate the key with the data in the hash table.
     * @param key The key into the hashtable.
     * @param data The data to associate with the key.
     */
    public void put( Object key , Object data ){
        int hash = key.hashCode() % list.length;
        HashNode hn = pop();
        hn.key = key;
        hn.data = data;
        synchronized( list ){
            hn.next = list[hash];
            list[hash] = hn;
        }
    }
    
    /** Lookup the key in the hashtable.
     * @param key Key to loop up.
     * @return The object associated with the key or null.
     */
    public Object get( Object key ){
        int hash = key.hashCode() % list.length;
        synchronized( list ){
            for( HashNode i = list[hash] ; i != null ; i = i.next ){
                if( i.key.equals( key ) )
                    return i.data;
            }
        }
        return null;
    }

    public void clear(){
    }
    
    /** Remove the key association from the hash table.
     * @param key The key to remove.
     */
    public void remove( Object key ){
        int hash = key.hashCode() % list.length;
        synchronized( list ){
            HashNode prev = null;
            for( HashNode i = list[hash] ; i != null ; prev = i , i = i.next ){
                if( i.key.equals( key ) ){
                    if( prev == null ){
                        HashNode tmp = i;
                        list[hash] = i.next;
                        push( tmp );
                    }else{
                        HashNode tmp = i;
                        prev.next = i.next;
                        push( tmp );
                    }
                }
            }
        }
    }
}
