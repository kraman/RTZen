//
// $Id: TypeCode.java,v 1.2 2004/01/20 21:55:06 bmiller Exp $
//

package edu.uci.ece.zen.orb;


import org.omg.CORBA.TCKind;

/**
 * ZEN implementation of CORBA TypeCodes.
 *
 * <p> TypeCodes are used by Anys to keep track of the type being
 * stored and by the Inteface Repository.  ZEN's implentation of
 * pseudo-IDL interface TypeCode is affected by the aspectization of
 * TypeCodes.  
 *
 * <p> Methods and variables that are common to both the minimal and
 * the full implementations of TypeCodes are kept in this file.  See
 * the files <code>TypeCodeMinimalAspect.java</code> and
 * <code>TypeCodeAspect.java</code> for the minimal and full
 * implementations of TypeCodes.  They contain alternative
 * implementations of certain methods.
 *
 * <p> If in the course of maintaining this code, you change an
 * accessor method, you will also probably have to change the methods
 * named equal and equivalent.  I took a shortcut by having them
 * access member fields directly instead of using the accessor
 * methods.
 *
 * @author Krishna Ramen
 * @author Bruce Miller
 * @version $Revision: 1.2 $ $Date: 2004/01/20 21:55:06 $
 */


public class TypeCode extends org.omg.CORBA.TypeCode {
    // = TITLE
    //   ZEN's TypeCode class
    //
    // = DESCRIPTION
    //
    static TypeCode[] primitive_type_codes = new TypeCode[33];
    static {
        // Establish _tk_null, _tk_void, then tk_short through
        // _tk_TypeCode and _tk_Principal.
        for (int i = 0; i < 14; i++) 
            primitive_type_codes[i] = new TypeCode(i);
        // Establish _tk_longlong through _tk_value.
        for (int i = 23; i < 29; i++)
            primitive_type_codes[i] = new TypeCode(i);
    }

    /**
     * Constructs a new TypeCode whose kind references to integer
     * constants in {@link org.omg.CORBA.TCKind org.omg.CORBA.TCKind}
     *
     * @param _kind kind of TypeCode to create.  From org.omg.TCKind.
     */
    public TypeCode(int _kind) {
        kind = _kind;
    }



    /**
     * Constructor for Struct typecode and Exception TypeCode
     *
     * @param _kind TCKind._tk_struct for struction, _tk_except for exception
     * @param _id String in the interface repository globally identifying this type.
     * @param _name Simple name identifying this enumeration in its enclosing scope
     * @param _members org.omg.CORBA.StructMember objects representing members of the Struct or Exception
     */
    public TypeCode(int _kind, java.lang.String _id, java.lang.String _name, org.omg.CORBA.StructMember[] _members) {
        kind = _kind;
        id = _id;
        name = _name;
        member_count = _members.length;
        member_names = new String[member_count];
        member_types = new TypeCode[member_count];
        for (int i = 0; i < member_count; i++) {
            member_names[i] = _members[i].name;
            member_types[i] = (TypeCode) _members[i].type;
        }
    }    


    
    // Constructor for Union type code
    public TypeCode(java.lang.String _id, 
                    java.lang.String _name,
                    org.omg.CORBA.TypeCode _discriminator_type,
                    org.omg.CORBA.UnionMember[] _members) {
        kind = org.omg.CORBA.TCKind._tk_union;
        id = _id;
        name = _name;
        discriminator_type = (TypeCode) _discriminator_type;
        default_index = -1;  // represents no default member
        
        member_count = _members.length;
        member_names = new String[member_count];
        member_labels = new edu.uci.ece.zen.orb.any.Any[member_count];
        member_types = new TypeCode[member_count];
        member_idlTypes = new org.omg.CORBA.IDLType[member_count];
        for (int i = 0; i < member_count; i++) {
            member_names[i] = _members[i].name;
            member_types[i] = (TypeCode) _members[i].type;
            member_labels[i] = (edu.uci.ece.zen.orb.any.Any) _members[i].label;
            member_idlTypes[i] = (org.omg.CORBA.IDLType) _members[i].type_def;
            
        }
    }



    /**
     * Constructor for Enum TypeCode
     * 
     * @param _id String in the interface repository globally identifying this type.
     * @param _name Simple name identifying this enumeration in its enclosing scope
     * @param _members enumeration name constants for each member of the enumeration.
     */
    public TypeCode(java.lang.String _id,
                    java.lang.String _name,
                    java.lang.String[] _members) {
        kind = org.omg.CORBA.TCKind._tk_enum;
        id = _id;
        name = _name;
        member_count = _members.length;
        member_names = new String[member_count];
        for (int i = 0; i < member_count; i++) {
            member_names[i] = _members[i];
        }
    }



    /**
     * Constructor for alias TypeCode.  Creates a alias TypeCode with
     * the name <code>_name</code> and being an alias for the original
     * type of <code>_original_type</code>.
     *
     * @param _id String identifying the package and class of the alias,
     * e.g."IDL:test/anytests/AnyTypesPackage/long_array:1.0"
     * @param _name String, IDL name for the alias e.g. "long_array"
     * @param _original_type Type that the alias refers to e.g. an
     * array typecode.
     */
    public TypeCode(java.lang.String _id,
                    java.lang.String _name,
                    org.omg.CORBA.TypeCode _original_type) {
        kind = org.omg.CORBA.TCKind._tk_alias;
        id = _id;
        name = _name;
        type = _original_type;
    }



