//
// $Id: TypeCode.java,v 1.2 2004/01/20 21:55:06 bmiller Exp $
//

package edu.uci.ece.zen.orb;


import org.omg.CORBA.*;

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

    /* ajc moved
    // Constructor for Struct and Exception TypeCode
    public TypeCode(int _kind,
        java.lang.String _id,
        java.lang.String _name,
        org.omg.CORBA.StructMember[] _members) {
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
*/
    // Constructor for union Type Code
    /* ajc moved
    public TypeCode(java.lang.String _id,
        java.lang.String _name,
        org.omg.CORBA.TypeCode _discriminator_type,
        org.omg.CORBA.UnionMember[] _members) {
        kind = TCKind._tk_union;
        id = _id;
        name = _name;
        discriminator_type = (TypeCode) _discriminator_type;
        member_count = _members.length;
        member_names = new String[member_count];
        member_labels = new edu.uci.ece.zen.orb.any.Any[member_count];
        member_types = new TypeCode[member_count];
        member_idlTypes = new IDLType[member_count];
        for (int i = 0; i < member_count; i++) {
            member_names[i] = _members[i].name;
            member_types[i] = (TypeCode) _members[i].type;
            member_labels[i] = (edu.uci.ece.zen.orb.any.Any) _members[i].label;
            member_idlTypes[i] = (IDLType) _members[i].type_def;

        }
    }
    */
/* ajc moved
    // Constructor for Enum TypeCode
    public TypeCode(java.lang.String _id,
        java.lang.String _name,
        java.lang.String[] _members) {
        kind = TCKind._tk_enum;
        id = _id;
        name = _name;
        member_count = _members.length;
        member_names = new String[member_count];
        for (int i = 0; i < member_count; i++)
            member_names[i] = _members[i];
    }
*/
    // 



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
        kind = TCKind._tk_alias;
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
    public TypeCode(java.lang.String _id,
        java.lang.String _name) {
        kind = TCKind._tk_abstract_interface;
        id = _id;
        name = _name;
    }

    /* in the aspect file, TypeCodeAspect.aj
    public static TypeCode create_tc(Class cls) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    */


    // Contructor for string and wstring Type Code
    /*public TypeCode(int bound)
     {
     throw new org.omg.CORBA.NO_IMPLEMENT();
     }*/
/* ajc moved
    // Contructor for sequence array TypeCode
    public TypeCode(int bound,
        org.omg.CORBA.TypeCode element_type) {
        kind = TCKind._tk_sequence;
        length = bound;
        type = element_type;
    }

    // Constructor for recursive Sequence
    public TypeCode(int bound,
        int offset) {
        kind = TCKind._tk_sequence;
        length = bound;
        this.offset = offset;
        // what does offset get stored to?
    }
*/
    /*
     // This has not been tested because the Interface Repository
     // has not been implemented yet
     // There's a problem here with a profile that duplicates the
     // one for the interface TypeCode
     // Constructor for native TypeCode
     public TypeCode(java.lang.String _id,
     java.lang.String _name)
     {
     kind = TCKind._tk_native;
     id = _id;
     name = _name;
     }

    // This has not been tested because the Interface Repository
    // has not been implemented yet
    // Constructor for fixed TypeCode
    public TypeCode(short _digits,
        short _scale) {
        kind = TCKind._tk_fixed;
        digits = _digits;
        scale = _scale;
    }*/
