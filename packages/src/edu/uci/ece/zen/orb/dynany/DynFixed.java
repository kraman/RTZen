/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

// Note: We don't actually support the fixed data type as of 2003/12/01

package edu.uci.ece.zen.orb.dynany;

// See CORBA v2.3 Spec page 9-14, section 9.2.5

/**
 * Subtype of DynAny that is used to dynamically parse and create Any
 * objects that represent Fixed data type.
 *
 * @author Bruce Miller
 * @version $Revision: 1.1 $ $Date: 2004/01/21 00:52:59 $
 */

public class DynFixed extends DynAny implements org.omg.DynamicAny.DynFixed {

    DynFixed ( org.omg.DynamicAny.DynAnyFactory factory, org.omg.CORBA.TypeCode tc, org.omg.CORBA.ORB _orb) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {

        super ( factory, tc, _orb, org.omg.CORBA.TCKind.tk_fixed);

        anyRepresentation = orb.create_any();
        // Default value from page 9-9 of spec.
        anyRepresentation.insert_fixed( new java.math.BigDecimal("0"), tc);
    }



    /** 
     * @see DynAny#from_any(org.omg.CORBA.Any)
     */ 
    public void from_any(org.omg.CORBA.Any value)
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
               org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        checkAssign(this.type(), value.type());

        //anyRepresentation = orb.create_any();
        this.type = value.type();
        anyRepresentation.insert_fixed(value.extract_fixed(), this.type);
    }



    /** 
     * @see DynAny#to_any()
     */ 
    // Doesn't need to be overridden for DynFixed
    //public org.omg.CORBA.Any to_any()



    public org.omg.DynamicAny.DynAny current_component() {
        return this;
    }


    // Doesn't need to be overriden
    // public boolean equal(org.omg.DynamicAny.DynAny dynany)

    // Same as for parent
    // public void destroy() {
    // }

    // Doesn't need to be overridden
    // public org.omg.DynamicAny.DynAny copy()

    // Doesn't need to be overidden 
    // getRepresentation()


    /**
     * Retrieve the value of this DynFixed.
     */
    public String get_value() {
        return anyRepresentation.extract_fixed().toString();
    }



    /**
     * Set the value of this DynFixed
     */
    public boolean set_value( String newValue ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        // Remove trailing whitespace and/or d character
        newValue = newValue.trim();
        if ( newValue.endsWith("D") || newValue.endsWith("d") ) {
            newValue = newValue.substring(0, newValue.length() - 1);
        }

        try {
             java.math.BigDecimal newFixed = new java.math.BigDecimal(newValue);
        }
        catch (NumberFormatException nfe) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch(nfe.toString());
        }

        boolean wasTruncated = false;

        // Number of fractional digits, digits to right of decimal point
        int fracAmt = newValue.length() - newValue.lastIndexOf(".");
        // Number of non-fractional digits and decimal point
        int digitsAmt = newValue.lastIndexOf(".");

        try {
            // Truncate trailing fractional part if necessary
            if ( fracAmt > type.fixed_scale() ) {
                int truncateAmount = fracAmt - type.fixed_scale();
                newValue = newValue.substring(0, newValue.length() - truncateAmount);
                wasTruncated = true;
            }
            // Value to be set has a scale that exceeds that of the
            // current DynFxed.
            if ( digitsAmt > type.fixed_digits() ) {
                throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue("Value to large to be stored");
            }

            // Insert into representation.
            java.math.BigDecimal newBD = new java.math.BigDecimal(newValue);
            anyRepresentation.insert_fixed(newBD, this.type);
        }

        catch ( org.omg.CORBA.TypeCodePackage.BadKind ex ) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        }

        return ! wasTruncated;
    }




}
