package edu.uci.ece.zen.orb;

import org.omg.CORBA.*;
import edu.uci.ece.zen.utils.*;
import javax.realtime.*;

public class CDRInputStream extends org.omg.CORBA.portable.InputStream {
    ReadBuffer buffer;
    private edu.uci.ece.zen.orb.ORB orb;
    private static edu.uci.ece.zen.utils.Queue cdrInputStreamCache;
    
    static{
        try{
            cdrInputStreamCache = (Queue) ImmortalMemory.instance().newInstance( Queue.class );
        }catch( Exception e ){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static CDRInputStream instance(){
        try{
            if( cdrInputStreamCache.isEmpty() )
                return (CDRInputStream) ImmortalMemory.instance().newInstance( CDRInputStream.class );
            else
                return (CDRInputStream) cdrInputStreamCache.dequeue();
        }catch( Exception e ){
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    private static void release( CDRInputStream str ){
        cdrInputStreamCache.enqueue( str );
    }

    public CDRInputStream(){
    }

    public void init( edu.uci.ece.zen.orb.ORB orb , ReadBuffer b ){
        this.orb = orb;
        this.buffer = b;
    }

    public CDRInputStream read_CDRInputStream() {
        int len = this.read_long();
        ReadBuffer buf = buffer.readBuffer( len );
        CDRInputStream str = CDRInputStream.instance();
        str.init(orb, buffer);
        return str;
    }

    /** Reads a octet from CDR encapsulation
     * @return octet (byte)
     */    
    public final byte read_octet() {
        return buffer.readByte();
    }

    /** Reads a octet array from CDR encapsulation
     * @param value array to enter read data into.
     * @param offset Array offset to start at.
     * @param length Number of octets to read.
     */    
    public final void read_octet_array(byte[] value, int offset, int length) {
        buffer.readByteArray(value, offset, length);
    }

    /** Reads a boolean from CDR encapsulation
     * @return boolean value.
     */    
    public final boolean read_boolean() {
        return read_octet() != 0;
    }

    /** Reads a boolean array from CDR encapsulation
     * @param value array to enter read data into.
     * @param offset array offset to start at.
     * @param length number of booleans to read.
     */    
    public final void read_boolean_array(boolean[] value, int offset, int length) {
        int finish = offset + length;

        for (int i = offset; i < finish; i++) {
            value[i] = read_boolean();
        }
    }

    /** Read a character from the CDR encapsulation
     * @return Read character.
     */    
    public final char read_char() {
        return (char) read_octet();
    }

    /** Read an array of characters from CDR encapsulation.
     * @param value Array to read data into.
     * @param offset Array offset to start at.
     * @param length Number of characters to read into array.
     */    
    public final void read_char_array(char[] value, int offset, int length) {
        int finish = offset + length;

        for (int i = offset; i < finish; i++) {
            value[i] = read_char();
        }
    }

    /** Read a string from CDR Encapsulation.
     * @return Read string.
     */    
    public String read_string() {
        return buffer.readString();
    }

    // Read a string without advancing nextByteToRead.
    // This is needed by the Delegate.
    /** Internal method to peek at strings in CDR encapsulation. Used to demarshall
     * System exceptions.
     * @return Read string.
     */    
    public String peekAtString() {
        return buffer.peekString();
    }

    /** Reads a double from CDR encapsulation.
     * @return Read double.
     */    
    public final double read_double() {
        return Double.longBitsToDouble(read_longlong());
    }

    /** Reads an array of doubles from CDR encapsulation.
     * @param value Array to read data into.
     * @param offset Array offset to start at.
     * @param length Number of doubles to read from CDR stream.
     */    
    public final void read_double_array(double[] value, int offset, int length) {
        int finish = offset + length;

        for (int i = offset; i < finish; i++) {
            value[i] = read_double();
        }
    }

    /** Read a float from CDR encapsulation.
     * @return Read float
     */    
    public final float read_float() {
        return Float.intBitsToFloat(read_long());
    }

    /** read an array of floats from CDR encapsulation.
     * @param value Array to read floats into.
     * @param offset Array offset to start at.
     * @param length Number of floats to read.
     */    
    public final void read_float_array(float[] value, int offset, int length) {
        int finish = offset + length;

        for (int i = offset; i < finish; i++) {
            value[i] = read_float();
        }
    }

    /** Read a long (java int) from cdr encapsulation.
     * @return read long value.
     */    
    public final int read_long() {
        return buffer.readLong();
    }

    /** Read an array of longs from CDR encapsulation.
     * @param value Array to store read data into.
     * @param offset Array offset to start at.
     * @param length Number of longs to read.
     */    
    public final void read_long_array(int[] value, int offset, int length) {
        int finish = offset + length;

        for (int i = offset; i < finish; i++) {
            value[i] = read_long();
        }
    }

    /** read a long long (java long) from CDR encapsulation.
     * @return Read long long value.
     */    
    public final long read_longlong() {
        return buffer.readLongLong();
    }

    /** Read an array of long longs (java long) from CDR encapsulation.
     * @param value Array to read data into.
     * @param offset Array offset to start at.
     * @param length Number of long longs to read.
     */    
    public final void read_longlong_array(long[] value, int offset, int length) {
        int finish = offset + length;

        for (int i = offset; i < finish; i++) {
            value[i] = read_longlong();
        }
    }

    /** Read a short value from CDR encapsulation.
     * @return Short value.
     */    
    public final short read_short() {
        return buffer.readShort();
    }

    /** Read an array of shorts from CDR Encapsulation.
     * @param value The array to read thr data into.
     * @param offset Array offset to start at.
     * @param length Number of shorts to read.
     */    
    public final void read_short_array(short[] value, int offset, int length) {
        int finish = offset + length;

        for (int i = offset; i < finish; i++) {
            value[i] = read_short();
        }
    }
    
    /** Read a wide character from CDR encapsulation.
     * @return Read wide character
     */    
    public final char read_wchar() {
        return (char) read_short();
    }

    /** Reads an array of wide characters from CDR encapsulation.
     * @param value Array to read data into.
     * @param offset Array offset to start at.
     * @param length Number of wide characters to read from CDR stream.
     */
    public final void read_wchar_array(char[] value, int offset, int length) {
        int finish = offset + length;

        for (int i = offset; i < finish; i++) {
            value[i] = read_wchar();
        }
    }
    
    /** Read a wide string from CDR encapsulation.
     * @return Read wide string
     */    
    public final String read_wstring() {
        int len = read_long();
        int actualLen = len - 1;
        char[] buf = new char[actualLen];

        for (int i = 0; i < actualLen; i++) {
            buf[i] = read_wchar();
        }
        char toss = read_wchar(); // skip the null

        return new String(buf);
    }
    
    /** Read a unsigned short from CDR encapsulation.
     * @return Read unsigned short
     */    
    public final short read_ushort() {
        return read_short();
    }
    
    /** Reads an array of unsigned shorts from CDR encapsulation.
     * @param value Array to read data into.
     * @param offset Array offset to start at.
     * @param length Number of unsigned shorts to read from CDR stream.
     */
    public final void read_ushort_array(short[] value, int offset, int length) {
        read_short_array(value, offset, length);
    }
    
    /** Read a unsigned long from CDR encapsulation.
     * @return Read unsigned long
     */    
    public final int read_ulong() {
        return read_long();
    }
    
    /** Reads an array of unsigned longs from CDR encapsulation.
     * @param value Array to read data into.
     * @param offset Array offset to start at.
     * @param length Number of unsigned longs to read from CDR stream.
     */
    public final void read_ulong_array(int[] value, int offset, int length) {
        read_long_array(value, offset, length);
    }
    
    /** Read a unisgned long long from CDR encapsulation.
     * @return Read unsigned long long
     */    
    public final long read_ulonglong() {
        return read_longlong();
    }
    
    /** Reads an array of unsigned long longs from CDR encapsulation.
     * @param value Array to read data into.
     * @param offset Array offset to start at.
     * @param length Number of unsigned long longs to read from CDR stream.
     */
    public final void read_ulonglong_array(long[] value, int offset, int length) {
        read_longlong_array(value, offset, length);
    }
    
    /** Read a Corba object from CDR encapsulation.
     * @return Read object 
     */    
    public final org.omg.CORBA.Object read_Object() {
        return null;
    }
    
    /** Read a Corba object from CDR encapsulation.
     * @return Read object
     * @param clz
     */    
    public org.omg.CORBA.Object read_Object(java.lang.Class clz) {
        return this.read_Object();
    }
    
    /** Read a Coeba context from CDR encapsulation. (Not Implemented)
     * @return Read context
     */    
    public org.omg.CORBA.Context read_Context() {
        return null;
    }

    /** Read a principle from CDR encapsulation. (Not Implemented)
     * @return Read principle.
     * @deprecated Method implemented to make sure zen compiles.
     */    
    public org.omg.CORBA.Principal read_Principal() {
        return null;
    }
    
    /** Read a int from CDR encapsulation. (Not Implemented)
     * @return Read int.
     * @throws IOException When I/O error occurs.
     */    
    public int read(){
        return read_long();
    }

    /** Set stream to interpret read data as little endian. */    
    public void setEndian( boolean endian ) {
        buffer.setEndian( endian );
    }

    /** Skip the number of octets (bytes) in the stream.
     * @param n Number of bytes to skip.
     */    
    void skipOctets(int n) {
        for( int i=0;i<n;i++ )
            read_octet();
    }

    /** Free all allocated buffers and resources. */    
    public void free() {
        buffer.free();
        CDRInputStream.release( this );
    }

    // Needs to be implemented
    public long getPosition() {
        return 0;
    }
}
