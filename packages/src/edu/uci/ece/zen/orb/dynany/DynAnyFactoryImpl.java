package edu.uci.ece.zen.orb.dynany;

import edu.uci.ece.zen.orb.dynany.*;


/**
 * Implementation of Dynamic Any factory, used to create new instances
 * of DynAny and its subtypes.  
 *
 * @author Bruce Miller
 * @version $Revision: 1.2 $ $Date: 2004/01/21 18:58:15 $
 */

public class DynAnyFactoryImpl extends org.omg.CORBA.LocalObject implements org.omg.DynamicAny.DynAnyFactory {

    /** Reference to the orb.  Creaed DynAnys use for calling
     * orb.create_any() to create internlly used Any objects.  
     */
    org.omg.CORBA.ORB orb;


    /** Create new DynAnyFactory object, storing a reference to the
     * orb to give to all DynAny objects that it creates.
     */
    public DynAnyFactoryImpl (org.omg.CORBA.ORB _orb) {
        orb = _orb;
    }



    /** 
     * Assigns a copy of an Any to be stored in a new DynAny,
     * creating the new DynAny based on the passed Any's type.  A
     * copy of the TypeCode in <code>value</code> will be assigned to
     * the returned DynAny.
     *
     */
    public org.omg.DynamicAny.DynAny create_dyn_any( org.omg.CORBA.Any value ) throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode {
        try {
            // Create a copy of the Any's typecode
            org.omg.CORBA.TypeCode tcCopy = DynAny.copyTypeCode( value.type() );
            // Create new DynAny of the appropriate type
            org.omg.DynamicAny.DynAny da = create_dyn_any_from_type_code(tcCopy);
            // Create a copy of the Any
            edu.uci.ece.zen.orb.CDRInputStream is = (edu.uci.ece.zen.orb.CDRInputStream) value.create_input_stream();
            org.omg.CORBA.Any anyCopy = orb.create_any();
            anyCopy.read_value(is, da.type());

            // Initialize the new DynAny with a copy of the any
            da.from_any(anyCopy);
            return da;
        }
        // Could occur as result of from_any method
        catch ( Exception e ) {
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        }
    }



    /**
     * Create a new DynAny object for an Any having the ypecode tc,
     * and being set to the default value.
     *
     * @param tc TypeCode that new DynAny must represent.
     * @return DynAny newly created with default value.
     */
    public org.omg.DynamicAny.DynAny create_dyn_any_from_type_code(org.omg.CORBA.TypeCode tc) 
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode {
        
        edu.uci.ece.zen.orb.dynany.DynAny da;
        int tcKindVal = tc.kind().value();
        try {

            switch( tcKindVal ) {
            case org.omg.CORBA.TCKind._tk_null:
            case org.omg.CORBA.TCKind._tk_void:
            case org.omg.CORBA.TCKind._tk_short:
            case org.omg.CORBA.TCKind._tk_long:
            case org.omg.CORBA.TCKind._tk_ushort:
            case org.omg.CORBA.TCKind._tk_ulong:
            case org.omg.CORBA.TCKind._tk_float:
            case org.omg.CORBA.TCKind._tk_double:
            case org.omg.CORBA.TCKind._tk_boolean:
            case org.omg.CORBA.TCKind._tk_char:
            case org.omg.CORBA.TCKind._tk_octet:
            case org.omg.CORBA.TCKind._tk_any:
            case org.omg.CORBA.TCKind._tk_TypeCode:
            case org.omg.CORBA.TCKind._tk_objref:
            case org.omg.CORBA.TCKind._tk_string:
            case org.omg.CORBA.TCKind._tk_longlong:
            case org.omg.CORBA.TCKind._tk_ulonglong:
            case org.omg.CORBA.TCKind._tk_wchar:
            case org.omg.CORBA.TCKind._tk_wstring: {
                return new DynAny(this, tc, orb);
            }
                
            case org.omg.CORBA.TCKind._tk_fixed:
                da = new DynFixed(this, tc, orb);
                break;
                
            case org.omg.CORBA.TCKind._tk_enum:
                da = new DynEnum(this, tc, orb);
                break;

            case org.omg.CORBA.TCKind._tk_except:
            case org.omg.CORBA.TCKind._tk_struct:
                da = new DynStruct(this, tc, orb);
                break;
                
            case org.omg.CORBA.TCKind._tk_union:
                da = new DynUnion(this, tc, orb);
                break;
                
            case org.omg.CORBA.TCKind._tk_sequence:
                da = new DynSequence(this, tc, orb);
                break;
                
            case org.omg.CORBA.TCKind._tk_array:
                da = new DynArray(this, tc, orb);
                break;
                
            case org.omg.CORBA.TCKind._tk_value:
            case org.omg.CORBA.TCKind._tk_value_box:
                //da = new edu.uci.ece.zen.orb.dynany.DynValue(this, tc);
                throw new org.omg.CORBA.NO_IMPLEMENT("DynAny for value types not supported yet");
                
            default:
                throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode("Unable to build value for type" + tcKindVal);
            }
            return da;
            
        }
        catch (Exception e) {
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode("Unable to build value for type" + e);

        }
    }
    
}
