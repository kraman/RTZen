
package edu.uci.ece.zen.orb;
//
import org.omg.CORBA.*;
import edu.uci.ece.zen.utils.*;


/**
 * TypeCode Methods and implementation that is used when minimal CORBA
 * support of ValueTypes is required.  This file is used in
 * conjunction with TypeCode.java in order to provide the minimal
 * level of support for TypeCodes used in minimal CORBA spec, and this
 * file provides some alternative implementations of the minimalist
 * methods that appear in TypeCodeMinimalAspect.java.  TypeCodes also
 * make use of a file called TypeCode.java which contains methods
 * common to both the full and the minimal implementations of
 * typecodes.
 *
 * <p> TypeCodes and many of the methods related to them are described
 * in section 10.7.1 of the CORBA V2.3 spec, pages 10-50 through
 * 10-53.
 *
 * @see TypeCode, TypeCodeMinimalAspect
 * @author Mark Panahi, Bruce Miller
 */

public privileged aspect TypeCodeAspect {

    // We don't have a pluggable any cache for RTZen.
    /** A public pluggable any cache instace.  It is required that the
     * pluggableAnyCache be created with a reference to the orb
     * somewhere. 
     */
    //    public edu.uci.ece.zen.orb.any.pluggable.PluggableAnyCache pluggableAnyCache = 
    //  new PluggableAnyCache(edu.uci.ece.zen.orb.ORB.ORB_CORE);


    /**
     * Determine if legal operations permissible on two TypeCodes is
     * equal and the results of those operations are equal.
     *
     * <p> Can be invoked on any TypeCode.  Returns true if this and
     * <code>tc</code> have the same set of legal operations.
     *
     * @param tc TypeCode to compare this TypeCode to.
     * @return true if tc can be acted on as this.
     */
    public boolean edu.uci.ece.zen.orb.TypeCode.equal(org.omg.CORBA.TypeCode tc) {

        int this_kind_val = this.kind().value();
        int tc_kind_val = tc.kind().value();

        if (this_kind_val != tc.kind().value() ) {
            return false;
        }

        // We now know that tc and type have the same type.
        
        long kindMask = 1L << this_kind_val;

        try {
            
            // If id operation is valid...
            
            if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_objref) 
                               | (1L << org.omg.CORBA.TCKind._tk_struct)
                               | (1L << org.omg.CORBA.TCKind._tk_union)
                               | (1L << org.omg.CORBA.TCKind._tk_enum)
                               | (1L << org.omg.CORBA.TCKind._tk_alias)
                               | (1L << org.omg.CORBA.TCKind._tk_value)
                               | (1L << org.omg.CORBA.TCKind._tk_value_box)
                               | (1L << org.omg.CORBA.TCKind._tk_native)
                               | (1L << org.omg.CORBA.TCKind._tk_abstract_interface)
                               | (1L << org.omg.CORBA.TCKind._tk_except)
                               )
                  ) != 0 ) {
                
                // If both id's are equal
                if ( ! this.id.equals(tc.id())) {
                    return false;
                }
                if ( ! this.name.equals(tc.name())) {
                    return false;
                }
            }


            if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_struct)
                               | (1L << org.omg.CORBA.TCKind._tk_union)
                               | (1L << org.omg.CORBA.TCKind._tk_enum)
                               | (1L << org.omg.CORBA.TCKind._tk_value)
                               | (1L << org.omg.CORBA.TCKind._tk_except)
                              )
                  ) != 0 ) {
                if ( this.member_count != tc.member_count() )
                    return false;
                
                
                
                // structs, unions, enums, and valuetypes reach this line
                
                if ( this_kind_val != org.omg.CORBA.TCKind._tk_enum ) {
                    for (int i = 0; i < this.member_count; i++) {
                        
                        if ( ! this.member_names[i].equals(tc.member_name(i)) )
                            return false;
                                
                        if (this_kind_val != org.omg.CORBA.TCKind._tk_enum && ! this.member_types[i].equal(tc.member_type(i)) )
                            return false;

                        if ( this_kind_val == org.omg.CORBA.TCKind._tk_union && ! this.member_labels[i].equal(tc.member_label(i)))
                            return false;
                        
                        if ( this_kind_val == org.omg.CORBA.TCKind._tk_value && this.member_visibility[i] != tc.member_visibility(i))
                            return false;
                        
                    }
                }
            }


            if (this_kind_val == org.omg.CORBA.TCKind._tk_union) {
                if ( ! this.discriminator_type.equal(tc.discriminator_type()) || this.default_index != tc.default_index() ) {
                    return false;
                }
            }

            if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_string)
                              | (1L << org.omg.CORBA.TCKind._tk_wstring)
                              | (1L << org.omg.CORBA.TCKind._tk_sequence)
                              | (1L << org.omg.CORBA.TCKind._tk_array)
                              )
                  ) != 0 ) {
                if (this.length != tc.length()) {
                    return false;
                }
            }     
            
            if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_sequence)
                               | (1L << org.omg.CORBA.TCKind._tk_array)
                               | (1L << org.omg.CORBA.TCKind._tk_value_box)
                               | (1L << org.omg.CORBA.TCKind._tk_alias)
                              )
                  ) != 0 ) {
                
                if ( ! this.type.equal(tc.content_type()) ) {
                    return false;
                }
            }     


            if (this_kind_val == org.omg.CORBA.TCKind._tk_fixed) {
                if (this.digits != tc.fixed_digits() || this.scale != tc.fixed_scale()) {
                    return false;
                }
            }
                
            if (this_kind_val == org.omg.CORBA.TCKind._tk_value) {
                if ( this.type_modifier != tc.type_modifier() || ! this.concrete_base_type().equal(tc.concrete_base_type()) ) {
                        return false;
                }
            }
                
                
        }      
        catch ( org.omg.CORBA.TypeCodePackage.Bounds be ) {
            be.printStackTrace();
            return false;
        }
        
        catch ( org.omg.CORBA.TypeCodePackage.BadKind bk ) {
            bk.printStackTrace();
            return false;
        }
            
        return true;
    }



    /**
     * Determine if the types in a data item having this type are
     * equivalent to the types in <code>tc</code>.  Used by Anys to
     * know when it is safe to extract data stored in an Any into
     * another variable.  This is the full version.
     *
     * <p> This method unwinds aliases, and checks that they have the
     * same kind.  If the id() method is available for both TypeCodes
     * and is non-empty, then they must have the same id.  If one or
     * more id string is empty, then the TypeCodes are compared for
     * structural equivalence by examining their members.
     *
     * @param tc TypeCode to compare this TypeCode to.
     * @return true if tc is structurally equivalent to this; false otherwise.
     */
    public boolean edu.uci.ece.zen.orb.TypeCode.equivalent(org.omg.CORBA.TypeCode anotherTypeCode) {
    // See CORBA spec for implementation details.  The Spec clearly
    // states how to implement this.

        int this_kind_val = this.kind().value();

        if (this_kind_val == org.omg.CORBA.TCKind._tk_alias) {
            return originalType(this).equivalent(anotherTypeCode);
        }

        org.omg.CORBA.TypeCode tc = originalType(anotherTypeCode);



      if (this_kind_val != originalType(tc).kind().value() ) {
      return false;
      }

      // We now know that tc and type have the same type.

      long kindMask = 1L << this_kind_val;


      try {

          // If id operation is valid...
          
          if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_objref)
                             | (1L << org.omg.CORBA.TCKind._tk_struct)
                             | (1L << org.omg.CORBA.TCKind._tk_union)
                             | (1L << org.omg.CORBA.TCKind._tk_enum)
                             | (1L << org.omg.CORBA.TCKind._tk_alias)
                             | (1L << org.omg.CORBA.TCKind._tk_value)
                             | (1L << org.omg.CORBA.TCKind._tk_value_box)
                             | (1L << org.omg.CORBA.TCKind._tk_native)
                             | (1L << org.omg.CORBA.TCKind._tk_abstract_interface)
                             | (1L << org.omg.CORBA.TCKind._tk_except)
                            )
                ) != 0 ) {
              
              // If both id's either non-null
              String tcId = tc.id();
              if (this.id != null && !this.id.equals("") && tcId != null && !tcId.equals("") ) {
                  if (this.id.equals(tcId)) {
                      return true;
                  }
                  else {
                      return false;
                  }
              }
          }

          // If member_count operation is valid
          if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_struct)
                             | (1L << org.omg.CORBA.TCKind._tk_union)
                             | (1L << org.omg.CORBA.TCKind._tk_enum)
                             | (1L << org.omg.CORBA.TCKind._tk_value)
                             | (1L << org.omg.CORBA.TCKind._tk_except)
                            )
                ) != 0 ) {
              if (this.member_count != tc.member_count())
                  return false;
          }

          // If default_index operation is valid
          if (this_kind_val == org.omg.CORBA.TCKind._tk_union) {
              if (this.default_index != tc.default_index())
                  return false;
          }
          
          // If the length operation is valid
          if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_string)
                             | (1L << org.omg.CORBA.TCKind._tk_wstring)
                             | (1L << org.omg.CORBA.TCKind._tk_sequence)
                             | (1L << org.omg.CORBA.TCKind._tk_array)
                            )
                ) != 0 ) {
              if (this.length != tc.length())
                  return false;
          }

          // If the digits and scale operations are valid
          if (this_kind_val == org.omg.CORBA.TCKind._tk_fixed) {
              if (this.digits != tc.fixed_digits())
                  return false;
              if (this.scale != tc.fixed_scale())
                  return false;
          }


          // If the member labels valid (used for union)
          if (this_kind_val == org.omg.CORBA.TCKind._tk_union) {
              // Note that member_count was already compared, so this and tc
              // have the same number of member_labels
              for (int i = 0; i < this.member_labels.length; i++) {
                  if (this.member_labels[i] != tc.member_label(i))
                      return false;
              }
              if ( ! this.discriminator_type.equivalent(tc.discriminator_type()) )
                  return false;
              
          }


          // If the member_type operation is valid
          if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_struct)
                             | (1L << org.omg.CORBA.TCKind._tk_union)
                             | (1L << org.omg.CORBA.TCKind._tk_enum)
                             | (1L << org.omg.CORBA.TCKind._tk_value)
                             | (1L << org.omg.CORBA.TCKind._tk_except)
                            )
                ) != 0 ) {
              for (int i = 0; i < this.member_types.length; i++) {
                  if ( ! this.member_types[i].equivalent(tc.member_type(i)) )
                      return false;
              }
          }

          // If the content_type operation is valid
          if ( (kindMask & ( (1L << org.omg.CORBA.TCKind._tk_sequence)
                             | (1L << org.omg.CORBA.TCKind._tk_array)
                             | (1L << org.omg.CORBA.TCKind._tk_value_box)
                             | (1L << org.omg.CORBA.TCKind._tk_alias)
                            )
                ) != 0 ) {
              if ( ! this.content_type().equivalent(tc.content_type()) )
                  return false;
          }

      } // end of try {

      catch ( org.omg.CORBA.TypeCodePackage.Bounds be ) {
          be.printStackTrace();
          return false;
      }
      
      catch ( org.omg.CORBA.TypeCodePackage.BadKind bk ) {
          bk.printStackTrace();
          return false;
      }


      return true;
    }






