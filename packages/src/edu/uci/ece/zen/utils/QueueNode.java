/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.utils;

/**
 * Queue internal data structure used to maintain a lisk list of values.
 * 
 * @author Krishna Raman
 */
public class QueueNode {
    public QueueNode() {
    }

    public QueueNode next;

    public Object value;
}