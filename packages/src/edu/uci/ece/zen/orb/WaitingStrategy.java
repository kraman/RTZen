/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.orb.protocol.Message;

/**
 * A reply received by a Transport object is forwarded to a WaitingStrategy for
 * processing. Different request styles use different versions of this interface
 * to wait for a reply, for example, asynchronous requests process the reply in
 * the same thread that reads the request, while deferred synchronous requests
 * save the reply for processing when the application polls for the reply
 * contents.
 */
public abstract class WaitingStrategy {

    /**
     * Invoked by the thread receiving the reply to indicate that it was
     * successfully received. <br/><br/>NOTE: eventually we will need to handle
     * closed connections and other failures, it may be necessary to add new
     * methods for those cases.
     */
    public abstract void replyReceived(Message reply);

    /**
     * Invoked by the waiting thread to block until the replyReceived method is
     * invoked. <br/><br/>NOTE: it seems like only TwoWay invocations need this
     * method, may want to drop it.
     */
    public abstract CDRInputStream waitForReply();
    public abstract int getReplyStatus();

    private static int messageId = 0;

    public static synchronized int newMessageId() {
        messageId = (messageId + 1) % Integer.MAX_VALUE;
        return messageId;
    }
}
