package edu.uci.ece.zen.utils;

public class Hashtable{
    class HashNode{
        Object key;
        Object data;

        HashNode next;
    }
    
    HashNode[] list;
    HashNode unusedHashNodes;

    private void push( HashNode h ){
        h.next = unusedHashNodes;
        unusedHashNodes = h;
    }

    private HashNode pop(){
        synchronized( list ){
            HashNode tmp = unusedHashNodes;
            unusedHashNodes = unusedHashNodes.next;
            return tmp;
        }
    }
    
    public void init( int limit ){
        list = new HashNode[limit];
        for( int i=0;i<limit;i++ ){
            push( new HashNode() );
        }
    }
    
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

    public void removeAll(){
    }
}
