package edu.uci.ece.zen.orb.giop;

import javax.realtime.ImmortalMemory;

import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

/**
 * Inner class used to represent the information parsend from a GIOP message
 * header.
 * 
 * @author Bruce Miller
 */
public class GIOPHeaderInfo {
    public boolean nextMessageIsFragment = false;

    public int messageSize = 0;

    public boolean isLittleEndian;

    public byte giopMajorVersion;

    public byte giopMinorVersion;

    public byte messageType;

}
