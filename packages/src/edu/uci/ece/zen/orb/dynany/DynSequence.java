package edu.uci.ece.zen.orb.dynany;

/**
 * Subtype of DynAny that is used to dynamically parse and create Any
 * objects that represent Sequences.  Shares a lot of functionality with
 * DynArray, so both extend DynSeqOrArray.
 *
 * @author Bruce Miller
 * @version $Revision: 1.1 $ $Date: 2004/01/21 00:52:59 $
 * @see DynSeqOrArray
 */

public class DynSequence extends DynSeqOrArray implements org.omg.DynamicAny.DynSequence {

    // componentCount stores the number of elements in the sequence.
    // componentCount may be less than this.elements.length
    
    //private int length;
    // bound is zero if sequence is unbounded
    // bound is an alias for this.type.length()
   private int bound;

   DynSequence( org.omg.DynamicAny.DynAnyFactory dynFactory,
                org.omg.CORBA.TypeCode tc, 
                org.omg.CORBA.ORB anOrb)
      throws org.omg.DynamicAny.DynAnyPackage.InvalidValue, 
             org.omg.DynamicAny.DynAnyPackage.TypeMismatch {

      super(dynFactory, tc, anOrb, org.omg.CORBA.TCKind.tk_sequence);

      try {
         bound = type.length();
         componentCount = 0;
         elementType = edu.uci.ece.zen.orb.TypeCode.originalType(type().content_type());
         elements = nullInitialValue;//new java.util.Vector(bound);
      }
      // Required for TypeCode length() and content_type() methods
      catch( org.omg.CORBA.TypeCodePackage.BadKind bk ) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "<init>", bk);
      }
   }



    public void from_any_determine_component_count(org.omg.CORBA.portable.InputStream is)
        throws org.omg.CORBA.TypeCodePackage.BadKind, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        bound = type.length();
        if( bound != 0 && componentCount > bound ) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }
          
        componentCount = is.read_long();
    }


    /** 
     * @see DynSeqOrArray#to_any_write_sequence_length_if_sequence(edu.uci.ece.zen.orb.CDROutputStream os)
     */
    public void to_any_write_sequence_length_if_sequence(edu.uci.ece.zen.orb.CDROutputStream os) {
        os.write_ulong(componentCount);
    }


    // Same as in DynSeqOrArray
    // to_any

    // Same as in DynSeqOrArray
    // equal


    /** Returns the number of elements actually stored in the sequence.
     * @return integer number of elements actually stored in the sequence, often less than the bound
     */
    public int get_length() {
        return component_count();
    }



    /**
     * Sets the number of elements in the sequence, either by adding
     * new default-initialized elements or truncating some.
     *
     * <p>
     * See CORBA v2.3 Spec p  9-20.
     *
     * @throws InvalidValue if len is negative, or greater than the bound of a bounded sequence
     */
    public void set_length(int len) 
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkDestroyed ();

       // Len is supposed to be unsigned in the CORBA spec
       if (len < 0) {
           throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue("len is less than zero " + len);
       }

       // If previous position was -1 and length is increased, then
       // the position is set to the first new element.
       if ( (position == -1) && (len > componentCount) ){
           position = componentCount;
       }

       // If the sequence is bounded and the new length is greater
       // than it, raise InvalidValue.
       if( (bound > 0) && (len > bound) ) {
           throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue("len is greater than bound " + len + " "  + bound);
       }

       // If length is set to zero, current position is set to -1.
       if( len == 0 ) {
           clearElements();
           position = -1;
       }

       else if( len > componentCount ) {
           try {
               for( int i = componentCount; i < len; i++ ) {
                   org.omg.CORBA.Any a = dynAnyFactory.create_dyn_any_from_type_code( elementType ).to_any();
                   elements[i] = a;
               }
           }
           // Thrown by dynAnyFactory
           catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itce ) {
               ZenProperties.logger.log(Logger.WARN, getClass(), "set_length", itce);
           }
           
       }
       else if ( (len < componentCount) && (position > len) ) {
           // Decreasing the length of a sequence.  If current
           // position indicates a valid element, and that element is
           // removed, then current position is set to -1.
           if( position > len ) {
               position = -1;
           }
       }
       componentCount = len;
    }
    


    // Same as DynSeqOrArray
    // get_elements()



    /**
     * Sets the elements of the sequence.
     *
     * @throws InvalidValue if the number of elements in value doesn't match the number of elements in this
     * @throws TypeMismatch if the type of elements stored in value doesn't match the type of elements in this array
     */
    public void set_elements( org.omg.CORBA.Any[] value ) 
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        checkDestroyed ();

        // If the length of the value exceeds the bound of a bounded
        // sequence, the operation raises InvalidValue.
        if( (bound > 0) && (value.length > bound) ) {
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        }

        // Check the types of the elements being set to
        checkSetElementsTypes(value);
        
        componentCount = value.length;
        
        // Make sure elements is cleared out and large enough to
        // hold the values to be read.
        clearElementsToComponentCount();
        
        elements = value;

        if( componentCount > 0 ) {
            position = 0;
        }
        else {
            position = -1;
        }       
    }



    // Same as for DynSeqOrArray
    // get_elements_as_dyn_any


    // Same as for DynSeqOrArray
    // set_elements_as_dyn_any



    public void destroy() {
        super.destroy();
        bound = 0;
    }



    // Same as DynSeqOrArray
    // getRepresentation


    // Same as DynSeqOrArray
    // current_component

   
    // Same as DynAny
    //public int component_count() {

}
