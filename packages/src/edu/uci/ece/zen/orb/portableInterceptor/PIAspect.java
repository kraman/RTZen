/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.portableInterceptor;

import edu.uci.ece.zen.orb.*;
import org.omg.GIOP.ReplyStatusType_1_0;

import edu.uci.ece.zen.utils.ZenProperties;
//import java.lang.String;

/**
  Functionality of portable interceptors.common to both client and server.

  @author Mark Panahi
*/
public aspect PIAspect{
    private static org.omg.CORBA.ORB orb = null;

    public static InterceptorList interceptorList = new InterceptorList();

    public static java.util.Hashtable currents = new java.util.Hashtable();

    //This pointcut defines where to install ORBInitializers
    //BEGIN ORBInitializers installation
    pointcut init(org.omg.CORBA.ORB this_orb, javax.realtime.ScopedMemory mem, String orbId, String[] argv,java.util.Properties props):
            //execution(public void *..ORB.internalInit(..)) && args(param_orb,argv,props) && target(this_orb);
            execution(private void edu.uci.ece.zen.orb.ORB.internalInit(..)) && args(mem, orbId, argv,props) && target(this_orb);
                //String[] argv,

    //Get Orb initialzers from properties file
    static java.util.Vector orbInitializers;
    static ORBInitInfo orbInitInfo = null;

    public static final short SUCCESSFUL = 0;
    public static final short USER_EXCEPTION = 1;
    public static final short SYSTEM_EXCEPTION = 2;
    public static final short LOCATION_FORWARD = 3;

    //pre init
    before(org.omg.CORBA.ORB this_orb, javax.realtime.ScopedMemory mem, String orbId, String[] argv,java.util.Properties props):
            init(this_orb, mem, orbId, argv,props) {

        //Logger.warn("JOINPOINT: " + thisJoinPoint.getSignature().toLongString() );

        orbInitializers = getORBInitializers(props);
        orb = this_orb;  //Grab a reference to the ORB

        //Don't need to create this if there are no initializers
        if(orbInitializers.size() > 0)
            orbInitInfo = new ORBInitInfo((edu.uci.ece.zen.orb.ORB)orb, interceptorList);

        //Pre init
        for(int i = 0; i < orbInitializers.size(); ++i)
            ((org.omg.PortableInterceptor.ORBInitializer)
            (orbInitializers.get(i))).pre_init(orbInitInfo);
    }

    //post init
    after(org.omg.CORBA.ORB this_orb, javax.realtime.ScopedMemory mem, String orbId, String[] argv,java.util.Properties props):
            init(this_orb, mem, orbId, argv,props) {

        //Orb initializer post init
        for(int i = 0; i < orbInitializers.size(); ++i)
            ((org.omg.PortableInterceptor.ORBInitializer)
            (orbInitializers.get(i))).post_init(orbInitInfo);
    }

    //END ORBInitializers installation
//
//
//    //This is where we resolve initial references to the PICurrent
//    pointcut piCurrentResolver(String obj_id): execution(public org.omg.CORBA.Object *..ORB.resolve_initial_references(..)) && args(obj_id);
//
//    org.omg.CORBA.Object around(String obj_id): piCurrentResolver(obj_id) {
//
//        if(obj_id.equals("PICurrent"))
//            return (org.omg.CORBA.Object)getCurrent();
//        else
//            return proceed(obj_id);
//    }
//
//    public static PICurrent getCurrent()
//    {
//        PICurrent current = (PICurrent)(currents.get(Thread.currentThread()));
//        if(current == null)
//        {
//            ////Logger.warn("Current NOT found.  Creating one at: " + Thread.currentThread());
//            current = new PICurrent(orb, 2);
//            currents.put(Thread.currentThread(),current);
//        }
//        else
//        {
//            ////Logger.warn("Current found at: " + Thread.currentThread());
//        }
//        return current;
//    }

    /**
    * Gets the list of all Orb initializers based on properties set
    * by the user.
    */

    private static final java.util.Vector getORBInitializers(java.util.Properties paramProps)
    {
        java.util.Vector list = new java.util.Vector();
        java.util.Enumeration props = null;//edu.uci.ece.zen.sys.ZenProperties.propertyNames();

        String nextProp = "";
        final String find = "org.omg.PortableInterceptor.ORBInitializerClass.";

        //ZenProperties.getGlobalProperty(find, "1")

        //Logger.warn("Looking for props");
        try
        {
//            while(props.hasMoreElements())
//            {
//                nextProp = (String)(props.nextElement());
//
//                if(nextProp.startsWith(find))
//                {   //Logger.warn(nextProp);
//                    //Object temp = edu.uci.ece.zen.orb.ClassLoader.loadClass("","",nextProp.substring(find.length()));
//                    Object temp = Class.forName(nextProp.substring(find.length())).newInstance();
//                    if(temp != null)
//                        list.add(temp);
//                    else
//                        Logger.warn("Could not find: " + nextProp.toString());
//                }
//            }

            props = System.getProperties().propertyNames();
            while(props.hasMoreElements())
            {
                nextProp = (String)(props.nextElement());
                //Logger.warn(nextProp);
                if(nextProp.startsWith(find))
                {   //Logger.warn(nextProp);
                    //Object temp = edu.uci.ece.zen.orb.ClassLoader.loadClass("","",nextProp.substring(find.length()));
                    Object temp = Class.forName(nextProp.substring(find.length())).newInstance();
                    if(temp != null)
                        list.add(temp);
                    else
                        ZenProperties.logger.log("Could not find: " + nextProp.toString());
                }
            }

//            if(paramProps != null)
//                props = paramProps.propertyNames();
//                while(props.hasMoreElements())
//                {
//                    nextProp = (String)(props.nextElement());
//                    //Logger.warn(nextProp);
//                    if(nextProp.startsWith(find))
//                    {   //Logger.warn(nextProp);
//                        //Object temp = edu.uci.ece.zen.orb.ClassLoader.loadClass("","",nextProp.substring(find.length()));
//                        Object temp = Class.forName(nextProp.substring(find.length())).newInstance();
//                        if(temp != null)
//                            list.add(temp);
//                        else
//                            ZenProperties.logger.log("Could not find: " + nextProp.toString());
//                    }
//                }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return list;
    }
}
















