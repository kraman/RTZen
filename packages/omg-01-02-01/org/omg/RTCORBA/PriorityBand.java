package org.omg.RTCORBA;


/**
 *	Generated from IDL definition of struct "PriorityBand"
 */

public final class PriorityBand
    implements org.omg.CORBA.portable.IDLEntity {
    public PriorityBand() {}
    public short low;
    public short high;
    public PriorityBand(short low, short high) {
        this.low = low;
        this.high = high;
    }
}
