package edu.uci.ece.zen.orb;

import org.omg.CORBA.*;
import edu.uci.ece.zen.utils.*;
import javax.realtime.*;

public class CDROutputStream extends org.omg.CORBA.portable.OutputStream {
    WriteBuffer buffer;
    private edu.uci.ece.zen.orb.ORB orb;

    private static Queue cdrCache;
    static{
    try{
            cdrCache = (Queue) ImmortalMemory.instance().newInstance( Queue.class );
        }catch( Exception e ){
            e.printStackTrace();
            System.exit(-1);
        }
    }
    public static CDROutputStream instance(){
        try{
            if( cdrCache.isEmpty() )
                return (CDROutputStream) ImmortalMemory.instance().newInstance( CDROutputStream.class );
            else
                return (CDROutputStream) cdrCache.dequeue();
        }catch( Exception e ){
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    private static void release( CDROutputStream self ){
        cdrCache.enqueue( self );
    }

    public CDROutputStream() {
    }

    public void init( edu.uci.ece.zen.orb.ORB orb ){
        buffer = WriteBuffer.instance();
        buffer.init();
        buffer.setEndian( false );
        this.orb = orb;
    }

    public WriteBuffer getBuffer(){
        return buffer;
    }

    /** Writes an octet to the output stream.
     * @param value octet to write
     */
    public final void write_octet(final byte value) {
        buffer.writeByte(value);
    }

    /** Writes an octet array to the CDR stream.
     * @param value Array of octets to write.
     * @param offset Array offset to start at.
     * @param length Number of octets to write.
     */
    public void write_octet_array(byte[] value, int offset, int length) {
        buffer.writeByteArray(value, offset, length);
    }

    /** Writes a boolean to the output stream.
     * @param value boolean to write
     */
    public void write_boolean(boolean value) {
        buffer.writeByte(value ? (byte) 1 : (byte) 0);
    }

    /** Writes a boolean array to the CDR stream.
     * @param value Array of boolean to write.
     * @param offset Array offset to start at.
     * @param length Number of boolean to write.
     */
    public void write_boolean_array(boolean[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            buffer.writeByte(value[i + offset] ? (byte) 1 : (byte) 0);
        }
    }

    /** Writes a character to the output stream.
     * @param c Character to write
     */
    public void write_char(char c) {
        buffer.writeByte((byte) (c & 0xff));
    }

    /** Writes a char array to the CDR stream.
     * @param value Array of char to write.
     * @param offset Array offset to start at.
     * @param length Number of char to write.
     */
    public void write_char_array(char[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            buffer.writeByte((byte) (value[i + offset] & 0xff));
        }
    }

    /** Writes a string to the output stream.
     * @param s srting to write
     */
    public void write_string(String s) {
        int length = s.length();
        write_long(length + 1);

        for (int i = 0; i < length; i++) {
            buffer.writeByte((byte) (s.charAt(i) & 0xff));
        }
        buffer.writeByte((byte) 0);
    }

    /** Creates a CDRInputSTream from the data written to this CDROutputStream.
     * @return A concrete CDRInputStream
     */
    public org.omg.CORBA.portable.InputStream create_input_stream() {
        return null;
    }

    /** Writes a short to the output stream.
     * @param value short to write
     */
    public void write_short(short value) {
        buffer.writeShort(value);
    }

    /** Writes a short array to the CDR stream.
     * @param value Array of short to write.
     * @param offset Array offset to start at.
     * @param length Number of short to write.
     */
    public void write_short_array(short[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            buffer.writeShort(value[i + offset]);
        }
    }

    /** Writes a long to the output stream.
     * @param value long to write
     */
    public void write_long(int value) {
        buffer.writeLong(value);
    }

    /** Writes a long array to the CDR stream.
     * @param value Array of long to write.
     * @param offset Array offset to start at.
     * @param length Number of long to write.
     */
    public void write_long_array(int[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            buffer.writeLong(value[i + offset]);
        }
    }

    /** Writes a wide character to the output stream.
     * @param c wide character to write
     */
    public void write_wchar(char c) {
        short value = (short) c;

        buffer.writeShort(value);
    }

    /** Writes a wide character array to the CDR stream.
     * @param value Array of wide character to write.
     * @param offset Array offset to start at.
     * @param length Number of wide characters to write.
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
        buffer.free();
        CDROutputStream.release( this );
    }

    /** Writes a wide string to the output stream.
     * @param s wide string to write
     */
    public void write_wstring(String s) {
        int length = s.length() + 1;

        write_long(length);  // the length of this string

        for (int i = 0; i < length - 1; i++) {
            short value = (short) s.charAt(i);

            buffer.writeShort(value);
        }
        buffer.writeShort((short) 0);
    }

    /** Writes a float to the output stream.
     * @param value float to write
     */
    public void write_float(float value) {
        write_long(Float.floatToIntBits(value));
    }

    /** Writes a float array to the CDR stream.
     * @param value Array of float to write.
     * @param offset Array offset to start at.
     * @param length Number of floats to write.
     */
    public void write_float_array(float[] value, int offset, int length) {
        int finish = offset + length;

        for (int i = offset; i < finish; i++) {
            write_float(value[i]);
        }
    }

    /** Writes a double to the output stream.
     * @param value double to write
     */
    public void write_double(double value) {
        write_longlong(Double.doubleToLongBits(value));
    }

    /** Writes a double array to the CDR stream.
     * @param value Array of double to write.
     * @param offset Array offset to start at.
     * @param length Number of doubles to write.
     */
    public void write_double_array(double[] value, int offset, int length) {
        int finish = offset + length;

        for (int i = offset; i < finish; i++) {
            write_double(value[i]);
        }
    }

    /** Writes a long long to the output stream.
     * @param value long long to write
     */
    public void write_longlong(long value) {
        buffer.writeLongLong(value);
    }

    /** Writes a long long array to the CDR stream.
     * @param value Array of long long to write.
     * @param offset Array offset to start at.
     * @param length Number of long longs to write.
     */
    public void write_longlong_array(long[] value, int offset, int length) {
        for (int i = 0; i < length; i++) {
            buffer.writeLongLong(value[offset + i]);
        }
    }

    /** Writes an unsigned long to the output stream.
     * @param value unsigned long to write
     */
    public void write_ulong(int value) {
        write_long(value);
    }

    /** Writes an unsigned long array to the CDR stream.
     * @param value Array of unsigned long long to write.
     * @param offset Array offset to start at.
     * @param length Number of unsigned long longs to write.
     */
    public void write_ulong_array(int[] value, int offset, int length) {
        write_long_array(value, offset, length);
    }

    /** Writes an unsigned long long to the output stream.
     * @param value unsigned long long to write
     */
    public void write_ulonglong(long value) {
        write_longlong(value);
    }

    /** Writes an unsigned long long array to the CDR stream.
     * @param value Array of unsigned long long to write.
     * @param offset Array offset to start at.
     * @param length Number of unsigned long longs to write.
     */
    public void write_ulonglong_array(long[] value, int offset, int length) {
        write_longlong_array(value, offset, length);
    }

    /** Writes an unsigned short to the output stream.
     * @param value unsigned short to write
     */
    public void write_ushort(short value) {
        write_short(value);
    }

    /** Writes an unsigned short array to the CDR stream.
     * @param value Array of unsigned short to write.
     * @param offset Array offset to start at.
     * @param length Number of unsigned shorts to write.
     */
    public void write_ushort_array(short[] value, int offset, int length) {
        write_short_array(value, offset, length);
    }

    /** Writes an object to the output stream.
     * @param value object to write
     */
    public void write_Object(org.omg.CORBA.Object value) {
        org.omg.IOP.IOR ior = ((edu.uci.ece.zen.orb.ObjectImpl)value).ior;
        //org.omg.IOP.IOR ior = ((edu.uci.ece.zen.orb.ObjRefDelegate)((org.omg.CORBA.portable.ObjectImpl)value)._get_delegate()).getIOR();
        org.omg.IOP.IORHelper.write( this , ior );
    }

    /** Marshalls another CDROutputStream to this stream.
     * @param cdr CDROutputSTream
     */
    public void write_CDROutputStream(CDROutputStream cdr) {
        write_long( (int) cdr.buffer.getLimit() );
        //cdr.buffer.dumpBuffer( buffer );
    }

    public boolean equals( CDROutputStream rhs ){
        return buffer.equals( rhs.buffer );
    }

    /* this function is needed in order for ZEN to compile, we can
     have it throw an exception or something but we cannot just delete it.
     So I added it back in. */

    /** Writes a Principle object to stream. (Not implemented)
     * @param pr Principle to write.
     * @deprecated
     */
    public void write_Principal(org.omg.CORBA.Principal pr) {}

    public void setLocationMemento(){
        buffer.setLocationMemento();
    }
    public void updateLength(){
        buffer.writeLongAtLocationMemento( (int) buffer.getLimit()-12 );
    }
}
