package edu.uci.ece.zen.orb.dynany;

import org.omg.DynamicAny.*;

/**
 * Subtype of DynAny that is used to dynamically parse and create Any
 * objects that represent Arrays.  Shares a lot of functionality with
 * DynSequence, so both extend DynSeqOrArray.
 *
 * @author Bruce Miller
 * @version $Revision: 1.1 $ $Date: 2004/01/21 00:52:59 $
 * @see DynSeqOrArray
 */

public class DynArray extends DynSeqOrArray implements org.omg.DynamicAny.DynArray {


    // componentCount is an alias for type.length()
    // componentCount may be less than this.elements.length

    // The get_elements method indicates that the implementation
    // should just use an array of Anys to store the members of the
    // array.

    DynArray( org.omg.DynamicAny.DynAnyFactory dynFactory,
              org.omg.CORBA.TypeCode tc,
              org.omg.CORBA.ORB anOrb) 
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue, 
               org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
        
        super(dynFactory, tc, anOrb, org.omg.CORBA.TCKind.tk_array);
        
        try {
            componentCount = type.length();
            elementType = edu.uci.ece.zen.orb.TypeCode.originalType( type.content_type() );
            
            elements = new edu.uci.ece.zen.orb.any.Any[componentCount];
            for( int i = 0; i < componentCount; i++ ) {
                elements[i] = dynAnyFactory.create_dyn_any_from_type_code( elementType ).to_any();
            }
        }
        catch( org.omg.CORBA.TypeCodePackage.BadKind bk ) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "<init>", bk);
        }
        catch( org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode itc ) {
            // Shouldn't happen.  Required for factory method.
            ZenProperties.logger.log(Logger.WARN, getClass(), "<init>", itc);
        }
    }



    public void from_any_determine_component_count(org.omg.CORBA.portable.InputStream is) 
        throws org.omg.CORBA.TypeCodePackage.BadKind, 
               org.omg.DynamicAny.DynAnyPackage.InvalidValue {
        componentCount = type.length();
    }


    // Same as in DynSeqOrArray
    // to_any


    // Same as in DynSeqOrArray
    // equal


    // Same as in DynSeqOrArray
    // public void destroy() {
   

    // Same as for DynAny
    // copy


    // Same as DynSeqOrArray
    // getRepresentation


    // Same as for DynAny
    // component_count


    // Same as DynSeqOrArray
    // current_component
   

    // Same as for DynSeqOrArray
    // get_elements

    /**
     * Set the elements stored in this DynArray from the elements of a
     * DynAny array.
     * 
     * <p>
     * See CORBA v2.3 Spec page 9-21
     *
     * @param value Any array that this will be set to store
     * @throws InvalidValue if the number of elements in value doesn't match the number of elements in this
     * @throws TypeMismatch if the type of elements stored in value doesn't match the type of elements in this array
     */
    public void set_elements(org.omg.CORBA.Any[] value) 
      throws org.omg.DynamicAny.DynAnyPackage.InvalidValue, 
             org.omg.DynamicAny.DynAnyPackage.TypeMismatch {
   
       checkDestroyed ();
       if ( componentCount != value.length ) {
           throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
       }

       checkSetElementsTypes(value);
       
       elements = value;
    }


    // Same as for DynSeqOrArray
    // get_elements_as_dyn_any

    // Same as for DynSeqOrArray
    // set_elements_as_dyn_any

}
