package edu.uci.ece.zen.utils;

import javax.realtime.*;

public class Queue{
    private QueueNode freeListHead;
    private QueueNode freeListTail;
    private final static Integer syncObject = new Integer(0);

    private QueueNode getNode(){
        QueueNode ret = null;
        synchronized( syncObject ){
            if( freeListHead == null )
                try{
                    ret = (QueueNode) MemoryArea.getMemoryArea( this ).newInstance( QueueNode.class );
                }catch( Exception e ){
                    e.printStackTrace();
                    System.exit(-1);
                }
            else{
                ret = freeListHead;
                freeListHead = freeListHead.next;
            }
        }
        ret.next = null;
        return ret;
    }

    private void freeNode( QueueNode node ){
        node.value = null;
        node.next = null;
        synchronized( syncObject ){
            if( freeListHead == null ){
                freeListHead = freeListTail = node;
            }else{
                freeListTail.next = node;
                freeListTail = node;
            }
        }
    }

    private QueueNode allocListHead;
    private QueueNode allocListTail;
    private final Integer sObject = new Integer(0);

    public void enqueue( Object data ){
        QueueNode node = getNode();
        node.value=data;
        synchronized( sObject ){
            if( allocListHead == null ){
                allocListHead = allocListTail = node;
            }else{
                allocListTail.next = node;
                allocListTail = node;
            }
        }
    }

    public Object peek(){
        synchronized( sObject ){
            if( allocListHead == null )
                return null;
            else
                return allocListHead.value;
        }
    }

    public boolean isEmpty(){
        synchronized( sObject ){
            return allocListHead == null;
        }
    }

    public Object dequeue(){
        QueueNode ret;
        synchronized( sObject ){
            if( allocListHead == null )
                return null;
            else{
                ret = allocListHead;
                allocListHead = allocListHead.next;
            }
        }
        Object obj = ret.value;
        freeNode( ret );
        return obj;
    }
}

