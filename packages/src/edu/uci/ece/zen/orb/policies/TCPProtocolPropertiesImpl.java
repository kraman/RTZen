/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.policies;

import org.omg.RTCORBA.TCPProtocolProperties;

/**
 * Interface definition : TCPProtocolProperties
 * 
 * @author OpenORB Compiler
 */
public class TCPProtocolPropertiesImpl extends org.omg.CORBA.LocalObject
        implements TCPProtocolProperties {

    int send_buffer_size;

    int recv_buffer_size;

    boolean keep_alive;

    boolean dont_route;

    boolean no_delay;

    public TCPProtocolPropertiesImpl() {
        send_buffer_size = 0;
        ;
        recv_buffer_size = 0;
        keep_alive = false;
        dont_route = false;
        no_delay = false;
    }

    /**
     * Read accessor for send_buffer_size attribute
     * 
     * @return the attribute value
     */
    public int send_buffer_size() {
        return send_buffer_size;
    }

    /**
     * Write accessor for send_buffer_size attribute
     * 
     * @param value
     *            the attribute value
     */
    public void send_buffer_size(int value) {
        send_buffer_size = value;
    }

    /**
     * Read accessor for recv_buffer_size attribute
     * 
     * @return the attribute value
     */
    public int recv_buffer_size() {
        return recv_buffer_size;
    }

    /**
     * Write accessor for recv_buffer_size attribute
     * 
     * @param value
     *            the attribute value
     */
    public void recv_buffer_size(int value) {
        recv_buffer_size = value;
    }

    /**
     * Read accessor for keep_alive attribute
     * 
     * @return the attribute value
     */
    public boolean keep_alive() {
        return keep_alive;
    }

    /**
     * Write accessor for keep_alive attribute
     * 
     * @param value
     *            the attribute value
     */
    public void keep_alive(boolean value) {
        keep_alive = value;
    }

    /**
     * Read accessor for dont_route attribute
     * 
     * @return the attribute value
     */
    public boolean dont_route() {
        return dont_route;
    }

    /**
     * Write accessor for dont_route attribute
     * 
     * @param value
     *            the attribute value
     */
    public void dont_route(boolean value) {
        dont_route = value;
    }

    /**
     * Read accessor for no_delay attribute
     * 
     * @return the attribute value
     */
    public boolean no_delay() {
        return no_delay;
    }

    /**
     * Write accessor for no_delay attribute
     * 
     * @param value
     *            the attribute value
     */
    public void no_delay(boolean value) {
        no_delay = value;
    }

}