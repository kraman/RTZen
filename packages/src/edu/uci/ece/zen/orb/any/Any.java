//
// $Id: Any.java,v 1.2 2004/02/04 23:58:02 kraman Exp $
//

package edu.uci.ece.zen.orb.any;

//import org.omg.CORBA.*;
import edu.uci.ece.zen.orb.*; 

/**
 * Implement the CORBA Any class, proxying off to strategies that
 * perform the actual computation and work.
 *
 * <p>
 * The CORBA any data type can hold all other CORBA datatypes
 * (including any), the CORBA.Any class is the language mapping entity
 * used to manipulate and access Anys.  This class is the
 * implementation of the CORBA.Any type.
 *
 * <p>
 * This version of Any.java represents a strategized Any -- an Any
 * class that follows the strategization design pattern.
 *
 * <p> 
 * The most important methods in this module are the constructors and
 * the setStrategy(AnyStrategy) method.  The rest are better
 * documented in the {@link edu.uci.ece.zen.orb.any.pluggable.Any
 * edu.uci.ece.zen.orb.any.pluggable.Any} class and the {@link
 * edu.uci.ece.zen.orb.any.monolithic.Any
 * edu.uci.ece.zen.orb.any.monolithic.Any} class.
 *
 * @author Bruce Miller
 * @version $Revision: 1.2 $ $Date: 2004/02/04 23:58:02 $
 * @see edu.uci.ece.zen.orb.any.pluggable.Any edu.uci.ece.zen.orb.any.pluggable.Any
 * @see edu.uci.ece.zen.orb.any.monolithic.Any edu.uci.ece.zen.orb.any.monolithic.Any
 *
 */

