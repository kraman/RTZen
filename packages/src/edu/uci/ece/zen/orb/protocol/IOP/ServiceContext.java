/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.protocol.IOP;

import edu.uci.ece.zen.utils.FString;
import javax.realtime.ImmortalMemory;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.Queue;
import edu.uci.ece.zen.orb.ORB;

/**
 * Struct definition : ServiceContext
 *
 * @author OpenORB Compiler
 */
public final class ServiceContext implements org.omg.CORBA.portable.IDLEntity {
    /**
     * Struct member context_id
     */
    public int context_id;

    /**
     * Struct member context_data
     */
    public byte[] context_data; // = new byte[1024];

    public int context_data_length = 0;

    /**
     * Constructor with fields initialization
     *
     * @param context_id
     *            context_id struct member
     * @param context_data
     *            context_data struct member public static void init(int
     *            context_id, byte[] context_data, int context_data_length) {
     *            this.context_id = context_id; this.context_data =
     *            context_data; this.context_data_length = context_data_length; }
     */

    //public static void release(FString fs){
    //    queue.enqueue(fs);
    //}

}
