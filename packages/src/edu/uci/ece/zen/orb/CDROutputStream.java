/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import javax.realtime.ImmortalMemory;

import edu.uci.ece.zen.utils.Queue;
import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;

public class CDROutputStream extends org.omg.CORBA.portable.OutputStream {
    WriteBuffer buffer;

    private edu.uci.ece.zen.orb.ORB orb;

    private boolean inUse = false;

    private static Queue cdrCache;
    static {
        try {
            cdrCache = (Queue) ImmortalMemory.instance().newInstance( Queue.class );
            int preAlloc = Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.orb.cdr.preAllocate" , "5" ) );
            CDROutputStream.preAllocate( preAlloc );
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, CDROutputStream.class, "static <init>", e);
            System.exit(-1);
        }
    }

    public String toString(){
        return getBuffer().toString();
    }

    private static void preAllocate( int num ){
        try{
            for( int i=0;i<num;i++ ){
                ZenProperties.logger.log(Logger.INFO, CDROutputStream.class, "preAllocate", "Creating new instance.");
                cdrCache.enqueue( ImmortalMemory.instance().newInstance(CDROutputStream.class) );
            }
        }catch( Exception e ){
            ZenProperties.logger.log(Logger.WARN, CDROutputStream.class, "preAllocate", "Unable to pre-allocate.");
        }
    }

    public static CDROutputStream instance() {
        try {
            CDROutputStream cdr = (CDROutputStream) cdrCache.dequeue();
            if ( cdr == null ){
                ZenProperties.logger.log(Logger.WARN, CDROutputStream.class, "instance", "Creating new instance.");
                cdr = (CDROutputStream) ImmortalMemory.instance().newInstance(CDROutputStream.class);
                cdr.inUse = true;
                return cdr;
            } else {
                cdr.inUse = true;
                return cdr;
            }
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, CDROutputStream.class, "instance", e);
            System.exit(-1);
        }
        return null;
    }

    private static void release(CDROutputStream self) {
        cdrCache.enqueue(self);
    }

    public static CDROutputStream create(edu.uci.ece.zen.orb.ORB orb) {
        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);
        out.write_boolean(false); //BIGENDIAN
        return out;
    }

    public CDROutputStream() {
    }

    public void init(edu.uci.ece.zen.orb.ORB orb ) {
	    //System.out.println( "CDROutputStream.init( edu.uci.ece.zen.orb.ORB )" );
        buffer = WriteBuffer.instance();
        buffer.init();
        buffer.setEndian(false);
        this.orb = orb;
    }

    public WriteBuffer getBuffer() {
        return buffer;
    }

    public void setAlignment( boolean enable ){
        buffer.setAlignment( enable );
    }
    
