package edu.uci.ece.zen.utils;

public class ActiveDemuxTable{
    class Node{
        public Node next;
        public int idx;
        public int genCount;

        public Object key;
        public long primitiveKey;

        public Object data;

        public boolean inUse;
    }

    Node[] data;
    Node freeList;

    private void push( Node idx ){
        synchronized( data ){
            idx.data = null;
            idx.inUse = false;
            idx.genCount++;
            System.out.println( "genCount increased: " + idx.genCount );
            idx.next = freeList;
            freeList = idx;
        }
    }

    private int pop(){
        synchronized( data ){
            int idx = freeList.idx;
            freeList = freeList.next;
            data[idx].next = null;
            data[idx].inUse = true;
            return idx;
        }
    }

    public ActiveDemuxTable(){}

    public ActiveDemuxTable( int numEntries ){
        init( numEntries );
    }

    public void init( int numEntries ){
        data = new Node[numEntries];
        for( int i=0;i<numEntries;i++ ){
            data[i] = new Node();
            data[i].idx = i;
            System.out.print( "init " );
            push( data[i] );
        }
    }

    public int bind( Object key , Object data ){
        int idx = pop();
        this.data[idx].key = key;
        this.data[idx].data = data;
        return idx;
    }

    public int bind( long primitiveKey , Object data ){
        int idx = pop();
        this.data[idx].primitiveKey = primitiveKey;
        this.data[idx].data = data;
        return idx;
    }

    public void unbind( int idx ){
        System.out.print( "unbind " );
        push( data[idx] );
    }

    public Object mapEntry( int idx ){
        if( idx < 0 || idx > data.length )
            return null;
        return data[idx].data;
    }

    public int getGenCount( int idx ){
        return data[idx].genCount;
    }

    public int find( Object key ){
        for( int i=0;i<data.length;i++ ){
            if( data[i].inUse && data[i].key.equals( key ) )
                return i;
        }
        return -1;
    }

    public int find( long primitiveKey ){
        for( int i=0;i<data.length;i++ ){
            if( data[i].inUse && data[i].primitiveKey == primitiveKey )
                return i;
        }
        return -1;
    }
}
