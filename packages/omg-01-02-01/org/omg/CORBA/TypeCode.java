/***** Copyright (c) 1999 Object Management Group. Unlimited rights to
 duplicate and use this code are hereby granted provided that this
 copyright notice is included.
 *****/

package org.omg.CORBA;


abstract public class TypeCode implements org.omg.CORBA.portable.IDLEntity {

    abstract public boolean equal(org.omg.CORBA.TypeCode tc);

    abstract public boolean equivalent(org.omg.CORBA.TypeCode tc);

    abstract public org.omg.CORBA.TypeCode get_compact_typecode();

    abstract public org.omg.CORBA.TCKind kind();

    abstract public java.lang.String id() throws
                org.omg.CORBA.TypeCodePackage.BadKind;

    abstract public java.lang.String name() throws
                org.omg.CORBA.TypeCodePackage.BadKind;

    /** @see edu.uci.ece.zen.orb.TypeCode#member_count()
     */
    abstract public int member_count() throws 
        org.omg.CORBA.TypeCodePackage.BadKind;

    /** @see edu.uci.ece.zen.orb.TypeCode#member_name(int)
     */
    abstract public java.lang.String member_name(int index) throws
        org.omg.CORBA.TypeCodePackage.BadKind,
        org.omg.CORBA.TypeCodePackage.Bounds;

    /** @see edu.uci.ece.zen.orb.TypeCode#member_type(int)
     */
    abstract public org.omg.CORBA.TypeCode member_type(int index) throws
        org.omg.CORBA.TypeCodePackage.BadKind,
        org.omg.CORBA.TypeCodePackage.Bounds;

    /** @see edu.uci.ece.zen.orb.TypeCode#member_label(int)
     */
    abstract public org.omg.CORBA.Any member_label(int index) throws
        org.omg.CORBA.TypeCodePackage.BadKind,
        org.omg.CORBA.TypeCodePackage.Bounds;

    /** @see edu.uci.ece.zen.orb.TypeCode#discriminator_type()
     */
    abstract public org.omg.CORBA.TypeCode discriminator_type() throws
        org.omg.CORBA.TypeCodePackage.BadKind;

    /** @see edu.uci.ece.zen.orb.TypeCode#default_index()
     */ 
    abstract public int default_index() throws
        org.omg.CORBA.TypeCodePackage.BadKind;

    /** @see edu.uci.ece.zen.orb.TypeCode#length()
     */
    abstract public int length() throws 
        org.omg.CORBA.TypeCodePackage.BadKind;

    /** @see edu.uci.ece.zen.orb.TypeCode#content_type()
     */
    abstract public org.omg.CORBA.TypeCode content_type() throws
        org.omg.CORBA.TypeCodePackage.BadKind;

    /** @see edu.uci.ece.zen.orb.TypeCode#member_visibility(int)
     */
    abstract public short member_visibility(int index) throws
        org.omg.CORBA.TypeCodePackage.BadKind, 
        org.omg.CORBA.TypeCodePackage.Bounds;




    public short fixed_digits() throws
        org.omg.CORBA.TypeCodePackage.BadKind {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    public short fixed_scale() throws
        org.omg.CORBA.TypeCodePackage.BadKind {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    

    /** @see edu.uci.ece.zen.orb.TypeCode#type_modifier()
     */
    abstract public short type_modifier()
        throws org.omg.CORBA.TypeCodePackage.BadKind;

    /** @see edu.uci.ece.zen.orb.TypeCode#concrete_base_type()
     */
    abstract public org.omg.CORBA.TypeCode concrete_base_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind;

}