//Introductions to CDRInputStream


    /**
     * Abstract declaration of method to read TypeCode from this stream.
     *
     * @return TypeCode read from from this stream.
     */
    public abstract org.omg.CORBA.TypeCode org.omg.CORBA.portable.InputStream.read_TypeCode();


    /** 
     * Declaration of method to read an Any, inserted into abstract class InputStream.
     * @return Any read from this stream.
     */
    public abstract org.omg.CORBA.Any org.omg.CORBA.portable.InputStream.read_any();




    /** 
     * Starts reading from an encapsulated CDRInputStream on the input
     * buffer.  The method closeEncapsulation must be called when done.
     * @return CDRInputStream that was encapsulated on the input buffer.
     */
    private CDRInputStream edu.uci.ece.zen.orb.CDRInputStream.openEncapsulation() {
        
        // The length and endianness is automatically picked up from the stream.
        CDRInputStream cdr = read_CDRInputStream();
        int endianness = (int) cdr.read_octet();
        // The encapsulated header uses 0 for big-endian (Java's
        // representation) and 1 for little-endian, but Krishna uses
        // false for big-endian, true for little-endian.
        if (endianness == 0) {
            cdr.buffer.setEndian(false);
        }
        else if (endianness == 1) {
            cdr.buffer.setEndian(true);
        }
        else {
            throw new org.omg.CORBA.MARSHAL("Invalid endianness octet read");
        }

        return cdr;
    }



    /** 
     * Frees up resources allocated when openEncapsulation() was called.
     * The method closeEncapsulation must be called when done.
     */
    private void edu.uci.ece.zen.orb.CDRInputStream.closeEncapsulation(CDRInputStream cdr) {
        cdr.free();
    }



    /**
     * Reads an any object from the the CDRInputStream.  This
     * processes consists of reading a TypeCode representing an
     * Any, then the Any's stored value.
     *
     * @return Any object just read from CDRInputStream.
     */
    public final org.omg.CORBA.Any edu.uci.ece.zen.orb.CDRInputStream.read_any() {
        org.omg.CORBA.TypeCode anyType = read_TypeCode();
        org.omg.CORBA.Any any = orb.create_any();
        any.read_value (this, anyType);
        return any;
    }



    /**
     * Read the value of type <code>tc</code>from this CDRInputStream
     * and write it to <code>out</code>
     *
     * <p>The method is called from the monolithic Any
     * implementation.
     * 
     * @param tc TypeCode of type to try to read.
     * @param out OutputStream to which to write read data
     */
    public final void edu.uci.ece.zen.orb.CDRInputStream.read_value_of_type(org.omg.CORBA.TypeCode tc, org.omg.CORBA.portable.OutputStream out) {
        int kind = ((edu.uci.ece.zen.orb.TypeCode) tc)._kind();

        try {
            switch (kind) {
            case TCKind._tk_null:
            case TCKind._tk_void:
                break;

            case TCKind._tk_boolean:
                out.write_boolean(read_boolean());
                break;

            case TCKind._tk_char:
                out.write_char(read_char());
                break;

            case TCKind._tk_wchar:
                out.write_wchar(read_wchar());
                break;

            case TCKind._tk_octet:
                out.write_octet(read_octet());
                break;

            case TCKind._tk_ushort:
                out.write_ushort(read_ushort());
                break;

            case TCKind._tk_short:
                out.write_short(read_short());
                break;

            case TCKind._tk_long:
                out.write_long(read_long());
                break;

            case TCKind._tk_ulong:
                out.write_ulong(read_ulong());
                break;

            case TCKind._tk_float:
                out.write_float(read_float());
                break;

            case TCKind._tk_double:
                out.write_double(read_double());
                break;

            case TCKind._tk_longlong:
                out.write_longlong(read_longlong());
                break;

            case TCKind._tk_ulonglong:
                out.write_ulonglong(read_ulonglong());
                break;

            case TCKind._tk_any:
                out.write_any(read_any());
                break;

            case TCKind._tk_TypeCode:
                out.write_TypeCode(read_TypeCode());
                break;

            case TCKind._tk_Principal:
                out.write_Principal(read_Principal());
                break;

            case TCKind._tk_objref:
                out.write_Object(read_Object());
                break;

            case TCKind._tk_string:
                out.write_string(read_string());
                break;

            case TCKind._tk_wstring:
                out.write_wstring(read_wstring());
                break;

            case TCKind._tk_array: {
                int length = tc.length();
                for (int i = 0; i < length; i++)
                    read_value_of_type(tc.content_type(), out);
                break;
            }
            case TCKind._tk_sequence: {
                int length = read_long();

                out.write_long(length);
                for (int i = 0; i < length; i++)
                    read_value_of_type(tc.content_type(), out);
                break;
            }
            case TCKind._tk_except:
                out.write_string(read_string());

                // don't break, fall through to .
         
            case TCKind._tk_struct:
                for (int i = 0; i < tc.member_count(); i++)
                    read_value_of_type(tc.member_type(i), out);
                break;

            case TCKind._tk_enum:
                out.write_long(read_long());
                break;

            case TCKind._tk_alias:
                //            out.write_string( read_string());
                //            out.write_string( read_string());
                //            out.write_TypeCode( read_TypeCode());
                    //BM I encountered the line below which differs from
                    //how JacORB's CDRInputStream implementation handles
                    //the _tk_alias type.  I decided to change the
                    //handling of _tk_alias to how JacORB does it.
                    //out.write_value(tc.content_type(), this);
                read_value_of_type( tc.content_type(), out );

                break;

            case TCKind._tk_value:
                // This case never happens because this method is not called for type _tk_value

                // The default case may be reached for _tk_union which
                // this method did not include support for when it was
                // written (before our IDL compiler supported the
                // tk_union data type)
            default:
                ZenProperties.logger.log(
                    Logger.FATAL ,
                    "edu.uci.ece.zen.orb.CDRInputStream (TypeCodeAspect)",
                    "read_value_of_type()",
                    "Unimplemented method in CDRInputStream for kind " + kind );
                try {
                    throw new org.omg.CORBA.NO_IMPLEMENT();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }

        } // end of: try

        // Thrown by code for _tk_array, _tk_sequence, _tk_struct
        catch (org.omg.CORBA.TypeCodePackage.BadKind b) {
            System.err.println("CDRInputStream#read_value threw BadKind exception for kind " + kind);
            b.printStackTrace();
        }

        // Thrown by code for handling _tk_struct
        catch (org.omg.CORBA.TypeCodePackage.Bounds b) {
            System.err.println("CDRInputStream#read_value threw Bounds exception for kind " + kind);
            b.printStackTrace();
        }

    }



    public org.omg.CORBA.TypeCode edu.uci.ece.zen.orb.CDRInputStream.read_TypeCode() {
        // The indirection map is created here because indirections
        // are not "freestanding" but rathor only valid within outer
        // enclosing scopes that began with a read_TypeCode operation
        // (CORBA v2.3 SPEC section 15.3.5.1, page 15-27).
        java.util.Hashtable tcIndirectionMap = new java.util.Hashtable();
        org.omg.CORBA.TypeCode retValue = read_TypeCode(tcIndirectionMap, 0);
        return retValue;
    }


    
    /**
     * Read a TypeCode from the input stream and return a newly
     * constructed object copying its values.
     *
     * @param tcIndirectionMap mapping of strings for integer values to either Strings as ids or TypeCodes already read
     * @param outerEncapsPos Position of the start of this CDRInputStream in any outermost CDRInputStream that has encapsulated one or more down to this CDRInputStream
     * @return TypeCode read from the inputStream.
     */
    private org.omg.CORBA.TypeCode edu.uci.ece.zen.orb.CDRInputStream.read_TypeCode(java.util.Hashtable tcIndirectionMap, int outerEncapsPos) {
        int thisKindPos = outerEncapsPos + (int) getPosition();
        String thisKindPosStr = Integer.toString(thisKindPos);
        int kind = read_long();

        switch (kind) {
        case TCKind._tk_null:
        case TCKind._tk_void:
        case TCKind._tk_short:
        case TCKind._tk_long:
        case TCKind._tk_ushort:
        case TCKind._tk_ulong:
        case TCKind._tk_float:
        case TCKind._tk_double:
        case TCKind._tk_boolean:
        case TCKind._tk_char:
        case TCKind._tk_wchar:
        case TCKind._tk_octet:
        case TCKind._tk_any:  // TypeCode representing an Any only has to read its numeric kind
        case TCKind._tk_TypeCode:  // TypeCode representing a TypeCode only has to read its numeric kind
        case TCKind._tk_longlong:
        case TCKind._tk_ulonglong:
        case TCKind._tk_longdouble:
            return orb.get_primitive_tc(org.omg.CORBA.TCKind.from_int(kind));

        case TCKind._tk_string:
        case TCKind._tk_wstring:
            int len = read_long();
            return edu.uci.ece.zen.orb.TypeCode.newStringTC(kind, len);

        case TCKind._tk_objref: {
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();
            String objref_id = cdr.read_string();
            String objref_name = cdr.read_string();
            closeEncapsulation(cdr);
            return edu.uci.ece.zen.orb.TypeCode.newObjRefTC(objref_name, objref_id);
        }

        case TCKind._tk_struct:
        case TCKind._tk_except: {
            if( ZenProperties.dbg )
                ZenProperties.logger.log(
                        Logger.INFO ,
                        "edu.uci.ece.zen.orb.CDRInputStream (TypeCodeAspect)",
                        "read_TypeCode()",
                        "Reading structs or Exception type code");

            // Calculate the position of the start of the next
            // encapsulation in the outermost CDRInputStream.  It will
            // be the position of the start of this input stream
            // (outerEncapsPos) plus the current position in this
            // input stream plus four bytes
            int outermostEncapsPos = outerEncapsPos + (int) getPosition() + 4;
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();

            String id = cdr.read_string();
            // Initially, store id in indirection mapping, will cause
            // recursive typecode to be created if referred to later.
            tcIndirectionMap.put(thisKindPosStr, id);

            String name = cdr.read_string();
            int memCount = cdr.read_ulong();
            org.omg.CORBA.StructMember[] sm = new org.omg.CORBA.StructMember[memCount];

            for (int i = 0; i < memCount; i++) {
                String sname = cdr.read_string();
                org.omg.CORBA.TypeCode type = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);

                sm[i] = new org.omg.CORBA.StructMember(sname, type, null);
            }
            closeEncapsulation(cdr);

            org.omg.CORBA.TypeCode retTC = new TypeCode(kind, id, name, sm);
            // Replace indirection mapping with complete typecode
            tcIndirectionMap.put(thisKindPosStr, retTC);
            return retTC;
        }

        case TCKind._tk_union: {
            int outermostEncapsPos = outerEncapsPos + (int) getPosition() + 4;
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();

            String id = cdr.read_string();
            tcIndirectionMap.put(thisKindPosStr, id);

            String union_name = cdr.read_string();
            org.omg.CORBA.TypeCode discriminator = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);

            int default_index = cdr.read_long();

            int union_memCount = cdr.read_ulong();
            org.omg.CORBA.UnionMember union_members[] = new org.omg.CORBA.UnionMember[union_memCount];

            for (int i = 0; i < union_memCount; i++) {
                //System.out.println("CDR Istream :: Writing >> " + i);
                org.omg.CORBA.Any uLabel = orb.create_any();
                if ( i != default_index ) {
                    uLabel.read_value(cdr, discriminator);
                }
                else {
                    uLabel.insert_octet( cdr.read_octet() );
                }
                String uname = cdr.read_string();
                org.omg.CORBA.TypeCode utypecode = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);
                // read_object needs to be implemented!!!!
                //org.omg.CORBA.IDLType utypeDef = (org.omg.CORBA.IDLType) read_Object();


                //union_members[i] = new org.omg.CORBA.UnionMember(uname, uLabel, utypecode, utypeDef);
                union_members[i] = new org.omg.CORBA.UnionMember(uname, uLabel, utypecode, null);
            }
            closeEncapsulation(cdr);

            TypeCode retTC = new TypeCode(id, union_name, discriminator, union_members);

            tcIndirectionMap.put(thisKindPosStr, retTC);
            return retTC;
        }

        case TCKind._tk_enum: {
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();
            String id = cdr.read_string();
            // Initially, store id in indirection mapping, will cause
            // recursive typecode to be created if referred to later.
            tcIndirectionMap.put(thisKindPosStr, id);
            String enum_name = cdr.read_string();
            int enum_memCount = cdr.read_ulong();
            String enum_members[] = new String[enum_memCount];

            for (int i = 0; i < enum_memCount; i++) {
                enum_members[i] = cdr.read_string();
            }
            closeEncapsulation(cdr);

            TypeCode retTC = new TypeCode(id, enum_name, enum_members);
            // Replace indirection mapping with complete typecode
            tcIndirectionMap.put(thisKindPosStr, retTC);
            return retTC;
        }

        case TCKind._tk_sequence:
        case TCKind._tk_array: {
            int outermostEncapsPos = outerEncapsPos + (int) getPosition() + 4;
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();
            
            org.omg.CORBA.TypeCode seqElemType = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);
            int bound = cdr.read_ulong();
            closeEncapsulation(cdr);
            
            // Create a new typecode for sequence types
            TypeCode retTC = new TypeCode(bound, seqElemType);
            if (kind == TCKind._tk_array) {
                retTC.kind = TCKind._tk_array;
            }
            return retTC;
        }

        case TCKind._tk_alias: {
            int outermostEncapsPos = outerEncapsPos + (int) getPosition() + 4;
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();
            String aliasId = cdr.read_string();
            String aliasName = cdr.read_string();
            org.omg.CORBA.TypeCode aliasTc = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);
            closeEncapsulation(cdr);

            TypeCode retTC = new TypeCode(aliasId, aliasName, aliasTc);
            return retTC;
        }

        case TCKind._tk_value: {
            int outermostEncapsPos = outerEncapsPos + (int) getPosition() + 4;
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();
            
            String id = cdr.read_string();
            String name = cdr.read_string();
            short typeModifier = cdr.read_short();
            
            org.omg.CORBA.TypeCode concreteBaseType = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);

            int memberCount = cdr.read_ulong();
            org.omg.CORBA.ValueMember [] valueMembers = new org.omg.CORBA.ValueMember[memberCount];
            for (int i = 0; i < memberCount; i++) {
                String valueName = cdr.read_string();
                org.omg.CORBA.TypeCode valueTypeCode = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);
                short valueVisibilityAccess = cdr.read_short();
                valueMembers[i] = new org.omg.CORBA.ValueMember(valueName, null, null, null, valueTypeCode, null, valueVisibilityAccess); 
            }

            closeEncapsulation(cdr);

            org.omg.CORBA.TypeCode retTC = orb.create_value_tc(id, name, typeModifier, concreteBaseType, valueMembers);
            return retTC;
        }

        case TCKind._tk_value_box: {
            int outermostEncapsPos = outerEncapsPos + (int) getPosition() + 4;
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();
            String id = cdr.read_string();
            String name = cdr.read_string();
            org.omg.CORBA.TypeCode contentType = cdr.read_TypeCode(tcIndirectionMap, outermostEncapsPos);
            closeEncapsulation(cdr);

            org.omg.CORBA.TypeCode retTC = orb.create_value_box_tc(id, name, contentType);
            return retTC;
        }

        case TCKind._tk_abstract_interface: {
            edu.uci.ece.zen.orb.CDRInputStream cdr = openEncapsulation();
            String intId = cdr.read_string();
            String intName = cdr.read_string();
            closeEncapsulation(cdr);

            return new TypeCode(intId, intName);
        }

            // An indirection to a previous type ID string or TypeCode
        case 0xFFFFFFFF: {
            int otherLoc = (int) getPosition() + read_long();
            java.lang.Object prevIDOrTC = tcIndirectionMap.get( Integer.toString(otherLoc) );
            org.omg.CORBA.TypeCode retTC;
            // Only a string existed in the cache because the TypeCode
            // hadn't been completely written at the time it appeared
            // again (which means it was recursive).
            if ( prevIDOrTC instanceof String) {
                retTC = orb.create_recursive_tc( (String) prevIDOrTC );
            }
            // A complete TypeCode had been saved in the cache and
            // hence had been fully processed before this indirection
            // was found.
            else if ( prevIDOrTC instanceof org.omg.CORBA.TypeCode ) {
                retTC = (org.omg.CORBA.TypeCode) prevIDOrTC;
            }
            else {
                throw new org.omg.CORBA.MARSHAL("TypeCode Indirection Not Found at " + otherLoc);
            }
            return retTC;
        }
            
        default:
            ZenProperties.logger.log(
                    Logger.FATAL ,
                    "edu.uci.ece.zen.orb.CDRInputStream (TypeCodeAspect)",
                    "read_TypeCode()",
                    "Unimplemented method in CDRInputStream for kind " + kind);
            return null;
        }
    }