    /**
     * Constructor for Interface TypeCode.  The kind will be set to
     * <code>TCKind._tk_abstract_interface</code>.
     * 
     * @param _id String identifying package and class of the interface.
     * @param _name String giving IDL name for the interface.
     */
    // Peter: I changed the code below to the following.  I am not sure that
    // it is correct but a call to create_interface_tc passes the id and name 
    // which could be either a Object or AbstractBase.  I asked Bruce to look
    // at this and confirm its accuracy since I haven't read the specification
    public TypeCode(java.lang.String _id,
                    java.lang.String _name) {

        if (_name.indexOf("AbstractBase") != -1) {
            kind = TCKind._tk_abstract_interface;
        }
        else if (_name.indexOf("Object") != -1) {
            kind = TCKind._tk_objref;
        }
        // Bruce discovered on 2004-04-30 that the default is to make something an Object reference
        /*
        else {
            System.out.println("Unrecognized.  _name " + _name);
            System.out.println("_id " + _id);
            throw new org.omg.CORBA.NO_IMPLEMENT("Unrecognized name for " + _name);
        }
        */
        else {
            kind = TCKind._tk_objref;
        }
        // kind = TCKind._tk_abstract_interface;
        id = _id;
        name = _name;
    }



    /* in the aspect file, TypeCodeAspect.aj
    public static TypeCode create_tc(Class cls) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    */



    /** Contructor for sequence and array TypeCode
     * 
     * @param bound Maximum number of elements in sequence or array, 0 if unbounded
     * @param element_type type of elements stored in the sequence or array
     */
    public TypeCode(int bound,
                    org.omg.CORBA.TypeCode element_type) {
        kind = TCKind._tk_sequence;
        length = bound;
        type = element_type;
    }
        
    /** 
     * Constructor for recursive Sequence TypeCode
     * 
     * @param bound maximum length of sequence, or 0 is unbounded
     * @param offset Offset
     * @deprecated since CORBA v2.3, see page 10-52 of CORBA v2.3 Spec (that's section 10.7.3)
     */
    public TypeCode(int bound,
                    int offset) {
        kind = TCKind._tk_sequence;
        length = bound;
        this.offset = offset;
        // what does offset get stored to?
    }




    /*
     // This has not been tested because the Interface Repository
     // has not been implemented yet
     // There's a problem here with a method signature (name and parameters) that duplicates the
     // one for the interface TypeCode
     // Constructor for native TypeCode
     public TypeCode(java.lang.String _id,
     java.lang.String _name)
     {
     kind = TCKind._tk_native;
     id = _id;
     name = _name;
     }


 
   /** 
     * Constructor for fixed TypeCode
     * <p>
     * Fixed Typecode has not been tested because the Interface Repository
     * has not been implemented yet
     *
     * @param _digits the number of digits that the number will have.
     * @param _scale number of digits that are to the right of the decimal point.
     */
    public TypeCode(short _digits,
                    short _scale) {
        kind = TCKind._tk_fixed;
        digits = _digits;
        scale = _scale;
    }



    // This has not been tested because the Interface Repository
    // has not been implemented yet
    // Constructor for value TypeCode
    public TypeCode(java.lang.String _id,
                    java.lang.String _name,
                    short _type_modifier,
                    org.omg.CORBA.TypeCode concrete_base,
                    org.omg.CORBA.ValueMember[] _members) {
        kind = TCKind._tk_value;
        id = _id;
        name = _name;
        type_modifier = _type_modifier;
        type = concrete_base;
        member_count = _members.length;
        member_names = new String[member_count];
        member_ids = new String[member_count];
        member_defined_ins = new String[member_count];
        member_versions = new String[member_count];
        member_types = new TypeCode[member_count];
        member_idlTypes = new org.omg.CORBA.IDLType[member_count];
        member_visibility = new short[member_count];

        for (int i = 0; i < member_count; i++) {
            member_names[i] = _members[i].name;
            member_ids[i] = _members[i].id;
            member_defined_ins[i] = _members[i].defined_in;
            member_versions[i] = _members[i].version;
            member_types[i] = (TypeCode) _members[i].type;
            member_idlTypes[i] = (org.omg.CORBA.IDLType) _members[i].type_def;
            member_visibility[i] = (short) _members[i].access;
        } 
    }    







    /**
     * Constructor for recursive TypeCode.  kind will be set to
     * <code>TCKind._tk_sequence</code>.
     *
     * <p>
     * This has not been tested because the Interface Repository
     * has not been implemented yet
     * Constructor for recursive TypeCode
     *
     * @param _id String identifying package and class of the interface.
     */
    public TypeCode(java.lang.String _id) {
        kind = TCKind._tk_sequence;
        id = _id;
    }

