package org.omg.IOP_N;


/**
 *	Generated from IDL definition of interface "Codec"
 *	@author IDL compiler 
 */

public interface CodecOperations {
    byte[] encode(org.omg.CORBA.Any data) throws org.omg.IOP_N.CodecPackage.InvalidTypeForEncoding;
    org.omg.CORBA.Any decode(byte[] data) throws org.omg.IOP_N.CodecPackage.FormatMismatch;
    byte[] encode_value(org.omg.CORBA.Any data) throws org.omg.IOP_N.CodecPackage.InvalidTypeForEncoding;
    org.omg.CORBA.Any decode_value(byte[] data, org.omg.CORBA.TypeCode tc) throws org.omg.IOP_N.CodecPackage.TypeMismatch, org.omg.IOP_N.CodecPackage.FormatMismatch;
}