/*
    public void printWriteBuffer() {
        buffer.printBuffer();
    }
*/
    /**
     * Writes an octet to the output stream.
     *
     * @param value
     *            octet to write
     */
    public final void write_octet(final byte value) {
        buffer.writeByte(value);
    }

    /**
     * Writes an octet array to the CDR stream.
     *
     * @param value
     *            Array of octets to write.
     * @param offset
     *            Array offset to start at.
     * @param length
     *            Number of octets to write.
     */
    public void write_octet_array(byte[] value, int offset, int length) {
        buffer.writeByteArray(value, offset, length);
    }

    /**
     * Writes a boolean to the output stream.
     *
     * @param value
     *            boolean to write
     */
    public void write_boolean(boolean value) {
        buffer.writeByte(value ? (byte) 1 : (byte) 0);
    }

    /**
     * Writes a boolean array to the CDR stream.
     *
     * @param value
     *            Array of boolean to write.
     * @param offset
     *            Array offset to start at.
     * @param length
     *            Number of boolean to write.
     */
    public void write_boolean_array(boolean[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            buffer.writeByte(value[i + offset] ? (byte) 1 : (byte) 0);
        }
    }

    /**
     * Writes a character to the output stream.
     *
     * @param c
     *            Character to write
     */
    public void write_char(char c) {
        buffer.writeByte((byte) (c & 0xff));
    }

    /**
     * Writes a char array to the CDR stream.
     *
     * @param value
     *            Array of char to write.
     * @param offset
     *            Array offset to start at.
     * @param length
     *            Number of char to write.
     */
    public void write_char_array(char[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            buffer.writeByte((byte) (value[i + offset] & 0xff));
        }
    }

    /**
     * Writes a string to the output stream.
     *
     * @param s
     *            srting to write
     */
    public void write_string(String s) {
        int length = s.length();
        write_long(length + 1);

        for (int i = 0; i < length; i++) {
            buffer.writeByte((byte) (s.charAt(i) & 0xff));
        }
        buffer.writeByte((byte) 0);
    }

    /**
     * Creates a CDRInputSTream from the data written to this CDROutputStream.
     *
     * @return A concrete CDRInputStream
     */
    public org.omg.CORBA.portable.InputStream create_input_stream() {
        CDRInputStream in = CDRInputStream.instance();
        in.init(orb, buffer.getReadBuffer());
        return in;
    }

    /**
     * Writes a short to the output stream.
     *
     * @param value
     *            short to write
     */
    public void write_short(short value) {
        buffer.writeShort(value);
    }

    /**
     * Writes a short array to the CDR stream.
     *
     * @param value
     *            Array of short to write.
     * @param offset
     *            Array offset to start at.
     * @param length
     *            Number of short to write.
     */
    public void write_short_array(short[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            buffer.writeShort(value[i + offset]);
        }
    }

    /**
     * Writes a long to the output stream.
     *
     * @param value
     *            long to write
     */
    public void write_long(int value) {
        buffer.writeLong(value);
    }

    /**
     * Writes a long array to the CDR stream.
     *
     * @param value
     *            Array of long to write.
     * @param offset
     *            Array offset to start at.
     * @param length
     *            Number of long to write.
     */
    public void write_long_array(int[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            buffer.writeLong(value[i + offset]);
        }
    }

    /**
     * Writes a wide character to the output stream.
     *
     * @param c
     *            wide character to write
     */
    public void write_wchar(char c) {
        short value = (short) c;

        buffer.writeShort(value);
    }

    /**
     * Writes a wide character array to the CDR stream.
     *
     * @param value
     *            Array of wide character to write.
     * @param offset
     *            Array offset to start at.
     * @param length
     *            Number of wide characters to write.
     */
    public void write_wchar_array(char[] value, int offset, int length) {
        short svalue = 0;

        for (int i = 0; i < length; i++) {
            svalue = (short) value[offset + i];
            buffer.writeShort(svalue);
        }
    }

    /** Releases all resources. */
    public void free() {
        if(!inUse){
            Thread.dumpStack();
            ZenProperties.logger.log(Logger.WARN, CDROutputStream.class,
                "free",
                "Stream already freed.");
                //System.exit(-1);
                //still deciding what to do here
            return;
        }
        buffer.free();
        buffer = null;
        CDROutputStream.release(this);
        inUse = false;
    }

    /**
     * Writes a wide string to the output stream.
     *
     * @param s
     *            wide string to write
     */
    public void write_wstring(String s) {
        int length = s.length() + 1;

        write_long(length); // the length of this string

        for (int i = 0; i < length - 1; i++) {
            short value = (short) s.charAt(i);

            buffer.writeShort(value);
        }
        buffer.writeShort((short) 0);
    }

    /**
     * Writes a float to the output stream.
     *
     * @param value
     *            float to write
     */
    public void write_float(float value) {
        write_long(Float.floatToIntBits(value));
    }

    /**
     * Writes a float array to the CDR stream.
     *
     * @param value
     *            Array of float to write.
     * @param offset
     *            Array offset to start at.
     * @param length
     *            Number of floats to write.
     */
    public void write_float_array(float[] value, int offset, int length) {
        int finish = offset + length;

        for (int i = offset; i < finish; i++) {
            write_float(value[i]);
        }
    }

    /**
     * Writes a double to the output stream.
     *
     * @param value
     *            double to write
     */
    public void write_double(double value) {
        write_longlong(Double.doubleToLongBits(value));
    }

    /**
     * Writes a double array to the CDR stream.
     *
     * @param value
     *            Array of double to write.
     * @param offset
     *            Array offset to start at.
     * @param length
     *            Number of doubles to write.
     */
    public void write_double_array(double[] value, int offset, int length) {
        int finish = offset + length;

        for (int i = offset; i < finish; i++) {
            write_double(value[i]);
        }
    }

    /**
     * Writes a long long to the output stream.
     *
     * @param value
     *            long long to write
     */
    public void write_longlong(long value) {
        buffer.writeLongLong(value);
    }

    /**
     * Writes a long long array to the CDR stream.
     *
     * @param value
     *            Array of long long to write.
     * @param offset
     *            Array offset to start at.
     * @param length
     *            Number of long longs to write.
     */
    public void write_longlong_array(long[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            buffer.writeLongLong(value[offset + i]);
        }
    }

    /**
     * Writes an unsigned long to the output stream.
     *
     * @param value
     *            unsigned long to write
     */
    public void write_ulong(int value) {
        write_long(value);
    }

    /**
     * Writes an unsigned long array to the CDR stream.
     *
     * @param value
     *            Array of unsigned long long to write.
     * @param offset
     *            Array offset to start at.
     * @param length
     *            Number of unsigned long longs to write.
     */
    public void write_ulong_array(int[] value, int offset, int length) {
        write_long_array(value, offset, length);
    }

    /**
     * Writes an unsigned long long to the output stream.
     *
     * @param value
     *            unsigned long long to write
     */
    public void write_ulonglong(long value) {
        write_longlong(value);
    }

    /**
     * Writes an unsigned long long array to the CDR stream.
     *
     * @param value
     *            Array of unsigned long long to write.
     * @param offset
     *            Array offset to start at.
     * @param length
     *            Number of unsigned long longs to write.
     */
    public void write_ulonglong_array(long[] value, int offset, int length) {
        write_longlong_array(value, offset, length);
    }

    /**
     * Writes an unsigned short to the output stream.
     *
     * @param value
     *            unsigned short to write
     */
    public void write_ushort(short value) {
        write_short(value);
    }

    /**
     * Writes an unsigned short array to the CDR stream.
     *
     * @param value
     *            Array of unsigned short to write.
     * @param offset
     *            Array offset to start at.
     * @param length
     *            Number of unsigned shorts to write.
     */
    public void write_ushort_array(short[] value, int offset, int length) {
        write_short_array(value, offset, length);
    }

    /**
     * Writes an object to the output stream.
     *
     * @param value
     *            object to write
     */
    public void write_Object(org.omg.CORBA.Object value) {
        //org.omg.IOP.IOR ior = ((edu.uci.ece.zen.orb.ObjectImpl) value).ior;
        org.omg.IOP.IOR ior =
          ((edu.uci.ece.zen.orb.ObjRefDelegate)
          ((org.omg.CORBA.portable.ObjectImpl)value)
          ._get_delegate()).getIOR();
        org.omg.IOP.IORHelper.write(this, ior);
    }

    /**
     * Marshalls another CDROutputStream to this stream.
     *
     * @param cdr
     *            CDROutputSTream
     */
    public void write_CDROutputStream(CDROutputStream cdr) {
        write_long((int) cdr.buffer.getPosition());
        //cdr.buffer.dumpBuffer( buffer );
    }

    public boolean equals(CDROutputStream rhs) {
        return buffer.equals(rhs.buffer);
    }

    /*
     * this function is needed in order for ZEN to compile, we can have it throw
     * an exception or something but we cannot just delete it. So I added it
     * back in.
     */

    /**
     * Writes a Principle object to stream. (Not implemented)
     *
     * @param pr
     *            Principle to write.
     * @deprecated
     */
    public void write_Principal(org.omg.CORBA.Principal pr) {
    }

    public void setLocationMemento() {
        buffer.setLocationMemento();
    }
    
    public void setProfileLengthMemento() {
        buffer.setProfileLengthMemento();
    }    

    public void updateLength() {
        buffer.writeLongAtLocationMemento((int) buffer.getPosition() - 12);
    }
    
    public void updateProfileLength(int val) {
        buffer.writeLongAtProfileLengthMemento(val);
    }    

    /**
     * Writes an any object from the the CDROutputStream. This processes
     * consists of writing a TypeCode representing an Any, then the Any's stored
     * value.
     *
     * @return Any object just read from CDRInputStream.
     */
    public final void write_any(org.omg.CORBA.Any any) {
        write_TypeCode(any.type());
        any.write_value(this);
    }

    /**
     * Returns a CDROutputStream that will be encapsulated within this stream.
     * Calld endEncapsulation() when finished writing to it.
     */
    public CDROutputStream beginEncapsulation() {
        CDROutputStream cdr = edu.uci.ece.zen.orb.CDROutputStream.instance();
        cdr.init(orb);
        // The encapsulated header uses 0 for big-endian (Java's
        // representation) and 1 for little-endian
        cdr.write_octet((byte) 0);
        return cdr;
    }

    /**
     * Finish marshaling the CDROutputStream <code>cdr</code> into this
     * stream.
     *
     * @param cdr
     *            The CDROutputStream to be marshaled into this stream.
     */
    public void endEncapsulation(CDROutputStream cdr) {

        this.write_CDROutputStream(cdr);
        cdr.free();

    }

    /**
     * Writes a TypeCode to this CDROutputStream.
     *
     * @param value
     *            TypeCode to write to this CDROutputStream.
     */
    public final void write_TypeCode(org.omg.CORBA.TypeCode value) {
        /*
         * int memCount = 0; int value_kind_val = value.kind().value();
         * write_long(value_kind_val); // Write TypeCode Kind..... // Writing
         * the typecode kind is enough for simple data types like long or wchar.
         * try { switch (value_kind_val) { case TCKind._tk_null: case
         * TCKind._tk_void: case TCKind._tk_short: case TCKind._tk_long: case
         * TCKind._tk_ushort: case TCKind._tk_ulong: case TCKind._tk_float: case
         * TCKind._tk_double: case TCKind._tk_boolean: case TCKind._tk_char:
         * case TCKind._tk_wchar: case TCKind._tk_octet: case TCKind._tk_any: //
         * TypeCode representing a TypeCode only has to write its numeric kind
         * case TCKind._tk_TypeCode: // TypeCode representing a TypeCode only
         * has to write its numeric kind case TCKind._tk_longlong: case
         * TCKind._tk_ulonglong: case TCKind._tk_longdouble: // Do nothing
         * further. break; case TCKind._tk_string: case TCKind._tk_wstring:
         * write_long(value.length()); break; case TCKind._tk_objref: {
         * CDROutputStream cdr = beginEncapsulation();
         * cdr.write_string(value.id()); cdr.write_string(value.name());
         * endEncapsulation(cdr); break; } case TCKind._tk_struct: case
         * TCKind._tk_except: { CDROutputStream cdr = beginEncapsulation();
         * //Logger.error("Writing struct or exception type Code");
         * cdr.write_string(value.id()); cdr.write_string(value.name());
         * memCount = value.member_count(); cdr.write_ulong(memCount); for (int
         * i = 0; i < memCount; i++) { cdr.write_string(value.member_name(i));
         * cdr.write_TypeCode(value.member_type(i)); } endEncapsulation(cdr);
         * break; } case TCKind._tk_union: { CDROutputStream cdr =
         * beginEncapsulation(); cdr.write_string(value.id());
         * cdr.write_string(value.name());
         * cdr.write_TypeCode(value.discriminator_type()); int def_ind =
         * value.default_index(); cdr.write_long(def_ind); memCount =
         * value.member_count(); cdr.write_ulong(memCount); for (int i = 0; i <
         * memCount; i++) { if ( i != def_ind ) {
         * value.member_label(i).write_value(cdr); } else { cdr.write_octet(
         * (byte) 0 ); } cdr.write_string(value.member_name(i));
         * cdr.write_TypeCode(value.member_type(i));
         * //cdr.write_any(value.member_label(i)); // this method (write_Object)
         * has not been written yet.....
         * //write_Object(value.member_idlType(i)); } endEncapsulation(cdr);
         * break; } case TCKind._tk_enum: { CDROutputStream cdr =
         * beginEncapsulation(); memCount = value.member_count();
         * cdr.write_string(value.id()); cdr.write_string(value.name());
         * cdr.write_ulong(memCount); for (int i = 0; i < memCount; i++) {
         * cdr.write_string(value.member_name(i)); } endEncapsulation(cdr);
         * break; } case TCKind._tk_sequence: case TCKind._tk_array: {
         * CDROutputStream cdr = beginEncapsulation(); // how do you
         * discriminate a seqeunce from a recursive sequence??? //
         * write_TypeCode will recurse if the object stored is a
         * multidimensional array cdr.write_TypeCode(value.content_type());
         * cdr.write_ulong(value.length()); endEncapsulation(cdr); break; } case
         * TCKind._tk_alias: { CDROutputStream cdr = beginEncapsulation();
         * cdr.write_string(value.id()); cdr.write_string(value.name());
         * cdr.write_TypeCode(value.content_type()); endEncapsulation(cdr);
         * break; } case TCKind._tk_value: { CDROutputStream cdr =
         * beginEncapsulation(); cdr.write_string(value.id());
         * cdr.write_string(value.name());
         * cdr.write_short(value.type_modifier()); org.omg.CORBA.TypeCode
         * baseType = value.concrete_base_type(); if (baseType == null) {
         * baseType =
         * edu.uci.ece.zen.orb.TypeCode.lookupPrimitiveTc(TCKind._tk_null); }
         * cdr.write_TypeCode(baseType); int memberCount = value.member_count();
         * cdr.write_ulong(memberCount); for (int i = 0; i < memberCount; i++) {
         * cdr.write_string(value.member_name(i));
         * cdr.write_TypeCode(value.member_type(i));
         * cdr.write_short(value.member_visibility(i)); } endEncapsulation(cdr);
         * break; } case TCKind._tk_value_box: { CDROutputStream cdr =
         * beginEncapsulation(); cdr.write_string(value.id());
         * cdr.write_string(value.name());
         * cdr.write_TypeCode(value.content_type()); endEncapsulation(cdr);
         * break; } case TCKind._tk_abstract_interface: { CDROutputStream cdr =
         * beginEncapsulation(); cdr.write_string(value.id());
         * cdr.write_string(value.name()); endEncapsulation(cdr); break; } case
         * TCKind._tk_Principal: default: ZenProperties.logger.log( Logger.WARN,
         * "edu.uci.ece.zen.orb.CDROutputStream", "write_Typecode()", "Unwritten
         * method in CDROutputStream for TypeCode"); /* // This code was used to
         * print a stack trace in order // to know how this method was being
         * called. try { throw new org.omg.CORBA.NO_IMPLEMENT(); } catch
         * (java.lang.Exception e) { try { e.printStackTrace();
         * java.lang.Thread.sleep(1000); } catch (java.lang.Exception e2) {} }
         * //break; / } // end of: switch(value_kind) } catch
         * (org.omg.CORBA.TypeCodePackage.BadKind bk) {
         * System.err.println("CDROutputStream#write_TypeCode threw BadKind
         * exception"); bk.printStackTrace(); } catch
         * (org.omg.CORBA.TypeCodePackage.Bounds b) {
         * System.err.println("CDROutputStream#write_TypeCode threw BadKind
         * exception"); b.printStackTrace(); }
         */
    }

    // It is apparent that the union data type consists of two items:
    // 1. The discriminator value that sets the case for this union. It's type
    // is determined by the typecode's data member discriminator_type.
    // 2. The actual value of the union having whatever type is
    // appropriate for the case of the union is.
    private final void write_value_union(org.omg.CORBA.TypeCode tc,
            org.omg.CORBA.portable.InputStream in) {
        //this.transcribe_union(tc, in, this);
    }

    public static final void transcribe_union(org.omg.CORBA.TypeCode tc,
            org.omg.CORBA.portable.InputStream in,
            org.omg.CORBA.portable.OutputStream out) {
        /*
         * try { edu.uci.ece.zen.orb.TypeCode discriminatorType =
         * (edu.uci.ece.zen.orb.TypeCode)
         * edu.uci.ece.zen.orb.TypeCode.originalType(tc.discriminator_type());
         * int selected_index = -1; int defaultIndex = tc.default_index(); //
         * Switch based on the kind of discriminator used. The // discriminator
         * could be an IDL short, long, longlong, (or // unsigned versions of
         * the last three), boolean, char, wchar, // enum int kind =
         * discriminatorType._kind(); switch ( kind ) { case TCKind._tk_short:
         * case TCKind._tk_ushort: { short aValue = in.read_short();
         * out.write_short(aValue); selected_index = union_find_member_label(tc, "" +
         * aValue); break; } case TCKind._tk_long: case TCKind._tk_ulong: { int
         * aValue = in.read_long(); out.write_long(aValue); selected_index =
         * union_find_member_label(tc, "" + aValue); break; } case
         * TCKind._tk_longlong: case TCKind._tk_ulonglong: { long aValue =
         * in.read_longlong(); out.write_longlong(aValue); selected_index =
         * union_find_member_label(tc, "" + aValue); break; } case
         * TCKind._tk_boolean: { boolean aValue = in.read_boolean();
         * out.write_boolean(aValue); selected_index =
         * union_find_member_label(tc, "" + aValue); break; } case
         * TCKind._tk_char: { char aValue = in.read_char();
         * out.write_char(aValue); selected_index = union_find_member_label(tc, "" +
         * aValue); break; } case TCKind._tk_wchar: { char aValue =
         * in.read_wchar(); out.write_wchar(aValue); selected_index =
         * union_find_member_label(tc, "" + aValue); break; } case
         * TCKind._tk_enum: default: throw new org.omg.CORBA.NO_IMPLEMENT("union
         * support not added yet for type " + kind); } // end of switch // If
         * member_index was never set, the discriminator should be // set to the
         * default case because we were skipping checking // defaultIndex. If no
         * match was found between the // discriminator's value and the many
         * case labels, try to set // member_index to the default_index.
         * However, defaultIndex // will be -1 if there is no default member,
         * which would leave // member_index still as -1. if (selected_index ==
         * -1) { selected_index = defaultIndex; } // Make recursive call to
         * write the value stored in the union. if (selected_index != -1) { (
         * (CDROutputStream) out).write_value(tc.member_type(selected_index),
         * in); } // Member is -1, default is -1, so member couldn't be
         * identified. else { throw new org.omg.CORBA.NO_IMPLEMENT("Unhandled
         * case"); } } // end of try // These exceptions should never really
         * happen, but are // required for the union's accessor methods catch
         * (org.omg.CORBA.TypeCodePackage.BadKind bk) {
         * System.err.println("BadKind exception occurred in
         * CDROutputStream.write_value_union"); } catch
         * (org.omg.CORBA.TypeCodePackage.Bounds b) { System.err.println("Bounds
         * exception occurred in CDROutputStream.write_value_union"); }
         */

    }

    /**
     * Return the index of the member_label that the discriminator selects in a
     * union's typecode.
     *
     * @param tc
     *            TypeCode whose member labels are going to be searched for
     *            equality with the value in sthe string discriminatorValue.
     * @param discriminatorValue
     *            string holding value of the discriminator.
     *            tc.discriminator_type() indicates the type of the value if the
     *            string is parsed.
     * @return int value indexing into tc.member_labels(int i) that indicates
     *         which member_label is represented by the discriminator or -1 if
     *         tc had no default index and a label wasn't found.
     */
    public static final int union_find_member_label(org.omg.CORBA.TypeCode tc,
            String discriminatorValue)
            throws org.omg.CORBA.TypeCodePackage.BadKind,
            org.omg.CORBA.TypeCodePackage.Bounds {
        /*
         * int defaultIndex = tc.default_index(); // member_index will end up
         * being set to the selected value // (the case label matched by
         * discriminatorValue) or left at // -1 if discriminatorValue doesn't
         * match any case label. int member_index = -1; // Go through all the
         * union's member labels, looking to see // which one when converted to
         * a string is equal to // discriminatorValue. int kind =
         * ((edu.uci.ece.zen.orb.TypeCode) tc.discriminator_type())._kind();
         * switch (kind) { case TCKind._tk_short: case TCKind._tk_ushort: {
         * short discShort = Short.parseShort(discriminatorValue); for (int i =
         * 0; i < tc.member_count(); i++) { // For the default index, the
         * member_label is the zero // octet as metioned on page 10-51 of the
         * CORBA v2.3 spec. if (i == defaultIndex) { continue; } short
         * memLabelShort; if (kind == TCKind._tk_short) { memLabelShort =
         * tc.member_label(i).extract_short(); } else { memLabelShort =
         * tc.member_label(i).extract_ushort(); } if ( discShort ==
         * memLabelShort) { member_index = i; break; } } break; } case
         * TCKind._tk_long: case TCKind._tk_ulong: { int discLong =
         * Integer.parseInt(discriminatorValue); for (int i = 0; i <
         * tc.member_count(); i++) { int memLabelLong; if (kind ==
         * TCKind._tk_long) { memLabelLong = tc.member_label(i).extract_long(); }
         * else { memLabelLong = tc.member_label(i).extract_ulong(); } if (
         * discLong == memLabelLong) { member_index = i; break; } } break; }
         * case TCKind._tk_longlong: case TCKind._tk_ulonglong: { long
         * discLongLong = Long.parseLong(discriminatorValue); for (int i = 0; i <
         * tc.member_count(); i++) { long memLabelLongLong; if (kind ==
         * TCKind._tk_longlong) { memLabelLongLong =
         * tc.member_label(i).extract_longlong(); } else { memLabelLongLong =
         * tc.member_label(i).extract_ulonglong(); } if ( discLongLong ==
         * memLabelLongLong) { member_index = i; break; } } break; } case
         * TCKind._tk_boolean: { boolean discBoolean =
         * Boolean.getBoolean(discriminatorValue); for (int i = 0; i <
         * tc.member_count(); i++) { boolean memLabelBoolean; memLabelBoolean =
         * tc.member_label(i).extract_boolean(); if ( discBoolean ==
         * memLabelBoolean) { member_index = i; break; } } break; } case
         * TCKind._tk_char: case TCKind._tk_wchar: { long discChar =
         * discriminatorValue.charAt(0); for (int i = 0; i < tc.member_count();
         * i++) { char memLabelChar; if (kind == TCKind._tk_char) { memLabelChar =
         * tc.member_label(i).extract_char(); } else { memLabelChar =
         * tc.member_label(i).extract_wchar(); } if ( discChar == memLabelChar) {
         * member_index = i; break; } } break; } case TCKind._tk_enum: //BM
         * continue here throw new org.omg.CORBA.NO_IMPLEMENT("enum data type
         * support not added yet"); default: //BM continue here throw new
         * org.omg.CORBA.NO_IMPLEMENT("enum data type support not added yet for
         * kind " + kind); } // end of switch return member_index;
         */
        return -1;
    } // end of method

    /**
     * Writes the value stored in the InputStream <code>in</code> to this
     * outputstream, treating the value on the inputstream as being of TypeCode
     * <code>tc</code>. Just to make it clear, whatever data is stored next
     * in the inputstream <code>in</code> will be assumed to be of the type
     * given by the TypeCode <code>tc</code>; an InputStream holds data but
     * does not necessarily store the type of the data inside it, hence the need
     * for the <code>tc</code> parameter.
     *
     * @param tc
     *            TypeCode to interpret object on inputstream as
     * @param in
     *            InputStream storing data to be written to this outputstream.
     */
    public final void write_value(org.omg.CORBA.TypeCode tc,
            org.omg.CORBA.portable.InputStream in) {
        /*
         * int kind = ((edu.uci.ece.zen.orb.TypeCode) tc)._kind(); //int kind =
         * tc.kind().value(); // Above method is faster switch (kind) { case
         * TCKind._tk_null: case TCKind._tk_void: break; case
         * TCKind._tk_boolean: write_boolean(in.read_boolean()); break; case
         * TCKind._tk_char: write_char(in.read_char()); break; case
         * TCKind._tk_wchar: write_wchar(in.read_wchar()); break; case
         * TCKind._tk_octet: write_octet(in.read_octet()); break; case
         * TCKind._tk_short: write_short(in.read_short()); break; case
         * TCKind._tk_ushort: write_ushort(in.read_ushort()); break; case
         * TCKind._tk_long: write_long(in.read_long()); break; case
         * TCKind._tk_ulong: write_ulong(in.read_ulong()); break; case
         * TCKind._tk_float: write_float(in.read_float()); break; case
         * TCKind._tk_double: write_double(in.read_double()); break; case
         * TCKind._tk_longlong: write_longlong(in.read_longlong()); break; case
         * TCKind._tk_ulonglong: write_ulonglong(in.read_ulonglong()); break;
         * case TCKind._tk_any: write_any(in.read_any()); break; case
         * TCKind._tk_TypeCode: write_TypeCode(in.read_TypeCode()); break; case
         * TCKind._tk_Principal: write_Principal(in.read_Principal()); break;
         * case TCKind._tk_objref: write_Object(in.read_Object()); break; case
         * TCKind._tk_string: write_string(in.read_string()); break; case
         * TCKind._tk_wstring: write_wstring(in.read_wstring()); break; case
         * TCKind._tk_array: try { int length = tc.length(); for (int i = 0; i <
         * length; i++) write_value(tc.content_type(), in); } catch
         * (org.omg.CORBA.TypeCodePackage.BadKind b) {
         * System.err.println("CDRInputStream#write_value threw BadKind
         * exception for array"); b.printStackTrace(); } break; case
         * TCKind._tk_sequence: try { int len = in.read_long(); write_long(len);
         * for (int i = 0; i < len; i++) { org.omg.CORBA.TypeCode tck =
         * tc.content_type(); write_value(tck, in); } } catch
         * (org.omg.CORBA.TypeCodePackage.BadKind b) {
         * System.err.println("CDROutputStream#write_value: Unable to write
         * sequence"); b.printStackTrace(); } break; case TCKind._tk_except:
         * write_string(in.read_string()); // don't break, fall through to ...
         * case TCKind._tk_struct: // recursiveTCStack.push(tc); try { for (int
         * i = 0; i < tc.member_count(); i++) write_value(tc.member_type(i),
         * in); } catch (org.omg.CORBA.TypeCodePackage.BadKind b) {
         * System.err.println("CDROutputStream#write_value: Unable to write
         * struct"); b.printStackTrace(); } catch
         * (org.omg.CORBA.TypeCodePackage.Bounds b) {
         * System.err.println("CDROutputStream#write_value: Unable to write
         * struct"); b.printStackTrace(); } // recursiveTCStack.pop(); break;
         * case TCKind._tk_enum: write_long(in.read_long()); break; case
         * TCKind._tk_alias:
         * write_value(edu.uci.ece.zen.orb.TypeCode.originalType(tc), in);
         * break; case TCKind._tk_union: write_value_union(tc, in); break;
         * default: ZenProperties.logger.log( Logger.WARN,
         * "edu.uci.ece.zen.orb.CDROutputStream", "write_value()", "Unwritten
         * method in CDROutputStream for Valuetype"); break; }
         */
    }

    // The method below is commented out for real-time Zen because it is not
    // used.

    // Bruce added this class for use with Any's based on the fact
    // that the implementations of Any's frequently needed the
    // function.
    /**
     * Returns a <bold>copy <bold>of the contents of the buffer. Similar to
     * <code>CDROutputStream.getBuffer()</code>.
     *
     * @return Copy of the contents of the buffer, as a byte array.
     */
    /*
     * public final byte[] edu.uci.ece.zen.orb.CDROutputStream.getBufferCopy() {
     * byte[] bbmanager_buffer = bbmanager.getBuffer(); byte[] buffer_copy = new
     * byte [bbmanager_buffer.length]; System.arraycopy(bbmanager_buffer, 0,
     * buffer_copy, 0, bbmanager_buffer.length); return buffer_copy; }
     */
}
