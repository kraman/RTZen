/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.dynany;

/**
 * DynValue provides interface for Dynamic Anys representing Value
 * Type Objects.
 *
 * @author Bruce Miller
 * @version $Revision: 1.1 $ $Date: 2004/01/21 00:52:59 $
 */

public class DynValue extends DynStructOrValue implements org.omg.DynamicAny.DynValue {

    // use this.type.member_count() for the count of items in members
    // use this.currentPosition for the element of members currently
    // being examined.


    /**
     * Whether this is storing a null or a value type object.
     */
    protected boolean isNull;



    DynValue( org.omg.DynamicAny.DynAnyFactory dynFactory,
              org.omg.CORBA.TypeCode tc,
              org.omg.CORBA.ORB anOrb)
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue, 
               org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        
        super(dynFactory, tc, anOrb);

    }



    /**
     * @see DynStructOrValue#checkAcceptableType()
     */
    protected void checkAcceptableType() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        org.omg.CORBA.TypeCode origTC = edu.uci.ece.zen.orb.TypeCode.originalType(this.type);
        if (origTC.kind().value() != org.omg.CORBA.TCKind._tk_value) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
    }



    // In DynStructOrValue
    // memberType

    // In DynStructOrValue
    // from_any

    // In DynStructOrValue
    // to_any


    /** 
     * @see DynAny#equal(org.omg.DynamicAny.DynAny)
     */ 
    public boolean equal (org.omg.DynamicAny.DynAny anotherDyn) {
        checkDestroyed();
        return equal(anotherDyn, componentCount);
    }


    // In DynStructOrValue
    // destroy()

    // Same as in DynAny
    // copy

    // Same as in DynStructOrValue
    // getRepresentation

    // Same as in DynStructOrValue
    // current_component()

    // Same as in DynStructOrValue
    // current_member_name()

    // Same as in DynStructOrValue
    // current_member_kind()

    // Same as in DynStructOrValue
    // get_members()

    // Same as in DynStructOrValue
    // set_members()

    // Same as in DynStructOrValue
    // get_members_as_dyn_any()

    // Same as in DynStructOrValue
    // set_members_as_dyn_any()



    /**
     * Returns true if this is supposed to be null, false otherwise.
     * Inherited from org.omg.DynamicAny.DynValue but not mentioned in
     * the CORBA V2.3 spec.
     */
    public boolean is_null() {
        return isNull;
    }

    /**
     * Sets this to supposed to be null.  Inherited from
     * org.omg.DynamicAny.DynValue but not mentioned in the CORBA V2.3
     * spec.
     */
    public void set_to_null() {
        isNull = true;
    }

    /**
     * Sets this to supposed to be a value.  Inherited from
     * org.omg.DynamicAny.DynValue but not mentioned in the CORBA V2.3
     * spec.
     */
    public void set_to_value() {
        isNull = false;
    }

}
