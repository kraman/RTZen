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
