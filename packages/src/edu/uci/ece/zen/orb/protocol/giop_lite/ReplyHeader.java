/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.protocol.giop_lite;

import javax.realtime.ImmortalMemory;

import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

/**
 * Struct definition : ReplyHeader_1_0
 * 
 * @author OpenORB Compiler
 */
public final class ReplyHeader implements org.omg.CORBA.portable.IDLEntity {

    private static ReplyHeader rh;

    public static ReplyHeader instance() {

        try {
            if (rh == null) rh = (ReplyHeader) ImmortalMemory.instance()
                    .newInstance(ReplyHeader.class);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.WARN, ReplyHeader.class, "instance", e);
        }

        return rh;
    }

    /**
     * Struct member service_context
     */
    public FString service_context;

    /**
     * Struct member request_id
     */
    public int request_id;

    /**
     * Struct member reply_status
     */
    public int reply_status;

    /**
     * Default constructor
     */
    public ReplyHeader() {
    }

    /**
     * Constructor with fields initialization
     * 
     * @param service_context
     *            service_context struct member
     * @param request_id
     *            request_id struct member
     * @param reply_status
     *            reply_status struct member
     */
    public void init(FString service_context, int request_id, int reply_status) {
        this.service_context = service_context;
        this.request_id = request_id;
        this.reply_status = reply_status;
    }

}
