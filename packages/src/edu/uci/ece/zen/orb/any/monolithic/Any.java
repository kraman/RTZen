
package edu.uci.ece.zen.orb.any.monolithic;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.orb.CDRInputStream;

/**
 * Monolithic implementation of the IDL type Any.
 *
 * This class is the implementation of the CORBA.Any type.
 *
 * <p> Any objects store two major items: a data value, and a TypeCode
 * object representing the type of the stored data value.

 * <p> Many of the functions of the Any are of the form insert_X(X)
 * where X is some type.  This will set the type of the any to X and
 * make it store the given value.
 *
 * <p> Other functios are of the form X extract_X().  These functions
 * will attempt to return the data value stored by the any, leaving
 * the Any still storing the value (rather than becoming null).  The
 * exception <code>org.omg.CORBA.BAD_OPERATION</code> is thrown if the
 * type being extracted does not match the type of data stored or if
 * the Any's value isn't set (e.g. when the Any is first created).
 *
 *
 * @author Bruce Miller
 * @version $Revision: 1.2 $ $Date: 2004/01/20 21:55:09 $
 *
 */

public final class Any extends edu.uci.ece.zen.orb.any.AnyStrategy {

    /** The currently held object */
    private java.lang.Object value;
    /** TypeCode used to represent the currently held object's type */
    private org.omg.CORBA.TypeCode typeCode;
    /** A reference to the orb, needed to creaet CDRInputStream instances */
    //private org.omg.CORBA.ORB orb;
    private edu.uci.ece.zen.orb.ORB orb;


    
    /** Default omg specified constructor creates a monolithic any with
     * no reference to the orb, so setORB(org.omg.CORBA.orb) must be
     * called immediately afterwards.
     */
    public Any() {
        //type( new edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_null) );
        type( edu.uci.ece.zen.orb.TypeCode.lookupPrimitiveTc(org.omg.CORBA.TCKind._tk_null) );
    }



    /**
     * Sets the reference to the orb, needed when storing certain
     * complex data types that use CDROutputStreams and
     * CDRInputStreams.
     *
     * @param _orb org.omg.CORBA.orb reference to store.
     */
    public void setOrb(edu.uci.ece.zen.orb.ORB _orb) {
        orb = _orb;
    }


