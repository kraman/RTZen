/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.dynany;

import org.omg.CORBA.TCKind;

/**
 * Subtype of DynStruct that is used to dynamically parse and create
 * Any objects that represent Unions.
 *
 * @author Bruce Miller
 * @version $Revision: 1.4 $ $Date: 2004/01/29 20:47:18 $
 */

public class DynUnion extends DynAny implements org.omg.DynamicAny.DynUnion {

    private org.omg.CORBA.Any discriminator;
    private org.omg.DynamicAny.DynAny theMember;

    /** Index into the various members of the TypeCode this.type that is currently used to represent the union. */
    private int memberIndex;
    // To access the name of the current member, call private method memberName(memberIndex);


    DynUnion( org.omg.DynamicAny.DynAnyFactory factory, org.omg.CORBA.TypeCode tc, org.omg.CORBA.ORB _orb)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {

        super (factory, tc, _orb, org.omg.CORBA.TCKind.tk_union);

        position = 0;
        componentCount = 2;

        discriminator = org.omg.CORBA.ORB.init().create_any();
    }        



    int selectedLabelIndex() {
        return findSelectedLabelIndex(discriminator);
    }


    /**
     * Determine which member_label of the Union that
     * <code>candidate</code> is equal to.
     */
    int findSelectedLabelIndex(org.omg.CORBA.Any candidate) {
        try {
            for (int i = 0; i < type.member_count(); i++) {
                if ( discriminator.equal( type.member_label(i) ) ) {
                    return i;
                }
            }
        }
        // Must be caught from member_count(), but should never be thrown
        catch (org.omg.CORBA.TypeCodePackage.BadKind bke) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "findSelectedLabelIndex", bke);
        }
        catch (org.omg.CORBA.TypeCodePackage.Bounds be) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "findSelectedLabelIndex", be);
        }
        return -1;
    }



    /** @see DynAny#from_any(org.omg.CORBA.Any)
     */
    public void from_any (org.omg.CORBA.Any value) 
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
               org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        try {
            
            if ( ! this.type.equivalent(value.type()) ) {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
            }
            
            this.type = edu.uci.ece.zen.orb.TypeCode.originalType( value.type() );
            
            org.omg.CORBA.portable.InputStream is = value.create_input_stream();
            
            discriminator.type( type.discriminator_type());
            discriminator.read_value(is, type.discriminator_type());
            
            org.omg.CORBA.Any member_any = orb.create_any();
            
            // Find the member that is currently selected, then create an
            // any for it, and read the value.
            memberIndex = selectedLabelIndex();
            if (memberIndex == -1) {
                memberIndex = type.default_index();
            }
            if (memberIndex >= 0) {
                member_any.read_value( is, type.member_type(memberIndex) );
            }
            
            if( member_any != null ) {
                theMember = dynAnyFactory.create_dyn_any( member_any );
            }
            else {
                theMember = null;
            }
            
            componentCount = 2;
        }
        // Discriminator_type can throw BadKind, member_type() can
        // throw Bounds, create_dyn_any() can throw
        // InconsistentTypeCode
        catch ( org.omg.CORBA.TypeCodePackage.Bounds be ) {
            handleShouldntHappenException(be);
        }   	
        catch ( org.omg.CORBA.TypeCodePackage.BadKind bke ) {
            handleShouldntHappenException(bke);
        }
        catch ( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode ite ) {
            handleShouldntHappenException(ite);
        }
    }



    /** @see DynAny#to_any()
     */
    public org.omg.CORBA.Any to_any()  {
        checkDestroyed ();
        
        edu.uci.ece.zen.orb.any.Any retAny = (edu.uci.ece.zen.orb.any.Any) orb.create_any();
        retAny.type( DynAny.copyTypeCode(this.type) );
        
        edu.uci.ece.zen.orb.CDROutputStream cdr = (edu.uci.ece.zen.orb.CDROutputStream) retAny.create_output_stream();
        
        cdr.write_value( discriminator.type(), (edu.uci.ece.zen.orb.CDRInputStream) discriminator.create_input_stream() );
        cdr.write_value( theMember.type(), (edu.uci.ece.zen.orb.CDRInputStream) theMember.to_any().create_input_stream());
        
        return retAny;
    }



    /** @see DynAny#equal(DynAny)
     */
    public boolean equal( org.omg.DynamicAny.DynAny anotherDyn ) {
        return equal(anotherDyn, component_count());
    }

    

    /** 
     * @see DynAny#destroy()
     */ 
   public void destroy() {
      super.destroy();
      discriminator = null;
      theMember = null;
      memberIndex = -1;
   }



   // Same as in DynAny
   // copy



    // Should only be used as a helper method to DynAny#equal(), should not be called anywhere else 
    /** @see DynAny#getRepresentation()
     */
    protected org.omg.CORBA.Any getRepresentation() {
        if (position == 0)
            return discriminator;
        else
            return theMember.to_any();
    }


    /**
     * @see DynAny#current_component()
     */
    public org.omg.DynamicAny.DynAny current_component() {
        checkDestroyed();

        try {
            if (position == 0) {
                return dynAnyFactory.create_dyn_any(discriminator);
            }
            else {
                return theMember;
            }
        }
        catch (Exception e) {
            handleShouldntHappenException(e);
            return null;
        }
    }



    // Reverse the role of this with "has_no_active_member"
    public int component_count () {
        if ( has_no_active_member() ) {
            return 1;
        }
        return 2;
    }



    /** Returns the current discriminator value of the Union, as a
     *  DynAny.
     *  @return DynAny discriminator value of union
     */ 
    public org.omg.DynamicAny.DynAny get_discriminator() {
        checkDestroyed();
        org.omg.DynamicAny.DynAny retDyn = null;
        try {
            retDyn = dynAnyFactory.create_dyn_any( discriminator );
        }
        // create_dyn_any() can throw InconsistentTypeCode
        catch (Exception e) {
            handleShouldntHappenException(e);
        }
        return retDyn;
    }



    /** Sets the discriminator of this DynUnion.
     *
     * <p> See CORBA v2.3 Spec page 9-18
     * @throws TypeMismatch if <code>d</code> doesn't match type of current discriminator.
     */
    public void set_discriminator( org.omg.DynamicAny.DynAny d) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {

        checkDestroyed();

        if ( ! d.type().equivalent( discriminator.type() ) ) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("Assigned discriminator type not equivalent to current");
        }

        try {
            int currentlyActiveMemberIndex = selectedLabelIndex();
            
            discriminator = d.to_any();

            memberIndex = selectedLabelIndex();
            
            // If there was no member index, try to set to a default case.
            if (memberIndex == -1) {
                memberIndex = type.default_index();
            }
            
            // If there still is no active member
            if (memberIndex == -1) {
                theMember = null;
                position = 0;
                componentCount = 1;
            }
            else if (memberIndex != currentlyActiveMemberIndex) {
                theMember = dynAnyFactory.create_dyn_any_from_type_code( type.member_type(memberIndex) );
                position = 1;
                componentCount = 2;
            }

        }
        // TypeCode handling methods can throw BadKind or Bounds, factory method can throw InconsistentTypeCode
        catch (org.omg.CORBA.TypeCodePackage.Bounds be) {
            handleShouldntHappenException(be);
        }
        catch (org.omg.CORBA.TypeCodePackage.BadKind bke) {
            handleShouldntHappenException(bke);
        }
        catch (org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode ie) {
            handleShouldntHappenException(ie);
        }
    }



    /** Sets the discriminator to a value that is consistent with the
     * value of the default case of the union Our implementation does
     * this by copying the value of the discriminator for the default
     * case of the union.
     *
     * <p> See CORBA v2.3 spec page 9-18
     */
    public void set_to_default_member() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        try {
            checkDestroyed();
            if ( type.default_index() == -1) {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("No default member");
            }
            
            memberIndex = type.default_index();
            discriminator.read_value( type.member_label(memberIndex).create_input_stream(), type.discriminator_type() );
            position = 0;
            componentCount = 2;
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind bk ) {
            handleShouldntHappenException(bk);
        }
        catch ( org.omg.CORBA.TypeCodePackage.Bounds b ) {
            handleShouldntHappenException(b);
        }
    }


    /**
     * Sets the discriminator to a value that does not correspond to
     * any of the union's case labels, and position to zero, and
     * component count to 1.
     *
     * <p> See CORBA v2.3 Spec page 9-18.
     * @throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch if all possible discriminator values are used for its type or there is an explicit default case
     */

    public void set_to_no_active_member()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        // If there is an explicit default case, throw TypeMismatch
        try {
            if (type.default_index() != -1) {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
            }
        }
        // default_index can throw BadKind
        catch ( org.omg.CORBA.TypeCodePackage.BadKind bk) {
            handleShouldntHappenException(bk);
        }
        // Try to set the discriminator to a value that does not
        // correspond to any of the union's case labels, but throw
        // TYpeMismatch if all case labels are used.
        if ( ! setDiscriminatorNoCaseLabel() ) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("All possible cases used");
        }
        position = 0;
        componentCount = 1;
    }


    /**
     * Set the descriminator to refer to an element of the appropriate
     * discriminator_type but having no case label.  Returns true if
     * successful, false otherwise.
     *
     * @return true if successfully set, false otherwise
     * @throws TypeMismatch if an unexpected error occurs
     */
    private boolean setDiscriminatorNoCaseLabel() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {

        org.omg.CORBA.Any candidateDiscrim = orb.create_any();

        try {
            int kindVal = edu.uci.ece.zen.orb.TypeCode.originalType(type.discriminator_type()).kind().value();

            switch(kindVal) {
                
            case TCKind._tk_short: {
                // Try every possible candidate value i for discriminator that
                // could be unused.
                short i;
                for (i = java.lang.Short.MIN_VALUE; i < java.lang.Short.MAX_VALUE; i++) {
                    boolean foundI = false;

                    // Now go through each member label and see if it
                    // is equal to our candidate i.  If it is, we have
                    // to get a new i.
                    int j;
                    for (j = 0; j < type.member_count(); j++) {
                        if (type.member_label(j).extract_short() == i) {
                            foundI = true;
                            break;
                        }
                    }

                    // If i was equal to a member label, try the next i.
                    if (foundI == true) {
                        continue;
                    }
                }

                // If there are no values not used as labels, return false.
                if ( i == java.lang.Short.MAX_VALUE ) {
                    return false;
                }
                // Otherwise, we found an unused value to use
                candidateDiscrim.insert_short( (short) i);
                break;
            }

            case TCKind._tk_ushort: {
                // Try every possible candidate value i for discriminator that
                // could be unused.
                short i;
                for (i = 0; i < java.lang.Short.MAX_VALUE; i++) {
                    boolean foundI = false;

                    // Now go through each member label and see if it
                    // is equal to our candidate i.  If it is, we have
                    // to get a new i.
                    int j;
                    for (j = 0; j < type.member_count(); j++) {
                        if (type.member_label(j).extract_ushort() == i) {
                            foundI = true;
                            break;
                        }
                    }

                    // If i was equal to a member label, try the next i.
                    if (foundI == true) {
                        continue;
                    }
                }

                // If there are no values not used as labels, return false.
                if ( i == java.lang.Short.MAX_VALUE ) {
                    return false;
                }
                // Otherwise, we found an unused value to use
                candidateDiscrim.insert_ushort( (short) i);
                break;
            }

            case TCKind._tk_long: {
                // Try every possible candidate value i for discriminator that
                // could be unused.
                int i;
                for (i = java.lang.Integer.MIN_VALUE; i < java.lang.Integer.MAX_VALUE; i++) {
                    boolean foundI = false;

                    // Now go through each member label and see if it
                    // is equal to our candidate i.  If it is, we have
                    // to get a new i.
                    int j;
                    for (j = 0; j < type.member_count(); j++) {
                        if (type.member_label(j).extract_long() == i) {
                            foundI = true;
                            break;
                        }
                    }

                    // If i was equal to a member label, try the next i.
                    if (foundI == true) {
                        continue;
                    }
                }

                // If all values were used.
                if ( i == java.lang.Integer.MAX_VALUE ) {
                    return false;
                }
                // Otherwise, we found an unused value to use
                candidateDiscrim.insert_long(i);
                break;
            }

            case TCKind._tk_ulong: {
                // Try every possible candidate value i for discriminator that
                // could be unused.
                int i;
                for (i = 0; i < java.lang.Integer.MAX_VALUE; i++) {
                    boolean foundI = false;

                    // Now go through each member label and see if it
                    // is equal to our candidate i.  If it is, we have
                    // to get a new i.
                    int j;
                    for (j = 0; j < type.member_count(); j++) {
                        if (type.member_label(j).extract_ulong() == i) {
                            foundI = true;
                            break;
                        }
                    }

                    // If i was equal to a member label, try the next i.
                    if (foundI == true) {
                        continue;
                    }
                }

                // If all values were used.
                if ( i == java.lang.Integer.MAX_VALUE ) {
                    return false;
                }
                // Otherwise, we found an unused value to use
                candidateDiscrim.insert_ulong(i);
                break;
            }

            case TCKind._tk_longlong: {
                // Try every possible candidate value i for discriminator that
                // could be unused.
                long i;
                for (i = java.lang.Long.MIN_VALUE; i < java.lang.Long.MAX_VALUE; i++) {
                    boolean foundI = false;

                    // Now go through each member label and see if it
                    // is equal to our candidate i.  If it is, we have
                    // to get a new i.
                    int j;
                    for (j = 0; j < type.member_count(); j++) {
                        if (type.member_label(j).extract_longlong() == i) {
                            foundI = true;
                            break;
                        }
                    }

                    // If i was equal to a member label, try the next i.
                    if (foundI == true) {
                        continue;
                    }
                }

                // If there are no values not used as labels, return false.
                if ( i == java.lang.Long.MAX_VALUE ) {
                    return false;
                }
                // Otherwise, we found an unused value to use
                candidateDiscrim.insert_longlong( (long) i);
                break;
            }
                
            case TCKind._tk_ulonglong: {
                // Try every possible candidate value i for discriminator that
                // could be unused.
                long i;
                for (i = 0; i < java.lang.Long.MAX_VALUE; i++) {
                    boolean foundI = false;

                    // Now go through each member label and see if it
                    // is equal to our candidate i.  If it is, we have
                    // to get a new i.
                    int j;
                    for (j = 0; j < type.member_count(); j++) {
                        if (type.member_label(j).extract_ulonglong() == i) {
                            foundI = true;
                            break;
                        }
                    }

                    // If i was equal to a member label, try the next i.
                    if (foundI == true) {
                        continue;
                    }
                }

                // If there are no values not used as labels, return false.
                if ( i == java.lang.Long.MAX_VALUE ) {
                    return false;
                }
                // Otherwise, we found an unused value to use
                candidateDiscrim.insert_ulonglong( (long) i);
                break;
            }
                

            case TCKind._tk_boolean: {
                candidateDiscrim.insert_boolean(true);
                if ( findSelectedLabelIndex(candidateDiscrim) != -1) {
                    candidateDiscrim.insert_boolean(false);
                    if ( findSelectedLabelIndex(candidateDiscrim) != -1) {
                        // Unable to find a boolean that is not in use as a label
                        return false;
                    }
                }
                break;
            }

                // CORBA Char is only 0 to 255
            case TCKind._tk_char: {
                // Try every possible candidate value i for discriminator that
                // could be unused.
                char i;
                for (i = 0; i < 256; i++) {
                    boolean foundI = false;

                    // Now go through each member label and see if it
                    // is equal to our candidate i.  If it is, we have
                    // to get a new i.
                    int j;
                    for (j = 0; j < type.member_count(); j++) {
                        if (type.member_label(j).extract_char() == i) {
                            foundI = true;
                            break;
                        }
                    }

                    // If i was equal to a member label, try the next i.
                    if (foundI == true) {
                        continue;
                    }
                }

                // If there are no values not used as labels, return false.
                if ( i == 256 ) {
                    return false;
                }
                // Otherwise, we found an unused value to use
                candidateDiscrim.insert_char( (char) i);
                break;
            }
                
            case TCKind._tk_wchar: {
                // Try every possible candidate value i for discriminator that
                // could be unused.
                char i;
                for (i = java.lang.Character.MIN_VALUE; i < java.lang.Character.MAX_VALUE; i++) {
                    boolean foundI = false;

                    // Now go through each member label and see if it
                    // is equal to our candidate i.  If it is, we have
                    // to get a new i.
                    int j;
                    for (j = 0; j < type.member_count(); j++) {
                        if (type.member_label(j).extract_wchar() == i) {
                            foundI = true;
                            break;
                        }
                    }

                    // If i was equal to a member label, try the next i.
                    if (foundI == true) {
                        continue;
                    }
                }

                // If there are no values not used as labels, return false.
                if ( i == java.lang.Character.MAX_VALUE ) {
                    return false;
                }
                // Otherwise, we found an unused value to use
                candidateDiscrim.insert_wchar( (char) i);
                break;
            }

                // Enum is treated just like ulong
            case TCKind._tk_enum: {
                // Try every possible candidate value i for discriminator that
                // could be unused.
                int i;
                for (i = 0; i < java.lang.Integer.MAX_VALUE; i++) {
                    boolean foundI = false;

                    // Now go through each member label and see if it
                    // is equal to our candidate i.  If it is, we have
                    // to get a new i.
                    int j;
                    for (j = 0; j < type.member_count(); j++) {
                        if (type.member_label(j).extract_ulong() == i) {
                            foundI = true;
                            break;
                        }
                    }

                    // If i was equal to a member label, try the next i.
                    if (foundI == true) {
                        continue;
                    }
                }

                // If there are no values not used as labels, return false.
                if ( i == java.lang.Integer.MAX_VALUE ) {
                    return false;
                }
                // Otherwise, we found an unused value to use
                candidateDiscrim.insert_ulong(i);
                break;
            }
                
                
            default:
                throw new org.omg.CORBA.NO_IMPLEMENT("Unhandled case statement tk_kind " + kindVal);

            } // End of switch
            discriminator = candidateDiscrim;
            candidateDiscrim.type( discriminator.type() );
            discriminator.type();
            return true;
        } //  end of try

        // Catches required for typecode methods that shouldn't ever raise exceptions
        catch (org.omg.CORBA.TypeCodePackage.BadKind bke) { }
        catch (org.omg.CORBA.TypeCodePackage.Bounds be) { }
        throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
    }



    /**
     * Return true if union has no active member, false
     * otherwise. Returns false for union with default label.
     */
    public boolean has_no_active_member() {
        checkDestroyed();

        try {
            if ( (type.default_index() != -1) && (selectedLabelIndex() != -1) ) {
                return false;
            }
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind bk ) {
            handleShouldntHappenException(bk);
        }
        return true;
    }



    /** Return the TCKind of the discriminator's TypeCode
     * @return the TCKind of the discriminator's TypeCode
     */
    public org.omg.CORBA.TCKind discriminator_kind() {
        checkDestroyed();
        org.omg.CORBA.TCKind tcKind = null;
        try {
            tcKind = type.discriminator_type().kind();
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind bk ) {
            handleShouldntHappenException(bk);
        }
        return tcKind;
    }



    /**
     * Returns the TCKind value of the currently active member's TypeCode.
     * @throws InvalidValue if union does not have currently active member.
     */
    public org.omg.CORBA.TCKind member_kind() 
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue {

        checkDestroyed();
        if ( has_no_active_member() ) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }

        org.omg.CORBA.TCKind tcKind = null;
        try {
            tcKind = type.member_type(memberIndex).kind();
        } // member_type can throw BadKind
        catch (Exception e) {
            handleShouldntHappenException(e);
        }
        return tcKind;
    }


    /**
     * Returns the currently active member.
     *
     * @throws InvalidValue if the union has no active member.
     */
    public org.omg.DynamicAny.DynAny member() 
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue {

        checkDestroyed();
        if ( has_no_active_member() ) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }

        return theMember;
    }


    /** Returns the name of the current active member.
     * @throws InvalidValue if the union has no active member
     */
    public String member_name() 
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        
        checkDestroyed();
        if ( has_no_active_member() ) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }

        String memberName = null;
        try {
           memberName = type.member_name(memberIndex);
        }
        // Must be caught from member_name, shouldn't happen
        catch ( org.omg.CORBA.TypeCodePackage.Bounds b) { 
            handleShouldntHappenException(b);
        }
        catch ( org.omg.CORBA.TypeCodePackage.BadKind bk) { 
            handleShouldntHappenException(bk);
        }
        return memberName;
    }

}
