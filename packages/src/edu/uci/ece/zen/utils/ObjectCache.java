package edu.uci.ece.zen.utils;

/**
 * This class is used to keep track of Objects that are allocated in immortal
 * memory so that we dont run out of memory. This cache doesnot allocate any
 * extra memory after initialization.
 */

public class ObjectCache{

    private ListNode freeListHead;
    private ListNode freeListTail;

    private ListNode allocatedListHead;
    private ListNode allocatedListTail;

    ObjectCache( int number ){
        for( int i=0;i<number;i++ ){
            ListNode n = new ListNode();
            
            if( allocatedListHead == null ){
                allocatedListHead = allocatedListTail = n;
            }else{
                n.next = allocatedListHead;
                allocatedListHead = n;
            }
        }
    }

    public synchronized Object get(){
        if( freeListHead == null )
            throw new RuntimeException( "Trying to take object out of an empty cache." );

        ListNode n = freeListHead;
        freeListHead = freeListHead.next;

        allocatedListTail.next = n;
        n.next = null;
        allocatedListTail = n;
        Object ret = n.val;
        n.val = null;

        if( freeListHead == null )
            freeListTail = null;
        return ret;
    }

    public synchronized void put( Object val ){
        if( allocatedListHead == null )
            throw new RuntimeException( "Already have more objects than was prealocated." );

        ListNode n = allocatedListHead;
        allocatedListHead = allocatedListHead.next;

        freeListTail.next = n;
        n.next = null;
        freeListTail = n;
        n.val = val;

        if( allocatedListHead == null )
            allocatedListTail = null;
    }


    public class ListNode{
        public ListNode next;
        public Object val;
    }
}