    /*
     // This has not been tested because the Interface Repository
     // has not been implemented yet

     // There's a problem here with a profile that duplicates the
     // one for the alias TypeCode
     // Constructor for value box TypeCode
     public TypeCode(java.lang.String _id,
     java.lang.String _name,
     org.omg.CORBA.TypeCode boxed_type)
     {
     kind = TCKind._tk_value_box;
     id = _id;
     name = _name;
     type = boxed_type;
     }
     */



   /** Constructor for object reference (objref, _tk_objref) type
    * @return TypeCode object for Object Reference Type
    */
    public static edu.uci.ece.zen.orb.TypeCode newObjRefTC(String _id, String _name) {
        edu.uci.ece.zen.orb.TypeCode new_tc = new TypeCode(_id, _name);
        //new_tc.kind = TCKind._tk_objref;
        return new_tc;
    }
        


   /** Constructor for string and wstring type, one of two ways to make.
    * @return TypeCode object for Object Reference Type
    */
    public static edu.uci.ece.zen.orb.TypeCode newStringTC(int _kind, int _length) {
        edu.uci.ece.zen.orb.TypeCode new_tc = new TypeCode(_kind);
        new_tc.length = _length;
        return new_tc;

    }



    /**
     * Determine if legal operations permissible on two TypeCodes is
     * equal and the results of those operations are equal.
     *
     * <p> Can be invoked on any TypeCode.  Returns true if this and
     * <code>tc</code> have the same set of legal operations.
     *
     * @param tc TypeCode to compare this TypeCode to.
     * @return true if tc can be acted on as this.
     */
    public boolean equal(org.omg.CORBA.TypeCode tc) {

        int this_kind_val = this.kind().value();
        int tc_kind_val = tc.kind().value();

        if (this_kind_val != tc.kind().value() ) {
            return false;
        }

        // We now know that tc and type have the same type.
        
        long kindMask = 1L << this_kind_val;

        try {
            
            // If id operation is valid...
            
            if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_objref) 
                               | (1L << org.omg.CORBA.TCKind._tk_struct)
                               | (1L << org.omg.CORBA.TCKind._tk_union)
                               | (1L << org.omg.CORBA.TCKind._tk_enum)
                               | (1L << org.omg.CORBA.TCKind._tk_alias)
                               | (1L << org.omg.CORBA.TCKind._tk_value)
                               | (1L << org.omg.CORBA.TCKind._tk_value_box)
                               | (1L << org.omg.CORBA.TCKind._tk_native)
                               | (1L << org.omg.CORBA.TCKind._tk_abstract_interface)
                               | (1L << org.omg.CORBA.TCKind._tk_except)
                               )
                  ) != 0 ) {
                
                // If both id's are equal
                if ( ! this.id.equals(tc.id())) {
                    return false;
                }
                if ( ! this.name.equals(tc.name())) {
                    return false;
                }
            }


            if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_struct)
                               | (1L << org.omg.CORBA.TCKind._tk_union)
                               | (1L << org.omg.CORBA.TCKind._tk_enum)
                               | (1L << org.omg.CORBA.TCKind._tk_value)
                               | (1L << org.omg.CORBA.TCKind._tk_except)
                              )
                  ) != 0 ) {
                if ( this.member_count != tc.member_count() )
                    return false;
                
                
                
                // structs, unions, enums, and valuetypes reach this line
                
                if ( this_kind_val != org.omg.CORBA.TCKind._tk_enum ) {
                    for (int i = 0; i < this.member_count; i++) {
                        
                        if ( ! this.member_names[i].equals(tc.member_name(i)) )
                            return false;
                                
                        if (this_kind_val != org.omg.CORBA.TCKind._tk_enum && ! this.member_types[i].equal(tc.member_type(i)) )
                            return false;

                        if ( this_kind_val == org.omg.CORBA.TCKind._tk_union && ! this.member_labels[i].equal(tc.member_label(i)))
                            return false;
                        
                        if ( this_kind_val == org.omg.CORBA.TCKind._tk_value && this.member_visibility[i] != tc.member_visibility(i))
                            return false;
                        
                    }
                }
            }


            if (this_kind_val == org.omg.CORBA.TCKind._tk_union) {
                if ( ! this.discriminator_type.equal(tc.discriminator_type()) || this.default_index != tc.default_index() ) {
                    return false;
                }
            }

            if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_string)
                              | (1L << org.omg.CORBA.TCKind._tk_wstring)
                              | (1L << org.omg.CORBA.TCKind._tk_sequence)
                              | (1L << org.omg.CORBA.TCKind._tk_array)
                              )
                  ) != 0 ) {
                if (this.length != tc.length()) {
                    return false;
                }
            }     
            
            if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_sequence)
                               | (1L << org.omg.CORBA.TCKind._tk_array)
                               | (1L << org.omg.CORBA.TCKind._tk_value_box)
                               | (1L << org.omg.CORBA.TCKind._tk_alias)
                              )
                  ) != 0 ) {
                
                if ( ! this.type.equal(tc.content_type()) ) {
                    return false;
                }
            }     


            if (this_kind_val == org.omg.CORBA.TCKind._tk_fixed) {
                if (this.digits != tc.fixed_digits() || this.scale != tc.fixed_scale()) {
                    return false;
                }
            }
                
            if (this_kind_val == org.omg.CORBA.TCKind._tk_value) {
                if ( this.type_modifier != tc.type_modifier() || ! this.concrete_base_type().equal(tc.concrete_base_type()) ) {
                        return false;
                }
            }
                
                
        }      
        catch ( org.omg.CORBA.TypeCodePackage.Bounds be ) {
            be.printStackTrace();
            return false;
        }
        
        catch ( org.omg.CORBA.TypeCodePackage.BadKind bk ) {
            bk.printStackTrace();
            return false;
        }
            
        return true;
    }



    /**
     * Determine if the types in a data item having this type are
     * equivalent to the types in <code>tc</code>.  Used by Anys to
     * know when it is safe to extract data stored in an Any into
     * another variable.  This is the full version.
     *
     * <p> This method unwinds aliases, and checks that they have the
     * same kind.  If the id() method is available for both TypeCodes
     * and is non-empty, then they must have the same id.  If one or
     * more id string is empty, then the TypeCodes are compared for
     * structural equivalence by examining their members.
     *
     * @param anotherTypeCode TypeCode to compare this TypeCode to.
     * @return true if tc is structurally equivalent to this; false otherwise.
     */
    public boolean equivalent(org.omg.CORBA.TypeCode anotherTypeCode) {
        // See CORBA spec for implementation details.  The Spec clearly
        // states how to implement this.
        
        int this_kind_val = this.kind().value();
        
        if (this_kind_val == org.omg.CORBA.TCKind._tk_alias) {
            return originalType(this).equivalent(anotherTypeCode);
        }
        
        org.omg.CORBA.TypeCode tc = originalType(anotherTypeCode);
        
        
        
        if (this_kind_val != originalType(tc).kind().value() ) {
            return false;
        }
        
        // We now know that tc and type have the same type.
        
        long kindMask = 1L << this_kind_val;
        
        
        try {
            
            // If id operation is valid...
            
            if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_objref)
                               | (1L << org.omg.CORBA.TCKind._tk_struct)
                               | (1L << org.omg.CORBA.TCKind._tk_union)
                               | (1L << org.omg.CORBA.TCKind._tk_enum)
                               | (1L << org.omg.CORBA.TCKind._tk_alias)
                               | (1L << org.omg.CORBA.TCKind._tk_value)
                               | (1L << org.omg.CORBA.TCKind._tk_value_box)
                               | (1L << org.omg.CORBA.TCKind._tk_native)
                               | (1L << org.omg.CORBA.TCKind._tk_abstract_interface)
                               | (1L << org.omg.CORBA.TCKind._tk_except)
                               )
                  ) != 0 ) {
                
                // If both id's either non-null
                String tcId = tc.id();
                if (this.id != null && !this.id.equals("") && tcId != null && !tcId.equals("") ) {
                    if (this.id.equals(tcId)) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            }
            
            // If member_count operation is valid
            if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_struct)
                               | (1L << org.omg.CORBA.TCKind._tk_union)
                               | (1L << org.omg.CORBA.TCKind._tk_enum)
                               | (1L << org.omg.CORBA.TCKind._tk_value)
                               | (1L << org.omg.CORBA.TCKind._tk_except)
                               )
                  ) != 0 ) {
                if (this.member_count != tc.member_count())
                    return false;
            }
            
            // If default_index operation is valid
            if (this_kind_val == org.omg.CORBA.TCKind._tk_union) {
                if (this.default_index != tc.default_index())
                    return false;
            }
            
            // If the length operation is valid
            if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_string)
                               | (1L << org.omg.CORBA.TCKind._tk_wstring)
                               | (1L << org.omg.CORBA.TCKind._tk_sequence)
                               | (1L << org.omg.CORBA.TCKind._tk_array)
                               )
                  ) != 0 ) {
                if (this.length != tc.length())
                    return false;
            }
            
            // If the digits and scale operations are valid
            if (this_kind_val == org.omg.CORBA.TCKind._tk_fixed) {
                if (this.digits != tc.fixed_digits())
                  return false;
                if (this.scale != tc.fixed_scale())
                    return false;
            }
            
            
            // If the member labels valid (used for union)
            if (this_kind_val == org.omg.CORBA.TCKind._tk_union) {
                // Note that member_count was already compared, so this and tc
                // have the same number of member_labels
                for (int i = 0; i < this.member_labels.length; i++) {
                    if (this.member_labels[i] != tc.member_label(i))
                        return false;
                }
                if ( ! this.discriminator_type.equivalent(tc.discriminator_type()) )
                    return false;
                
            }
            
            
            // If the member_type operation is valid
            if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_struct)
                               | (1L << org.omg.CORBA.TCKind._tk_union)
                               | (1L << org.omg.CORBA.TCKind._tk_enum)
                               | (1L << org.omg.CORBA.TCKind._tk_value)
                               | (1L << org.omg.CORBA.TCKind._tk_except)
                               )
                  ) != 0 ) {
                for (int i = 0; i < this.member_types.length; i++) {
                    if ( ! this.member_types[i].equivalent(tc.member_type(i)) )
                        return false;
                }
            }
            
            // If the content_type operation is valid
            if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_sequence)
                               | (1L << org.omg.CORBA.TCKind._tk_array)
                               | (1L << org.omg.CORBA.TCKind._tk_value_box)
                               | (1L << org.omg.CORBA.TCKind._tk_alias)
                               )
                  ) != 0 ) {
                if ( ! this.content_type().equivalent(tc.content_type()) )
                    return false;
            }
            
        } // end of try {
        
        catch ( org.omg.CORBA.TypeCodePackage.Bounds be ) {
            be.printStackTrace();
            return false;
        }
        
        catch ( org.omg.CORBA.TypeCodePackage.BadKind bk ) {
            bk.printStackTrace();
            return false;
        }
        

        return true;
    }



    /**
     * Return a compact representation of the TypeCode without
     * optional name and member_name fields.  
     * See Section 10.7.1 of the CORBA v2.3 Spec.
     * @return TypeCode without optional name and member_name fields
     */
    public org.omg.CORBA.TypeCode get_compact_typecode() {
        return duplicateWithoutNames();
    }



    /**
     * Copy the data of an existing typecode without copying the
     * optional name & member name fields.
     *
     * @return TypeCode without optional name & member_name fields
     */
    private edu.uci.ece.zen.orb.TypeCode duplicateWithoutNames() {
        edu.uci.ece.zen.orb.TypeCode newTC = new edu.uci.ece.zen.orb.TypeCode(kind);
        newTC.type = type;
        // Don't copy the name.
        //newTC.name = name;
        newTC.id = id;
        newTC.length = length;
        newTC.offset = offset;
        newTC.default_index = default_index;
        if (member_labels != null) {
            newTC.member_labels = new edu.uci.ece.zen.orb.any.Any [member_labels.length];
            for (int i = 0; i < member_labels.length; i++) {
                newTC.member_labels[i] = member_labels[i];
            }
        }
        if (member_types != null) {
            newTC.member_types = new TypeCode[member_types.length];
            for (int i = 0; i < member_types.length; i++) {
                newTC.member_types[i] = member_types[i].duplicateWithoutNames();
            }
        }

        if (member_names != null) {
            newTC.member_names = new String[member_names.length];
            for (int i = 0; i < member_names.length; i++) {
                // Set member names to empty string
                newTC.member_names[i] = "";
            }
        }
        
        if (member_idlTypes != null) {
            newTC.member_idlTypes = new org.omg.CORBA.IDLType [member_idlTypes.length];
            for (int i = 0; i < member_idlTypes.length; i++) {
                newTC.member_idlTypes[i] = member_idlTypes[i];
            }
        }
        
        newTC.member_count = member_count;
        newTC.discriminator_type = discriminator_type.duplicateWithoutNames();
        newTC.digits = digits;
        newTC.scale = scale;
        newTC.type_modifier = type_modifier;
        if (member_ids != null) {
            newTC.member_ids = new String [member_ids.length];
            for (int i = 0; i < member_ids.length; i++) {
                newTC.member_ids[i] = member_ids[i];
            }
        }

        if (member_defined_ins != null) {
            newTC.member_defined_ins = new String [member_defined_ins.length];
            for (int i = 0; i < member_defined_ins.length; i++) {
                newTC.member_defined_ins[i] = member_defined_ins[i];
            }
        }

        if (member_versions != null) {
            newTC.member_versions = new String [member_versions.length];
            for (int i = 0; i < member_versions.length; i++) {
                newTC.member_versions[i] = member_versions[i];
            }
        }
        if (member_visibility != null) {
            newTC.member_visibility = new short [member_visibility.length];
            for (int i = 0; i < member_visibility.length; i++) {
                newTC.member_visibility[i] = member_visibility[i];
            }
        }
        return newTC;
    }







    /** Accessor for the org.omg.CORBA.TCKind of this.  Used to
     * determine which operations are valid on this.
     * @return TCKind represented by this TypeCode.
     */
    public org.omg.CORBA.TCKind kind() {
        return org.omg.CORBA.TCKind.from_int(kind);
    }


    /** Accessor for the org.omg.CORBA.TCKind.value() of this.
     * @return Integer for TCKind represented by this TypeCode.
     */
    public int _kind() {
        return kind;
    }


    /** 
     * Accessor for the RepositoryId globally identifying this type.
     * Valid on object reference, structure, union, enumeration,
     * alias, and exception TypeCodes.  It may return an empty string.
     *
     * @return Integer for TCKind represented by this TypeCode.
     */
    public java.lang.String id() throws
    org.omg.CORBA.TypeCodePackage.BadKind {
        long kindMask = 1L << kind;
        final long acceptedKinds = 0L
            | (1L << org.omg.CORBA.TCKind._tk_objref)
            | (1L << org.omg.CORBA.TCKind._tk_struct)
            | (1L << org.omg.CORBA.TCKind._tk_union)
            | (1L << org.omg.CORBA.TCKind._tk_enum)
            | (1L << org.omg.CORBA.TCKind._tk_alias)
            | (1L << org.omg.CORBA.TCKind._tk_value)
            | (1L << org.omg.CORBA.TCKind._tk_value_box)
            | (1L << org.omg.CORBA.TCKind._tk_native)
            | (1L << org.omg.CORBA.TCKind._tk_abstract_interface)
            | (1L << org.omg.CORBA.TCKind._tk_except);

        if ( (kindMask & acceptedKinds) == 0) {
            System.out.println("TypeCode#id() kind " + kind + " kindMask " + kindMask + " acceptedKinds " + acceptedKinds);
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#id() called on invalid TypeCode of " + kind);
        }
        return id;
    }


    /**
     * Simple name identifying the type within its enclosing scope.
     * Valid on object reference, structure, union, enumeration,
     * alias, value type, and boxed valuetype, native and exception
     * TypeCodes.
     *
     * @return String for simple name of the type within enclosing scope.
     */
    public java.lang.String name() throws
    org.omg.CORBA.TypeCodePackage.BadKind {

        long kindMask = 1L << kind;

        final long acceptedKinds = 0L 
            | (1L << org.omg.CORBA.TCKind._tk_objref)
            | (1L << org.omg.CORBA.TCKind._tk_struct)
            | (1L << org.omg.CORBA.TCKind._tk_union)
            | (1L << org.omg.CORBA.TCKind._tk_enum)
            | (1L << org.omg.CORBA.TCKind._tk_alias)
            | (1L << org.omg.CORBA.TCKind._tk_value)
            | (1L << org.omg.CORBA.TCKind._tk_value_box)
            | (1L << org.omg.CORBA.TCKind._tk_native)
            | (1L << org.omg.CORBA.TCKind._tk_abstract_interface)
            | (1L << org.omg.CORBA.TCKind._tk_except);

        if ( (kindMask & acceptedKinds) == 0) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#name() called on invalid TypeCode of " + kind);
        }
        return name;
    }



    // Bruce Miller wrote the method below.
    // If the way that this and similar methods are written doesn't
    // make sense to you and you don't understand why it is better
    // than using a switch statement or a bunch of if ( (x == y1) ||
    // (x==y2) || (x==y3)) then you should not be editing it.

    /**
     * Return the count of member items of this type.  For example,
     * structures, unions, enumerations, etc. have members.  See
     * Section 10.7.1 of the CORBA v2.3 Spec
     *
     * @return int number of members of this TypeCode
     * @throws org.omg.CORBA.TypeCodePackage.BadKind if this Type does not support having members.
     */
    public int member_count() throws
        org.omg.CORBA.TypeCodePackage.BadKind {
        long kindMask = 1L << kind;

        // This is a bitmask of accepted types and the compiler will
        // fold into a constant stored in the data segment.
        final long acceptedKinds = 0L 
            | (1L << org.omg.CORBA.TCKind._tk_struct)
            | (1L << org.omg.CORBA.TCKind._tk_union)
            | (1L << org.omg.CORBA.TCKind._tk_enum)
            | (1L << org.omg.CORBA.TCKind._tk_value)
            | (1L << org.omg.CORBA.TCKind._tk_except);

        if ( (kindMask & acceptedKinds) == 0) {
            // In case you are wondering, it only took five machine
            // instructions to get here.  That's much smaller than any
            // switch statement will ever be.
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#member_count() called on invalid TypeCode");
        }

        return member_count;
    }



    /**
     * Return the name (human readable) of a member of this type.
     * @param index index at which to get member
     * @return String name of the member
     * @throws org.omg.CORBA.TypeCodePackage.BadKind if this type doesn't have members
     */
    public java.lang.String member_name(int index) throws
        org.omg.CORBA.TypeCodePackage.BadKind,
        org.omg.CORBA.TypeCodePackage.Bounds {

        long kindMask = 1L << kind;

        // This is a bitmask of accepted types and the compiler will
        // fold into a constant stored in the data segment.
        final long acceptedKinds = 0L 
            | (1L << org.omg.CORBA.TCKind._tk_struct)
            | (1L << org.omg.CORBA.TCKind._tk_union)
            | (1L << org.omg.CORBA.TCKind._tk_enum)
            | (1L << org.omg.CORBA.TCKind._tk_value)
            | (1L << org.omg.CORBA.TCKind._tk_except);

        if ( (kindMask & acceptedKinds) == 0) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#member_name(int) called on invalid TypeCode");
        }
        if ( index > member_count ) {
            throw new org.omg.CORBA.TypeCodePackage.Bounds("TypeCode#member_name(int index) called with index exceeding number of members");
        }

        return member_names[index];
    }



    /**
     * Return the TypeCode for a member of the typecode that this
     * represents.  See Section 10.7.1 of the CORBA v2.3 Spec.
     *
     * @return TypeCode for a member of this TypeCode at member index <code>index</code>.
     */
    public org.omg.CORBA.TypeCode member_type(int index) throws
        org.omg.CORBA.TypeCodePackage.BadKind,
        org.omg.CORBA.TypeCodePackage.Bounds {

        long kindMask = 1L << kind;

        final long acceptedKinds = 0L
            | (1L << org.omg.CORBA.TCKind._tk_struct)
            | (1L << org.omg.CORBA.TCKind._tk_union)
            | (1L << org.omg.CORBA.TCKind._tk_value)
            | (1L << org.omg.CORBA.TCKind._tk_except);

        if ( (kindMask & acceptedKinds) == 0) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#member_type(int) called on invalid TypeCode");
        }
        if ( index > member_count ) {
            throw new org.omg.CORBA.TypeCodePackage.Bounds("TypeCode#member_type(int index) called with index exceeding number of members");
        }
        return member_types[index];
    }



    /**
     * Get the member label of the union member identified by index.
     * See Section 10.7.1 of the CORBA v2.3 Spec.
     * @return the member label of the union member identified by index
     */
    public org.omg.CORBA.Any member_label(int index) throws
        org.omg.CORBA.TypeCodePackage.BadKind,
        org.omg.CORBA.TypeCodePackage.Bounds {

        if (kind != org.omg.CORBA.TCKind._tk_union) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#member_label(int) called on invalid TypeCode");
        }
        if ( index > member_count ) {
            throw new org.omg.CORBA.TypeCodePackage.Bounds("TypeCode#member_label(int index) called with index exceeding number of members");
        }

        return member_labels[index];
    }



    // This methed commented out by Bruce on 2003-09-16 because it is
    // never used, and is not listed in the CORBA spec.
    /*    
          public org.omg.CORBA.IDLType org.omg.CORBA.TypeCode.memberIdlType(int index) {

          //throws
          //org.omg.CORBA.TypeCodePackage.BadKind,
        //org.omg.CORBA.TypeCodePackage.Bounds {
        
        if (kind == org.omg.CORBA.TCKind._tk_union) {
        return member_idlTypes[index];
        }
        else {
        throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#memberIdlType() called on invalid TypeCode");
        }
        }
    */



    /**
     * Return the type of all non-default member labels. See Section
     * 10.7.1 of the CORBA v2.3 Spec.
     * @return TypeCode type of all non-default member labels.
     */
    public org.omg.CORBA.TypeCode discriminator_type() throws
        org.omg.CORBA.TypeCodePackage.BadKind {

        if (kind != org.omg.CORBA.TCKind._tk_union) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#discriminator_type() called on invalid TypeCode");
        }

        return discriminator_type;
    }



    /**
     * Return index of default member.
     * @return index of default member, or -1 if there is no default member.
     */
    public int default_index() throws
        org.omg.CORBA.TypeCodePackage.BadKind {

        if (kind != org.omg.CORBA.TCKind._tk_union) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#default_index() called on invalid TypeCode");
        }

        return default_index;
    }



    /** See Section 10.7.1 of the CORBA v2.3 Spec
     */
    public int length() throws org.omg.CORBA.TypeCodePackage.BadKind {
        long kindMask = 1L << kind;

        final long acceptedKinds = 0L
            | (1L << org.omg.CORBA.TCKind._tk_string)
            | (1L << org.omg.CORBA.TCKind._tk_sequence)
            | (1L << org.omg.CORBA.TCKind._tk_array);

        if ( (kindMask & acceptedKinds) == 0 ) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#length() called on invalid TypeCode");
        }

        return length;
    }



    // I have not implemented this method for value box
    // (_tk_value_box) types yet, but I assume from reading the spec
    // that they would just return the variable type, jsut like
    // sequence and array do.
    /** See Section 10.7.1 of the CORBA v2.3 Spec
     */
    public org.omg.CORBA.TypeCode content_type() throws
        org.omg.CORBA.TypeCodePackage.BadKind {

        long kindMask = 1L << kind;
        final long acceptedKinds = 0L
            | (1L << org.omg.CORBA.TCKind._tk_sequence)
            | (1L << org.omg.CORBA.TCKind._tk_array)
            | (1L << org.omg.CORBA.TCKind._tk_alias)
            | (1L << org.omg.CORBA.TCKind._tk_value_box);
        
        // Return type of stored element or what we are immediate
        // alias for, or what the boxed value type is boxing
        if ( (kindMask & acceptedKinds) > 0) {
            return type;  
        }

        throw new org.omg.CORBA.TypeCodePackage.BadKind("edu.uci.ece.zen.orb.TypeCode#content_type() should not be called on kind " + kind);
    }



    /** See Section 10.7.1 of the CORBA v2.3 Spec
     */
    public short member_visibility(int index) 
        throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds {
        if (kind != org.omg.CORBA.TCKind._tk_value) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("edu.uci.ece.zen.orb.TypeCode#content_type() should not be called on kind " + kind);
        }
        if ( index > member_visibility.length ) {
            throw new org.omg.CORBA.TypeCodePackage.Bounds("edu.uci.ece.zen.orb.TypeCode#member_label(int index) called with index exceeding number of members");
        }
        return member_visibility[index];
    }



    // The fixed_digits() and fixed_scale() methods were not in the
    // TypeCode code until Bruce needed them on 2003-06-02 for the
    // monolithic Any's insert_fixed() method.  The monolithic Any
    // implementation was copied from JacORB which apparently made
    // greater use of TypeCodes than our Zen implementation does.
    public short fixed_digits() throws
    org.omg.CORBA.TypeCodePackage.BadKind {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public short fixed_scale() throws
    org.omg.CORBA.TypeCodePackage.BadKind {
        throw new org.omg.CORBA.NO_IMPLEMENT();

    }



    /** 
     * For non-boxed valuetype, returns the ValueModifier that applies
     * to the valuetype represented by the target TypeCode, according
     * to section 10.7.1 of the CORBA v2.3 Spec
     * @return ValueModifier for valuetype
     * @throws org.omg.CORBA.TypeCodePackage.BadKind if not called on a non-boxed valuetype
     */
    public short type_modifier()
        throws org.omg.CORBA.TypeCodePackage.BadKind {
         if (kind != org.omg.CORBA.TCKind._tk_value) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("edu.uci.ece.zen.orb.TypeCode#type_modifier() should not be called on kind " + kind);
        }

        return type_modifier;
    }



    /** 
     * May be called on non-boxed valuetype TypeCodes.  According to
     * section 10.7.1 of the CORBA v2.3 Spec "If the valuetype
     * represented by the target TypeCode has a cencrete bas
     * valuetype, the concrete_base_type operation returns a TypeCode
     * for the concrete base, otherwise, it returns a nil TypeCode
     * reference.
     *
     * @return nil if ValueType did not have concrete base type, the base type otherwise
     * @throws org.omg.CORBA.TypeCodePackage.BadKind if not called on a non-boxed valuetype
     */
    public org.omg.CORBA.TypeCode concrete_base_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind {
         if (kind != org.omg.CORBA.TCKind._tk_value) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("edu.uci.ece.zen.orb.TypeCode#type_modifier() should not be called on kind " + kind);
        }

        return type;
    }



    // This function would used by the monolithic implementation of
    // Anys when an interface repository is present.  It is not
    // implemented here because we do not have an interface repository
    // implemented.
    
    // In JacORB, this method creates a TypeCode "for an an arbitrary
    // java class" but it only supports RMI classes.
    public static TypeCode create_tc(Class cls) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }



    /**
     * Unwinds any TCKind.tk_alias's (caused by idl typecodes) to find
     * original type of a TcypeCode.
     *
     * @param t TypeCode that may be a alias type, but for which a
     * base type is definitely wanted.
     * @return TypeCode at root of aliases.
     */
    public static org.omg.CORBA.TypeCode originalType(org.omg.CORBA.TypeCode t) {
        org.omg.CORBA.TypeCode tc_temp = t;
        try {
            while (tc_temp.kind().value() == org.omg.CORBA.TCKind._tk_alias) {
                tc_temp = tc_temp.content_type();
            }
        }
        catch (org.omg.CORBA.TypeCodePackage.BadKind bk) {
            // Does not happen according to JacORB's org.jacorb.orb.TypeCode#originalType() method
            System.err.println("TypeCode#originalType BadKind exception occurred");
            bk.printStackTrace();
        }
        return tc_temp;
    }



    /**
     * Returns a TypeCode object for a primitive Type Code when passed
     * a TCKind number for a typecode.  Used as a helper method for
     * org.omg.CORBA.ORB.get_primitive_tc().
     *
     * @param tcKindValue a TCKind.value() integer representing a Type Code Kind
     * @return TypeCode, being generic, for the tcKindValue
     */
    public static org.omg.CORBA.TypeCode lookupPrimitiveTc(int tcKindValue) {
        org.omg.CORBA.TypeCode mapValue = primitiveTCMap[tcKindValue];
        if (mapValue == null) {
            mapValue = new edu.uci.ece.zen.orb.TypeCode(tcKindValue);
            primitiveTCMap[tcKindValue] = mapValue;
        }
        return mapValue;
    }




