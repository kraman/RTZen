package edu.uci.ece.zen.orb.giop;

/** Inner class used to represent the information parsend from a
 * GIOP message header.
 */
public class GIOPHeaderInfo {
    boolean nextMessageIsFragment = false;
    int messageSize = 0;
    boolean isLittleEndian;
    byte giopMajorVersion;
    byte giopMinorVersion;
    byte messageType;
}



