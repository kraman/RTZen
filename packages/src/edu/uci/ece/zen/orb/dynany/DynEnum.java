package edu.uci.ece.zen.orb.dynany;

// See CORBA v2.3 Spec page 9-14, section 9.2.5

/**
 * Subtype of DynAny that is used to dynamically parse and create Any
 * objects that represent Enums.
 *
 * @author Bruce Miller
 * @version $Revision: 1.1 $ $Date: 2004/01/21 00:52:59 $
 */

public class DynEnum extends DynAny implements org.omg.DynamicAny.DynEnum {

    /** The value of the enumeration, which is an int the CORBA to Java Mapping */
    private int enumerator;

    DynEnum ( org.omg.DynamicAny.DynAnyFactory factory, org.omg.CORBA.TypeCode tc, org.omg.CORBA.ORB _orb) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {

        super ( factory, tc, _orb, org.omg.CORBA.TCKind.tk_enum );
        
        try {
            enumerator = 0;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }



    /*
    private void copyMemberNames(org.omg.CORBA.TypeCode tc) {
        try {
            int count = tc.member_count();
            for (int i = 0; i < count; i++) {
                enumNames[i] = tc.member_name(i);
            }
        }
        // These exceptions should not happen, but are thrown by
        // member_name(), so I have to catch them.
        catch( org.omg.CORBA.TypeCodePackage.Bounds b ) {
            // should not happen
            b.printStackTrace();
        }
        catch( org.omg.CORBA.TypeCodePackage.BadKind bk ) {
            // should not happen anymore
            bk.printStackTrace();
        }
        
    }
    */

    

    /** 
     * @see DynAny#from_any(org.omg.CORBA.Any)
     */ 
    public void from_any(org.omg.CORBA.Any newValue)
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
               org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        
        // Check that typecodes are the equivalent.  If Typecodes are the
        // same, then labels are equivalent.
        checkAssign(type, newValue.type());

        org.omg.CORBA.portable.InputStream is = newValue.create_input_stream();
        
        enumerator = is.read_ulong();
        // Even though this type is equivalent with newValue's type,
        // copy over new Value's type so that the labels retrieved
        // from this DynAny via the get_as_string() method will be
        // from it.
        this.type = edu.uci.ece.zen.orb.TypeCode.originalType( newValue.type() );
        //copyMemberNames(type);
    }



    /** 
     * @see DynAny#to_any()
     */ 
    public org.omg.CORBA.Any to_any() {
        checkDestroyed();
        org.omg.CORBA.Any outAny = orb.create_any();
        outAny.type( DynAny.copyTypeCode(this.type) );
        org.omg.CORBA.portable.OutputStream os = outAny.create_output_stream();
        os.write_ulong( enumerator );
        return outAny;
    }



    /** 
     * @see DynAny#equal(org.omg.DynamicAny.DynAny)
     */ 
    public boolean equal( org.omg.DynamicAny.DynAny dyn_any ) {
        checkDestroyed();
        if( !type().equal( dyn_any.type())) {
            return false;
        }
        
        return ((edu.uci.ece.zen.orb.dynany.DynEnum) dyn_any).enumerator == this.enumerator;

    }



    // Doesn't need to be overridden
    // public destroy()

    // Doesn't need to be overridden
    // public copy()

    // Doesn't need to be overridden
    // public getRepresentation()



    public org.omg.DynamicAny.DynAny current_component() {
        return this;
    }


    /** Return the value of the DynEnum as an IDL Identifier
     * See CORBA v2.3 Spec p 9-15
     * @return the value of the DynEnum as an IDL Identifier
     */
    public String get_as_string() {
        checkDestroyed();
        try {
            return this.type.member_name(enumerator);
        }
        // These shouldn't happen, but BadKind and Bounds can be thrown by the member_name method
        catch (Exception e) {
            ZenProperties.logger.log(Logger.SEVERE, getClass(), "get_as_string()", e);
        }
        return null;
    }


    /** Sets the value of the DynEnum to the enumeration value whose
     * label is soughtStr.
     *
     * @param soughtStr string whose enumeration value is desired to be set to
     */
    public void set_as_string(java.lang.String soughtStr) 
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        try {
            int numEnumElements = type.member_count();
            // Find the label with the name soughtStr
            for (int i = 0; i < numEnumElements; i++) {
                if ( soughtStr.equals( type.member_name(i) ) ) {
                    enumerator = i;
                    return;
                }
            }
        }
        catch (Exception ex) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue("Unable to set DynEnum");
        }
        // If soughtStr could not be found
        throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue("Unable to set DynEnum with identifier " + soughtStr);
    }


    /** 
     * Get the value of the enumeration, as an integer.
     */
    public int get_as_ulong() {
        checkDestroyed();
        return enumerator;
    }


    /**
     * Set the value stored in the enumeration.
     *
     * @param newv value to set the enumeration to store.
     */ 
    public void set_as_ulong(int newv) 
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        try {
            checkDestroyed();
            if ( newv < 0 || newv >= type.member_count() ) {
                throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue("Set index out of bounds " + newv);
            }
            enumerator = newv;
        }
        // For member_count method, should never happen
        catch (org.omg.CORBA.TypeCodePackage.BadKind bke) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue("Unable to set DynEnum");
        }
    }


}
