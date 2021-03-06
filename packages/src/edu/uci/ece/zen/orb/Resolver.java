/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import java.util.Vector;

import javax.realtime.ImmortalMemory;
import javax.realtime.RealtimeThread;

import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ZenProperties;

public abstract class Resolver {
    private static java.util.Vector resolverList;

    protected static void registerResolver(Resolver resolver) {
        try {
            if (resolverList == null) resolverList = (Vector) ImmortalMemory
                    .instance().newInstance(java.util.Vector.class);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL,
                    Resolver.class, "registerResolver", e);
        }
        resolverList.add(resolver);
    }

    public static String[] getResolverStrings() {
        String[] ret = new String[resolverList.size()];
        for (int i = 0; i < resolverList.size(); i++)
            ret[i] = (String) (((Resolver) resolverList.elementAt(i))
                    .toString());
        return ret;
    }

    public static org.omg.CORBA.Object resolve(ORB orb, String str) {
        for (int i = 0; i < resolverList.size(); i++) {
            Resolver res = ((Resolver) resolverList.elementAt(i));
            if (res.toString().equals(str)) return res.resolve(orb);
        }
        return null;
    }

    private final String resolverString;

    protected Resolver(String resolverString) {

        if (RealtimeThread.getCurrentMemoryArea() != ImmortalMemory.instance()) {
            ZenProperties.logger.log(Logger.FATAL,
                    getClass(), "<init>",
                    "Resolver is not allocated in ImmortalMemory");
            System.exit(-1);
        }
        this.resolverString = resolverString;
    }

    public final String toString() {
        return resolverString;
    }

    public abstract org.omg.CORBA.Object resolve(ORB orb);
}