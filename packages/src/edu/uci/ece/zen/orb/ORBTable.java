/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.Hashtable;

/**
 * This class keeps track of all the ORB's that are running. When a user calls
 * ORB.init with a certain name, they should get back the same ORB reference.
 * 
 * @author Krishna Raman
 * @version $Revision: 1.3 $ $Date: 2004/02/25 08:15:19 $
 */
public class ORBTable {
    private static Hashtable orbTable;

    public static synchronized ORB find(FString name) {
        return (ORB) orbTable.get(name);
    }

    public static void hash(FString orbId, ORB orb) {
        orbTable.put(orbId, orb);
    }

    public static void remove(FString orb) {
        orbTable.remove(orb);
    }
}