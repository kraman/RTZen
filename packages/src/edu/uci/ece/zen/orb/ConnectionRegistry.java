package edu.uci.ece.zen.orb;

import javax.realtime.ScopedMemory;

/**
 * Based on edu.uci.ece.zen.utils.Hashtable but the keytable is different
 */
public class ConnectionRegistry extends edu.uci.ece.zen.utils.ActiveDemuxTable {
    public void putConnection(long key, ScopedMemory mem) {
        super.bind(key, mem);
    }

    public ScopedMemory getConnection(long key) {
        return (ScopedMemory) super.mapEntry(super.find(key));
    }

    public static long ip2long(String ip, short port) {
        long lip = 0;
        short b = 0;
        for (int i = 0; i < ip.length(); i++) {
            if (ip.charAt(i) == '.') {
                lip <<= 8;
                lip |= b & 0xff;
                b = 0;
            } else {
                b *= 10;
                b += ip.charAt(i) - '0';
            }
        }
        lip <<= 8;
        lip |= b & 0xff;
        return (lip << 16) + port;
    }
}