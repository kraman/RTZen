package edu.uci.ece.zen.utils;

import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;
import javax.realtime.*;

public final class ZenProperties{
    /*
     * This property is set during compile time using an aspect.
     * private static String installDir = null;
     */
    public static final String installDir="";
    public static final boolean dbg=true;
    public static final boolean devDbg=false;
    public static final ImmortalMemory immortalMem = ImmortalMemory.instance();
    public static final HeapMemory heapMem = HeapMemory.instance();
    public static final Logger logger = Logger.instance();
    public static final String zenVersion = "Zen RT Corba ORB Version 1.1, UNSTABLE";
    public static final String zenStartupMessage = 
         zenVersion + "\n" +
        "Developed at:\n" + 
        "\tDistributed Object Computing Laboratory,\n" +
        "\tElectrical Engineering and Computer Science\n" +
        "\tUniversity of California, Irvine\n";

    //Global properties [install dir,user dir,working dir]
    //allocated in immortal memory because it doesnt change after loading.
    protected static Properties globalProperties;// = new Properties();

    //Orb specific properties
    //Allocated in orb specific memory
    private Properties orbProperties;

    private static boolean isInit = false;

    private static synchronized void init(){
        if( isInit )
            return;
        isInit = true;
        //load global properties

        Properties tmpProperties = new Properties();
        try{
            if( ZenProperties.dbg )
                System.out.println( "Loading zen.properties from " + installDir + File.separator + "zen.properties" );

            FileInputStream in = new FileInputStream( new File( installDir + File.separator + "zen.properties" ) );
            tmpProperties.load( in );
        }catch( Exception exception ){
            if( ZenProperties.dbg )
                System.out.println( "Unable to load default properties from the installation directory. (" + installDir + ")");
        }

        try{
            if( ZenProperties.dbg )
                System.out.println("Loading zen.properties from " + System.getProperty("user.home") + File.separator + "zen.properties" );

            FileInputStream in = new FileInputStream( new File( System.getProperty("user.home") + File.separator + "zen.properties" ) );
            tmpProperties.load( in );
        }catch( Exception exception ){
            if( ZenProperties.dbg )
                System.out.println("Unable to load default properties from the user home directory. (" + System.getProperty("user.home") + ")");
        }

        try{
            if( ZenProperties.dbg )
                System.out.println("Loading zen.properties from " + System.getProperty("user.dir") + File.separator + "zen.properties" );

            FileInputStream in = new FileInputStream( new File( System.getProperty("user.dir") + File.separator + "zen.properties" ) );
            tmpProperties.load( in );
        }catch( Exception exception ){
            if( ZenProperties.dbg )
                System.out.println("Unable to load default properties from the working directory. (" + System.getProperty("user.dir") + ")");
        }

        try{
            globalProperties = (Properties) immortalMem.newInstance( Properties.class );
            //System.out.println( "#" + globalProperties );
            java.util.Enumeration propsEnum = tmpProperties.propertyNames();
            Class[] refArgs = new Class[]{ String.class };
            java.lang.reflect.Constructor refConstr = String.class.getConstructor( refArgs );
            while( propsEnum.hasMoreElements() ){
                String key = (String) propsEnum.nextElement();
                String val = (String) tmpProperties.get( key );
                
                Object[] refVal = new Object[]{ key };
                String immKey = (String) ImmortalMemory.instance().newInstance( refConstr , refVal );
                refVal = new Object[]{ val };
                String immVal = (String) ImmortalMemory.instance().newInstance( refConstr , refVal );
                ZenProperties.globalProperties.setProperty( immKey , immVal );
            }
        }catch( Exception exception ){
            exception.printStackTrace();
        }
        if( ZenProperties.dbg )
            System.out.println("Global properties have been loaded");
        System.out.flush();
        //All properties loaded
    }

    //initialize everything in order
    //1) global properties
    //2) memory regions
    static{
        init();
    }

    /**
     * This method isused to access the STATIC properties set by the user. The properties
     * obtained in the following order. Last one takes priority.
     * 1) zen.properties in the installation directory (Loaded when Class is loaded)
     * 2) zen.properties in the user home directory (Loaded when Class is loaded)
     * 3) zen.properties in the current directory (Loaded when Class is loaded)
     * 
     * @param property The property that is required
     * @return The value of the property if it is set in props or System.properties, null otherwise
     */
    public static String getGlobalProperty(String property, String defaultValue ) {
        init();
        String propertyValue = defaultValue;
        propertyValue = globalProperties.getProperty( property , propertyValue );
        propertyValue = System.getProperty( property , propertyValue );
        return propertyValue;
    }
        
    public ZenProperties(){
        orbProperties = new java.util.Properties();
    }

    /**
     * This method isused to access properties set by the user. The properties
     * obtained in the following order. Last one takes priority.
     * 1) zen.properties in the installation directory (Loaded when Class is loaded)
     * 2) zen.properties in the user home directory (Loaded when Class is loaded)
     * 3) zen.properties in the current directory (Loaded when Class is loaded)
     * 4) System properties (Loaded from command line by Java)
     * 5) properties specified at ORB.init
     * 6) properties set using set_parameter
     * 
     * @param property The property that is required
     * @return The value of the property if it is set in props or System.properties, null otherwise
     */
    public String getProperty(String property, String defaultValue ) {
        String propertyValue = defaultValue;
        propertyValue = ZenProperties.globalProperties.getProperty( property , propertyValue );
        propertyValue = System.getProperty( property , propertyValue );
        propertyValue = orbProperties.getProperty( property , propertyValue );
        return propertyValue;
    }

