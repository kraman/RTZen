/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.dynany;

import org.omg.DynamicAny.*;

/**
 * Parent for DynStruct and DynValue classes, stores method
 * implementations common to each.
 *
 * <p>
 * IMPORTANT NOTE:
 * <p>
 * use this.type.member_count() for the count of items in members
 * use this.currentPosition for the element of members currently
 * being examined.
 *
 * @author Bruce Miller
 * @version $Revision: 1.1 $ $Date: 2004/01/21 00:52:59 $
 */

public abstract class DynStructOrValue extends DynAny {

    protected org.omg.DynamicAny.NameDynAnyPair [] members;

    // Dynamic Anys for Exceptions are represented as DynStruct
    // objects.  Since an exception may have a message associated with
    // it, the message is stored here in the variable asExceptionMsg.
    protected String asExceptionMsg;


    DynStructOrValue( org.omg.DynamicAny.DynAnyFactory dynFactory,
              org.omg.CORBA.TypeCode tc,
              org.omg.CORBA.ORB anOrb) 
        throws //org.omg.DynamicAny.DynAnyPackage.InvalidValue, 
               org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        
        super(dynFactory, tc, anOrb);

        try {
            checkAcceptableType();

            if( type.kind().value() == org.omg.CORBA.TCKind._tk_except ) {
                asExceptionMsg = type.id();
            }

            // Initialize members of the struct or exception
            componentCount = type.member_count();
            members = new NameDynAnyPair[componentCount];
            for( int i = 0 ; i < componentCount; i++ ) {
                org.omg.CORBA.TypeCode elementTC = edu.uci.ece.zen.orb.TypeCode.originalType(type.member_type(i));
                edu.uci.ece.zen.orb.dynany.DynAny elementDynAny = (edu.uci.ece.zen.orb.dynany.DynAny) dynAnyFactory.create_dyn_any_from_type_code(elementTC);
                elementDynAny.isComponent = true;

                members[i] = new NameDynAnyPair(type.member_name(i), elementDynAny);
            }
        }
        // InvalidTypeCode, BadKind, or Bounds exceptions may be thrown
        catch( Exception e ) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "<init>", "Unable to create DynStruct correctly, but trying to continue anyway", e);
        }

        // Set the currentPosition.
        setInitialPosition();

    }



    /** 
     * @see DynAny#from_any(org.omg.CORBA.Any)
     */ 
    public void from_any(org.omg.CORBA.Any value) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {

        checkDestroyed();
        checkAssign(this.type, value.type());

        this.type = edu.uci.ece.zen.orb.TypeCode.originalType( value.type() );

        try {
            org.omg.CORBA.portable.InputStream is = value.create_input_stream();
            
            componentCount = type.member_count();
            members = new NameDynAnyPair[componentCount];

            // Read the exception message
            if ( isException() ) {
                asExceptionMsg = is.read_string();
            }
            // Read the members
            for (int i = 0; i < componentCount; i++) {
                edu.uci.ece.zen.orb.any.Any a = (edu.uci.ece.zen.orb.any.Any) orb.create_any();
                org.omg.CORBA.TypeCode memberIType = edu.uci.ece.zen.orb.TypeCode.originalType( type.member_type(i) );
                a.read_value(is, memberIType);

                DynAny da = (edu.uci.ece.zen.orb.dynany.DynAny) dynAnyFactory.create_dyn_any(a); //new DynAny(dynAnyFactory, memberIType, orb);
                //da.from_any(a);
                da.isComponent = true;

                members[i] = new NameDynAnyPair(type().member_name(i), da);
            }
        }
        // The TypeCode.member_type() method may through Bounds or BadKind exception
        catch (Exception e) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue("Initialization failed" + e);
        }

    }



    /** 
     * @see DynAny#to_any()
     */ 
    public org.omg.CORBA.Any to_any() {
        checkDestroyed();

        edu.uci.ece.zen.orb.any.Any retAny = (edu.uci.ece.zen.orb.any.Any) orb.create_any();
        retAny.type( DynAny.copyTypeCode(this.type) );

        edu.uci.ece.zen.orb.CDROutputStream cdr = (edu.uci.ece.zen.orb.CDROutputStream) retAny.create_output_stream();

        // If this is an exception, write out the exception message
        if (type.kind().value() == org.omg.CORBA.TCKind._tk_except) {
            cdr.write_string(asExceptionMsg);
        }

        for (int i = 0; i < members.length; i++) {
            cdr.write_value ( members[i].value.type(), members[i].value.to_any().create_input_stream());
        }

        return retAny;
    }



    /** 
     * @see DynAny#destroy()
     */ 
    public void destroy() {
        super.destroy();
        members = null;
    }


    // Same as in DynAny
    // copy


    /** 
     * @see DynAny#getRepresentation()
     */ 
    protected org.omg.CORBA.Any getRepresentation() {
        return members[position].value.to_any();
    }




    /** 
     * @see DynAny#current_component()
     */ 
    public org.omg.DynamicAny.DynAny current_component() 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch {

        checkDestroyed ();

        // Throw TypeMismatch if this can't contain members
        if (componentCount == 0) {
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("Struct, Exception, or ValueType was empty");
        }
        // If the position is not valid, return null reference
        if ( (position < 0) || (position >= members.length) ) {
            return null;
        }
        // Finally, if there was a valid element
        return members[position].value;
    }



    /**
     * Returns the name of the member at the current position.
     *
     * <p> See CORBA v2.3 Spec page 9-16
     *
     * @return String name of the member at the current position.
     * @throws TypeMismatch if this contains an empty exception
     * @throws InvalidValue if the current position does not indicated a member
     */
    public String current_member_name()         
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
               org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        checkPositionValid();

        return members[position].id;
    }



    /**
     * Returns the TCKind associated with the member at the current position.
     *
     * <p> See CORBA v2.3 Spec page 9-16
     *
     * @return TCKind associated with the member at the current position.
     * @throws TypeMismatch if this contains an empty exception
     * @throws InvalidValue if the current position does not indicated a member
     */
    public org.omg.CORBA.TCKind current_member_kind() 
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
               org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();
        checkPositionValid();

        return members[position].value.type().kind();
    }



    /**
     * Return the current members as an array of
     * org.omg.DynamicAny.NameValuePair objects.
     *
     * <p> See CORB v2.3 page 9-16
     */
    public NameValuePair[] get_members() {
        checkDestroyed();

        NameValuePair [] nvp = new NameValuePair [members.length];

        for (int i = 0; i < componentCount; i++) {
            nvp[i].id = members[i].id;
            nvp[i].value = members[i].value.to_any();
        }
        return nvp;
    }



    /**
     * Initialize the structure members of this DynAny object to nvp.
     *
     * @throws InvalidValue if the length of nvp doesn't match this' length
     * @throws TypeMismatch if the types of nvp's members are not equivalent or the names of nvp's members are given and aren't equal to members of this
     */
    public void set_members( NameValuePair [] nvp ) 
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
               org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed();

        if ( componentCount != nvp.length ) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue("Setting number of elements does not match current number of elements");
        }
        
        for (int i = 0; i < componentCount; i++) {
            // If a sequence element has a typecode that is not
            // equivalent to the corresponding element of this DynAny
            // in order, then throw TypeMismatch.
            if ( ! members[i].value.type().equivalent( nvp[i].value.type() ) ) {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
            }

            // If an id was specified, then it must either match the
            // corresponidng memmber name in the current DynStruct or
            // be an empty string.
            if (nvp[i].id != null) {
                if ( ( ! nvp[i].id.equals( members[i].id) ) && ( ! nvp[i].id.equals("") ) ) {
                    throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
                }
            }
            
            DynAny da = new DynAny(dynAnyFactory, nvp[i].value.type(), orb);
            da.from_any(nvp[i].value);
            da.isComponent = true;
            members[i].value = da;
        }

        // Set the currentPosition to 0 or 1
        setInitialPosition();
    }


   
    /**
     * Like get_members, but returns array of type NameDynAnyPair.
     *
     * @see #get_members()
     */
    public org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any() {
        checkDestroyed ();
        
        NameDynAnyPair[] ndap = new NameDynAnyPair[componentCount];
        for (int i = 0; i < componentCount; i++) {
            ndap[i] = new NameDynAnyPair( members[i].id, 
                                          members[i].value.copy() ); 
        }
        return ndap;
    }



    /**
     * Like set_members, but takes array of type NameDynAnyPair.
     *
     * @see #set_members(NameValuePair[])
     */
    public void set_members_as_dyn_any(org.omg.DynamicAny.NameDynAnyPair[] ndap) 
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,
               org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        checkDestroyed ();

        if( componentCount != ndap.length ) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue("Setting number of elements does not match current number of elements");
        }

        for( int i = 0; i < componentCount; i++ ) {
            // If a sequence element has a typecode that is not
            // equivalent to the corresponding element of this DynAny
            // in order, then throw TypeMismatch.
            if ( ! members[i].value.type().equivalent( ndap[i].value.type() ) ) {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
            }

            // If an id was specified, then it must either match the
            // corresponidng memmber name in the current DynStruct or
            // be an empty string.
            if (ndap[i].id != null) {
                if ( ( ! ndap[i].id.equals( members[i].id) ) && ( ! ndap[i].id.equals("") ) ) {
                    throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
                } 
            }
	    
            DynAny da = (DynAny) ndap[i].value.copy();
            da.isComponent = true;
            members[i].value = da;
        }	
    }



    /**
     * Simply returns if this' type variable is set appropriately for
     * the the kind of objects that this object can store.  For
     * instance, a DynStruct returns true if this' type indicates that
     * it is storing a struct or an exception.  If the type stored is
     * not valid for this kind of object, it throws a TypeMismatch
     * exception.
     * @throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
     */
    protected abstract void checkAcceptableType()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch;



    /**
     * Returns the type of the i-th member.
     *
     * @return TypeCode of the i-th member.
     */
    protected org.omg.CORBA.TypeCode memberType(int i) {
        org.omg.CORBA.TypeCode retCode = null;
        try {
            retCode = edu.uci.ece.zen.orb.TypeCode.originalType( this.type.member_type(i) );
        }
        // Won't happen, but catch member_type's BadKind exception
        catch ( org.omg.CORBA.TypeCodePackage.BadKind bk ) {
            handleShouldntHappenException(bk);
        }
        catch ( org.omg.CORBA.TypeCodePackage.Bounds b ) {
            handleShouldntHappenException(b);
        }
        return retCode;
    }



    /**
     * @return true if this is an exception, false otherwise.
     */
    private boolean isException() {
        return type.kind().value() == org.omg.CORBA.TCKind._tk_except;
    }



    /**
     * Set the position variable as appropriate for a newly created or
     * initialized DynStruct.
     */
    private void setInitialPosition() {
        // Empty structures and exceptions are initialized to position of -1, all
        // others get position of 0.
        if( componentCount == 0) {
            position = -1;
        }
        else {
            position = 0;
        }
    }




    /**
     * Determine whether this is an empty exception and whether the
     * current position is valid.  Is helper to current_member_name()
     * and current_member_kind().
     *
     * @throws TypeMismatch if this is an exception that is empty
     * @throws InvalidValue if the current position does not indicate a member
     */
    private void checkPositionValid() 
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue, 
               org.omg.DynamicAny.DynAnyPackage.TypeMismatch { 

        try {
            if ( isException() && (type.member_count() == 0) ) {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("Exception was empty");
            }
        }
        // This will never happen, but is required for member_count() method
        catch (org.omg.CORBA.TypeCodePackage.BadKind bk) {
            ZenProperties.logger.log(Logger.SEVERE, getClass(), "checkPositionValid()", bk);
        }


        if ( (position < 0) || (position >= members.length) ) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
    }



}
