package edu.uci.ece.zen.orb.protocol;

import javax.realtime.ImmortalMemory;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

/**
 * Inner class used to represent the information parsend from a GIOP message
 * header.
 * 
 * @author Bruce Miller
 * @author Krishna Raman
 */
public class HeaderInfo {
    public boolean nextMessageIsFragment = false;
    public int messageSize = 0;
    public boolean isLittleEndian;
    public byte majorVersion;
    public byte minorVersion;
    public byte messageType;
}