/* ajc moved
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
        member_idlTypes = new IDLType[member_count];
        member_visibility = new short[member_count];

        for (int i = 0; i < member_count; i++) {
            member_names[i] = _members[i].name;
            member_ids[i] = _members[i].id;
            member_defined_ins[i] = _members[i].defined_in;
            member_versions[i] = _members[i].version;
            member_types[i] = (TypeCode) _members[i].type;
            member_idlTypes[i] = (IDLType) _members[i].type_def;
            member_visibility[i] = (short) _members[i].access;
        }
    }
*/

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

    /* aj into TypeCodeMinimalAspect.aj
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
    /* aj into TypeCodeMinimalAspect.aj
    public boolean equal(org.omg.CORBA.TypeCode tc) {

        int this_kind_val = kind;

        if (this_kind_val != tc.kind().value() ) {
            return false;
        }
        
        // We now know that tc and type have the same type.
        
        long kindMask = 1L << this_kind_val;
        

        try {
            if ( (kindMask & (1L << org.omg.CORBA.TCKind._tk_objref
                              | 1L << org.omg.CORBA.TCKind._tk_struct
                              | 1L << org.omg.CORBA.TCKind._tk_union
                              | 1L << org.omg.CORBA.TCKind._tk_enum
                              | 1L << org.omg.CORBA.TCKind._tk_alias
                              | 1L << org.omg.CORBA.TCKind._tk_value
                              | 1L << org.omg.CORBA.TCKind._tk_value_box
                              | 1L << org.omg.CORBA.TCKind._tk_native
                              | 1L << org.omg.CORBA.TCKind._tk_abstract_interface
                              | 1L << org.omg.CORBA.TCKind._tk_except
                              )
                  ) > 0 ) {
                
                // If both id's are equal
                if ( ! this.id.equals(tc.id())) {
                    return false;
                }
                if ( ! this.name.equals(tc.name())) {
                    return false;
                }
            }
        }
        catch (org.omg.CORBA.TypeCodePackage.BadKind bk) {
            bk.printStackTrace();
            return false;
        }
        return true;
    }
    */



    /**
     * Determine if the types in a data item having this type are
     * equivalent to the types in <code>tc</code>.  Used by Anys to
     * know when it is safe to extract data stored in an Any into
     * another variable.
     *
     * <p> This method unwinds aliases, and checks that they have the
     * same kind.  If the id() method is available for both TypeCodes
     * and is non-empty, then they must have the same id.  If one or
     * more id string is empty, then the TypeCodes are compared for
     * structural equivalence by examining their members.
     *
     * @param tc TypeCode to compare this TypeCode to.
     * @return true if tc is structurally equivalent to this; false otherwise.
     */
    /* aj into TypeCodeMinimalAspect.aj
    public boolean equivalent(org.omg.CORBA.TypeCode tc) {
        //return tc.kind().value() == type.kind().value();

        int this_kind_val = kind;

        if (this_kind_val != tc.kind().value() ) {
            return false;
        }
        
        // We now know that tc and type have the same type.
        
        long kindMask = 1L << this_kind_val;

        try {
            
            // If id operation is valid...
            if ( (kindMask & (1L << org.omg.CORBA.TCKind._tk_objref
                              | 1L << org.omg.CORBA.TCKind._tk_struct
                              | 1L << org.omg.CORBA.TCKind._tk_union
                              | 1L << org.omg.CORBA.TCKind._tk_enum
                              | 1L << org.omg.CORBA.TCKind._tk_alias
                              | 1L << org.omg.CORBA.TCKind._tk_value
                              | 1L << org.omg.CORBA.TCKind._tk_value_box
                              | 1L << org.omg.CORBA.TCKind._tk_native
                              | 1L << org.omg.CORBA.TCKind._tk_abstract_interface
                              | 1L << org.omg.CORBA.TCKind._tk_except
                              )
                  ) > 0 ) {
                
                // If both id's either non-null
                if (this.id != null && !this.id.equals("") && tc.id() != null && !tc.id().equals("") ) {
                    if (this.id.equals(tc.id())) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }

            }
        }
        catch (org.omg.CORBA.TypeCodePackage.BadKind bk) {
            bk.printStackTrace();
            return false;
        }
        return true;
    }
    */



    /* aj moved into TypeCodeMinimalAspect.java and TypeCodeAspect.java
    /**
     * Return a copy of this typecode with all optional name & member
     * fields stripped out.
     * 
     * @return A copy of this typecode with name & member name fields
     * stripped out.
     */
    /*    
    public org.omg.CORBA.TypeCode get_compact_typecode() {
        return type;
    }
    */


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


/* ajc moved
    public int member_count() throws
    org.omg.CORBA.TypeCodePackage.BadKind {
        return member_count;
    }

    public java.lang.String member_name(int index) throws
    org.omg.CORBA.TypeCodePackage.BadKind,
    org.omg.CORBA.TypeCodePackage.Bounds {
        return member_names[index];
    }

    public org.omg.CORBA.TypeCode member_type(int index) throws
    org.omg.CORBA.TypeCodePackage.BadKind,
    org.omg.CORBA.TypeCodePackage.Bounds {
        return member_types[index];
    }

    public org.omg.CORBA.Any member_label(int index) throws
    org.omg.CORBA.TypeCodePackage.BadKind,
    org.omg.CORBA.TypeCodePackage.Bounds {
        return member_labels[index];
    }

    public org.omg.CORBA.IDLType member_idlType(int index) throws
    org.omg.CORBA.TypeCodePackage.BadKind,
    org.omg.CORBA.TypeCodePackage.Bounds {
        return member_idlTypes[index];
    }

    public org.omg.CORBA.TypeCode discriminator_type() throws
    org.omg.CORBA.TypeCodePackage.BadKind {
        return discriminator_type;
    }

    public int default_index() throws
    org.omg.CORBA.TypeCodePackage.BadKind {
        return default_index;
    }

    public int length() throws org.omg.CORBA.TypeCodePackage.BadKind {
        return length;
    }

    public org.omg.CORBA.TypeCode content_type() throws
    org.omg.CORBA.TypeCodePackage.BadKind {
        return null;
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
/* ajc moved
    int length;
    int offset = -1;
    int default_index;
    edu.uci.ece.zen.orb.any.Any[] member_labels = null;
    TypeCode[] member_types = null;
    String[] member_names = null;
    moved IDLType[] member_idlTypes = null;
    int member_count;
    TypeCode discriminator_type;

    short digits;
    short scale;
    short type_modifier;
    String[] member_ids = null;
    String[] member_defined_ins = null;
    String[] member_versions = null;
    short[] member_visibility = null;
    */
}
