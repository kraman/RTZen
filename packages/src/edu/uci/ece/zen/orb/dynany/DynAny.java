package edu.uci.ece.zen.orb.dynany;

/**
 * DynAny is the base type to provide DynamicAny support.  DynamicAny
 * support allows a recipient of a TypeCode to read, process, and even
 * construct an Any value without having a local implementation of it,
 * simply by using its typecode.
 *
 * <p>
 * It is described in chapter 9 of the OMG CORBA v2.3 Spec.
 *
 * <p>
 * Descendants must override:
 * from_any
 * to_any
 * equal
 * destroy
 * copy
 * getRepresentation
 * current_component
 *
 * @author Bruce Miller
 * @version $Revision: 1.2 $ $Date: 2004/01/29 20:47:18 $
 */

import org.omg.DynamicAny.*;

public class DynAny extends org.omg.CORBA.LocalObject implements org.omg.DynamicAny.DynAny {


    // This class represents empty exceptions and simple types.

    /** Position within list of Dynamic Anys
     */
    protected int position = -1;

    /** Total number of components that this Dynamic Any has.
     */
    protected int componentCount = 0;

    // True if this is DynAny is a component, false otherwise.
    protected boolean isComponent = true;

    /** Reference to the orb
     */
    protected org.omg.CORBA.ORB orb;

    /** TypeCode for the DynAny
     */
    org.omg.CORBA.TypeCode type;


    protected org.omg.DynamicAny.DynAnyFactory dynAnyFactory;

    org.omg.CORBA.Any anyRepresentation = null;


    private DynAny() {
        position = -1;
        componentCount = 0;
    }


