package edu.uci.ece.zen.orb;

//import edu.uci.ece.zen.utils.Logger;
import org.omg.CORBA.TCKind;
import edu.uci.ece.zen.utils.*;
import javax.realtime.*;

public class CDRInputStream extends org.omg.CORBA.portable.InputStream {
    ReadBuffer buffer;
    public edu.uci.ece.zen.orb.ORB orb;
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

    public static CDRInputStream fromOctetSeq(byte[] barr, ORB orb){

        ReadBuffer readBuf = ReadBuffer.instance();
        readBuf.init();
        readBuf.writeByteArray(barr, 0 , barr.length );
        CDRInputStream in = CDRInputStream.instance();
        in.init( orb , readBuf );
        in.setEndian( in.read_boolean() );

        return in;
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

    /** Set stream to intRerpret read data as little endian. */
    public void setEndian( boolean isLittleEndian ) {
        buffer.setEndian( isLittleEndian );
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



    /**
     * Starts reading from an encapsulated CDRInputStream on the input
     * buffer.  The method closeEncapsulation must be called when done.
     * @return CDRInputStream that was encapsulated on the input buffer.
     */
    private CDRInputStream openEncapsulation() {

        // The length and endianness is automatically picked up from
        // the stream.
        CDRInputStream cdr = read_CDRInputStream();
        int endianness = (int) cdr.read_octet();
        // The encapsulated header uses 0 for big-endian (Java's
        // representation) and 1 for little-endian.
        if (endianness == 0) {
            setEndian(false);
        }
        else if (endianness == 1) {
            setEndian(true);
        }
        else {
            throw new org.omg.CORBA.MARSHAL("Invalid endianness octet read");
        }

        return cdr;

        //return this;
    }



    /**
     * Frees up resources allocated when openEncapsulation() was called.
     * The method closeEncapsulation must be called when done.
     */
    private void closeEncapsulation(CDRInputStream cdr) {
        cdr.free();
    }



    /**
     * Reads an any object from the the CDRInputStream.  This
     * processes consists of reading a TypeCode representing an
     * Any, then the Any's stored value.
     *
     * @return Any object just read from CDRInputStream.
     */
    public final org.omg.CORBA.Any read_any() {
        org.omg.CORBA.TypeCode anyType = read_TypeCode();
        org.omg.CORBA.Any any = orb.create_any();
        any.read_value (this, anyType);
        return any;
    }



    /**
     * Read the value of type <code>tc</code>from this CDRInputStream
     * and write it to <code>out</code>
     *
     * <p>The method is called from the monolithic Any
     * implementation.
     *
     * @param tc TypeCode of type to try to read.
     * @param out OutputStream to which to write read data
     */
    public final void read_value_of_type(org.omg.CORBA.TypeCode tc, org.omg.CORBA.portable.OutputStream out) {
        int kind = ((edu.uci.ece.zen.orb.TypeCode) tc)._kind();

        try {
            switch (kind) {
            case TCKind._tk_null:
            case TCKind._tk_void:
                break;

            case TCKind._tk_boolean:
                out.write_boolean(read_boolean());
                break;

            case TCKind._tk_char:
                out.write_char(read_char());
                break;

            case TCKind._tk_wchar:
                out.write_wchar(read_wchar());
                break;

            case TCKind._tk_octet:
                out.write_octet(read_octet());
                break;

            case TCKind._tk_ushort:
                out.write_ushort(read_ushort());
                break;

            case TCKind._tk_short:
                out.write_short(read_short());
                break;

            case TCKind._tk_long:
                out.write_long(read_long());
                break;

            case TCKind._tk_ulong:
                out.write_ulong(read_ulong());
                break;

            case TCKind._tk_float:
                out.write_float(read_float());
                break;

            case TCKind._tk_double:
                out.write_double(read_double());
                break;

            case TCKind._tk_longlong:
                out.write_longlong(read_longlong());
                break;

            case TCKind._tk_ulonglong:
                out.write_ulonglong(read_ulonglong());
                break;

            case TCKind._tk_any:
                out.write_any(read_any());
                break;

            case TCKind._tk_TypeCode:
                out.write_TypeCode(read_TypeCode());
                break;

            case TCKind._tk_Principal:
                out.write_Principal(read_Principal());
                break;

            case TCKind._tk_objref:
                out.write_Object(read_Object());
                break;

            case TCKind._tk_string:
                out.write_string(read_string());
                break;

            case TCKind._tk_wstring:
                out.write_wstring(read_wstring());
                break;

            case TCKind._tk_array: {
                int length = tc.length();
                for (int i = 0; i < length; i++)
                    read_value_of_type(tc.content_type(), out);
                break;
            }
            case TCKind._tk_sequence: {
                int length = read_long();

                out.write_long(length);
                for (int i = 0; i < length; i++)
                    read_value_of_type(tc.content_type(), out);
                break;
            }
            case TCKind._tk_except:
                out.write_string(read_string());

                // don't break, fall through to .

            case TCKind._tk_struct:
                for (int i = 0; i < tc.member_count(); i++)
                    read_value_of_type(tc.member_type(i), out);
                break;

            case TCKind._tk_enum:
                out.write_long(read_long());
                break;

            case TCKind._tk_alias:
                //            out.write_string( read_string());
                //            out.write_string( read_string());
                //            out.write_TypeCode( read_TypeCode());
                    //BM I encountered the line below which differs from
                    //how JacORB's CDRInputStream implementation handles
                    //the _tk_alias type.  I decided to change the
                    //handling of _tk_alias to how JacORB does it.
                    //out.write_value(tc.content_type(), this);
                read_value_of_type( tc.content_type(), out );

                break;


            case TCKind._tk_union:
                read_value_union(tc, out);
                break;


            case TCKind._tk_value:
                // This case never happens because this method is not called for type _tk_value

                // The default case may be reached for _tk_union which
                // this method did not include support for when it was
                // written (before our IDL compiler supported the
                // tk_union data type)


            default:
                ZenProperties.logger.log(
                    Logger.WARN,
                    "edu.uci.ece.zen.orb.CDRInputStream",
                    "read_value()",
                    "Unwritten method in CDRInputStream for Valuetype");
                try {
                    throw new org.omg.CORBA.NO_IMPLEMENT();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }

        } // end of: try

        // Thrown by code for _tk_array, _tk_sequence, _tk_struct
        catch (org.omg.CORBA.TypeCodePackage.BadKind b) {
            System.err.println("CDRInputStream#read_value threw BadKind exception for kind " + kind);
            b.printStackTrace();
        }

        // Thrown by code for handling _tk_struct
        catch (org.omg.CORBA.TypeCodePackage.Bounds b) {
            System.err.println("CDRInputStream#read_value threw Bounds exception for kind " + kind);
            b.printStackTrace();
        }

    }



    private final void read_value_union(org.omg.CORBA.TypeCode tc, org.omg.CORBA.portable.OutputStream out) {
        CDROutputStream.transcribe_union(tc, this, out);
    }

    /**
        try {

            // Determine the type of the union.  Its type will be read from the CDR Stream.
            edu.uci.ece.zen.orb.TypeCode discriminatorType = (edu.uci.ece.zen.orb.TypeCode) edu.uci.ece.zen.orb.TypeCode.originalType(tc.discriminator_type());

            int selected_index = -1;
            int defaultIndex = tc.default_index();

            // Switch based on the kind of discriminator used.  The
            // discriminator could be an IDL short, long, longlong, (or
            // unsigned versions of the last three), boolean, char, wchar,
            // enum
            int kind = discriminatorType._kind();
            switch (kind) {

            case TCKind._tk_short:
            case TCKind._tk_ushort: {
                short aValue = read_short();
                out.write_short(aValue);
                selected_index = CDROutputStream.union_find_member_label(tc, "" + aValue);
                break;
            }

            case TCKind._tk_long:
            case TCKind._tk_ulong: {
                long aValue = read_long();
                out.write_short(aValue);
                selected_index = CDROutputStream.union_find_member_label(tc, "" + aValue);
                break;
            }



            case TCKind._tk_enum:
            default:
                throw new org.omg.CORBA.NO_IMPLEMENT("union support not added yet for type " + kind);
            }

        }

        // These exceptions should never really happen, but are
        // required for the union's accessor methods
        catch (org.omg.CORBA.TypeCodePackage.BadKind bk) {
            System.err.println("BadKind exception occurred in CDRInputStream.write_value_union");
        }
        catch (org.omg.CORBA.TypeCodePackage.Bounds b) {
            System.err.println("Bounds exception occurred in CDRInputStream.write_value_union");
        }

    }
    */



    public org.omg.CORBA.TypeCode read_TypeCode() {
        // The indirection map is created here because indirections
        // are not "freestanding" but rathor only valid within outer
        // enclosing scopes that began with a read_TypeCode operation
        // (CORBA v2.3 SPEC section 15.3.5.1, page 15-27).
        java.util.Hashtable tcIndirectionMap = new java.util.Hashtable();
        org.omg.CORBA.TypeCode retValue = read_TypeCode(tcIndirectionMap, 0);
        return retValue;
    }



    /**
     * Read a TypeCode from the input stream and return a newly
     * constructed object copying its values.
     *
     * @param tcIndirectionMap mapping of strings for integer values to either Strings as ids or TypeCodes already read
     * @param outerEncapsPos Position of the start of this CDRInputStream in any outermost CDRInputStream that has encapsulated one or more down to this CDRInputStream
     * @return TypeCode read from the inputStream.
     */
    private org.omg.CORBA.TypeCode read_TypeCode(java.util.Hashtable tcIndirectionMap, long outerEncapsPos) {
        long thisKindPos = outerEncapsPos + getPosition();
        String thisKindPosStr = Long.toString(thisKindPos);
        int kind = read_long();

        switch (kind) {
        case TCKind._tk_null:
        case TCKind._tk_void:
        case TCKind._tk_short:
        case TCKind._tk_long:
        case TCKind._tk_ushort:
        case TCKind._tk_ulong:
        case TCKind._tk_float:
        case TCKind._tk_double:
        case TCKind._tk_boolean:
        case TCKind._tk_char:
        case TCKind._tk_wchar:
        case TCKind._tk_octet:
        case TCKind._tk_any:  // TypeCode representing an Any only has to read its numeric kind
        case TCKind._tk_TypeCode:  // TypeCode representing a TypeCode only has to read its numeric kind
        case TCKind._tk_longlong:
        case TCKind._tk_ulonglong:
        case TCKind._tk_longdouble:
            return orb.get_primitive_tc(org.omg.CORBA.TCKind.from_int(kind));

        case TCKind._tk_string:
        case TCKind._tk_wstring:
            int len = read_long();
            return edu.uci.ece.zen.orb.TypeCode.newStringTC(kind, len);

        case TCKind._tk_objref: {
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();
            String objref_id = cdr.read_string();
            String objref_name = cdr.read_string();
            closeEncapsulation(cdr);
            return edu.uci.ece.zen.orb.TypeCode.newObjRefTC(objref_id, objref_name);
        }

        case TCKind._tk_struct:
        case TCKind._tk_except: {
            // Calculate the position of the start of the next
            // encapsulation in the outermost CDRInputStream.  It will
            // be the position of the start of this input stream
            // (outerEncapsPos) plus the current position in this
            // input stream plus four bytes
            long outermostEncapsPos = outerEncapsPos + getPosition() + 4;
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();

            String id = cdr.read_string();
            // Initially, store id in indirection mapping, will cause
            // recursive typecode to be created if referred to later.
            tcIndirectionMap.put(thisKindPosStr, id);

            String name = cdr.read_string();
            int memCount = cdr.read_ulong();
            org.omg.CORBA.StructMember[] sm = new org.omg.CORBA.StructMember[memCount];

            for (int i = 0; i < memCount; i++) {
                String sname = cdr.read_string();
                org.omg.CORBA.TypeCode type = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);

                sm[i] = new org.omg.CORBA.StructMember(sname, type, null);
            }
            closeEncapsulation(cdr);

            org.omg.CORBA.TypeCode retTC = new TypeCode(kind, id, name, sm);
            // Replace indirection mapping with complete typecode
            tcIndirectionMap.put(thisKindPosStr, retTC);
            return retTC;
        }

        case TCKind._tk_union: {
            long outermostEncapsPos = outerEncapsPos + getPosition() + 4;
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();

            String id = cdr.read_string();
            tcIndirectionMap.put(thisKindPosStr, id);

            String union_name = cdr.read_string();
            org.omg.CORBA.TypeCode discriminator = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);

            int default_index = cdr.read_long();

            int union_memCount = cdr.read_ulong();

            org.omg.CORBA.UnionMember union_members[] = new org.omg.CORBA.UnionMember[union_memCount];

            for (int i = 0; i < union_memCount; i++) {
                if( ZenProperties.dbg )
                    System.out.println( Thread.currentThread() +"CDR Istream :: Writing >> " + i);
                org.omg.CORBA.Any uLabel = orb.create_any();
                if ( i != default_index ) {
                    uLabel.read_value(cdr, discriminator);
                }
                else {
                    uLabel.insert_octet( cdr.read_octet() );
                }
                String uname = cdr.read_string();
                org.omg.CORBA.TypeCode utypecode = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);
                // read_object needs to be implemented!!!!
                //org.omg.CORBA.IDLType utypeDef = (org.omg.CORBA.IDLType) read_Object();


                //union_members[i] = new org.omg.CORBA.UnionMember(uname, uLabel, utypecode, utypeDef);
                union_members[i] = new org.omg.CORBA.UnionMember(uname, uLabel, utypecode, null);
            }
            closeEncapsulation(cdr);


            TypeCode retTC = new TypeCode(id, union_name, discriminator, union_members);

            tcIndirectionMap.put(thisKindPosStr, retTC);
            return retTC;
        }

        case TCKind._tk_enum: {
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();
            String id = cdr.read_string();
            // Initially, store id in indirection mapping, will cause
            // recursive typecode to be created if referred to later.
            tcIndirectionMap.put(thisKindPosStr, id);
            String enum_name = cdr.read_string();
            int enum_memCount = cdr.read_ulong();
            String enum_members[] = new String[enum_memCount];

            for (int i = 0; i < enum_memCount; i++) {
                enum_members[i] = cdr.read_string();
            }
            closeEncapsulation(cdr);

            TypeCode retTC = new TypeCode(id, enum_name, enum_members);
            // Replace indirection mapping with complete typecode
            tcIndirectionMap.put(thisKindPosStr, retTC);
            return retTC;
        }

        case TCKind._tk_sequence:
        case TCKind._tk_array: {
            long outermostEncapsPos = outerEncapsPos + getPosition() + 4;
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();

            org.omg.CORBA.TypeCode seqElemType = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);
            int bound = cdr.read_ulong();
            closeEncapsulation(cdr);

            // Create a new typecode for sequence types
            TypeCode retTC = new TypeCode(bound, seqElemType);
            if (kind == TCKind._tk_array) {
                retTC.kind = TCKind._tk_array;
            }
            return retTC;
        }

        case TCKind._tk_alias: {
            long outermostEncapsPos = outerEncapsPos + getPosition() + 4;
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();
            String aliasId = cdr.read_string();
            String aliasName = cdr.read_string();
            org.omg.CORBA.TypeCode aliasTc = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);
            closeEncapsulation(cdr);

            TypeCode retTC = new TypeCode(aliasId, aliasName, aliasTc);
            return retTC;
        }

