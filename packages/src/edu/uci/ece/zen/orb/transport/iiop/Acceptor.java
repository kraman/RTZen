/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.transport.iiop;

import org.omg.IIOP.ProfileBody_1_0;
import org.omg.IIOP.ProfileBody_1_0Helper;
import org.omg.IIOP.ProfileBody_1_1;
import org.omg.IIOP.ProfileBody_1_1Helper;
import org.omg.IIOP.Version;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TaggedProfile;
import org.omg.Messaging.PolicyValue;
import org.omg.Messaging.PolicyValueHelper;

import edu.uci.ece.zen.orb.CDROutputStream;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ReadBuffer;
import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;
import edu.uci.ece.zen.poa.POA;
import edu.uci.ece.zen.orb.ORB;
import java.net.InetAddress;

import org.omg.RTCORBA.PRIORITY_MODEL_POLICY_TYPE;

public class Acceptor extends edu.uci.ece.zen.orb.transport.Acceptor {
    private java.net.ServerSocket ssock;
    private int threadPoolId;
    private boolean isShuttingDown = false;

    public Acceptor(edu.uci.ece.zen.orb.ORB orb,
            edu.uci.ece.zen.orb.ORBImpl orbImpl,
            int threadPoolId) {
        super(orb, orbImpl, threadPoolId);
        try {
            ssock = new java.net.ServerSocket(0, 0, null);
        } catch (Exception ex) {
            ZenProperties.logger.log(Logger.WARN,
                    getClass(), "<cinit>",
                    "Error binding to post.", ex);
        }
    }

    protected void accept() {
        try {
            Transport t = new Transport(orb, orbImpl, ssock.accept());
            registerTransport(t);
        } catch (java.net.SocketException se){
            if( !isShuttingDown )
                ZenProperties.logger.log(Logger.WARN, getClass(), "accept", se);
        } catch (java.io.IOException ioex) {
            ZenProperties.logger.log(Logger.WARN, getClass(), "accept", ioex); }
    }

    protected void internalShutdown() {
        isShuttingDown = true;
        try{
            ssock.close();
        }catch( java.io.IOException ioex ){
            //ignore
        }
    }

    protected TaggedProfile [] getInternalProfiles(byte iiopMajorVersion,
            byte iiopMinorVersion, byte[] objKey, POA poa) {
        Version version = new Version(iiopMajorVersion, iiopMinorVersion);
        
        //try this to test the connection error
        //String [] endpoints = new String [] {"128.195.174.86",ssock.getInetAddress().getHostAddress(),"127.0.0.1"};
        //String [] endpoints = new String [] {ssock.getInetAddress().getHostAddress(),"127.0.0.1"};
        //String [] endpoints = new String [] {ORB.sockAddr,"127.0.0.1"};
        String [] endpoints = ORB.endpoints;
        
        TaggedProfile [] tparr = new TaggedProfile[endpoints.length];
        
        for(int i = 0; i < endpoints.length; ++i){        
            CDROutputStream out = CDROutputStream.instance();
            out.init(orb);
            out.write_boolean(false); //BIGENDIAN
            edu.uci.ece.zen.utils.Logger.printThreadStack();
            if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("Acceptor version " + version);
            if (ZenBuildProperties.dbgIOR) {
                ZenProperties.logger.log("Acceptor, the current memory is :" + javax.realtime.RealtimeThread .getCurrentMemoryArea());
                ZenProperties.logger.log("Acceptor, the memory of ssock is " + javax.realtime.MemoryArea .getMemoryArea(ssock));
                ZenProperties.logger.log("Acceptor getHostAddress" + ssock.getInetAddress().getHostAddress());
                ZenProperties.logger.log("Acceptor getLocalPort()" + (short) ssock.getLocalPort());
                ZenProperties.logger.log("Acceptor objKey" + objKey);
            }
    /*
            InetAddress [] inetAddr = null;
            
            try{
                //inetAddr = InetAddress.getAllByName( ssock.getInetAddress().getHostName() );
                inetAddr = InetAddress.getAllByName( "localhost" );
                for(int i = 0; i < inetAddr.length; ++i)
                    System.out.println(inetAddr.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
            */

            switch (iiopMinorVersion) {
                case 0:
                    ProfileBody_1_0 pb10 = new ProfileBody_1_0(version, endpoints[i] /*ssock.getInetAddress().getHostAddress()*/, (short) ssock.getLocalPort(), objKey);
                    ProfileBody_1_0Helper.write(out, pb10);
                    break;
                case 1:
                case 2:
                    //org.omg.IOP.TaggedComponent[] components = new
                    // org.omg.IOP.TaggedComponent[0];
                    //TODO: insert rt policy info and other tagged components
                    ProfileBody_1_1 pb11 = new ProfileBody_1_1(version, endpoints[i] /*ssock.getInetAddress().getHostAddress()*/, (short) ssock.getLocalPort(), objKey, getComponents(poa));
                    ProfileBody_1_1Helper.write(out, pb11);
                    break;
            }
    
            WriteBuffer outb = out.getBuffer();
            ReadBuffer outrb = outb.getReadBuffer();
    
            TaggedProfile tp = new TaggedProfile();
            tp.tag = TAG_INTERNET_IOP.value;
            tp.profile_data = new byte[(int) outrb.getLimit()];
            outrb.readByteArray(tp.profile_data, 0, (int) outrb.getLimit());
    
            out.free();
            outrb.free();
            tparr[i] = tp;
        }
        

        return tparr;
    }

}