   DynAny( org.omg.DynamicAny.DynAnyFactory aFactory,  
           org.omg.CORBA.TypeCode tc,
           org.omg.CORBA.ORB anOrb)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
       this();
       this.dynAnyFactory = aFactory;
       this.type = edu.uci.ece.zen.orb.TypeCode.originalType( tc );
       this.orb = anOrb;
       anyRepresentation = orb.create_any();
       setDefaultValue(type);
   }

   DynAny( org.omg.DynamicAny.DynAnyFactory aFactory,  
           org.omg.CORBA.TypeCode tc,
           org.omg.CORBA.ORB anOrb, 
           org.omg.CORBA.TCKind checkTckind)
       throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
       this(aFactory, tc, anOrb);
       
       org.omg.CORBA.TypeCode origTC = edu.uci.ece.zen.orb.TypeCode.originalType(tc);
       if (origTC.kind().value() != checkTckind.value()) {
           throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
       }
   }



    /** Return the typecode associated with this DynAny object. (CORBA v2.3 Spec page 9-10)
     * @return TypeCode associated with this DynAny.
     */
    public org.omg.CORBA.TypeCode type() {
        return type;
    }



    /**
     * Checks whether the dynamic Any is destroyed, and throws a
     * OBJECT_NOT_EXIST exception if it is.  See CORBA v2.3 Spec p
     * 9-2.
     */
    void checkDestroyed() {
        if (type == null) {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST("Method invoke on destroyed dynamic Any");
        }
    }



    /**
     * Checks that the the typecode thisTc is equivalent to otherTc,
     * throwing a TypeMismatch exception if they are not.
     *
     * @param thisTc the TypeCode of one DynAny
     * @param otherTc the TypeCode of another DynAny
     * @throws TypeMismatch if the types don't match up
     */
    public static void checkAssign(org.omg.CORBA.TypeCode thisTc, org.omg.CORBA.TypeCode otherTc) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {

        if ( ! thisTc.equivalent( otherTc ) ) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("Unable to assign DynAny from one of another Type");
        }

    }



    /** 
     * Initializes this DynAny to the value passed in, but the Types
     * of both must match.
     */
    // Page 9-10
    // Descendants need not override
    public void assign(org.omg.DynamicAny.DynAny assignFrom) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {

        // checkDestroyed will be called by the from_any method
        //checkDestroyed();
        
        edu.uci.ece.zen.orb.dynany.DynAny assignFromDA = (edu.uci.ece.zen.orb.dynany.DynAny)  assignFrom;
        try {
            checkAssign(this.type(), assignFrom.type());
            this.from_any( assignFrom.to_any() );
        }
        catch (org.omg.DynamicAny.DynAnyPackage.InvalidValue iv) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("InvalidValue caught in assignFrom");
            // Shouldn't happen.
        }

    }



    // Descendants must override
    /**
     * Initialize the value stored in this DynAny to that stored in
     * <code>value</code>.  The way the spec defines it, it is not
     * necessary to make a copy of the value, but can assign it
     * directly.
     *
     * <p> 
     * Descendants must override.  See OMG CORBA v2.3 Spec Page 9-11
     *
     * @param value Any from which to set this' stored value
     * @throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch if the type stored in <code>value</code> is not eqivalent to that stored in this
     * @throws org.omg.DynamicAny.DynAnyPackage.InvalidValue if the passed Any does not store a legal value
     */
    public void from_any(org.omg.CORBA.Any value) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkDestroyed();

        checkAssign(this.type(), value.type());

        this.type = edu.uci.ece.zen.orb.TypeCode.originalType( value.type() );

        // make anyRepresentation be a copy of value.
        //anyRepresentation = (edu.uci.ece.zen.orb.any.Any) orb.create_any();
        //anyRepresentation.read_value(value.create_input_stream(), this.type);
        anyRepresentation = value;

        /*
        componentCount = type().member_count();

        members = new org.omg.DynamicAny.NameValuePair[componentCount];
        
        org.omg.CORBA.portable.InputStream is = value.create_input_stream();
        
        if (type.kind().value() == org.omg.CORBA.TCKind._tk_except ) {
            asExceptionStr = is.read_string();
        }
        */
            /*            
            for (int i = 0 ; i < componentCount; i++) {

                org.omg.CORBA.Any iAny = orb.create_any();
                iAny.read_value(is, type.member_type(i));
                asExceptionMembers[i] = new org.omg.DynamicAny.NameValuePair (type().member_name(i), iAny);
            }
            */

        componentCount = 0;
        position = -1;

    }



    // Descendants must override.
    /**
     * Create an Any from the value stored by this DynAny object,
     * assigning it a <b>copy</b> of this TypeCode.
     * 
     * <p>
     * Descendants must override.  See CORBA Spec Page 9-11
     *
     * @return Any initialized to the value stored in this DynAny
     */
    public org.omg.CORBA.Any to_any() {
        checkDestroyed();

        org.omg.CORBA.Any retVal = orb.create_any();

        // Create a copy of the typecode to return
        org.omg.CORBA.TypeCode tcCopy = copyTypeCode(this.type);

        retVal.read_value(anyRepresentation.create_input_stream(), tcCopy );

        return retVal;
    }



    /**
     * Create a duplicate of the TypeCode <code>tc</code>.
     *
     * @return TypeCode duplicate of this TypeCode.
     */
    public static org.omg.CORBA.TypeCode copyTypeCode(org.omg.CORBA.TypeCode tc) {
        edu.uci.ece.zen.orb.CDROutputStream cdrOut = edu.uci.ece.zen.orb.CDROutputStream.instance();
        cdrOut.write_TypeCode(tc);
        edu.uci.ece.zen.orb.CDRInputStream cdrIn = (edu.uci.ece.zen.orb.CDRInputStream) cdrOut.create_input_stream();
        return cdrIn.read_TypeCode();

    }



    // Descendants must override.
    /**
     * Compare two DynAny values for equality.  Equality means having
     * equivalent TypeCodes and equal stored values.
     *
     * <p>
     * Descendants must override.  See CORBA Spec page 9-11.
     */
    public boolean equal(org.omg.DynamicAny.DynAny dynany) {
        return equal(dynany, 1);
        /*
        checkDestroyed();
        if ( ! type().equivalent( dynany.type() ) ) {
            return false;
        } 
        return this.anyRepresentation().equal( dynany.anyRepresentation() );
        */
    }


    // numComponentsAssumed is the number of components to assume that
    // this and dyn_any have and is based on what getRepresentation()
    // returns for a particular type.  It should be 1 for generate
    // DynAny andDynEnum.  It should by component_count() for
    // DynArray.
    protected boolean equal( org.omg.DynamicAny.DynAny otherAny, int numComponentsAssumed ) {
        checkDestroyed ();

        edu.uci.ece.zen.orb.dynany.DynAny dynAny = (edu.uci.ece.zen.orb.dynany.DynAny) otherAny;

        if( !type().equivalent( otherAny.type())  ) {
            return false;
        }
        
        boolean isEqual = true;
        int oldThisPos = this.position;
        int oldDynAnyPos = dynAny.position;
        this.rewind();
        otherAny.rewind();

        for (int i = 0; i < numComponentsAssumed; i++) {
            if ( ! this.getRepresentation().equal( dynAny.getRepresentation() ) ) {
                isEqual = false;
                break;
            }
            this.next();
            otherAny.next();
        }

        this.seek(oldThisPos);
        otherAny.seek(oldDynAnyPos);
        return isEqual;

    }




    // Descendants must override.
    /**
     * Destroys a DynAny object, setting resources used by it to be
     * freed.  According to the Spec, destroy() must be invoked on
     * DynAny refereneces obtained from creation operations on the ORB
     * interface and references returned by the DynAny.copy() method
     * in order to avoid resource leaks, but Java has garbage
     * collection.
     *
     * Destroying a DynAny that is not a component itself destroys it
     * and all of its components.  Calling Destroy on a DynAny that is
     * a component does nothing.
     *
     * <p>
     * Descendants must override.  See CORBA Spec page 9-11.
     */
    public void destroy() {
        checkDestroyed();

        // Only Anys that are not contained as components can be destroyed.
        if ( ! isComponent) {
            this.type = null;
            anyRepresentation = null;
        }
    }



    // Descendants must override.
    /**
     * Create a new DynAny object that is a deep copy of this DynAny. 
     *
     * <p>
     * Descendants must override.  See CORBA Spec page 9-12.
     * @return DynAny object that is a copy of this DynAny
     */
    public org.omg.DynamicAny.DynAny copy() {
        checkDestroyed();
        try {
            edu.uci.ece.zen.orb.dynany.DynAny da = (edu.uci.ece.zen.orb.dynany.DynAny) dynAnyFactory.create_dyn_any( this.to_any() );
            da.isComponent = false;
            return da;
        }
        // Shouldn't happen
        catch (org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode it) {
            it.printStackTrace();
            return null;
        }
    }



    /** Return the currently pointed to component or representation
     * being pointed to by position in this DynAny.  Overridden by
     * descendents.
     *
     * <p>
     * @return Any the value stored by this DynAny.
     */
    protected org.omg.CORBA.Any getRepresentation() {
        return anyRepresentation;
    }



    /**
     * Check that this DynAny is not destroyed, it's type matches the
     * type being inserted, and its position is appropriate.
     *
     * <p>
     * See CORBA v2.3 Spec page 9-12, section 9.2.3.8
     *
     * @param insertKindVal TCKind integer constant for type to be inserted
     */
    protected void checkInsertionAndTypes (int insertKindVal )
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
        org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkDestroyed();

        int thisKindVal = getRepresentation().type().kind().value();
        if (thisKindVal != insertKindVal) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("Trying to insert kind " + insertKindVal + " into " + thisKindVal);
        }

        if ( componentCount > 0 && (position < 0 || position > componentCount) ) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue("Attempting to Insert and not currently pointing at a stored element");
        }

    }

    /**
     * Inserts a boolean value into the representation stored by this
     * DynAny.  If this DynAny has components, but the position is not
     * pointing at a component, throws InvalidValue exception.  If the
     * DynAny stores a type other than the type being inserted
     * (boolean), throws a TypeMismatch exception.
     */
    public void insert_boolean( boolean value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_boolean);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_boolean(value);
    }

    public void insert_octet( byte value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_octet);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_octet(value);
    }

    public void insert_char( char value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_char);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_char(value);
    }

    public void insert_short( short value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_short);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_short(value);
    }

    public void insert_ushort( short value )
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_ushort);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_ushort(value);
    }

    public void insert_long( int value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_long);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_long(value);
    }

    public void insert_ulong( int value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_ulong);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_ulong(value);
    }

    public void insert_float( float value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_float);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_float(value);
    }

    public void insert_double( double value )
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_double);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_double(value);
    }


    /*
     * If this' stored string is bounded and the inserted string
     * exceeds this' bound length, then the InvalidValue exception is
     * thrown.
     *
     * @param value String to be inserted.
     */
    public void insert_string( String value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        try {
            checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_string);
            if ( (this.type().length() != 0) && (value.length() > this.type().length()) ) {
                throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue("Inserted string too long");
            }
            // Otherwise, insertion is safe.
            org.omg.CORBA.Any insertPoint = this.getRepresentation();
            insertPoint.insert_string(value);
        }
        // Never happens, for type.length() method
        catch ( org.omg.CORBA.TypeCodePackage.BadKind bke) {
            bke.printStackTrace();
        }
    }

    public void insert_reference( org.omg.CORBA.Object value )
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_objref);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_Object(value);
    }

    public void insert_typecode( org.omg.CORBA.TypeCode value )
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_TypeCode);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_TypeCode(value);
    }

    public void insert_longlong( long value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_longlong);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_longlong(value);
    }

    public void insert_ulonglong( long value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_ulonglong);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_ulonglong(value);
    }

    public void insert_wchar( char value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_wchar);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_wchar(value);
    }


    /*
     * If this' stored string is bounded and the inserted string
     * exceeds this' bound length, then the InvalidValue exception is
     * thrown.
     *
     * @param value String to be inserted.
     */
    public void insert_wstring( String value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        try {
            checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_wstring);
            if ( value.length() > this.type().length() ) {
                throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue("Inserted string too long");
            }
            org.omg.CORBA.Any insertPoint = this.getRepresentation();
            insertPoint.insert_wstring(value);
        }
        // Never happens, for type.length() method
        catch ( org.omg.CORBA.TypeCodePackage.BadKind bke) {
            bke.printStackTrace();
        }
    }

    public void insert_any( org.omg.CORBA.Any value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_any);
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_any(value);
    }

    public void insert_dyn_any( org.omg.DynamicAny.DynAny value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_any( value.to_any() );
    }

    public void insert_val( java.io.Serializable value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {

        if ( value instanceof org.omg.CORBA.portable.StreamableValue ||
             value instanceof org.omg.CORBA.portable.CustomValue ) {
            type = orb.get_primitive_tc ( org.omg.CORBA.TCKind.tk_value );
            checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_value);
        }
        else if (value instanceof org.omg.CORBA.portable.ValueBase) {
            type = orb.get_primitive_tc ( org.omg.CORBA.TCKind.tk_value_box );
            checkInsertionAndTypes(org.omg.CORBA.TCKind._tk_value_box);
        }
        else {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("Unable to match value with a boxed or non-boxed value type");
        }
        org.omg.CORBA.Any insertPoint = this.getRepresentation();
        insertPoint.insert_Value(value);
    }



    /**
     * Extracts as a boolean the value stored in the representation by
     * this DynAny or in the representation of the component that it
     * currently points to.
     *
     * @return boolean of stored value of this or this' component
     */
    public boolean get_boolean() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        boolean it = extractPoint.extract_boolean();
        return it;
    }

    public byte get_octet() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        byte it = extractPoint.extract_octet();
        return it;
    }

    public char get_char() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        char it = extractPoint.extract_char();
        return it;
    }

    public short get_short() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        short it = extractPoint.extract_short();
        return it;
    }

    public short get_ushort() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        short it = extractPoint.extract_ushort();
        return it;
    }

    public int get_long() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        int it = extractPoint.extract_long();
        return it;
    }

    public int get_ulong() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        int it = extractPoint.extract_ulong();
        return it;
    }

    public float get_float() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        float it = extractPoint.extract_float();
        return it;
    }

    public double get_double() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        double it = extractPoint.extract_double();
        return it;
    }

    public String get_string() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        String it = extractPoint.extract_string();
        return it;
    }

    public org.omg.CORBA.Object get_reference() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        org.omg.CORBA.Object it = extractPoint.extract_Object();
        return it;
    }

    public org.omg.CORBA.TypeCode get_typecode() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        org.omg.CORBA.TypeCode it = extractPoint.extract_TypeCode();
        return it;
    }

    public long get_longlong() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        long it = extractPoint.extract_longlong();
        return it;
    }

    public long get_ulonglong() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        long it = extractPoint.extract_ulonglong();
        return it;
    }

    public char get_wchar() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        char it = extractPoint.extract_wchar();
        return it;
    }

    public java.lang.String get_wstring() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed ();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        String it = extractPoint.extract_wstring();
        return it;
    }

    public org.omg.CORBA.Any get_any() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed ();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        org.omg.CORBA.Any it = extractPoint.extract_any();
        return it;
    }

    public org.omg.DynamicAny.DynAny get_dyn_any() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        org.omg.CORBA.Any extractPoint = this.getRepresentation();
        try {
            org.omg.DynamicAny.DynAny it = dynAnyFactory.create_dyn_any( get_any() );
            return it;

        }
        catch (org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode it) {
            it.printStackTrace();
        }
        return null;
    }

    /**
     * Like {@link #get_boolean()} but for value type.
     */
    public java.io.Serializable get_val() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed ();
        if ( anyRepresentation.type().kind().value() == org.omg.CORBA.TCKind._tk_value ||
             anyRepresentation.type().kind().value() == org.omg.CORBA.TCKind._tk_value_box) {
            org.omg.CORBA.Any extractPoint = this.getRepresentation();
            java.io.Serializable it = extractPoint.extract_Value();
            return it;
        }
        throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("Value Type not found in DynAny");
    }


    /**
     * Increment <b>position</b>, the pointer to the component
     * currently being pointed to, returning true if successful or
     * returning false and setting position to -1 if no further
     * component exists.  See CORBA Spec page 9-13.
     */
    public boolean next() {
        return seek( ++position );
    }



    /**
     * Set <b>position</b>, the pointer to the component currently
     * being pointed to, to <code>seekIndex</code> returning true if
     * successful or returning false and setting position to -1 if no
     * component exists at that location.  See CORBA Spec page 9-13.
     * If there is no component, sets position to -1 and returns false.
     */
    public boolean seek(int seekIndex) {
        checkDestroyed();
        if ( (seekIndex < componentCount) && (seekIndex >= 0) ) {
            position = seekIndex;
            return true;
        }
        else {
            position = -1;
            return false;
        }
    }


    /**
     * Set to 0 the <b>position</b>, the pointer to the component currently
     * being pointed to.
     * See CORBA Spec page 9-13.
     */
    public void rewind() {
        // Seek will checkDestroyed() and make sure that the DynAny
        // has at least one component.
        seek(0);
    }



    /**
     * Return the number of components in this DynAny.  See CORBA v2.3
     * Spec p 9-13.
     * @return the number of components in this DynAny.
     */
    public int component_count() {
        checkDestroyed();
        return componentCount;
    }



    // Descendants must override
    /*
     * Retrieve the component at the position currently held in this
     * DynAny.
     * 
     * <p>
     * Descendants must override.  See CORBA v2.3 Spec Page 9-14
     */
    public org.omg.DynamicAny.DynAny current_component() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        /* Exception code moved to DynStruct
        // If this DynAny is for an exception
        if (this.type().kind().value() == org.omg.CORBA.TCKind._tk_except) {
            // If the exception has no members
            if (componentCount == 0) {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("DynAny#current_component() called on DynAny without components");
            }
            else {
                return asExceptionMembers[position];
            }
        }
        */
        throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("DynAny#current_component() called on DynAny without components");
    }



    /**
     * Sets the representation in this as a simple DynAny object to be
     * the default value for the given TypeCode <code>forType</code>.
     *
     * @param forType TypeCode to use when deciding what to set this to as a default value.
     * @return true if successfully set, false otherwise.
     * @throws TypeMismatch if forType isn't equivalent to the type of this.
     */
    private boolean setDefaultValue(org.omg.CORBA.TypeCode forType) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        
        // The Any's insert methods set the typecode of the Any
        // itself.  The typecode of this is set now.

        switch (forType.kind().value()) {
        case org.omg.CORBA.TCKind._tk_boolean:
            anyRepresentation.insert_boolean(false);
            break;
        case org.omg.CORBA.TCKind._tk_short:
            anyRepresentation.insert_short( (short) 0 );
            break;
        case org.omg.CORBA.TCKind._tk_ushort:
            anyRepresentation.insert_ushort( (short) 0 );
            break;
        case org.omg.CORBA.TCKind._tk_long:
            anyRepresentation.insert_long( 0 );
            break;
        case org.omg.CORBA.TCKind._tk_double:
            anyRepresentation.insert_double( 0 );
            break;
        case org.omg.CORBA.TCKind._tk_ulong:
            anyRepresentation.insert_ulong( 0 );
            break;
        case org.omg.CORBA.TCKind._tk_longlong:
            anyRepresentation.insert_longlong( 0 );
            break;
        case org.omg.CORBA.TCKind._tk_ulonglong:
            anyRepresentation.insert_ulonglong( 0 );
            break;
        case org.omg.CORBA.TCKind._tk_float:
            anyRepresentation.insert_float( 0 );
            break;
        case org.omg.CORBA.TCKind._tk_char:
            anyRepresentation.insert_char( (char) 0);
            break;
        case org.omg.CORBA.TCKind._tk_wchar:
            anyRepresentation.insert_wchar((char) 0);
            break;
        case org.omg.CORBA.TCKind._tk_octet:
            anyRepresentation.insert_octet((byte) 0);
            break;
        case org.omg.CORBA.TCKind._tk_string:
            anyRepresentation.insert_string("");
            break;
        case org.omg.CORBA.TCKind._tk_wstring:
            anyRepresentation.insert_wstring ("");
            break;
        case org.omg.CORBA.TCKind._tk_objref:
            anyRepresentation.insert_Object(null);
            break;
        case org.omg.CORBA.TCKind._tk_TypeCode:
            anyRepresentation.insert_TypeCode( orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_null) );
            break;
        case org.omg.CORBA.TCKind._tk_any:
            org.omg.CORBA.Any a = orb.create_any();
            anyRepresentation.insert_any( a );
            break;

            // These typecodes have no associated value but are still okay as defaults
        case org.omg.CORBA.TCKind._tk_null:
        case org.omg.CORBA.TCKind._tk_void:
            break;
        default:
            return false;
        }
        anyRepresentation.type(forType);

        return true;

    }



    /**
     * Print display an error message and stack trace, called in
     * response to an exception being thrown.
     *
     * @param e Exception whose mesage and stack trace will be printed.
     */
    protected void handleShouldntHappenException(Exception e) {
        System.out.println("An exception occurred indicating an internal error that should be fixed: " + e.toString());
        e.printStackTrace();
    }




}
