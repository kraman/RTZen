
package edu.uci.ece.zen.orb.dynany;

/**
*       
*/

public abstract class DynSeqOrArray extends DynAny {

    protected org.omg.CORBA.TypeCode elementType;

    protected org.omg.CORBA.Any [] elements;

    protected static org.omg.CORBA.Any [] nullInitialValue = new org.omg.CORBA.Any [0];



    DynSeqOrArray( org.omg.DynamicAny.DynAnyFactory dynFactory,
                   org.omg.CORBA.TypeCode tc,
                   org.omg.CORBA.ORB anOrb, 
                   org.omg.CORBA.TCKind checkTckind) 
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue, 
               org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        
        super(dynFactory, tc, anOrb, checkTckind);
    }


    protected final void clearElements() {
        for (int i = 0; i < elements.length; i++) {
            elements[i] = null;
        }
    }

    // Make sure elements is clear and large enough to hold a particular size
    protected void clearElementsToComponentCount() {
        if (elements.length < componentCount) {
            elements = new org.omg.CORBA.Any [elements.length];
        }
        else {
            clearElements();
        }
    }


    public abstract void from_any_determine_component_count(org.omg.CORBA.portable.InputStream is)
        throws org.omg.CORBA.TypeCodePackage.BadKind,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue;


    /** @see DynAny#from_any(org.omg.CORBA.Any)
     */
    public void from_any( org.omg.CORBA.Any value ) 
      throws org.omg.DynamicAny.DynAnyPackage.InvalidValue, 
             org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
      checkDestroyed ();
      checkAssign(this.type, value.type() );

      try {
          type = edu.uci.ece.zen.orb.TypeCode.originalType(value.type());
          elementType = edu.uci.ece.zen.orb.TypeCode.originalType(type.content_type());
          org.omg.CORBA.portable.InputStream is = value.create_input_stream();

          from_any_determine_component_count(is);

          // Make sure elements is cleared out and large enough to
          // hold the values to be read.
          clearElementsToComponentCount();

          for( int i = 0 ; i < componentCount; i++ ) {
              edu.uci.ece.zen.orb.any.Any a = (edu.uci.ece.zen.orb.any.Any) orb.create_any();
              a.read_value( is, elementType );
              elements[i] = a;
          }   
      }
      // Required catch for TypeCode method content_type()
      catch( org.omg.CORBA.TypeCodePackage.BadKind bk ) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "from_any", bk);
      }

      // CORBA v2.3 Spec p 9-11 says position set to 0 for value that have components, -1 otherwise
      if( componentCount > 0 ) {
          position = 0;
      }
      else {
          position = -1;
      }

   }


    // Overridden in DynSequence only
    public void to_any_write_sequence_length_if_sequence(edu.uci.ece.zen.orb.CDROutputStream os) { }



    public org.omg.CORBA.Any to_any() {
        checkDestroyed ();

        edu.uci.ece.zen.orb.any.Any retAny = (edu.uci.ece.zen.orb.any.Any) orb.create_any();
        retAny.type( DynAny.copyTypeCode(type) );

        edu.uci.ece.zen.orb.CDROutputStream cdr = (edu.uci.ece.zen.orb.CDROutputStream) retAny.create_output_stream();

        to_any_write_sequence_length_if_sequence(cdr);

        for( int i = 0; i < componentCount; i++) {
            cdr.write_value( elementType, elements[i].create_input_stream());
        }

        return retAny;
    }



    public boolean equal( org.omg.DynamicAny.DynAny dyn_any ) {
        return equal(dyn_any, component_count());
    }


   protected org.omg.CORBA.Any getRepresentation() {
      return elements[position];
   }



    // Return the current component
    public org.omg.DynamicAny.DynAny current_component() {
        checkDestroyed ();
        org.omg.DynamicAny.DynAny da = null;
        if (position >= 0) {
            try {
                da = dynAnyFactory.create_dyn_any_from_type_code(elementType);
                da.from_any(elements[position]);
            }
            // Required for create_dyn_any_from_type_code method, but doesn't actually happen.
            catch( Exception e ) {
				ZenProperties.logger.log(Logger.WARN, getClass(), "current_component", e);
            }
        }
        // Else, returns null
        return da;
    }
   



    /**
     * Directly returns the elements stored in this DynArray as an
     * array of DynAny objects, each DynAny object storing one element
     * of this array.
     * 
     * <p>
     * See CORBA v2.3 Spec page 9-21
     *
     * @return Any array which is the elemnts of this' stored array
     */
    public org.omg.CORBA.Any[] get_elements() {
        checkDestroyed ();
        return elements;
    }


    public abstract void set_elements( org.omg.CORBA.Any[] value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue;


    /**
     * If one or more elements have a type that is inconsistent
     * with this' elementType, the operation raises TypeMismatch.
     */
    protected void checkSetElementsTypes( org.omg.CORBA.Any[] value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {

        // If any element of value has a TypeCode that is not
        // equivalent to the elements stored herin, raise TypeMismatch
        for (int i = 0; i < value.length; i++) {
            if( ! value[i].type().equivalent(elementType) ) {
                throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("Element type mismatch");
            }
        }
    }



    public void destroy() {
        super.destroy();
        elementType = null;
        elements = null;
    }



    /**
     * Return the elements stored in this DynArray as an array of
     * DynAny objects, each DynAny object storing one element of this
     * array.
     * 
     * <p>
     * See CORBA v2.3 Spec page 9-21
     *
     * @return DynAny array storing elemnts of this' stored array
     */
    public org.omg.DynamicAny.DynAny[] get_elements_as_dyn_any() {
        checkDestroyed ();
        org.omg.DynamicAny.DynAny[] retArray = new edu.uci.ece.zen.orb.dynany.DynAny[elements.length];
        
        try {
            for( int i = 0; i < elements.length; i++ ) {
                org.omg.DynamicAny.DynAny da = dynAnyFactory.create_dyn_any(elements[i]);
                retArray[i] = da;
            }
        }
        // Required due to create_dyn_any call, but never really happens
        catch( Exception e ) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "get_elements_as_dyn_any", e);
        }
        return retArray;
   }




    /**
     * Set the elements stored in this DynArray from the elements of a
     * DynAny array.
     * 
     * <p>
     * See CORBA v2.3 Spec page 9-21
     *
     * @param value DynAny array whose elements will be used to set the elements of this
     * @throws InvalidValue if the number of elements in value doesn't match the number of elements in this
     * @throws TypeMismatch if the type of elements stored in value doesn't match the type of elements in this array
     */
    public void set_elements_as_dyn_any(org.omg.DynamicAny.DynAny[] value) 
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue, 
               org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        
        org.omg.CORBA.Any [] extractedAnys =  new org.omg.CORBA.Any[value.length];
        for( int i = 0; i < value.length; i++ ) {
            extractedAnys[i] = value[i].to_any();
        }
        
        set_elements( extractedAnys );
    }

}