//Introductions to CDROutputStream

    /**
     * Abstract declaration, writes the Any <code>any</code> to the
     * output stream.
     */
    public abstract void org.omg.CORBA.portable.OutputStream.write_any(org.omg.CORBA.Any any);

    /**
     * Writes an any object from the the CDROutputStream.  This
     * processes consists of writing a TypeCode representing an
     * Any, then the Any's stored value.
     *
     * @return Any object just read from CDRInputStream.
     */
    public final void edu.uci.ece.zen.orb.CDROutputStream.write_any(org.omg.CORBA.Any any) {
        write_TypeCode( any.type() );
        any.write_value(this);
    }


    /**
     * Abstract declaration, writes the TypeCode <code>value</code> to
     * this output stream.
     *
     * @param value TypeCode to write to outputstream.
     */
    public abstract void org.omg.CORBA.portable.OutputStream.write_TypeCode(org.omg.CORBA.TypeCode value);



    /** Returns a CDROutputStream that will be encapsulated within
     * this stream.  Calld endEncapsulation() when finished writing to
     * it.
     */
    public CDROutputStream edu.uci.ece.zen.orb.CDROutputStream.beginEncapsulation() {

        CDROutputStream cdr = CDROutputStream.instance();
        // The encapsulated header uses 0 for big-endian (Java's
        // representation) and 1 for little-endian
        cdr.write_octet((byte) 0);
        return cdr;

    }
    


    /** Finish marshaling the CDROutputStream <code>cdr</code> into
     * this stream.
     * @param cdr The CDROutputStream to be marshaled into this stream.
     */
    public void edu.uci.ece.zen.orb.CDROutputStream.endEncapsulation(CDROutputStream cdr) {
        this.write_CDROutputStream(cdr);
        cdr.free();
    }



    /**
     * Writes a TypeCode to this CDROutputStream.
     * 
     * @param value TypeCode to write to this CDROutputStream.
     */
    public final void edu.uci.ece.zen.orb.CDROutputStream.write_TypeCode(org.omg.CORBA.TypeCode value) {
        int memCount = 0;

        int value_kind_val = value.kind().value();
        write_long(value_kind_val);                       // Write TypeCode Kind.....
        // Writing the typecode kind is enough for simple data types like long or wchar.

        try {

            switch (value_kind_val) {
            case TCKind._tk_null:
            case TCKind._tk_void:
            case TCKind._tk_short:
            case TCKind._tk_long:
            case TCKind._tk_ushort:
            case TCKind._tk_ulong:
            case TCKind._tk_float:
            case TCKind._tk_double:
            case TCKind._tk_boolean:
            case TCKind._tk_char:
            case TCKind._tk_wchar:
            case TCKind._tk_octet:
            case TCKind._tk_any:  // TypeCode representing a TypeCode only has to write its numeric kind
            case TCKind._tk_TypeCode: // TypeCode representing a TypeCode only has to write its numeric kind
            case TCKind._tk_longlong:
            case TCKind._tk_ulonglong:
            case TCKind._tk_longdouble:
                // Do nothing further.
                break;

            case TCKind._tk_string:
            case TCKind._tk_wstring:
                write_long(value.length());
                break;

            case TCKind._tk_objref: {
                CDROutputStream cdr = beginEncapsulation();
                cdr.write_string(value.id());
                cdr.write_string(value.name());
                endEncapsulation(cdr);
                break;
            }

            case TCKind._tk_struct:
            case TCKind._tk_except: {
                CDROutputStream cdr = beginEncapsulation();
                cdr.write_string(value.id());
                cdr.write_string(value.name());
                memCount = value.member_count();
                cdr.write_ulong(memCount);
                for (int i = 0; i < memCount; i++) {
                    cdr.write_string(value.member_name(i));
                    cdr.write_TypeCode(value.member_type(i));
                }
                endEncapsulation(cdr);
                break;
            }

            case TCKind._tk_union: {
                CDROutputStream cdr = beginEncapsulation();
                cdr.write_string(value.id());
                cdr.write_string(value.name());
                cdr.write_TypeCode(value.discriminator_type());

                int def_ind = value.default_index();
                cdr.write_long(def_ind);
                memCount = value.member_count();
                cdr.write_ulong(memCount);
                for (int i = 0; i < memCount; i++) {
                    if ( i != def_ind ) {
                        value.member_label(i).write_value(this);
                    }
                    else {
                        write_octet( (byte) 0 );
                    }
                    cdr.write_string(value.member_name(i));
                    cdr.write_TypeCode(value.member_type(i));
                    //cdr.write_any(value.member_label(i));
                    // this method (write_Object) has not been written yet.....
                    //write_Object(value.member_idlType(i));
                }
                endEncapsulation(cdr);
                break;
            }

            case TCKind._tk_enum: {
                CDROutputStream cdr = beginEncapsulation();
                memCount = value.member_count();
                cdr.write_string(value.id());
                cdr.write_string(value.name());
                cdr.write_ulong(memCount);
                for (int i = 0; i < memCount; i++) {
                    cdr.write_string(value.member_name(i));
                }
                endEncapsulation(cdr);
                break;
            }
                
            case TCKind._tk_sequence:
            case TCKind._tk_array: {
                CDROutputStream cdr = beginEncapsulation();
                // how do you discriminate a seqeunce from a recursive sequence???

                // write_TypeCode will recurse if the object stored is a multidimensional array
                cdr.write_TypeCode(value.content_type());
                cdr.write_ulong(value.length());
                endEncapsulation(cdr);
                break;
            }

            case TCKind._tk_alias: {
                CDROutputStream cdr = beginEncapsulation();
                cdr.write_string(value.id());
                cdr.write_string(value.name());
                cdr.write_TypeCode(value.content_type());
                endEncapsulation(cdr);
                break;
            }

            case TCKind._tk_value: {
                CDROutputStream cdr = beginEncapsulation();
                
                cdr.write_string(value.id());
                cdr.write_string(value.name());
                cdr.write_short(value.type_modifier());

                org.omg.CORBA.TypeCode baseType = value.concrete_base_type();
                if (baseType == null) {
                    baseType = edu.uci.ece.zen.orb.TypeCode.lookupPrimitiveTc(TCKind._tk_null);
                }
                cdr.write_TypeCode(baseType);
                
                int memberCount = value.member_count();
                cdr.write_ulong(memberCount);
                for (int i = 0; i < memberCount; i++) {
                    cdr.write_string(value.member_name(i));
                    cdr.write_TypeCode(value.member_type(i));
                    cdr.write_short(value.member_visibility(i));
                }

                endEncapsulation(cdr);
                break;
            }

            case TCKind._tk_value_box: {
                CDROutputStream cdr = beginEncapsulation();
                cdr.write_string(value.id());
                cdr.write_string(value.name());
                cdr.write_TypeCode(value.content_type());
                endEncapsulation(cdr);
                break;
            }

            case TCKind._tk_abstract_interface: {
                CDROutputStream cdr = beginEncapsulation();
                cdr.write_string(value.id());
                cdr.write_string(value.name());
                endEncapsulation(cdr);
                break;
            }

            case TCKind._tk_Principal:
            default:
                ZenProperties.logger.log(
                    Logger.FATAL ,
                    "edu.uci.ece.zen.orb.CDROutputStream (TypeCodeAspect)",
                    "write_TypeCode()",
                    "Unimplemented method in CDROutputStream for kind " + value_kind_val );
                /*

                // This code was used to print a stack trace in order
                // to know how this method was being called.

       try {
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
        catch (java.lang.Exception e) {
            try {
                e.printStackTrace();
                java.lang.Thread.sleep(1000);
            }
            catch (java.lang.Exception e2) {}
        }
                 
                //break;
                */
            } // end of: switch(value_kind)
        }  
        catch (org.omg.CORBA.TypeCodePackage.BadKind bk) { 
            System.err.println("CDROutputStream#write_TypeCode threw BadKind exception");
            bk.printStackTrace();
        }
        catch (org.omg.CORBA.TypeCodePackage.Bounds b) { 
            System.err.println("CDROutputStream#write_TypeCode threw BadKind exception");
            b.printStackTrace();
        }
    }



    /**
     * Writes the value stored in the InputStream <code>in</code> to
     * this outputstream, treating the value on the inputstream as
     * being of TypeCode <code>tc</code>.  Just to make it clear,
     * whatever data is stored next in the inputstream <code>in</code>
     * will be assumed to be of the type given by the TypeCode
     * <code>tc</code>; an InputStream holds data but does not
     * necessarily store the type of the data inside it, hence the
     * need for the <code>tc</code> parameter.
     *
     * @param tc TypeCode to interpret object on inputstream as
     * @param in InputStream storing data to be written to this outputstream.
     */
    public final void edu.uci.ece.zen.orb.CDROutputStream.write_value(org.omg.CORBA.TypeCode tc, org.omg.CORBA.portable.InputStream in) {

        int kind = ((edu.uci.ece.zen.orb.TypeCode) tc)._kind();
        //int kind = tc.kind().value(); // Above method is faster
    
        switch (kind) {
        case TCKind._tk_null:
        case TCKind._tk_void:
            break;

        case TCKind._tk_boolean:
            write_boolean(in.read_boolean());
            break;

        case TCKind._tk_char:
            write_char(in.read_char());
            break;

        case TCKind._tk_wchar:
            write_wchar(in.read_wchar());
            break;

        case TCKind._tk_octet:
            write_octet(in.read_octet());
            break;

        case TCKind._tk_short:
            write_short(in.read_short());
            break;

        case TCKind._tk_ushort:
            write_ushort(in.read_ushort());
            break;

        case TCKind._tk_long:
            write_long(in.read_long());
            break;

        case TCKind._tk_ulong:
            write_ulong(in.read_ulong());
            break;

        case TCKind._tk_float:
            write_float(in.read_float());
            break;

        case TCKind._tk_double:
            write_double(in.read_double());
            break;

        case TCKind._tk_longlong:
            write_longlong(in.read_longlong());
            break;

        case TCKind._tk_ulonglong:
            write_ulonglong(in.read_ulonglong());
            break;
        case TCKind._tk_any:
            write_any(in.read_any());
            break;

        case TCKind._tk_TypeCode:
            write_TypeCode(in.read_TypeCode());
            break;

        case TCKind._tk_Principal:
            write_Principal(in.read_Principal());
            break;

        case TCKind._tk_objref:
            write_Object(in.read_Object());
            break;

        case TCKind._tk_string:
            write_string(in.read_string());
            break;

        case TCKind._tk_wstring:
            write_wstring(in.read_wstring());
            break;

        case TCKind._tk_array:
            try {
                int length = tc.length();

                for (int i = 0; i < length; i++)
                    write_value(tc.content_type(), in);
            } catch (org.omg.CORBA.TypeCodePackage.BadKind b) {
                System.err.println("CDRInputStream#write_value threw BadKind exception for array");
                b.printStackTrace();
           }
            break;

        case TCKind._tk_sequence:
            try {
                int len = in.read_long();
                write_long(len);
                for (int i = 0; i < len; i++) {
                    org.omg.CORBA.TypeCode tck = tc.content_type();
                    write_value(tck, in);
                }
            } catch (org.omg.CORBA.TypeCodePackage.BadKind b) {
                System.out.println("CDROutputStream#write_value: Unable to write sequence");
                b.printStackTrace();
            }
            break;

        case TCKind._tk_except:
            write_string(in.read_string());
            // don't break, fall through to ...
            
        case TCKind._tk_struct:
            //            recursiveTCStack.push(tc);
            try {
                for (int i = 0; i < tc.member_count(); i++)
                    write_value(tc.member_type(i), in);
            } catch (org.omg.CORBA.TypeCodePackage.BadKind b) {
                System.out.println("CDROutputStream#write_value: Unable to write struct");
                b.printStackTrace();
            } catch (org.omg.CORBA.TypeCodePackage.Bounds b) {
                System.out.println("CDROutputStream#write_value: Unable to write struct");
                b.printStackTrace();
            }
            // recursiveTCStack.pop();
            break;
            

        case TCKind._tk_enum:
            write_long(in.read_long());
            break;

        case TCKind._tk_alias:
            write_value(edu.uci.ece.zen.orb.TypeCode.originalType(tc), in);
            break;


        default:
            ZenProperties.logger.log(
                Logger.FATAL ,
                "edu.uci.ece.zen.orb.CDROutputStream (TypeCodeAspect)",
                "write_value()",
                "Unimplemented method in CDROutputStream" );
            break;
        }
    }



    // The method below is commented out for real-time Zen because it is not used.

    // Bruce added this class for use with Any's based on the fact
    // that the implementations of Any's frequently needed the
    // function.
    /**
     * Returns a <bold>copy<bold> of the contents of the buffer.
     * Similar to <code>CDROutputStream.getBuffer()</code>.
     *
     * @return Copy of the contents of the buffer, as a byte array.
    */
    /*
    public final byte[] edu.uci.ece.zen.orb.CDROutputStream.getBufferCopy() {
                byte[] bbmanager_buffer = bbmanager.getBuffer();
        byte[] buffer_copy = new byte [bbmanager_buffer.length];
        System.arraycopy(bbmanager_buffer, 0, buffer_copy, 0, bbmanager_buffer.length);
        return buffer_copy;
    }
    */


    // Introductions to the TypeCode class


    /** Contructor for sequence and array TypeCode
     * 
     * @param bound Maximum number of elements in sequence or array, 0 if unbounded
     * @param element_type type of elements stored in the sequence or array
     */
    public edu.uci.ece.zen.orb.TypeCode.new(int bound,
             org.omg.CORBA.TypeCode element_type) {
        kind = TCKind._tk_sequence;
        length = bound;
        type = element_type;
    }
        
    /** 
     * Constructor for recursive Sequence TypeCode
     * 
     * @param bound maximum length of sequence, or 0 is unbounded
     * @param offset Offset
     * @deprecated since CORBA v2.3, see page 10-52 of CORBA v2.3 Spec (that's section 10.7.3)
     */
    public edu.uci.ece.zen.orb.TypeCode.new(int bound,
        int offset) {
        kind = TCKind._tk_sequence;
        length = bound;
        this.offset = offset;
        // what does offset get stored to?
    }

    /** 
     * Constructor for fixed TypeCode
     * <p>
     * Fixed Typecode has not been tested because the Interface Repository
     * has not been implemented yet
     *
     * @param _digits the number of digits that the number will have.
     * @param _scale number of digits that are to the right of the decimal point.
     */
    public edu.uci.ece.zen.orb.TypeCode.new(short _digits,
        short _scale) {
        kind = TCKind._tk_fixed;
        digits = _digits;
        scale = _scale;
    }


    /**
     * Constructor for Enum TypeCode
     * 
     * @param _id String in the interface repository globally identifying this type.
     * @param _name Simple name identifying this enumeration in its enclosing scope
     * @param _members enumeration name constants for each member of the enumeration.
     */
    public edu.uci.ece.zen.orb.TypeCode.new(java.lang.String _id,
        java.lang.String _name,
        java.lang.String[] _members) {
        kind = TCKind._tk_enum;
        id = _id;
        name = _name;
        member_count = _members.length;
        member_names = new String[member_count];
        for (int i = 0; i < member_count; i++)
            member_names[i] = _members[i];
    }
        /**
         * Constructor for Struct and Exception TypeCode
         *
         * @param _kind TCKind._tk_struct for struction, _tk_except for exception
         * @param _id String in the interface repository globally identifying this type.
         * @param _name Simple name identifying this enumeration in its enclosing scope
         * @param _members org.omg.CORBA.StructMember objects representing members of the Struct or Exception
         */
    public edu.uci.ece.zen.orb.TypeCode.new(int _kind,
        java.lang.String _id,
        java.lang.String _name,
        org.omg.CORBA.StructMember[] _members) {
        kind = _kind;
        id = _id;
        name = _name;
        member_count = _members.length;
        member_names = new String[member_count];
        member_types = new TypeCode[member_count];
        for (int i = 0; i < member_count; i++) {
            member_names[i] = _members[i].name;
            member_types[i] = (TypeCode) _members[i].type;
        }
    }    
    
        // Constructor for Union
    public edu.uci.ece.zen.orb.TypeCode.new(java.lang.String _id,
        java.lang.String _name,
        org.omg.CORBA.TypeCode _discriminator_type,
        org.omg.CORBA.UnionMember[] _members) {
        kind = TCKind._tk_union;
        id = _id;
        name = _name;
        discriminator_type = (TypeCode) _discriminator_type;
        default_index = -1;  // represents no default member

        member_count = _members.length;
        member_names = new String[member_count];
        member_labels = new edu.uci.ece.zen.orb.any.Any[member_count];
        member_types = new TypeCode[member_count];
        member_idlTypes = new IDLType[member_count];
        for (int i = 0; i < member_count; i++) {
            member_names[i] = _members[i].name;
            member_types[i] = (TypeCode) _members[i].type;
            member_labels[i] = (edu.uci.ece.zen.orb.any.Any) _members[i].label;
            member_idlTypes[i] = (IDLType) _members[i].type_def;

        }
    }
    
    
    // This has not been tested because the Interface Repository
    // has not been implemented yet
    // Constructor for value TypeCode
    public edu.uci.ece.zen.orb.TypeCode.new(java.lang.String _id,
        java.lang.String _name,
        short _type_modifier,
        org.omg.CORBA.TypeCode concrete_base,
        org.omg.CORBA.ValueMember[] _members) {
        kind = TCKind._tk_value;
        id = _id;
        name = _name;
        type_modifier = _type_modifier;
        type = concrete_base;
        member_count = _members.length;
        member_names = new String[member_count];
        member_ids = new String[member_count];
        member_defined_ins = new String[member_count];
        member_versions = new String[member_count];
        member_types = new TypeCode[member_count];
        member_idlTypes = new IDLType[member_count];
        member_visibility = new short[member_count];

        for (int i = 0; i < member_count; i++) {
            member_names[i] = _members[i].name;
            member_ids[i] = _members[i].id;
            member_defined_ins[i] = _members[i].defined_in;
            member_versions[i] = _members[i].version;
            member_types[i] = (TypeCode) _members[i].type;
            member_idlTypes[i] = (IDLType) _members[i].type_def;
            member_visibility[i] = (short) _members[i].access;
        } 
    }    



   /** Constructor for object reference (objref, _tk_objref) type
    * @return TypeCode object for Object Reference Type
    */
    public static edu.uci.ece.zen.orb.TypeCode edu.uci.ece.zen.orb.TypeCode.newObjRefTC(String _id, String _name) {
        edu.uci.ece.zen.orb.TypeCode new_tc = new TypeCode(_id, _name);
        new_tc.kind = TCKind._tk_objref;
        return new_tc;
    }
        

   /** Constructor for string and wstring type, one of two ways to make.
    * @return TypeCode object for Object Reference Type
    */
    public static edu.uci.ece.zen.orb.TypeCode edu.uci.ece.zen.orb.TypeCode.newStringTC(int _kind, int _length) {
        edu.uci.ece.zen.orb.TypeCode new_tc = new TypeCode(_kind);
        new_tc.length = _length;
        return new_tc;

    }


   /**
    * Return a compact representation of the TypeCode without
    * optional name and member_name fields.  
    * See Section 10.7.1 of the CORBA v2.3 Spec.
    * @return TypeCode without optional name and member_name fields
    */
    public org.omg.CORBA.TypeCode edu.uci.ece.zen.orb.TypeCode.get_compact_typecode() {
        return duplicateWithoutNames();
    }


    /**
     * Copy the data of an existing typecode without copying the
     * optional name & member name fields.
     *
     * @return TypeCode without optional name & member_name fields
     */
    private edu.uci.ece.zen.orb.TypeCode edu.uci.ece.zen.orb.TypeCode.duplicateWithoutNames() {
        edu.uci.ece.zen.orb.TypeCode newTC = new edu.uci.ece.zen.orb.TypeCode(kind);
        newTC.type = type;
        // Don't copy the name.
        //newTC.name = name;
        newTC.id = id;
        newTC.length = length;
        newTC.offset = offset;
        newTC.default_index = default_index;
        if (member_labels != null) {
            newTC.member_labels = new edu.uci.ece.zen.orb.any.Any [member_labels.length];
            for (int i = 0; i < member_labels.length; i++) {
                newTC.member_labels[i] = member_labels[i];
            }
        }
        if (member_types != null) {
            newTC.member_types = new TypeCode[member_types.length];
            for (int i = 0; i < member_types.length; i++) {
                newTC.member_types[i] = member_types[i].duplicateWithoutNames();
            }
        }

        if (member_names != null) {
            newTC.member_names = new String[member_names.length];
            for (int i = 0; i < member_names.length; i++) {
                // Set member names to empty string
                newTC.member_names[i] = "";
            }
        }
        
        if (member_idlTypes != null) {
            newTC.member_idlTypes = new IDLType [member_idlTypes.length];
            for (int i = 0; i < member_idlTypes.length; i++) {
                newTC.member_idlTypes[i] = member_idlTypes[i];
            }
        }
        
        newTC.member_count = member_count;
        newTC.discriminator_type = discriminator_type.duplicateWithoutNames();
        newTC.digits = digits;
        newTC.scale = scale;
        newTC.type_modifier = type_modifier;
        if (member_ids != null) {
            newTC.member_ids = new String [member_ids.length];
            for (int i = 0; i < member_ids.length; i++) {
                newTC.member_ids[i] = member_ids[i];
            }
        }

        if (member_defined_ins != null) {
            newTC.member_defined_ins = new String [member_defined_ins.length];
            for (int i = 0; i < member_defined_ins.length; i++) {
                newTC.member_defined_ins[i] = member_defined_ins[i];
            }
        }

        if (member_versions != null) {
            newTC.member_versions = new String [member_versions.length];
            for (int i = 0; i < member_versions.length; i++) {
                newTC.member_versions[i] = member_versions[i];
            }
        }
        if (member_visibility != null) {
            newTC.member_visibility = new short [member_visibility.length];
            for (int i = 0; i < member_visibility.length; i++) {
                newTC.member_visibility[i] = member_visibility[i];
            }
        }
        return newTC;
    }


    /** @see edu.uci.ece.zen.orb.TypeCode#member_count()
     */
    abstract public int org.omg.CORBA.TypeCode.member_count() throws 
        org.omg.CORBA.TypeCodePackage.BadKind;


    // Bruce Miller wrote the method below.
    // If the way that this and similar methods are written doesn't
    // make sense to you and you don't understand why it is better
    // than using a switch statement or a bunch of if ( (x == y1) ||
    // (x==y2) || (x==y3)) then you should not be editing it.

    /**
     * Return the count of member items of this type.  For example,
     * structures, unions, enumerations, etc. have members.  See
     * Section 10.7.1 of the CORBA v2.3 Spec
     *
     * @return int number of members of this TypeCode
     * @throws org.omg.CORBA.TypeCodePackage.BadKind if this Type does not support having members.
     */
    public int edu.uci.ece.zen.orb.TypeCode.member_count() throws
        org.omg.CORBA.TypeCodePackage.BadKind {
        long kindMask = 1L << kind;

        // This is a bitmask of accepted types and the compiler will
        // fold into a constant stored in the data segment.
        final long acceptedKinds = 0L 
            | (1L << org.omg.CORBA.TCKind._tk_struct)
            | (1L << org.omg.CORBA.TCKind._tk_union)
            | (1L << org.omg.CORBA.TCKind._tk_enum)
            | (1L << org.omg.CORBA.TCKind._tk_value)
            | (1L << org.omg.CORBA.TCKind._tk_except);

        if ( (kindMask & acceptedKinds) == 0) {
            // In case you are wondering, it only took five machine
            // instructions to get here.  That's much smaller than any
            // switch statement will ever be.
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#member_count() called on invalid TypeCode");
        }

        return member_count;
    }


    /** @see edu.uci.ece.zen.orb.TypeCode#member_name(int)
     */
    abstract public java.lang.String org.omg.CORBA.TypeCode.member_name(int index) throws
        org.omg.CORBA.TypeCodePackage.BadKind,
        org.omg.CORBA.TypeCodePackage.Bounds;

    /**
     * Return the name (human readable) of a member of this type.
     * @param index index at which to get member
     * @return String name of the member
     * @throws org.omg.CORBA.TypeCodePackage.BadKind if this type doesn't have members
     */
    public java.lang.String edu.uci.ece.zen.orb.TypeCode.member_name(int index) throws
        org.omg.CORBA.TypeCodePackage.BadKind,
        org.omg.CORBA.TypeCodePackage.Bounds {

        long kindMask = 1L << kind;

        // This is a bitmask of accepted types and the compiler will
        // fold into a constant stored in the data segment.
        final long acceptedKinds = 0L 
            | (1L << org.omg.CORBA.TCKind._tk_struct)
            | (1L << org.omg.CORBA.TCKind._tk_union)
            | (1L << org.omg.CORBA.TCKind._tk_enum)
            | (1L << org.omg.CORBA.TCKind._tk_value)
            | (1L << org.omg.CORBA.TCKind._tk_except);

        if ( (kindMask & acceptedKinds) == 0) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#member_name(int) called on invalid TypeCode");
        }
        if ( index > member_count ) {
            throw new org.omg.CORBA.TypeCodePackage.Bounds("TypeCode#member_name(int index) called with index exceeding number of members");
        }

        return member_names[index];
    }


    /** @see edu.uci.ece.zen.orb.TypeCode#member_type(int)
     */
    abstract public org.omg.CORBA.TypeCode org.omg.CORBA.TypeCode.member_type(int index) throws
        org.omg.CORBA.TypeCodePackage.BadKind,
        org.omg.CORBA.TypeCodePackage.Bounds;


    /**
     * Return the TypeCode for a member of the typecode that this
     * represents.  See Section 10.7.1 of the CORBA v2.3 Spec.
     *
     * @return TypeCode for a member of this TypeCode at member index <code>index</code>.
     */
    public org.omg.CORBA.TypeCode edu.uci.ece.zen.orb.TypeCode.member_type(int index) throws
        org.omg.CORBA.TypeCodePackage.BadKind,
        org.omg.CORBA.TypeCodePackage.Bounds {

        long kindMask = 1L << kind;

        final long acceptedKinds = 0L
            | (1L << org.omg.CORBA.TCKind._tk_struct)
            | (1L << org.omg.CORBA.TCKind._tk_union)
            | (1L << org.omg.CORBA.TCKind._tk_value)
            | (1L << org.omg.CORBA.TCKind._tk_except);

        if ( (kindMask & acceptedKinds) == 0) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#member_type(int) called on invalid TypeCode");
        }
        if ( index > member_count ) {
            throw new org.omg.CORBA.TypeCodePackage.Bounds("TypeCode#member_type(int index) called with index exceeding number of members");
        }
        return member_types[index];
    }


    /** @see edu.uci.ece.zen.orb.TypeCode#member_label(int)
     */
    abstract public org.omg.CORBA.Any org.omg.CORBA.TypeCode.member_label(int index) throws
        org.omg.CORBA.TypeCodePackage.BadKind,
        org.omg.CORBA.TypeCodePackage.Bounds;

    /**
     * Get the member label of the union member identified by index.
     * See Section 10.7.1 of the CORBA v2.3 Spec.
     * @return the member label of the union member identified by index
     */
    public org.omg.CORBA.Any edu.uci.ece.zen.orb.TypeCode.member_label(int index) throws
        org.omg.CORBA.TypeCodePackage.BadKind,
        org.omg.CORBA.TypeCodePackage.Bounds {

        if (kind != org.omg.CORBA.TCKind._tk_union) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#member_label(int) called on invalid TypeCode");
        }
        if ( index > member_count ) {
            throw new org.omg.CORBA.TypeCodePackage.Bounds("TypeCode#member_label(int index) called with index exceeding number of members");
        }

        return member_labels[index];
    }


    // This methed commented out by Bruce on 2003-09-16 because it is
    // never used, and is not listed in the CORBA spec.
    /*    
    public org.omg.CORBA.IDLType org.omg.CORBA.TypeCode.memberIdlType(int index) {

        //throws
        //org.omg.CORBA.TypeCodePackage.BadKind,
        //org.omg.CORBA.TypeCodePackage.Bounds {

        if (kind == org.omg.CORBA.TCKind._tk_union) {
            return member_idlTypes[index];
        }
        else {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#memberIdlType() called on invalid TypeCode");
        }
    }
    */


    /** @see edu.uci.ece.zen.orb.TypeCode#discriminator_type()
     */
    abstract public org.omg.CORBA.TypeCode org.omg.CORBA.TypeCode.discriminator_type() throws
        org.omg.CORBA.TypeCodePackage.BadKind;
    /**
     * Return the type of all non-default member labels. See Section
     * 10.7.1 of the CORBA v2.3 Spec.
     * @return TypeCode type of all non-default member labels.
     */
    public org.omg.CORBA.TypeCode edu.uci.ece.zen.orb.TypeCode.discriminator_type() throws
        org.omg.CORBA.TypeCodePackage.BadKind {

        if (kind != org.omg.CORBA.TCKind._tk_union) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#discriminator_type() called on invalid TypeCode");
        }

        return discriminator_type;
    }


    /** @see edu.uci.ece.zen.orb.TypeCode#default_index()
     */ 
    abstract public int org.omg.CORBA.TypeCode.default_index() throws
        org.omg.CORBA.TypeCodePackage.BadKind;

    /**
     * Return index of default member.
     * @return index of default member, or -1 if there is no default member.
     */
    public int edu.uci.ece.zen.orb.TypeCode.default_index() throws
        org.omg.CORBA.TypeCodePackage.BadKind {

        if (kind != org.omg.CORBA.TCKind._tk_union) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#default_index() called on invalid TypeCode");
        }

        return default_index;
    }



    /** @see edu.uci.ece.zen.orb.TypeCode#length()
     */
    abstract public int org.omg.CORBA.TypeCode.length() throws org.omg.CORBA.TypeCodePackage.BadKind;

    /** See Section 10.7.1 of the CORBA v2.3 Spec
     */
    public int edu.uci.ece.zen.orb.TypeCode.length() throws org.omg.CORBA.TypeCodePackage.BadKind {
        long kindMask = 1L << kind;

        final long acceptedKinds = 0L
            | (1L << org.omg.CORBA.TCKind._tk_string)
            | (1L << org.omg.CORBA.TCKind._tk_sequence)
            | (1L << org.omg.CORBA.TCKind._tk_array);

        if ( (kindMask & acceptedKinds) == 0 ) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("TypeCode#length() called on invalid TypeCode");
        }

        return length;
    }


    /** @see edu.uci.ece.zen.orb.TypeCode#content_type()
     */
    abstract public org.omg.CORBA.TypeCode org.omg.CORBA.TypeCode.content_type() throws
        org.omg.CORBA.TypeCodePackage.BadKind;



    // I have not implemented this method for value box
    // (_tk_value_box) types yet, but I assume from reading the spec
    // that they would just return the variable type, jsut like
    // sequence and array do.
    /** See Section 10.7.1 of the CORBA v2.3 Spec
     */
    public org.omg.CORBA.TypeCode edu.uci.ece.zen.orb.TypeCode.content_type() throws
        org.omg.CORBA.TypeCodePackage.BadKind {

        long kindMask = 1L << kind;
        final long acceptedKinds = 0L
            | (1L << org.omg.CORBA.TCKind._tk_sequence)
            | (1L << org.omg.CORBA.TCKind._tk_array)
            | (1L << org.omg.CORBA.TCKind._tk_alias)
            | (1L << org.omg.CORBA.TCKind._tk_value_box);
        
        // Return type of stored element or what we are immediate
        // alias for, or what the boxed value type is boxing
        if ( (kindMask & acceptedKinds) > 0) {
            return type;  
        }

        throw new org.omg.CORBA.TypeCodePackage.BadKind("edu.uci.ece.zen.orb.TypeCode#content_type() should not be called on kind " + kind);
    }


    // The fixed_digits() and fixed_scale() methods were not in the
    // TypeCode code until Bruce needed them on 2003-06-02 for the
    // monolithic Any's insert_fixed() method.  The monolithic Any
    // implementation was copied from JacORB which apparently made
    // greater use of TypeCodes than our Zen implementation does.
    public short org.omg.CORBA.TypeCode.fixed_digits() throws
    org.omg.CORBA.TypeCodePackage.BadKind {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    public short org.omg.CORBA.TypeCode.fixed_scale() throws
    org.omg.CORBA.TypeCodePackage.BadKind {
        throw new org.omg.CORBA.NO_IMPLEMENT();

    }


    /** @see edu.uci.ece.zen.orb.TypeCode#member_visibility(int)
     */
    abstract public short org.omg.CORBA.TypeCode.member_visibility(int index)
        throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds;

    /** See Section 10.7.1 of the CORBA v2.3 Spec
     */
    public short edu.uci.ece.zen.orb.TypeCode.member_visibility(int index) 
        throws org.omg.CORBA.TypeCodePackage.BadKind, org.omg.CORBA.TypeCodePackage.Bounds {
        if (kind != org.omg.CORBA.TCKind._tk_value) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("edu.uci.ece.zen.orb.TypeCode#content_type() should not be called on kind " + kind);
        }
        if ( index > member_visibility.length ) {
            throw new org.omg.CORBA.TypeCodePackage.Bounds("edu.uci.ece.zen.orb.TypeCode#member_label(int index) called with index exceeding number of members");
        }
        return member_visibility[index];
    }


    /** @see edu.uci.ece.zen.orb.TypeCode#type_modifier()
     */
    abstract public short org.omg.CORBA.TypeCode.type_modifier()
        throws org.omg.CORBA.TypeCodePackage.BadKind;

    /** 
     * For non-boxed valuetype, returns the ValueModifier that applies
     * to the valuetype represented by the target TypeCode, according
     * to section 10.7.1 of the CORBA v2.3 Spec
     * @return ValueModifier for valuetype
     * @throws org.omg.CORBA.TypeCodePackage.BadKind if not called on a non-boxed valuetype
     */
    public short edu.uci.ece.zen.orb.TypeCode.type_modifier()
        throws org.omg.CORBA.TypeCodePackage.BadKind {
         if (kind != org.omg.CORBA.TCKind._tk_value) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("edu.uci.ece.zen.orb.TypeCode#type_modifier() should not be called on kind " + kind);
        }

        return type_modifier;
    }


    /** @see edu.uci.ece.zen.orb.TypeCode#concrete_base_type()
     */
    abstract public org.omg.CORBA.TypeCode org.omg.CORBA.TypeCode.concrete_base_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind;

    /** 
     * May be called on non-boxed valuetype TypeCodes.  According to
     * section 10.7.1 of the CORBA v2.3 Spec "If the valuetype
     * represented by the target TypeCode has a cencrete bas
     * valuetype, the concrete_base_type operation returns a TypeCode
     * for the concrete base, otherwise, it returns a nil TypeCode
     * reference.
     *
     * @return nil if ValueType did not have concrete base type, the base type otherwise
     * @throws org.omg.CORBA.TypeCodePackage.BadKind if not called on a non-boxed valuetype
     */
    public org.omg.CORBA.TypeCode edu.uci.ece.zen.orb.TypeCode.concrete_base_type()
        throws org.omg.CORBA.TypeCodePackage.BadKind {
         if (kind != org.omg.CORBA.TCKind._tk_value) {
            throw new org.omg.CORBA.TypeCodePackage.BadKind("edu.uci.ece.zen.orb.TypeCode#type_modifier() should not be called on kind " + kind);
        }

        return type;
    }



    // BM This function is used by the monolithic implementation of
    // Anys, and I am simply leaving a stub here because I plan to
    // finish this functionality later, but other people need me to
    // have Anys compiling now.
    
    // In JacORB, this method creates a TypeCode "for an an arbitrary
    // java class" but it only supports RMI classes.
    public static TypeCode org.omg.CORBA.TypeCode.create_tc(Class cls) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }



    /**
     * Unwinds any TCKind.tk_alias's (caused by idl typecodes) to find
     * original type of a TcypeCode.
     *
     * @param t TypeCode that may be a alias type, but for which a
     * base type is definitely wanted.
     * @return TypeCode at root of aliases.
     */
    public static org.omg.CORBA.TypeCode edu.uci.ece.zen.orb.TypeCode.originalType(org.omg.CORBA.TypeCode t) {
        org.omg.CORBA.TypeCode tc_temp = t;
        try {
            while (tc_temp.kind().value() == org.omg.CORBA.TCKind._tk_alias) {
                tc_temp = tc_temp.content_type();
            }
        }
        catch (org.omg.CORBA.TypeCodePackage.BadKind bk) {
            // Does not happen according to JacORB's org.jacorb.orb.TypeCode#originalType() method
            System.out.println("TypeCode#originalType BadKind exception occurred");
            bk.printStackTrace();
        }
        return tc_temp;
    }

    

    // If any variables are added here, make sure to update the clone() method
    int org.omg.CORBA.TypeCode.length;
    int org.omg.CORBA.TypeCode.offset = -1;
    int org.omg.CORBA.TypeCode.default_index;
    edu.uci.ece.zen.orb.any.Any[] org.omg.CORBA.TypeCode.member_labels = null;
    TypeCode[] org.omg.CORBA.TypeCode.member_types = null;
    String[] org.omg.CORBA.TypeCode.member_names = null;
    IDLType[] org.omg.CORBA.TypeCode.member_idlTypes = null;
    int org.omg.CORBA.TypeCode.member_count;
    TypeCode org.omg.CORBA.TypeCode.discriminator_type;
    short org.omg.CORBA.TypeCode.digits;
    short org.omg.CORBA.TypeCode.scale;
    short org.omg.CORBA.TypeCode.type_modifier;
    String[] org.omg.CORBA.TypeCode.member_ids = null;
    String[] org.omg.CORBA.TypeCode.member_defined_ins = null;
    String[] org.omg.CORBA.TypeCode.member_versions = null;
    short[] org.omg.CORBA.TypeCode.member_visibility = null;


    /**
     * Stores primitive typecode objects indexed by their TCKind
     * number.  Used by lookupPrimitiveTC, and an array of memory
     * pointers take up a lot less space then rewriting code to check
     * if a value is null.
     */
    private static org.omg.CORBA.TypeCode [] primitiveTCMap = new org.omg.CORBA.TypeCode [32];


    /**
     * Returns a TypeCode object for a primitive Type Code when passed
     * a TCKind number for a typecode.  Used as a helper method for
     * org.omg.CORBA.ORB.get_primitive_tc().
     *
     * @param tcKindValue a TCKind.value() integer representing a Type Code Kind
     * @return TypeCode, being generic, for the tcKindValue
     */
    public static org.omg.CORBA.TypeCode edu.uci.ece.zen.orb.TypeCode.lookupPrimitiveTc(int tcKindValue) {
        org.omg.CORBA.TypeCode mapValue = primitiveTCMap[tcKindValue];
        if (mapValue == null) {
            mapValue = new edu.uci.ece.zen.orb.TypeCode(tcKindValue);
            primitiveTCMap[tcKindValue] = mapValue;
        }
        return mapValue;
    }



    //Introductions to ORB
    
    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_enum_tc(String id, String name,
                        String[] members) {
        return new edu.uci.ece.zen.orb.TypeCode(id, name, members);
    }  
    
    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.get_primitive_tc(org.omg.CORBA.TCKind tcKind) {
        return edu.uci.ece.zen.orb.TypeCode.lookupPrimitiveTc(tcKind.value());
        // Discarded old implementation:
        //return new edu.uci.ece.zen.orb.TypeCode(tcKind.value());
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_struct_tc(String id, String name,
                        org.omg.CORBA.StructMember[] members) {
        return new edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_struct,
                        id, name, members);
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_union_tc(String id, String name,
                        org.omg.CORBA.TypeCode discriminator_type,
                        org.omg.CORBA.UnionMember[] members) {
        return new edu.uci.ece.zen.orb.TypeCode(id, name, discriminator_type,
                        members);
    }



    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_alias_tc(String id, String name,
                        org.omg.CORBA.TypeCode original_type) {
        return new edu.uci.ece.zen.orb.TypeCode(id, name, original_type);
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_exception_tc(String id, String name,
                            org.omg.CORBA.StructMember[] members) {
        return new edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_except, id, name, members);
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_interface_tc(String id, String name) {
        return new edu.uci.ece.zen.orb.TypeCode(id, name);
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_string_tc(int bound) {
        //return new edu.uci.ece.zen.orb.TypeCode(bound);
        return new edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_string);
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_wstring_tc(int bound) {
        //return new edu.uci.ece.zen.orb.TypeCode(bound);
        return new edu.uci.ece.zen.orb.TypeCode(org.omg.CORBA.TCKind._tk_wstring);
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_sequence_tc(int bound,
                            TypeCode element_type) {
        return new edu.uci.ece.zen.orb.TypeCode(bound, element_type);
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_sequence_tc(int bound,
                            org.omg.CORBA.TypeCode element_type) {
        return new edu.uci.ece.zen.orb.TypeCode(bound, element_type);
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_recursive_sequence_tc(int bound, int offset) {
        return new edu.uci.ece.zen.orb.TypeCode(bound, offset);
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_array_tc(int length, org.omg.CORBA.TypeCode element_type) {
        edu.uci.ece.zen.orb.TypeCode arrayTypecode = new edu.uci.ece.zen.orb.TypeCode(length, element_type);
        arrayTypecode.kind = TCKind._tk_array;

        return arrayTypecode;//new edu.uci.ece.zen.orb.TypeCode(length, element_type);
    }


    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_native_tc(String id,
                        String name) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_abstract_interface_tc(String id, String name) {
        return new edu.uci.ece.zen.orb.TypeCode(id, name);
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_fixed_tc(short digits, short scale) {
        return new edu.uci.ece.zen.orb.TypeCode(digits, scale);
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_value_tc(String id,
                                                String name,
                                                short type_modifier,
                                                org.omg.CORBA.TypeCode concrete_base,
                                                org.omg.CORBA.ValueMember[] members) {
        return new edu.uci.ece.zen.orb.TypeCode (id, name, type_modifier, concrete_base, members);
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_recursive_tc(String id) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.TypeCode org.omg.CORBA.ORB.create_value_box_tc(String id,
                            String name,
                            org.omg.CORBA.TypeCode boxed_type) {
        // Uses the constructor for alias TypeCode, then changes the kind.
        edu.uci.ece.zen.orb.TypeCode newTC = new edu.uci.ece.zen.orb.TypeCode(id, name, boxed_type);
        newTC.kind = org.omg.CORBA.TCKind._tk_value_box;
        return newTC;
    }    


    /**
     * Creata a new Any that has its orb reference populated by this
     * orb.
     *
     * @return edu.uci.ece.zen.orb.any.Any using default strategy for
     * implementing Anys, with its orb reference populated by this
     * orb.
    */
    public org.omg.CORBA.Any org.omg.CORBA.ORB.create_any() {
        return new edu.uci.ece.zen.orb.any.Any(this);
    }


    //Introductions to ORBSingleton
/*
    This is actually covered my the create_any() above
    
    public Any org.omg.CORBA.ORBSingleton.create_any() {
        // This method has to be implemented.
        return new edu.uci.ece.zen.orb.any.Any(this);
        //throw new org.omg.CORBA.NO_IMPLEMENT();
    }    
*/
/*
    
    //These are temporary since the corresponding classes are not generated by the IDL compiler.
    public static org.omg.CORBA.TypeCode org.omg.CORBA.SystemExceptionHelper._type = org.omg.CORBA.ORB.init().create_exception_tc( org.omg.CORBA.SystemExceptionHelper.id(),"SystemException",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("minor",org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(5)),null),new org.omg.CORBA.StructMember("completed",org.omg.CORBA.ORB.init().create_enum_tc(org.omg.CORBA.CompletionStatusHelper.id(),"CompletionStatus",new String[]{"COMPLETED_YES","COMPLETED_NO","COMPLETED_MAYBE"}),null)});   
    public static org.omg.CORBA.TypeCode org.omg.CORBA.CompletionStatusHelper._type = org.omg.CORBA.ORB.init().create_enum_tc(org.omg.CORBA.CompletionStatusHelper.id(),"CompletionStatus",new String[]{"COMPLETED_YES","COMPLETED_NO","COMPLETED_MAYBE"});

    before (org.omg.CORBA.ParameterModeHelper obj): target(obj) &&
        initialization(org.omg.CORBA.ParameterModeHelper.new(..) ){

        obj._type = org.omg.CORBA.ORB.init().create_enum_tc(org.omg.CORBA.ParameterModeHelper.id(),"ParameterMode",new String[]{"PARAM_IN","PARAM_OUT","PARAM_INOUT"});
            

    }
*/
}
