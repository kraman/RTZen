package org.omg.IOP_N;


/**
 *	Generated from IDL definition of struct "Encoding"
 *	@author IDL compiler 
 */

public final class Encoding
    implements org.omg.CORBA.portable.IDLEntity {
    public Encoding() {}
    public short format;
    public byte major_version;
    public byte minor_version;
    public Encoding(short format, byte major_version, byte minor_version) {
        this.format = format;
        this.major_version = major_version;
        this.minor_version = minor_version;
    }
}