    /** Constructor takes a reference to the orb.  Call this one.
     * @param orb the orb 
     */
    public Any (edu.uci.ece.zen.orb.ORB orb) {
        this.orb = orb;
        type( orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_null) );
    }


    /** Accessor for the typeCode object for the stored object.
     * @return TypeCode of the value stored by the Any.
     */
    public org.omg.CORBA.TypeCode type() {
        return typeCode;
    }

    /** Currently behaves just like
     * <code>edu.uci.ece.zen.orb.TypeCode.originalType()</code> which
     * is aspectized and dereferences chains of aliases
     *
     * @return TypeCode representing base type of type stored in this any.
     */
    public org.omg.CORBA.TypeCode originalType () {
        return edu.uci.ece.zen.orb.TypeCode.originalType(typeCode);
        //return typeCode;
    }


    /** Sets the TypeCode for the object the Any is representing.  
     * and sets the stored value to null.
     * @param newType the new TypeCode for the Any to represent.  
     */
    public void type (org.omg.CORBA.TypeCode newType) { 
        typeCode = newType; 
        value = null; 
    }


    /**
     * Returns stringified version of the value stored in the any, or
     * "null" if no value is stored.
     * @return stringified version of the stored value or "null" if stored value is null.
     */
    public String toString() {
        if ( value != null )
            return "Monolithic Any storing value" + value.toString();
        else
            return "Monolithic Any storing null";
    }



    
    // ----------------------------------------------------------------------


    /** Returns true if there is textual equivalence between the value
     * stored by this Any and the <code>a</code>.  False is returned
     * if the type of <code>a</code> does not match this type.  For
     * objects and unions, the actual values of are compared -- the
     * function does not do a cheapskate comparison of memory
     * addresses.
     *
     * @param anAny the Any to compare this Any to.
     * @return True if the a equals this Any by content of store value, false otherwise.
     * @throws org.omg.CORBA.BAD_PARAM if <code>a</code> is null.
    */
    public boolean equal (org.omg.CORBA.Any anAny) {
        if (anAny == null) {
            throw new org.omg.CORBA.BAD_PARAM ("Null pointer passed to monolithic Any.equal operation");
        }

        // If the the types of this Any and the Any a are different, return false (not equal)
        if (!typeCode.equal (anAny.type())) {
            return false;
        }

        int this_kind = originalType().kind().value();

        switch (this_kind) {
            
        case org.omg.CORBA.TCKind._tk_null:
        case org.omg.CORBA.TCKind._tk_void:
            return true;

        case org.omg.CORBA.TCKind._tk_short:
            return extract_short() == anAny.extract_short();
        case org.omg.CORBA.TCKind._tk_long:
            return extract_long() == anAny.extract_long();
        case org.omg.CORBA.TCKind._tk_ushort:
            return extract_ushort() == anAny.extract_ushort();
        case org.omg.CORBA.TCKind._tk_ulong:
            return extract_ulong() == anAny.extract_ulong();
        case org.omg.CORBA.TCKind._tk_float:
            return extract_float() == anAny.extract_float();
        case org.omg.CORBA.TCKind._tk_double:
            return extract_double() == anAny.extract_double();
        case org.omg.CORBA.TCKind._tk_boolean:
            return extract_boolean() == anAny.extract_boolean();
        case org.omg.CORBA.TCKind._tk_char:
            return extract_char() == anAny.extract_char();
        case org.omg.CORBA.TCKind._tk_octet:
            return extract_octet() == anAny.extract_octet();
        case org.omg.CORBA.TCKind._tk_any:
            return extract_any().equals( anAny.extract_any() );
        case org.omg.CORBA.TCKind._tk_TypeCode:
            return extract_TypeCode().equal( anAny.extract_TypeCode() );
        case org.omg.CORBA.TCKind._tk_Principal:
            throw new org.omg.CORBA.NO_IMPLEMENT ("Monolithic Any#equal() found Principal, which has been a deprecated type since CORBA v2.3");
        case org.omg.CORBA.TCKind._tk_objref: 
            return extract_Object().equals( anAny.extract_Object() );
        case org.omg.CORBA.TCKind._tk_string: 
            return extract_string().equals( anAny.extract_string() );

        case org.omg.CORBA.TCKind._tk_longlong:
            return extract_longlong() == anAny.extract_longlong();
        case org.omg.CORBA.TCKind._tk_ulonglong:
            return extract_ulonglong() == anAny.extract_ulonglong();
        case org.omg.CORBA.TCKind._tk_wchar:
            return extract_wchar() == anAny.extract_wchar();
        case org.omg.CORBA.TCKind._tk_wstring: 
            return extract_wstring().equals( anAny.extract_wstring() );
            // Even though fixed isn't supported in the pluggable Anys
            // on 2003-05-22, I'm including support.
        case org.omg.CORBA.TCKind._tk_fixed:
            return extract_fixed().equals( anAny.extract_fixed() );

        case org.omg.CORBA.TCKind._tk_array: 
        case org.omg.CORBA.TCKind._tk_sequence: 
        case org.omg.CORBA.TCKind._tk_struct: 
        case org.omg.CORBA.TCKind._tk_except:
        case org.omg.CORBA.TCKind._tk_enum:
        case org.omg.CORBA.TCKind._tk_union:
            {
                // Write both unions to a CDROutputStream and see if
                // their raw binary values are the same.
                CDROutputStream out1, out2;

                out1 = CDROutputStream.instance();
                out1.init(orb);
                out2 = CDROutputStream.instance();
                out2.init(orb);
                write_value( out1 );
                anAny.write_value( out2 );

                return out1.equals( out2 );
            }
        default:
            throw new org.omg.CORBA.BAD_TYPECODE("Cannot call monolithic Any.equal to compare with TypeCode kind " + this_kind);
        } 
    }




    /**
     * Read a value from the input stream of a particular type from
     * <code>input</code>, setting this' type to <code>tc</code>.
     *
     * @param input input stream to read from.
     * @param tc TypeCode of type to read 
     */
    public void read_value (org.omg.CORBA.portable.InputStream input, 
                            org.omg.CORBA.TypeCode tc)
        throws org.omg.CORBA.MARSHAL {

        if (tc == null) {
           throw new org.omg.CORBA.BAD_PARAM("TypeCode is null");
        }   
        typeCode = tc;

        int kind = tc.kind().value();
        switch (kind)
        {
        case org.omg.CORBA.TCKind._tk_null: 
            break;
        case org.omg.CORBA.TCKind._tk_void:
            break;
        case org.omg.CORBA.TCKind._tk_short:
            insert_short( input.read_short());
            break;
        case org.omg.CORBA.TCKind._tk_long:
            insert_long( input.read_long());
            break;
        case org.omg.CORBA.TCKind._tk_ushort:
            insert_ushort(input.read_ushort());
            break;
        case org.omg.CORBA.TCKind._tk_ulong:
            insert_ulong( input.read_ulong());
            break;
        case org.omg.CORBA.TCKind._tk_float:
            insert_float( input.read_float());
            break;
        case org.omg.CORBA.TCKind._tk_double:
            insert_double( input.read_double());
            break;
        case org.omg.CORBA.TCKind._tk_boolean:
            insert_boolean( input.read_boolean());
            break;
        case org.omg.CORBA.TCKind._tk_char:
            insert_char( input.read_char());
            break;
        case org.omg.CORBA.TCKind._tk_octet:
            insert_octet( input.read_octet());
            break;
        case org.omg.CORBA.TCKind._tk_any:
            insert_any( input.read_any());
            break;
        case org.omg.CORBA.TCKind._tk_TypeCode:
            insert_TypeCode( input.read_TypeCode());
            break;
        case org.omg.CORBA.TCKind._tk_Principal:
            throw new org.omg.CORBA.NO_IMPLEMENT ("Monolithic Any#equal() found Principal, which is a deprecated type since CORBA v2.3");
        case org.omg.CORBA.TCKind._tk_objref: 
            insert_Object( input.read_Object());
            break;
        case org.omg.CORBA.TCKind._tk_string: 
            insert_string( input.read_string());
            break;

        case org.omg.CORBA.TCKind._tk_longlong:
            insert_longlong( input.read_longlong());
            break;
        case org.omg.CORBA.TCKind._tk_ulonglong:
            insert_ulonglong( input.read_ulonglong());
            break;

        case org.omg.CORBA.TCKind._tk_wchar:
            insert_wchar( input.read_wchar());
            break;
        case org.omg.CORBA.TCKind._tk_wstring: 
            insert_wstring( input.read_wstring());
            break;

            // Even though fixed isn't supported in the pluggable Anys
            // on 2003-08-28, I'm including support.
        case org.omg.CORBA.TCKind._tk_fixed:
            try
            {
               // move the decimal based on the scale
               java.math.BigDecimal fixed = input.read_fixed();
               int scale = (int)tc.fixed_scale();
               insert_fixed( fixed.movePointLeft( scale ), tc );
            }
            catch( org.omg.CORBA.TypeCodePackage.BadKind bk ){}
            break;

        case org.omg.CORBA.TCKind._tk_array: 
        case org.omg.CORBA.TCKind._tk_sequence: 
        case org.omg.CORBA.TCKind._tk_struct: 
        case org.omg.CORBA.TCKind._tk_except:
        case org.omg.CORBA.TCKind._tk_enum:
        case org.omg.CORBA.TCKind._tk_union:
        case org.omg.CORBA.TCKind._tk_alias:

            value = CDROutputStream.instance();
            ((CDROutputStream)value).init(orb);
            ((CDROutputStream)value).write_value(tc, input);
            break;
        case org.omg.CORBA.TCKind._tk_value:
            insert_Value 
                (((org.omg.CORBA_2_3.portable.InputStream)input).read_value());
            break;
        default:
            throw new org.omg.CORBA.BAD_TYPECODE("Monolithic Any#read_value doesn't have support yet for TypeCode with kind " + kind);
        }
    }


    /**
     * Write the value stored in the Any to an outputstream.
     *
     * @param out OutputStream to which to write value stored in Any. 
     */
    public void write_value (org.omg.CORBA.portable.OutputStream out)
    {
        int kind = typeCode.kind().value();
        switch (kind)
        {
        case org.omg.CORBA.TCKind._tk_null: 
            break;
        case org.omg.CORBA.TCKind._tk_void:
            break;

        case org.omg.CORBA.TCKind._tk_short:
            out.write_short(extract_short());
            break;
        case org.omg.CORBA.TCKind._tk_long:
            out.write_long(extract_long());
            break;

        case org.omg.CORBA.TCKind._tk_ushort:
            out.write_ushort(extract_ushort());
            break;
        case org.omg.CORBA.TCKind._tk_ulong:
            out.write_ulong(extract_ulong());
            break;
        case org.omg.CORBA.TCKind._tk_float:
            out.write_float(extract_float());
            break;
        case org.omg.CORBA.TCKind._tk_double:
            out.write_double(extract_double());
            break;
        case org.omg.CORBA.TCKind._tk_boolean:
            out.write_boolean(extract_boolean());
            break;
        case org.omg.CORBA.TCKind._tk_char:
            out.write_char(extract_char());
            break;
        case org.omg.CORBA.TCKind._tk_octet:
            out.write_octet(extract_octet());
            break;
        case org.omg.CORBA.TCKind._tk_any:
            out.write_any(extract_any());
            break;
        case org.omg.CORBA.TCKind._tk_TypeCode:
            out.write_TypeCode(extract_TypeCode());
            break;
        case org.omg.CORBA.TCKind._tk_Principal:
            throw new org.omg.CORBA.NO_IMPLEMENT ("Monolithic Any#equal() found Principal, which has been a deprecated type since CORBA v2.3");
        case org.omg.CORBA.TCKind._tk_objref: 
            out.write_Object(extract_Object());
            break;
        case org.omg.CORBA.TCKind._tk_string: 
            out.write_string(extract_string());
            break;

        case org.omg.CORBA.TCKind._tk_longlong:
            out.write_longlong(extract_longlong());
            break;
        case org.omg.CORBA.TCKind._tk_ulonglong:
            out.write_ulonglong(extract_ulonglong());
            break;
        case org.omg.CORBA.TCKind._tk_wchar:
            out.write_wchar(extract_wchar());
            break;
        case org.omg.CORBA.TCKind._tk_wstring: 
            out.write_wstring(extract_wstring());
            break;
        case org.omg.CORBA.TCKind._tk_fixed:
            out.write_fixed(extract_fixed());
            break;

        case org.omg.CORBA.TCKind._tk_struct: 
        case org.omg.CORBA.TCKind._tk_except:
        case org.omg.CORBA.TCKind._tk_enum:
        case org.omg.CORBA.TCKind._tk_union:
        case org.omg.CORBA.TCKind._tk_array: 
        case org.omg.CORBA.TCKind._tk_sequence: 
        case org.omg.CORBA.TCKind._tk_alias:
            try
            {
                // If value is a value type object
                if (value instanceof org.omg.CORBA.portable.Streamable)
                { 
                    org.omg.CORBA.portable.Streamable s = 
                        (org.omg.CORBA.portable.Streamable) value;
                    s._write (out);
                }
                else if (value instanceof org.omg.CORBA.portable.OutputStream)
                { 
                    // If a ORBSingleton method to create the any was
                    // called, this instance may not have its orb
                    // variable set, so we have to use the ORB from
                    // CDROutputStream.

                    org.omg.CORBA.ORB iSOrb = orb;
                    if (! (iSOrb instanceof edu.uci.ece.zen.orb.ORB))
                        iSOrb = ((CDROutputStream) out).orb ();

                    // CDRInputStream is going to overwrite the buffer
                    // given to it, so pass in a copy of the buffer
                    // via getBufferCopy().
                    CDRInputStream in = (CDRInputStream) ((CDROutputStream) out).create_input_stream();

                    in.read_value_of_type (typeCode, out);
                }
                else {
                    throw new org.omg.CORBA.NO_IMPLEMENT("Unable to write kind " + kind);
                }
                break;
            } 
            catch( Exception e )
            {
                e.printStackTrace();
                throw new org.omg.CORBA.INTERNAL( e.getMessage());
            }
        case org.omg.CORBA.TCKind._tk_value:
            ((org.omg.CORBA_2_3.portable.OutputStream)out).write_value ((java.io.Serializable)value);
            break;
        default:
            throw new org.omg.CORBA.BAD_TYPECODE("Monolithic Any#write_value doesn't have suppot for TypeCode with kind "+ kind);
        }
    }




    /**
     * Create an empty CDROutputStream that becomes this Any's stored
     * value; user may write into the OutputStream to store into this
     * Any, but not that this Any's type must be set separately.
     *
     * <p>
     * There is more documentation in {@link edu.uci.ece.zen.orb.any.pluggable.Any#create_output_stream()}
     *
     * @return CDROutputStream that the Any now stores as its value.
     * @see edu.uci.ece.zen.orb.any.pluggable.Any#create_output_stream()
     */

    public org.omg.CORBA.portable.OutputStream create_output_stream() { 

        value = CDROutputStream.instance();
        ((CDROutputStream)value).init(orb);
        return (edu.uci.ece.zen.orb.CDROutputStream) value; 

    }


    /**
     * Create a new CDRInputStream that is populated with the value
     * stored in this Any.  
     *
     * <p>
     * There is more documentation in {@link edu.uci.ece.zen.orb.any.pluggable.Any#create_output_stream()}
     *
     * @return edu.uci.ece.zen.orb.CDROutputStream with the value of
     * the Any ready to be read.
     * @see edu.uci.ece.zen.orb.any.pluggable.Any#create_output_stream()
     */
    public org.omg.CORBA.portable.InputStream create_input_stream() {
        if( value instanceof CDROutputStream ) {
            return ((edu.uci.ece.zen.orb.CDROutputStream) value).create_input_stream();
        }
        else {
            CDROutputStream outStream = CDROutputStream.instance();
            outStream.init(orb);
            write_value(outStream);
            return outStream.create_input_stream();
        }
    }


    // short

    /**
     * Returns a Java short (IDL <code>short</code>) if it is being
     * stored by the Any, throwing an exception otherwise.
     * 
     * @return The short being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>short</code>.
     */
    public short extract_short ()
        throws org.omg.CORBA.BAD_OPERATION
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_short, "Monolithic Any#extract_short found wrong type stored");
        return ((Short)value).shortValue ();
    }

    /**
     * Inserts a Java short (IDL <code>short</code>) into this Any,
     * setting the type of this Any to be representing a
     * <code>short</code> and throwing out any previously held value.
     * 
     * @param s short to set as value stored by this Any.
     */
    public void insert_short (short s) {
        type( orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_short) ); 
        value = new Short(s);
    }


    // ushort

    /**
     * Returns a Java short (IDL <code>unsigned short</code>) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The short being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>unsigned short</code>.
     */
    public short extract_ushort ()
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_ushort, "Monolithic Any#extract_ushort found wrong type stored");
        return ((Short)value).shortValue ();
    }

    /**
     * Inserts a Java short (IDL <code>unsigned short</code>) into this Any, setting
     * the type of this Any to be representing an IDL <code>unsigned short</code>
     * and throwing out any previously held value.
     * 
     * @param s short to set as value stored by this Any.
     */
    public void insert_ushort (short s)
    {
        type( orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_ushort) );
        value = new Short (s);
    }

    // long

    /**
     * Returns a Java int (IDL <code>long</code>) if it is being stored
     * by this Any, throwing an exception otherwise.
     * 
     * @return The int being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>long</code>.
     */
    public int extract_long () 
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_long, "Monolithic Any#extract_long found wrong type stored");
        return ((Integer)value).intValue ();
    }

    /**
     * Inserts a Java int (IDL <code>long</code>) into this Any, setting
     * the type of this Any to be representing an IDL <code>long</code>
     * and throwing out any previously held value.
     * 
     * @param i int to set as value stored by this Any.
     */
    public void insert_long (int i)
    {
        type( orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_long) );
        value = new Integer (i);
    }


    // ulong

    /**
     * Returns a Java int (IDL <code>unsigned long</code>) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The int being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>unsigned long</code>.
     */
    public int extract_ulong () 
    {
         checkStoredType (org.omg.CORBA.TCKind._tk_ulong, "Monolithic Any#extract_ulong found wrong type stored");
        return ((Integer)value).intValue ();
    }

    /**
     * Inserts a Java int (IDL <code>unsigned long</code>) into this Any, setting
     * the type of this Any to be representing an IDL <code>unsigned long</code>
     * and throwing out any previously held value.
     * 
     * @param i int to set as value stored by this Any.
     */
    public void insert_ulong (int i)
    {
        type( orb.get_primitive_tc( org.omg.CORBA.TCKind.tk_ulong ) );
        value = new Integer (i);
    }


    // longlong

    /**
     * Returns a Java long (IDL <code>long long</code>) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The long being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>long long</code>.
     */
    public long extract_longlong () 
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_longlong, "Monolithic Any#extract_longlong found wrong type stored");
        return ((Long)value).longValue ();
    }

    /**
     * Inserts a Java long (IDL <code>long long</code>) into this Any,
     * setting the type of this Any to be representing an IDL
     * <code>long long</code> and throwing out any previously held
     * value.
     * 
     * @param l long to set as value stored by this Any.
     */
    public void insert_longlong (long l)
    {
        type( orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_longlong) );
        value = new Long (l);
    }


    // ulonglong

    /**
     * Returns a Java long (IDL <code>unsigned long long</code>) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The long being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>unsigned long long</code>.
     */
    public long extract_ulonglong () 
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_ulonglong, "Monolithic Any#extract_ulonglong found wrong type stored");
        return ((Long)value).longValue ();
    }

    /**
     * Inserts a Java long (IDL <code>unsigned long long</code>) into this Any,
     * setting the type of this Any to be representing an IDL
     * <code>unsigned long long</code> and throwing out any previously held
     * value.
     * 
     * @param l long to set as value stored by this Any.
     */
    public void insert_ulonglong (long l)
    {
        type( orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_ulonglong) );
        value = new Long (l);
    }


    // float

    /**
     * Returns a Java float (IDL <code>float</code>) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The double being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>float</code>.
     */
    public float extract_float () 
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_float, "Monolithic Any#extract_float found wrong type stored");
        return ((Float)value).floatValue ();
    }

    /**
     * Inserts a Java float (IDL <code>float</code>) into this Any,
     * setting the type of this Any to be representing an IDL
     * <code>float</code> and throwing out any previously held
     * value.
     * 
     * @param f float to set as value stored by this Any.
     */
    public void insert_float (float f)
    {
        type( orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_float) );
        value = new Float (f);
    }


    // double

    /**
     * Returns a Java double (IDL <code>double</code>) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The double being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>double</code>.
     */
    public double extract_double () 
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_double, "Monolithic Any#extract_double found wrong type stored");
        return ((Double)value).doubleValue ();
    }

    /**
     * Inserts a Java double (IDL <code>double</code>) into this Any,
     * setting the type of this Any to be representing an IDL
     * <code>double</code> and throwing out any previously held
     * value.
     * 
     * @param d double to set as value stored by this Any.
     */
    public void insert_double (double d)
    {
        type( orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_double) );
        value = new Double (d);
    }


    // boolean

    /**
     * Returns a Java boolean (IDL <code>boolean</code>) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The boolean being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>boolean</code>.
     */
    public boolean extract_boolean () 
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_boolean, "Monolithic Any#extract_boolean found wrong type stored");
        return ((Boolean)value).booleanValue ();
    }

    /**
     * Inserts a Java boolean (IDL <code>boolean</code>) into this Any,
     * setting the type of this Any to be representing an IDL
     * <code>boolean</code> and throwing out any previously held
     * value.
     * 
     * @param b boolean to set as value stored by this Any.
     */
    public void insert_boolean (boolean b)
    {
        type( orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_boolean) );
        value = new Boolean (b);
    }


    // char

    /**
      * Returns a Java char (IDL <code>char</code>) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The char being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>char</code>.
     */
    public char extract_char () 
    {
         checkStoredType (org.omg.CORBA.TCKind._tk_char, "Monolithic Any#extract_char found wrong type stored");
        return ((Character)value).charValue ();
    }

    /**
     * Inserts a Java char (IDL <code>char</code>) into this Any,
     * setting the type of this Any to be representing an IDL
     * <code>char</code> and throwing out any previously held
     * value.
     * 
     * @param c char to set as value stored by this Any.
     */
    public void insert_char (char c)
    {
        type( orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_char) );
        value = new Character (c);
    }


    // wchar

    /**
     * Returns a Java char (IDL <code>wchar</code>) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The char being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>wchar</code>.
     */
    public char extract_wchar () 
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_wchar, "Monolithic Any#extract_wchar found wrong type stored");
        return ((Character)value).charValue ();
    }

    /**
     * Inserts a Java char (IDL <code>wchar</code>) into this Any,
     * setting the type of this Any to be representing an IDL
     * <code>wchar</code> and throwing out any previously held
     * value.
     * 
     * @param c char to set as value stored by this Any.
     */
    public void insert_wchar (char c)
    {
        type( orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_wchar) );
        value = new Character (c);
    }


    // octet

    /**
     * Returns a Java byte (IDL <code>octet</code>) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The byte being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>octet</code>.
     */
    public byte extract_octet () 
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_octet, "Monolithic Any#extract_octet found wrong type stored");
         return ((Byte)value).byteValue ();
    }

    /**
     * Inserts a Java byte (IDL <code>octet</code>) into this Any,
     * setting the type of this Any to be representing an IDL
     * <code>octet</code> and throwing out any previously held
     * value.
     * 
     * @param b byte to set as value stored by this Any.
     */
    public void insert_octet (byte b)
    {
        type( orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_octet) );
        value = new Byte (b);
    }


    // any

    /**
     * Returns an any object (IDL <code>any</code> type) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The Any object being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>any</code>.
     */
    public org.omg.CORBA.Any extract_any () 
    {
         checkStoredType (org.omg.CORBA.TCKind._tk_any, "Monolithic Any#extract_any found wrong type stored");
        return (org.omg.CORBA.Any)value;
    }

    /**
     * Inserts an Any object into this Any, setting the type of this Any
     * to be representing an IDL <code>any</code> and throwing out any
     * previously held value.
     * 
     * @param a org.omg.CORBA.Any object to set as value stored by this Any.
     */
    public void insert_any (org.omg.CORBA.Any a)
    {
        type( orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_any) );
        value = a;
    }


    // obj refs

    /**
     * Returns an object (IDL
     * <code>org.omg.CORBA.TCKind._tk_objref</code> type) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The CORBA object being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing a CORBA object.
     */
    public org.omg.CORBA.Object extract_Object ()
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_objref, "Cannot extract object");
        if (value instanceof CDROutputStream)
        {
           return create_input_stream().read_Object ();
        }
        return (org.omg.CORBA.Object)value;
    }

    /**
     * Inserts a CORBA object into this Any, setting the type of this
     * Any to be representing
     * <code>org.omg.CORBA.TCKind._tk_objref</code> and throwing out
     * any previously held value.
     * 
     * @param o org.omg.CORBA.Any object to set as value stored by this Any.
     */
    public void insert_Object (org.omg.CORBA.Object o)
    { 
        value = o;

        org.omg.CORBA.ORB orb = null;
        String typeId = null;
        String name = "";

        if( value == null )
        {
           orb = org.omg.CORBA.ORB.init();
           typeId = "IDL:omg.org/CORBA/Object:1.0";
           name = "Object";
        }
        else
        {            
           orb = ((org.omg.CORBA.portable.ObjectImpl)o)._orb();
           typeId = ((org.omg.CORBA.portable.ObjectImpl)o)._ids()[0];

           // check if the repository Id is in IDL format
           if (typeId.startsWith ("IDL:"))
           {
              // parse the name from the repository Id string
              name = typeId.substring (4, typeId.lastIndexOf (':'));
              name = name.substring (name.lastIndexOf ('/') + 1);
           }
        }
        typeCode = orb.create_interface_tc( typeId , name );
    }


    /**
     * Inserts a CORBA object into this Any, setting the type of this
     * Any to be representing the TypeCGde <code>type</code> and
     * throwing out any previously held value.
     * 
     * @param o org.omg.CORBA.Any object to set as value stored by this Any.
     * @param newType TypeCode representing the type; the Any will be set to represent TypeCode type
     */
    public void insert_Object (org.omg.CORBA.Object o, 
                               org.omg.CORBA.TypeCode newType)
    { 
        if( typeCode.kind().value() != org.omg.CORBA.TCKind._tk_objref )
            wrongTypeError("Illegal, non-object TypeCode!"); 

        type(newType);
        value = o;
    }



    // Bruce writes: One difference between the pluggable
    // implementation and the monolithic implementation is that the
    // monolithic implementation stores all types as objects in the
    // Any.  For instance, a short is stored as a java.lang.Short
    // object, a float is stored as a java.lang.Float object.  The
    // extract_Value() function for the monolithic implementation can
    // return any object, including a Short or Float or Int object.

    /**
     * Returns as an object anything stored in the any.
     * CDROutputStreams and CDRInputStreams are not treated specially.
     * A primitive type is returned inside the class that the
     * monolithic implementation stores it in.  E.g. a short is
     * returned as a java.lang.Short object, an int is returned as a
     * java.lang.Int object.  Following the OMG IDL to Java mapping
     * that ignores the IDL unsigned declaration, an IDL unsigned long
     * is returned as a java.lang.Int object.
     *
     * @return The object being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any is stored n't storing a CORBA object.
     */
    public java.io.Serializable extract_Value() 
        throws org.omg.CORBA.BAD_OPERATION
    {
        int kind = typeCode.kind().value();
        // Bruce noted that the documentation on
        // http://java.sun.com/products/jdk/1.2/docs/api/org/omg/CORBA/Any.html
        // said that extract_Value() should be able to return any type
        // of object, not just tk_value (which are valuetypes).

        if (kind == org.omg.CORBA.TCKind._tk_null)
            wrongTypeError ("Monolithic Any#extract_Value found wrong type stored");
        return (java.io.Serializable) value;
    }


    /**
     * Inserts any java.io.Serializable object into this Any, setting
     * the type of this Any to be representing
     * <code>org.omg.CORBA.TCKind._tk_objref</code> and throwing out
     * any previously held value.
     *
     * <p> Note that other ORB implementations that have interface
     * repositories may make use of the interface repository to
     * actually determine what type should go here; it doesn't just
     * set the type to tk_objref.  We always set the stored type to be
     * an object reference (TCKind.tk_objref).  Our pluggable
     * implementation has been changed to act the same way.
     * 
     * @param newValue org.omg.CORBA.Any object to set as value stored by this Any.
     */
    public void insert_Value(java.io.Serializable newValue)
    {
        if (newValue != null)
        {
            value    = newValue;

            //this.typeCode = edu.uci.ece.zen.orb.TypeCode.create_tc (value.getClass()); 
            type( new edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_objref) );
        }
        else
        {
            value    = null;
            //type( new edu.uci.ece.zen.orb.TypeCode (org.omg.CORBA.TCKind._tk_null) );
            type( edu.uci.ece.zen.orb.TypeCode.lookupPrimitiveTc(org.omg.CORBA.TCKind._tk_null) );
 
        }
    }

    /**
     * Inserts any java.io.Serializable object into this Any, setting
     * the type of this Any to be representing the parameter
     * <code>type</code> and throwing out any previously held value.
     * This method coulde be used to insert valuetypes or valuebox
     * types.
     * 
     * @param newValue org.omg.CORBA.Any object to set as value stored by this Any.
     * @param newType org.omg.CORBA.TypeCode for the type that the Any
     * should be storing.
     */
    public void insert_Value(java.io.Serializable newValue, org.omg.CORBA.TypeCode newType) 
        throws org.omg.CORBA.MARSHAL
    {
        type(newType);
        value    = newValue;
    }


    // string

    /**
     * Returns a Java string (IDL <code>string</code>) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The string being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>string</code>.
     */
    public String extract_string () 
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_string, "Monolithic Any#extract_string found wrong type stored");
        return value.toString ();
    }

    /**
     * Inserts a Java String (IDL <code>string</code>) into this Any,
     * setting the type of this Any to be representing an IDL
     * <code>string</code> and throwing out any previously held
     * value.
     * 
     * @param s string to set as value stored by this Any.
     */
    public void insert_string (String s)
    { 
        type( orb.create_string_tc (0) );
        value = s;
    }


    // wstring

    /**
     * Returns a Java string (IDL <code>wstring</code>) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The string being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>wstring</code>.
     */
    public String extract_wstring () 
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_wstring, "Monolithic Any#extract_wstring found wrong type stored");
        return value.toString ();
    }

    /**
     * Inserts a Java String (IDL <code>wstring</code>) into this Any,
     * setting the type of this Any to be representing an IDL
     * <code>wstring</code> and throwing out any previously held
     * value.
     * 
     * @param s string to set as value stored by this Any.
     */
    public void insert_wstring (String s)
    {
        type( orb.create_wstring_tc (0) );
        value = s;
    }


    // TypeCode

    /**
     * Returns a CORBA TypeCode object (IDL <code>TypeCode</code>) if it is
     * being stored by this Any, throwing an exception otherwise.
     * 
     * @return The typecode object being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>TypeCode</code>.
     */
    public org.omg.CORBA.TypeCode extract_TypeCode () 
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_TypeCode, "Monolithic Any#extract_TypeCode found wrong type stored");
        return (org.omg.CORBA.TypeCode) value;
    }

    /**
     * Inserts a CORBA TypeCode object (IDL <code>TypeCode</code>)
     * into this Any, setting the type of this Any to be representing
     * an IDL <code>TypeCode</code> and throwing out any previously held
     * value.
     * 
     * @param tc typecode object to set as value stored by this Any.
     */
    public void insert_TypeCode (org.omg.CORBA.TypeCode tc)
    {
        type(orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_TypeCode));
        value = tc;
    }


    // Principal (deprecated)

    /**
     * @deprecated by CORBA 2.2, the Principal data type is no longer supported.
     */
    public org.omg.CORBA.Principal extract_Principal ()
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("Monolithic Any#extract_principal method called, which has been a deprecated type since CORBA v2.3");
    }

    /**
     * @deprecated by CORBA 2.2, the Principal data type is no longer supported.
     */

    public void insert_Principal (org.omg.CORBA.Principal p)
    {
        throw new org.omg.CORBA.NO_IMPLEMENT ("Monolithic Any#insert_principal method called, which has been a deprecated type since CORBA v2.3");
    }


    // streamable

    /**
     * Returns a org.omg.CORBA.portable.Streamable object (IDL
     * <code>string</code>) if it is being stored by this Any,
     * throwing an exception otherwise.
     * 
     * @return The string being held by the any.
     * @throws org.omg.CORBA.BAD_INV_ORDER if this Any wasn't convertable to a Streamable object.
     */
    public org.omg.CORBA.portable.Streamable extract_Streamable()
        throws org.omg.CORBA.BAD_INV_ORDER
    {
        org.omg.CORBA.portable.Streamable retVal;
        try {
            retVal = (org.omg.CORBA.portable.Streamable) value;
        } 
        catch ( ClassCastException cce ) {
            throw new org.omg.CORBA.BAD_INV_ORDER("Monolithic Any#extract_Streamable method called when Any's stored value is not castable to streamable object");
        }
        return retVal;
    }

    /**
     * Sets the Any to store the streamable object <code>s</code>.
     * The type of the stored value is set to be
     * {@link org.omg.CORBA.portable.Streamable#_type() s._type()}
     * Non-primitive IDL types can be inserted using this method.
     * 
     * @param s streamable object to store in this Any.
     */
    public void insert_Streamable (org.omg.CORBA.portable.Streamable s)
    {
        type( s._type() );
        value = s;
    }


    // fixed

    /**
     * Returns a Java BigDecimal (IDL <code>fixed</code>) if it is
     * being stored by this Any, throwing an exception otherwise.
     *
     * @return The BigDecimal being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing an IDL type <code>fixed</code>.
     */
    public java.math.BigDecimal extract_fixed () 
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("Monolithic Any: extract_fixed is not supported due to type fixed not being supported");
    }

    /**
     * Inserts a Java BigDecicmal object (IDL <code>fixed</code>) into
     * the Any, setting the type of this Any to be <code>type</code>.
     *
     * @param _value BigDecimal object to set as the value stoyed by this Any.
     * @param type BigDecimal object to set as the value stored by this Any.
     */
    public void insert_fixed(java.math.BigDecimal _value,
                            org.omg.CORBA.TypeCode type) 
        // ??       throws org.omg.CORBA.BAD_INV_ORDER 
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("Monolithic Any: insert_fixed is not supported due to type fixed not being supported");
    }

    /**
     * Inserts a Java BigDecicmal object (IDL <code>fixed</code>) into
     * the Any, setting the type of this Any to be representing an IDL
     * <code>fixed</ocde> and throwing out any previously held value.
     *
     * @param _value BigDecimal object to set as the value stoyed by this Any.
     */
    public void insert_fixed (java.math.BigDecimal _value) 
    {
        throw new org.omg.CORBA.NO_IMPLEMENT("Monolithic Any: insert_fixed is not supported due to type fixed not being supported");
    }





    // ----------------------------------------------------------------------
    // Non-OMG methods.


    /** Direct Accessor for the object stored in the Any, used for debugging.
     * @return The object stored by the Any 
     */
    public java.lang.Object getValue() {
        return value;
    }

    /** Gets TCKind.value() integer stored in the any.
     * @return TCKind.value() integer stored in the any.
     */

    public int getKindValue() {
        return typeCode.kind().value();
    }

    /**
     * Sets the type stored by this Any to null and clears its stored
     * value.
     */
    public void setNull() {
        type ( orb.get_primitive_tc (org.omg.CORBA.TCKind.tk_void) );
        value = null;
    }
   


    /** Throws an org.omg.CORBA.BAD_OPERATION exception having the description s.
     * @param s The description of the exception. 
     * @throws org.omg.CORBA.BAD_OPERATION 
     */
    private void wrongTypeError (String s) {
        throw new org.omg.CORBA.BAD_OPERATION(s);
    }

    /** Checks if the TCKIND value is compatible with the type stored
     * in the Any, throwing a BAD_OPERATION exception if not.
     * @param value TCKind to cmpare the stared Any's type with.
     * @param s the description of error to throw otherwise. 
     */
    private void checkStoredType(int value, String s) {
        if (originalType().kind().value() != value) {
            wrongTypeError(s);
        }
    }


    // Bruce writes: This workaround that allows the direct extraction
    // of the object encapsulated by this Any, just in case we need it
    // for debugging or something.

    /**
     * Returns an object as a <code>java.lang.Object</code> if this
     * Any is storing a <code>org.omg.CORBA.TCKind._tk_objref</code>,
     * throwing an exception otherwise.
     *
     * <b>Note:</b> This is a workaround that allows the direct
     * extraction of the object encapsulated by the Any.
     * 
     * @return The object being held by the any.
     * @throws org.omg.CORBA.BAD_OPERATION if this Any isn't storing a CORBA object.
     */
    /* Not in CORBA 2.3
    public java.lang.Object extract_objref ()
    {
        checkStoredType (org.omg.CORBA.TCKind._tk_objref, "Monolithic Any#extract_objref found wrong type stored");
        return value;
    }
    */

    // We don't currently need such a method
    /** Returns true if <code>obj</code> is also an any and whose
     * contents are equal to this Any; returns false otherwise.
     *
     * @see edu.uci.ece.zen.orb.any.monolithic.Any#equal(org.omg.CORBA.Any)
     * @param obj The object to compare directly to this Any.  
     * @return true if <code>obj</code> is also an Any whose contents are equivalent to this Any.
     */
    /****b
    public boolean equals (java.lang.Object anObject) {
        if (anObject instanceof org.omg.CORBA.Any) {
            return equal( (org.omg.CORBA.Any) anObject);
        }
        else
            return false;
    }
    */

    /** 
     * Returns the hashcode of the value stored by the any.
     * @return Hashcode of the stored value.
     */
    /***b
    public int hashCode() {
        return value.hashCode();
    }
    */


}

