/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.transport.iiop;

import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

public class Connector extends edu.uci.ece.zen.orb.transport.Connector {
    public Connector() {
    }

    protected edu.uci.ece.zen.orb.transport.Transport internalConnect(
            String host, int port, edu.uci.ece.zen.orb.ORB orb,
            edu.uci.ece.zen.orb.ORBImpl orbImpl) {
        return new Transport(orb, orbImpl, host, port);
    }

    private static Connector _instance;

    public static Connector instance() {
        if (_instance == null) {
            try {
                _instance = (Connector) javax.realtime.ImmortalMemory
                        .instance()
                        .newInstance(
                                edu.uci.ece.zen.orb.transport.iiop.Connector.class);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.WARN, Connector.class, "instance", e);
            }
        }
        return _instance;
    }
}