        case TCKind._tk_value: {
            long outermostEncapsPos = outerEncapsPos + getPosition() + 4;
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();

            String id = cdr.read_string();
            String name = cdr.read_string();
            short typeModifier = cdr.read_short();

            org.omg.CORBA.TypeCode concreteBaseType = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);

            int memberCount = cdr.read_ulong();
            org.omg.CORBA.ValueMember [] valueMembers = new org.omg.CORBA.ValueMember[memberCount];
            for (int i = 0; i < memberCount; i++) {
                String valueName = cdr.read_string();
                org.omg.CORBA.TypeCode valueTypeCode = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);
                short valueVisibilityAccess = cdr.read_short();
                valueMembers[i] = new org.omg.CORBA.ValueMember(valueName, null, null, null, valueTypeCode, null, valueVisibilityAccess);
            }

            closeEncapsulation(cdr);

            org.omg.CORBA.TypeCode retTC = orb.create_value_tc(id, name, typeModifier, concreteBaseType, valueMembers);
            return retTC;
        }

        case TCKind._tk_value_box: {
            long outermostEncapsPos = outerEncapsPos + getPosition() + 4;
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();
            String id = cdr.read_string();
            String name = cdr.read_string();
            org.omg.CORBA.TypeCode contentType = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);
            closeEncapsulation(cdr);

            org.omg.CORBA.TypeCode retTC = orb.create_value_box_tc(id, name, contentType);
            return retTC;
        }

        case TCKind._tk_abstract_interface: {
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();
            String intId = cdr.read_string();
            String intName = cdr.read_string();
            closeEncapsulation(cdr);

            return new TypeCode(intId, intName);
        }


            // An indirection to a previous type ID string or TypeCode
        case 0xFFFFFFFF: {
            long otherLoc = getPosition() + read_long();
            java.lang.Object prevIDOrTC = tcIndirectionMap.get( Long.toString(otherLoc) );
            org.omg.CORBA.TypeCode retTC;
            // Only a string existed in the cache because the TypeCode
            // hadn't been completely written at the time it appeared
            // again (which means it was recursive).
            if ( prevIDOrTC instanceof String) {
                retTC = orb.create_recursive_tc( (String) prevIDOrTC );
            }
            // A complete TypeCode had been saved in the cache and
            // hence had been fully processed before this indirection
            // was found.
            else if ( prevIDOrTC instanceof org.omg.CORBA.TypeCode ) {
                retTC = (org.omg.CORBA.TypeCode) prevIDOrTC;
            }
            else {
                throw new org.omg.CORBA.MARSHAL("TypeCode Indirection Not Found at " + otherLoc);
            }
            return retTC;
        }

        default:
            ZenProperties.logger.log(
                Logger.WARN,
                "edu.uci.ece.zen.orb.CDRInputStream",
                "read_TypeCode()",
                "Unwritten method in CDRInputStream for TypeCode");
            return null;
        }
    }


}