    public void addProperties( Properties props ){
        System.out.println( "entering addProperties" );
        if( props != null ){
            Enumeration keys = props.keys();
            while( keys.hasMoreElements() ){
                String element = ((String)keys.nextElement()) + "";
                orbProperties.put( element , props.getProperty( element ) + "" );
            }
        }
        System.out.println( "exiting addProperties" );
    }

    public void addPropertiesFromArgs( String args[] ){
        System.out.println( "entering addPropertiesFromArgs" );
        Properties props = orbProperties;
        if( args != null ){
            for( int i=0;i<args.length;i++ ){
                String arg = args[i].trim();
                String value = "";

                if( arg.startsWith( "-ORBid" ) ){
                    if( arg.length() > 6 ){
                        value = arg.substring(6).trim();
                    }else{
                        value = args[++i].trim();
                    }
                    props.setProperty( "org.omg.CORBA.ORBid" , value + "" );
                }else if( arg.startsWith( "-ORBServerId" ) ){
                    if( arg.length() > 12 ){
                        value = arg.substring(12).trim();
                    }else{
                        value = args[++i].trim();
                    }
                    props.setProperty( "org.omg.CORBA.ORBServerId" , value+"" );
                }else if( arg.startsWith( "-ORBListenEndpoints" ) ){
                /*
                    if( arg.length() > 19 ){
                        value = arg.substring(19).trim();
                    }else{
                        value = args[++i].trim();
                    }
                    Vector endpoints = props.getProperty( "edu.uci.ece.zen.AdditionalEndpoints" );
                    if( endpoints != null )
                        endpoints.add( value );
                    else{
                        endpoints = new Vector();
                        endpoints.add(value);
                        props.setProperty( "edu.uci.ece.zen.AdditionalEndpoints" , endpoints );
                    }
                */
                }else if( arg.startsWith( "-ORBNoProprietaryActivation" ) ){
                    props.setProperty( "org.omg.CORBA.ORBNoProprietaryActivation" , "true" );
                }else if( arg.startsWith( "-ORBInitRef" ) ){
                    if( arg.length() > 11 ){
                        value = arg.substring(11).trim();
                    }else{
                        value = args[++i].trim();
                    }

                    java.util.StringTokenizer strtok = new java.util.StringTokenizer( value , "=" );
                    String service = strtok.nextToken();
                    String ref = strtok.nextToken();
                    props.setProperty( "edu.uci.ece.zen.initRef." + service , ref+"" );
                }else if( arg.startsWith( "-ORBDefaultInitRef" ) ){
                    if( arg.length() > 18 ){
                        value = arg.substring(18).trim();
                    }else{
                        value = args[++i].trim();
                    }
                    props.setProperty( "org.omg.CORBA.ORBDefaultInitRef" , value+"" );
                }else if( arg.startsWith( "-ORB" ) ){
                    throw new org.omg.CORBA.BAD_PARAM( "Bad parameter " + arg );
                }
            }
        }
        System.out.println( "exiting addPropertiesFromArgs" );
    }

    public static String getORBId( String args[] , java.util.Properties properties ){
        init();
        if( properties == null )
            properties = new Properties();
        if( args != null ){
            for( int i=0;i<args.length;i++ ){
                String arg = args[i].trim();
                String value = "";

                if( arg.startsWith( "-ORBid" ) ){
                    if( arg.length() > 6 ){
                        value = arg.substring(6).trim();
                    }else{
                        value = args[++i].trim();
                    }
                    properties.setProperty( "org.omg.CORBA.ORBid" , value );
                }else if( arg.startsWith( "-ORBServerId" ) ){
                    if( arg.length() > 12 ){
                    }else{
                        ++i;
                    }
                }else if( arg.startsWith( "-ORBListenEndpoints" ) ){
                /*
                    if( arg.length() > 19 ){
                        value = arg.substring(19).trim();
                    }else{
                        value = args[++i].trim();
                    }
                    Vector endpoints = props.getProperty( "edu.uci.ece.zen.AdditionalEndpoints" );
                    if( endpoints != null )
                        endpoints.add( value );
                    else{
                        endpoints = new Vector();
                        endpoints.add(value);
                        props.setProperty( "edu.uci.ece.zen.AdditionalEndpoints" , endpoints );
                    }
                */
                }else if( arg.startsWith( "-ORBNoProprietaryActivation" ) ){
                }else if( arg.startsWith( "-ORBInitRef" ) ){
                    if( arg.length() > 11 ){
                    }else{
                        ++i;
                    }
                }else if( arg.startsWith( "-ORBDefaultInitRef" ) ){
                    if( arg.length() > 18 ){
                    }else{
                        ++i;
                    }
                }else if( arg.startsWith( "-ORB" ) ){
                    throw new org.omg.CORBA.BAD_PARAM( "Bad parameter " + arg );
                }
            }
        }
        return properties.getProperty( "org.omg.CORBA.ORBid" );
    }
}