public class Any
    extends org.omg.CORBA.Any {

    /**
     * Create an Any using the default implementation of being
     * pluggable -- Please use the method that takes an orb as a
     * parameter because the implementation must pass the orb
     * reference needs to be passed to CDROutputStream for some
     * methods.
     */
    /*
    public Any() {
        if (currentStrategy == 0)
            anyStrategy = new edu.uci.ece.zen.orb.any.pluggable.Any();
        else 
            anyStrategy = new edu.uci.ece.zen.orb.any.monolithic.Any();
    }
    */

    /**
     * Create an Any using the default implementation of being
     * pluggable.
     *
     * @param _orb ORB reference to store.
     */
    public Any(org.omg.CORBA.ORB _orb) {
        /*
        if (currentStrategy == 0)
            anyStrategy = new edu.uci.ece.zen.orb.any.pluggable.Any(_orb);
        else
            anyStrategy = new edu.uci.ece.zen.orb.any.monolithic.Any(_orb);
        */
        try {
            anyStrategy = (edu.uci.ece.zen.orb.any.AnyStrategy) currentAnyStrategyClass.newInstance();
            anyStrategy.setOrb((edu.uci.ece.zen.orb.ORB) _orb);
        }
        catch (InstantiationException ie) {
            System.err.println("edu.uci.ece.zen.orb.any.Any had instantiation exception while instantiating the class set in the zen.properties file for the property name \"zen.any.anyStrategy\".  That property led to the strategy class being set to \"" + currentAnyStrategyClass + "\"");
            System.err.println(ie);
            ie.printStackTrace();
        }
        catch (IllegalAccessException iae) {
            System.err.println("edu.uci.ece.zen.orb.any.Any had IllegalAccessException while instantiating the class set in the zen.properties file for the property name \"zen.any.anyStrategy\".  That property led to the strategy class being set to \"" + currentAnyStrategyClass + "\"");
            iae.printStackTrace();
        }

    }


    /** Set the strategy used to implement Anys. 
     *
     * @param _newStrategy either "pluggable" for pluggable anys or "monolithic" for monolithic anys.
     */
    /*
    public static void setStrategy(String _newStrategy) {
        if (_newStrategy.equals("pluggable"))
            currentStrategy = 0;
        else if (_newStrategy.equals("monolithic"))
            currentStrategy = 1;
        else
            throw new org.omg.CORBA.BAD_PARAM("edu.uci.ece.zen.orb.Any#setStrategy called with bad param");
    }
    */

    public static void setStrategy(String _newStrategy) {
        String desiredClassName = "edu.uci.ece.zen.orb.any." + _newStrategy + ".Any";
        try {
            currentAnyStrategyClass = Class.forName(desiredClassName);
        }
        catch (ClassNotFoundException cnfe) {
            System.err.println("edu.uci.ece.zen.orb.any.Any could not find the any strategy set in the zen.properties file for the property name \"zen.any.anyStrategy\".  That property was found to be set to \"" + _newStrategy + "\"");
            cnfe.printStackTrace();
        }

        // Next two lines were just for debugging
        // System.out.println("Current Any Strategy String is : " + _newStrategy);
        // System.out.println("Current Any Strategy is : " + currentAnyStrategyClass);
        
    }


    public boolean equal (org.omg.CORBA.Any a) {
        edu.uci.ece.zen.orb.any.Any anAny = (edu.uci.ece.zen.orb.any.Any) a;
        return anyStrategy.equal(anAny.anyStrategy);
    }

    public org.omg.CORBA.TypeCode type() {
        return anyStrategy.type();
    }

    public void type(org.omg.CORBA.TypeCode type) {
        anyStrategy.type(type);
    }

    public void read_value(org.omg.CORBA.portable.InputStream is, org.omg.CORBA.TypeCode type)
        throws org.omg.CORBA.MARSHAL {
        anyStrategy.read_value(is, type);
    }

    public void write_value(org.omg.CORBA.portable.OutputStream out)
        throws org.omg.CORBA.BAD_OPERATION {
        anyStrategy.write_value(out);
    }

    // Short operations.....

    public short extract_short() throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_short();
    }

    public void insert_short(short s) {
        anyStrategy.insert_short(s);
    }

    // Long operations....

    public int extract_long() throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_long();
    }

    public void insert_long(int i) {
        anyStrategy.insert_long(i);
    }

    // LongLong Operations...

    public long extract_longlong()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_longlong();
    }

    public void insert_longlong(long l) {
        anyStrategy.insert_longlong(l);
    }

    // UShort Operations...

    public short extract_ushort()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_ushort();
    }

    public void insert_ushort(short s) {
        anyStrategy.insert_ushort(s);
    }

    // ULong Operations...

    public int extract_ulong()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_ulong();
    }

    public void insert_ulong(int i) {
        anyStrategy.insert_ulong(i);
    }

    // ULongLong Operations...

    public long extract_ulonglong()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_ulonglong();
    }

    public void insert_ulonglong(long l) {
        anyStrategy.insert_ulonglong(l);
    }

    // Float operations....

    public float extract_float()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_float();
    }

    public void insert_float(float f) {
        anyStrategy.insert_float(f);
    }

    // Double Operations...

    public double extract_double()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_double();
    }

    public void insert_double(double d) {
        anyStrategy.insert_double(d);
    }

    // Boolean Operations...

    public boolean extract_boolean()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_boolean();
    }

    public void insert_boolean(boolean b)  {
        anyStrategy.insert_boolean(b);
    }

    // Char Operations...

    public char extract_char()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_char();
    }

    public void insert_char(char c) {
        anyStrategy.insert_char(c);
    }

    // WChar Operations...

    public char extract_wchar()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_wchar();
    }

    public void insert_wchar(char w) {
        anyStrategy.insert_wchar(w);
    }

    // Octet Operations....

    public byte extract_octet()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_octet();
    }

    public void insert_octet(byte b) {
        anyStrategy.insert_octet(b);
    }

    // Any operations...

    public org.omg.CORBA.Any extract_any()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_any();
    }

    public void insert_any(org.omg.CORBA.Any a)  throws org.omg.CORBA.BAD_OPERATION {
        anyStrategy.insert_any(a);
    }

   // Object operations....

    public org.omg.CORBA.Object extract_Object()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_Object();
    }

    public void insert_Object(org.omg.CORBA.Object obj) {
        anyStrategy.insert_Object(obj);
    }

    public void insert_Object(org.omg.CORBA.Object obj, org.omg.CORBA.TypeCode t)
        throws org.omg.CORBA.BAD_PARAM {
        anyStrategy.insert_Object(obj, t);
    }

    // Value Operations....

    public java.io.Serializable extract_Value()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_Value();
    }

    public void insert_Value(java.io.Serializable v)  throws org.omg.CORBA.MARSHAL {
        anyStrategy.insert_Value(v);
    }

    public void insert_Value(java.io.Serializable v, org.omg.CORBA.TypeCode t)
        throws org.omg.CORBA.MARSHAL {
        anyStrategy.insert_Value(v, t);
    }

    // String Operations...

    public String extract_string()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_string();
    }

    public void insert_string(String s)  
        throws org.omg.CORBA.DATA_CONVERSION, org.omg.CORBA.MARSHAL {
        anyStrategy.insert_string(s);
    }

    // WString Operations...

    public String extract_wstring()  throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_wstring();
    }

    public void insert_wstring(String value)  throws org.omg.CORBA.MARSHAL {
        anyStrategy.insert_wstring(value);
    }

    // Typecode Operations...

    public org.omg.CORBA.TypeCode extract_TypeCode()
        throws org.omg.CORBA.BAD_OPERATION {
        return anyStrategy.extract_TypeCode();
    }

    public void insert_TypeCode(org.omg.CORBA.TypeCode value) {
        anyStrategy.insert_TypeCode(value);
    }

    //--------------------------------------------------------------------------
    //                          DEPRECATED METHODS
    /**
     * @throws org.omg.CORBA.NO_IMPLEMENT
     * @deprecated by CORBA 2.2, the Principal data type is no longer supported.
     * @see edu.uci.ece.zen.orb.any.pluggable.Any edu.uci.ece.zen.orb.any.pluggable.Any
     * @see edu.uci.ece.zen.orb.any.monolithic.Any edu.uci.ece.zen.orb.any.monolithic.Any
     */
    public org.omg.CORBA.Principal extract_Principal() throws org.omg.CORBA.BAD_OPERATION {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    /**
     * @throws org.omg.CORBA.NO_IMPLEMENT
     * @deprecated by CORBA 2.2, the Principal data type is no longer supported.
     * @see edu.uci.ece.zen.orb.any.pluggable.Any edu.uci.ece.zen.orb.any.pluggable.Any
     * @see edu.uci.ece.zen.orb.any.monolithic.Any edu.uci.ece.zen.orb.any.monolithic.Any
     */
    public void insert_Principal(org.omg.CORBA.Principal p) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    //
    //--------------------------------------------------------------------------



    // Insert Streamable....

    public void insert_Streamable(org.omg.CORBA.portable.Streamable str) {
        //throw new org.omg.CORBA.NO_IMPLEMENT("Any.java: insert_streamable has not been implemented");
        anyStrategy.insert_Streamable(str);
    }

    public org.omg.CORBA.portable.Streamable extract_Streamable() {
        throw new org.omg.CORBA.NO_IMPLEMENT("Any.java: insert_streamable has not been implemented");
    }

    
    // Create outputstream....
    public org.omg.CORBA.portable.OutputStream create_output_stream() {
        return anyStrategy.create_output_stream();
    }

    // Create InputStream...
    public org.omg.CORBA.portable.InputStream create_input_stream() {
        return anyStrategy.create_input_stream();
    }


    // ----------------------------------------------------------------------
    // MEMBER VARIABLES


    //private edu.uci.ece.zen.orb.ORB orb;
//private PluggableAny thisAny;
    //private org.omg.CORBA.ORB orb;

    /** In the strategy design pattern, this class represents the
     * "StrategyClient".  The strategy being referenced is stored in a
     * variable and calls made to the strategy are proxied through
     * this class.
     */
    private edu.uci.ece.zen.orb.any.AnyStrategy anyStrategy;

    //private static byte currentStrategy = 0;
    /** Class holding the current strategy.  By making this static, we
     * prevent garbage collection from removing the desired Any so
     * that there isn't a performance hit when the next one is
     * created. */
    private static Class currentAnyStrategyClass;
    static {
        String anyStrategyString = edu.uci.ece.zen.utils.ZenProperties.getGlobalProperty("zen.any.anyStrategy", "");
        setStrategy(anyStrategyString);
    }

}