/* ajc moved
    public org.omg.CORBA.IDLType member_idlType(int index) throws
    org.omg.CORBA.TypeCodePackage.BadKind,
    org.omg.CORBA.TypeCodePackage.Bounds {
        return member_idlTypes[index];
    }
*/




    /** The TCKind of this type, see class {@link
     * org.omg.CORBA.TCKind}.  Do not change kind unless you alse
     * change kind_mask*/
    int kind = -1;
    /** has a 1 bit set in the bit number for the kind that this is.
     * E.g. something with kind = 3 would also have bit 3 of its
     * kindMask bit set.  Start counting from the rightmost bit being
     * bit 0, which is set for kind = TCKind._tk_null. */
    //long kindMask = 0;

    /** Pointer to internally stored type for aliases, sequences, arrays, etc */
    org.omg.CORBA.TypeCode type;
    /** Name of Typecdoe within enclosing scope */
    String name;
    /** Unique Repository Id of Typecdoe. */
    String id;

    int length;
    int offset = -1;
    int default_index;
    edu.uci.ece.zen.orb.any.Any[] member_labels = null;
    TypeCode[] member_types = null;
    String[] member_names = null;
    org.omg.CORBA.IDLType[] member_idlTypes = null;
    int member_count;
    TypeCode discriminator_type;

    short digits;
    short scale;
    short type_modifier;
    String[] member_ids = null;
    String[] member_defined_ins = null;
    String[] member_versions = null;
    short[] member_visibility = null;



    /**
     * Stores primitive typecode objects indexed by their TCKind
     * number.  Used by lookupPrimitiveTC, and an array of memory
     * pointers take up a lot less space then rewriting code to check
     * if a value is null.
     */
    private static org.omg.CORBA.TypeCode [] primitiveTCMap = new org.omg.CORBA.TypeCode [32];


}
