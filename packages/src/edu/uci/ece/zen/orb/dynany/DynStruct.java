/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

// replace asExceptionMsg with DynAny's any
package edu.uci.ece.zen.orb.dynany;

import org.omg.DynamicAny.*;

/**
 * Subtype of DynStructOrValue that is used to dynamically parse and
 * create Any objects that represent Structs.
 *
 * @author Bruce Miller
 * @version $Revision: 1.1 $ $Date: 2004/01/21 00:52:59 $
 */

public class DynStruct extends DynStructOrValue implements org.omg.DynamicAny.DynStruct {


    DynStruct ( org.omg.DynamicAny.DynAnyFactory factory, org.omg.CORBA.TypeCode tc, org.omg.CORBA.ORB _orb ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        super ( factory, tc, _orb );
    }



    /**
     * @see DynStructOrValue#checkAcceptableType()
     */
    protected void checkAcceptableType() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        org.omg.CORBA.TypeCode origTC = edu.uci.ece.zen.orb.TypeCode.originalType(this.type);
        if (origTC.kind().value() != org.omg.CORBA.TCKind._tk_struct &&
            origTC.kind().value() != org.omg.CORBA.TCKind._tk_except) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }
    }



    /** 
     * @see DynAny#equal(org.omg.DynamicAny.DynAny)
     */ 
    public boolean equal (org.omg.DynamicAny.DynAny anotherDyn) {
        checkDestroyed();

        /* Nothing in the CORBA v2.3 spec Chapter 9 indicates that we
           need to compare the exception messages

        // For two exceptions, check that their message is the same.
        if ( (this.type.kind().value() == org.omg.CORBA.TCKind) && this.type.equivalent( anotherDyn.type() ) ) {
            if ( ! this.asExceptionMsg.equal( ((DynStruct) anotherDyn).asExceptionMsg) ) {
                return false;
            }
        }
        */

        return equal(anotherDyn, componentCount);
    }



    /** 
     * @see DynStructOrValue#destroy()
     */ 
    public void destroy() {
        super.destroy();
        asExceptionMsg = null;
    }




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


}
