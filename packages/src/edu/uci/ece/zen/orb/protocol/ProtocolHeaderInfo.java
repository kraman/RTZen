/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.protocol;

/**
 * Inner class used to represent the information parsend from a GIOP message
 * header.
 * 
 * @author Bruce Miller
 * @author Krishna Raman
 */
public class ProtocolHeaderInfo {
    public boolean nextMessageIsFragment = false;
    public int messageSize = 0;
    public boolean isLittleEndian;
    public byte majorVersion;
    public byte minorVersion;
    public byte messageType;
}
