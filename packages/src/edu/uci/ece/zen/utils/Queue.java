/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.utils;

import javax.realtime.MemoryArea;
import javax.realtime.ImmortalMemory;

/**
 * This class maintains a queue which is scoped memory safe. It reuses as many
 * data structures as possible.
 * 
 * @author Krishna Raman
 */
public class Queue {
    /** Head to the linked list of unused queue nodes. */
    private QueueNode freeListHead;

    /** Tail to the linked list of unused queue nodes. */
    private QueueNode freeListTail;

    /** Object to synchronize the queue on. */
    private final Integer syncObject = new Integer(0);

    private int size = 0;

    /**
     * Retrieve an empty queue node from the linked list
     * 
     * @return An empty QueueNode object
     */
    private QueueNode getNode() {
        QueueNode ret = null;
        synchronized (syncObject) {
            if (freeListHead == null) try {
                ret = (QueueNode) MemoryArea.getMemoryArea(this).newInstance(QueueNode.class);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.FATAL, getClass(), "getNode", e);
                System.exit(-1);
            }
            else {
                ret = freeListHead;
                freeListHead = freeListHead.next;
            }
        }
        ret.next = null;
        return ret;
    }
    
    /**
     * Return the size of the linked list
     * 
     */
    public int size() {
        return size;
    }
    
    /**
     * Return a QueueNode to the freee linked list
     * 
     * @param node
     *            The QueueNode object to put on the list.
     */
    private void freeNode(QueueNode node) {
        node.value = null;
        node.next = null;
        synchronized (syncObject) {
            if (freeListHead == null) {
                freeListHead = freeListTail = node;
            } else {
                freeListTail.next = node;
                freeListTail = node;
            }
        }
    }

    /** Head of the linked list (internal representation of the queue) */
    private QueueNode allocListHead;

    /** Tail of the linked list (internal representation of the queue) */
    private QueueNode allocListTail;

    /** Object to synchronize the queue on. */
    private final Integer sObject = new Integer(0);

    public static Queue fromImmortal() {
        Queue q = null;
        
        try {
            q = (Queue)javax.realtime.ImmortalMemory.instance().newInstance(Queue.class); 
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, Queue.class, "fromImmortal", e);
        }
 
        return q;
    }


    /**
     * Enqueue an object onto the queue. Use a preexisting queue node if
     * possible, otherwise create a new queue node.
     * 
     * @param data
     *            The object to enqueue
     */
    public void enqueue(Object data) {
        QueueNode node = getNode();
        node.value = data;
        synchronized (sObject) {
            size++;
            if (allocListHead == null) {
                allocListHead = allocListTail = node;
            } else {
                allocListTail.next = node;
                allocListTail = node;
            }
        }
    }

    /**
     * Peek the top of the queue.
     * 
     * @return Object at the top of the queue or null.
     */
    public Object peek() {
        synchronized (sObject) {
            if (allocListHead == null) return null;
            else return allocListHead.value;
        }
    }

    /**
     * Returns true if the queue is empty. If you are using the
     * 
     * @return true if queue is empty, false otherwise.
     */
    public boolean isEmpty() {
        synchronized (sObject) {
            return allocListHead == null;
        }
    }

    /**
     * Return the top value on the Queue or null if none is available.
     * 
     * @return The top value on the queue or null.
     */
    public Object dequeue() {
        QueueNode ret;
	Object obj = null;
        synchronized (sObject) {
            if (allocListHead == null) return null;
            else {
                size--;
                ret = allocListHead;
                allocListHead = allocListHead.next;
            }
	    obj = ret.value;
	    freeNode(ret);
        }
        return obj;
    }

    public static Object getQueuedInstance(Class cls, Queue q) {

        if(q == null){
            ZenProperties.logger.log(Logger.FATAL, cls, "getQueuedInstance", "Queue not created");
            return null;
        }

        if(ZenBuildProperties.dbgDataStructures) {
            System.out.write( 'q' );
            System.out.write( '_' );
            System.out.write( 'i' );
            System.out.write( 'n' );
            System.out.write( 's' );
            System.out.write( 't' );
            Logger.writeln( q.size() );
            //System.out.println(cls.toString() + " current queue size: " + q.size());
        }

        Object ret = q.dequeue();
        if( ret == null ){
            try {
                ZenProperties.logger.log(Logger.WARN, cls, "getQueuedInstance", "Creating new instance.");
                ret = MemoryArea.getMemoryArea(q).newInstance(cls);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, cls, "getQueuedInstance", e);
            }
        }
        return ret;
    }
}

