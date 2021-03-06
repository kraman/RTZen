/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb;

import javax.realtime.ImmortalMemory;
import javax.realtime.RealtimeThread;
import javax.realtime.ScopedMemory;

import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TAG_MULTIPLE_COMPONENTS;
import org.omg.IOP.TAG_SERIAL;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TaggedProfile;
import org.omg.Messaging.PolicyValue;
import org.omg.Messaging.PolicyValueSeqHelper;
import org.omg.RTCORBA.CLIENT_PROTOCOL_POLICY_TYPE;
import org.omg.RTCORBA.PRIORITY_BANDED_CONNECTION_POLICY_TYPE;
import org.omg.RTCORBA.PRIORITY_MODEL_POLICY_TYPE;
import org.omg.RTCORBA.PRIVATE_CONNECTION_POLICY_TYPE;
import org.omg.RTCORBA.PriorityModel;
import org.omg.RTCORBA.SERVER_PROTOCOL_POLICY_TYPE;
import org.omg.RTCORBA.THREADPOOL_POLICY_TYPE;
import org.omg.CORBA.SystemExceptionHelper;

import edu.uci.ece.zen.orb.policies.PriorityModelPolicyImpl;
import edu.uci.ece.zen.utils.Queue;
import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.ZenBuildProperties;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.FString;

public final class ObjRefDelegate extends org.omg.CORBA_2_3.portable.Delegate {

    public int giopType;

    private static Queue objRefDelegateCache;
    static {
        try {
            objRefDelegateCache = (Queue) ImmortalMemory.instance()
                    .newInstance(Queue.class);
        } catch (Exception e) {
            ZenProperties.logger.log(Logger.FATAL, ObjRefDelegate.class, "static <init>", e);
            System.exit(-1);
        }
    }

    public static ObjRefDelegate instance() {
        ZenProperties.logger.log("ObjRefDelegate.instance()");
        ObjRefDelegate ret = (ObjRefDelegate)objRefDelegateCache.dequeue();

        if ( ret == null ) {
            try {
                ZenProperties.logger.log("Allocating brand new ObjRefDelegate");
                //Thread.dumpStack();
                ObjRefDelegate ord = (ObjRefDelegate) ImmortalMemory.instance().newInstance(ObjRefDelegate.class);
                ord.id = idGen++;
                return ord;
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.FATAL, ObjRefDelegate.class, "instance", e);
                System.exit(-1);
            }
        }
        return ret;
    }

    private static void release(ObjRefDelegate self) {
        if( ZenBuildProperties.dbgIOR ) ZenProperties.logger.log("Trying to free ObjRefDelegate " + self.id);
        if(!self.released){
            objRefDelegateCache.enqueue(self);
            self.ior.free();
            self.objImpl = null;
            //FString.free(self.priorityLanes[0].objectKey);
            for(int i = 0; i < self.priorityLanes.length; ++i)
                if(self.priorityLanes[i].objectKey != null){
                    FString.free(self.priorityLanes[i].objectKey);
                    self.priorityLanes[i].releaseTransport();
                }
        }
        self.released = true;
    }

    public ObjRefDelegate() {
        priorityLanes = new LaneInfo[10];
        for (int i = 0; i < priorityLanes.length; i++)
            priorityLanes[i] = new LaneInfo();
        numLanes = 0;
    }

    private boolean released = false;

    private WriteBuffer ior;

    private static int idGen = 0;
    private int id;

    private ORB orb;

    private LaneInfo priorityLanes[];

    private int numLanes;

    public int id(){
        return id;
    }

    protected void init(org.omg.IOP.IOR ior, ObjectImpl obj, ORB orb, ORBImpl orbImpl) {
        init( ior, obj, orb, orbImpl, false );
    }

    ObjectImpl objImpl;
    protected void init(org.omg.IOP.IOR ior, ObjectImpl obj, ORB orb, ORBImpl orbImpl , boolean isCollocated ) {
        released = false;
        referenceCount = 1;
        this.orb = orb;

        this.ior = WriteBuffer.instance();
        this.ior.init();

        ////////////////////////////////
        //TODO: Krishna (a.k.a. Boss), this line below has to be enabled in JVM so the
        //ObjectImpl is not collected prematurely
        //objImpl = obj;
        ////////////////////////////////

        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);
        org.omg.IOP.IORHelper.write(out, ior);
        out.getBuffer().dumpBuffer(this.ior);
        out.free();
        ZenProperties.logger.log("ObjRefDel init 1");
        if( ZenBuildProperties.dbgIOR )
            ZenProperties.logger.log("++++++++++++++++++++++++++++++++++ObjRefDel no of prof:" + ior.profiles.length);
        //process all the tagged profiles
        for (int i = 0; i < ior.profiles.length; i++) { //go through each
            // tagged profile
            processTaggedProfile(ior.profiles[i], obj, orbImpl, isCollocated);
        }
        ZenProperties.logger.log("ObjRefDel init 2");
        numLanes = 0;
    }

    public synchronized void addLaneData(int min, int max, ScopedMemory transport, FString objectKey, Class protocolFactory , ORB orb ) {
        if (ZenBuildProperties.dbgTP) ZenProperties.logger.log(RealtimeThread
                .currentThread()
                + " "
                + RealtimeThread.getCurrentMemoryArea()
                + " "
                + "New lane info: "
                + min
                + " <--> "
                + max
                + "  :  "
                + transport);
        priorityLanes[numLanes++].init(min, max, transport, objectKey, protocolFactory , orb );
    }

    public LaneInfo getLane() {
        int priority = orb.getRTCurrent().the_priority();//RealtimeThread.currentThread().getPriority();
        for (int i = 0; i < priorityLanes.length; i++) {
            LaneInfo ln = (LaneInfo) priorityLanes[i];
            if (ZenBuildProperties.dbgTP) ZenProperties.logger.log("Checking if "
                    + priority + " is within " + ln.minPri + " <--> "
                    + ln.maxPri + " objkey: " + ln.getObjectKey().decode());
            if (ln.minPri <= priority && ln.maxPri >= priority) return ln;
        }
        return null;
    }

    private void processTaggedProfile(TaggedProfile profile, ObjectImpl obj, ORBImpl orbImpl, boolean isCollocated) {
        int tag = profile.tag;
        if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("processTaggedProfile " + tag);
        ZenProperties.logger.log("ObjRefDel processTaggedProfile 1");
        switch (tag) {
            case TAG_INTERNET_IOP.value: //establish appropriate connections
                ZenProperties.logger.log( "IIOP transport profile found" );
            // and
            // register them
            {
                boolean startSerialTransportAcceptor = ZenProperties.getGlobalProperty("serial.client.only" , "" ).equals("1");

                if( startSerialTransportAcceptor && !isCollocated ){
                //if( startSerialTransportAcceptor){

                    ZenProperties.logger.log("+++++++++++++++++++++++++++++++++++++++++++++Skipping IIOP");
                    return;
                }
                ZenProperties.logger.log("ObjRefDel processTaggedProfile IIOP 1");
                byte[] data = profile.profile_data;

                CDRInputStream in = CDRInputStream.fromOctetSeq(data, orb);
                /*
                 * ReadBuffer rb = ReadBuffer.instance(); rb.init();
                 * rb.writeByteArray( data , 0 , data.length ); CDRInputStream
                 * in = CDRInputStream.instance(); in.init( orb , rb );
                 * in.setEndian( in.read_boolean() );
                 */
                byte iiopMinor = data[2];
                if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("iiop minor " + iiopMinor);
                switch (iiopMinor) {
                    case 0: {
                        ZenProperties.logger.log("ObjRefDel processTaggedProfile IIOPv1.0 1");
                        /*
                        TaggedProfileRunnable profRun = TaggedProfileRunnable.instance();
                        profRun.init(in);
                        ScopedMemory profScope = ORB.getScopedRegion();
                        profScope.enter(profRun);

                        ORB.freeScopedRegion(profScope);
*/
                        /////////////////////////

                        //edu.uci.ece.zen.utils.Logger.printMemStats(400);
                        //edu.uci.ece.zen.utils.Logger.printMemStatsImm(500);
                        in.read_octet(); //iiop major
                        in.read_octet(); //iiop minor

                        FString host = in.getBuffer().readFString(true);
                        short port = in.read_ushort();

                        FString object_key = in.getBuffer().readFString(false);
                        //edu.uci.ece.zen.utils.Logger.printMemStats(402);
                        //edu.uci.ece.zen.utils.Logger.printMemStatsImm(502);

                        ///////////////////
                        //org.omg.IIOP.ProfileBody_1_0 profilebody = org.omg.IIOP.ProfileBody_1_0Helper.read(in);

                        long connectionKey = ConnectionRegistry.ip2long(host, port);
                        ScopedMemory transportScope = orb.getConnectionRegistry().getConnection(connectionKey);

                        if (transportScope == null) {
                            transportScope = edu.uci.ece.zen.orb.transport.iiop.Connector.instance().connect(host, port, orb, orbImpl);
                            orb.getConnectionRegistry().putConnection(connectionKey, transportScope);
                            addLaneData( org.omg.RTCORBA.minPriority.value ,
                                    org.omg.RTCORBA.maxPriority.value,
                                    transportScope, object_key,
                                    edu.uci.ece.zen.orb.protocol.giop.GIOPMessageFactory.class ,
                                    orb );
                        } else {
                            FString.free(host);
                        }
                        ZenProperties.logger.log("ObjRefDel processTaggedProfile IIOPv1.0 2");
                    }
                        break;

                    case 1:
                    case 2: {

                        in.read_octet(); //iiop major
                        in.read_octet(); //iiop minor

                        FString host = in.getBuffer().readFString(true);
                        short port = in.read_ushort();

                        FString object_key = in.getBuffer().readFString(false);
                        //edu.uci.ece.zen.utils.Logger.printMemStats(402);
                        //edu.uci.ece.zen.utils.Logger.printMemStatsImm(502);

                        ///////////////////
                        //org.omg.IIOP.ProfileBody_1_0 profilebody = org.omg.IIOP.ProfileBody_1_0Helper.read(in);

                        long connectionKey = ConnectionRegistry.ip2long(host, port);
                        ScopedMemory transportScope = orb.getConnectionRegistry().getConnection(connectionKey);

                        if (transportScope == null) {
                            transportScope = edu.uci.ece.zen.orb.transport.iiop.Connector
                                    .instance().connect(host, port, orb, orbImpl);
                            orb.getConnectionRegistry().putConnection(connectionKey, transportScope);
                        } else {
                            FString.free(host);
                        }

                        int numComp = in.read_ulong();

                        if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log(in.toString());
                        if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("number of components: " + numComp);

                        short minSupportedPriority = org.omg.RTCORBA.minPriority.value;
                        short maxSupportedPriority = org.omg.RTCORBA.maxPriority.value;

                        for (int i = 0; i < numComp; ++i) {
                            //TaggedComponent tc = profilebody.components[i];

                            int ctag = in.read_ulong();


                            if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("found tag: " + ctag);

                            if (ctag == org.omg.IOP.TAG_ORB_TYPE.value) {

                                int byteLen = in.read_ulong();
                                in.read_boolean(); //endianess

                                int orbType = in.read_ulong();

                                if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("ORB type: " + orbType);


                            } else if (ctag == org.omg.IOP.TAG_CODE_SETS.value) {
                                //just eat for now
                                int byteLen = in.read_ulong();
                                for(int i1 = 0; i1 < byteLen; ++i1)
                                    in.read_octet();
                                /*
                                int codeId = in.read_ulong();
                                if (ZenBuildProperties.dbg) ZenProperties.logger.log("Code is: " + codeId);

                                int arrSize = in.read_ulong();
                                if (ZenBuildProperties.dbg) ZenProperties.logger.log("arrSize: " + arrSize);

                                for(int i1 = 0; i1 < arrSize; ++i1){
                                    int tempCS = in.read_ulong();

                                    if (ZenBuildProperties.dbg) ZenProperties.logger.log("arrelem: " + tempCS);

                                }

                                codeId = in.read_ulong();
                                if (ZenBuildProperties.dbg) ZenProperties.logger.log("Code is: " + codeId);

                                arrSize = in.read_ulong();
                                if (ZenBuildProperties.dbg) ZenProperties.logger.log("arrSize: " + arrSize);

                                for(int i1 = 0; i1 < arrSize; ++i1){
                                    int tempCS = in.read_ulong();

                                    if (ZenBuildProperties.dbg) ZenProperties.logger.log("arrelem: " + tempCS);

                                }
                                */

                            } else if (ctag == org.omg.IOP.TAG_POLICIES.value) {

                                int byteLen = in.read_ulong();
                                in.read_boolean(); //endianess
                                int numPol = in.read_ulong();

                                //CDRInputStream in1 = CDRInputStream
                                 //       .fromOctetSeq(tc.component_data, orb);

                                //PolicyValue[] pvarr = PolicyValueSeqHelper
                                //        .read(in1);
                                //in1.free();

                                if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("number of policies: " + numPol);

                                for (int j = 0; j < numPol; ++j) {

                                    int polType = in.read_ulong();

                                    int byteLen1 = in.read_ulong();
                                    in.read_boolean(); //endianess

                                    if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("found policy value: " + polType);

                                    //CDRInputStream in2 = CDRInputStream
                                    //        .fromOctetSeq(pvarr[j].pvalue, orb);
                                    //PriorityModelPolicyHelper.extract(in2.read_any());

                                    switch (polType) {

                                        case PRIORITY_MODEL_POLICY_TYPE.value:

                                            ZenProperties.logger.log("\tPRIORITY_MODEL_POLICY_TYPE");
                                            priorityModel = in.read_long();
                                            serverPriority = in.read_short();
                                            if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("\tpriority model: " + priorityModel);
                                            if (ZenBuildProperties.dbgIOR) ZenProperties.logger.log("\tpriority: " + serverPriority);
                                            minSupportedPriority = serverPriority;
                                            maxSupportedPriority = serverPriority;
                                            break;

                                        case THREADPOOL_POLICY_TYPE.value:
                                            for(int i1 = 0; i1 < byteLen-1; ++i1)
                                                in.read_octet();
                                            ZenProperties.logger.log("\tTHREADPOOL_POLICY_TYPE");
                                            break;

                                        case SERVER_PROTOCOL_POLICY_TYPE.value:
                                            for(int i1 = 0; i1 < byteLen-1; ++i1)
                                                in.read_octet();
                                            ZenProperties.logger.log("\tSERVER_PROTOCOL_POLICY_TYPE");
                                            break;

                                        case CLIENT_PROTOCOL_POLICY_TYPE.value:
                                            for(int i1 = 0; i1 < byteLen-1; ++i1)
                                                in.read_octet();
                                            ZenProperties.logger.log("\tCLIENT_PROTOCOL_POLICY_TYPE");
                                            break;

                                        case PRIVATE_CONNECTION_POLICY_TYPE.value:
                                            for(int i1 = 0; i1 < byteLen-1; ++i1)
                                                in.read_octet();
                                            ZenProperties.logger.log("\tPRIVATE_CONNECTION_POLICY_TYPE");
                                            break;

                                        case PRIORITY_BANDED_CONNECTION_POLICY_TYPE.value:
                                            for(int i1 = 0; i1 < byteLen-1; ++i1)
                                                in.read_octet();
                                            ZenProperties.logger.log("\tPRIORITY_BANDED_CONNECTION_POLICY_TYPE");
                                            break;

                                        default:
                                            for(int i1 = 0; i1 < byteLen-1; ++i1)
                                                in.read_octet();
                                            ZenProperties.logger.log("ERROR: Invalid policy type");

                                    } //end switch

                                    //in2.free();

                                } //end for

                            }else{
                                //just eat if we don't know the type
                                int byteLen = in.read_ulong();
                                for(int i1 = 0; i1 < byteLen; ++i1)
                                    in.read_octet();
                            }

                        }

                        //TODO: process priority policies here and add the
                        // appropriate
                        // lanes
                        addLaneData( minSupportedPriority,
                                maxSupportedPriority,
                                transportScope, object_key,
                                edu.uci.ece.zen.orb.protocol.giop.GIOPMessageFactory.class ,
                                orb);
                    }
                        break;
                }
                in.free();
            }
                break;
            case TAG_MULTIPLE_COMPONENTS.value: //process the tagged components
                //TODO: Currently ignored because they are of no immediate use
                ZenProperties.logger.log("TAG_MULTIPLE_COMPONENTS ignored");
                break;
            case TAG_SERIAL.value: //process serial
/*
                System.out.println( "Serial transport profile found" );

                if( isCollocated ){
                    ZenProperties.logger.log("+++++++++++++++++++++++++++++++++++++++++++++Collocated object, Skipping Serial");
                    return;
                }

                ZenProperties.logger.log("ObjRefDel processTaggedProfile SERIAL 1");

                byte[] data = profile.profile_data;
                if (ZenBuildProperties.dbg) ZenProperties.logger.log("ObjRefDel processTaggedProfile SERIAL prof data len:" + data.length);
                CDRInputStream in = CDRInputStream.fromOctetSeq(data, orb);

                //for(int i = 0 ; i < in.getBuffer().getLimit()/4; ++i)
                //    if (ZenBuildProperties.dbg) ZenProperties.logger.log("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ObjRefDel processTaggedProfile SERIAL obj key" + in.read_ulong());

                FString object_key = in.getBuffer().readFString(false);
                //if (ZenBuildProperties.dbg) ZenProperties.logger.log("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ObjRefDel processTaggedProfile SERIAL obj key" + object_key.length());
               // if (ZenBuildProperties.dbg) ZenProperties.logger.log("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ObjRefDel processTaggedProfile SERIAL obj key" + object_key.decode());
                long connectionKey = -TAG_SERIAL.value;
                ScopedMemory transportScope = null;
                synchronized(edu.uci.ece.zen.orb.transport.serial.SerialPort.class){
                    transportScope = orb.getConnectionRegistry().getConnection(connectionKey);
                }
                ZenProperties.logger.log("ObjRefDel processTaggedProfile SERIAL 2");
                if( transportScope == null ){
                    try{
                        transportScope = edu.uci.ece.zen.orb.transport.serial.Connector
                            .instance().connect(null, (short)0, orb, orbImpl);
                        orb.getConnectionRegistry().putConnection(connectionKey, transportScope);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                if (transportScope != null) {
                    System.out.println( "Serial connection succesful" );
            addLaneData( javax.realtime.PriorityScheduler.instance().getMinPriority(),
                        javax.realtime.PriorityScheduler.instance().getMaxPriority(),
                        transportScope, object_key,
                    edu.uci.ece.zen.orb.protocol.giop.GIOPMessageFactory.class );
                }else{
                    System.out.println( "Serial connection unsuccesful" );
                }
                ZenProperties.logger.log("ObjRefDel processTaggedProfile SERIAL 3");
                in.free();

                break;*/
            default:
                ZenProperties.logger.log(Logger.WARN, getClass(), "processTaggedProfile", "unhandled tag: " + tag);
        }
    }

    public org.omg.IOP.IOR getIOR() {
        CDRInputStream in = CDRInputStream.instance();
        in.init(orb, ior.getReadBuffer());
        org.omg.IOP.IOR ret = org.omg.IOP.IORHelper.read(in);
        in.free();
        return ret;
    }

    public String toString() {
        return IOR.makeIOR(ior);
    }

    /**
     * The logical_type_id is a string denoting a shared type identifier
     * (RepositoryId). The operation returns true if the object is really an
     * instance of that type, including if that type is an ancestor of the most
     * derived type of that object. Determining whether an object's type is
     * compatible with the logical_type_id may require contacting a remote ORB
     * or interface repository. Such an attempt may fail at either the local or
     * the remote end. If is_a cannot make a reliable determination of type
     * compatibility due to failure, it raises an exception in the calling
     * application code. This enables the application to distinguish among the
     * TRUE, FALSE, and indeterminate cases.
     */
    public boolean is_a(org.omg.CORBA.Object self, String repository_id) {
        //TODO: Send _is_a message
        return true;
    }

    private int referenceCount;

    private void incrementReferenceCount() {
        referenceCount++;
    }

    private void decrementReferenceCount() {
        referenceCount--;
        if (referenceCount <= 0) {
            referenceCount = 0;
            ObjRefDelegate.release(this);
        }
    }

    public org.omg.CORBA.Object duplicate(org.omg.CORBA.Object self) {
        org.omg.CORBA.portable.ObjectImpl impl = (org.omg.CORBA.portable.ObjectImpl) self;
        ObjRefDelegate delegate = (ObjRefDelegate) impl._get_delegate();
        delegate.incrementReferenceCount();
        return self;
    }

    public void release(org.omg.CORBA.Object self) {
        org.omg.CORBA.portable.ObjectImpl impl = (org.omg.CORBA.portable.ObjectImpl) self;
        ObjRefDelegate delegate = (ObjRefDelegate) impl._get_delegate();
        delegate.decrementReferenceCount();
    }

    public boolean non_existent(org.omg.CORBA.Object self) {
        //TODO:send _non_existent GIOP message
        return false;
    }

    public boolean is_local(org.omg.CORBA.Object self) {
        return false;
    }

    public boolean is_equivalent(org.omg.CORBA.Object self,
            org.omg.CORBA.Object rhs) {
        org.omg.CORBA.portable.ObjectImpl impl1 = (org.omg.CORBA.portable.ObjectImpl) self;
        ObjRefDelegate delegate1 = (ObjRefDelegate) impl1._get_delegate();

        org.omg.CORBA.portable.ObjectImpl impl2 = (org.omg.CORBA.portable.ObjectImpl) rhs;
        ObjRefDelegate delegate2 = (ObjRefDelegate) impl2._get_delegate();

        return delegate1 == delegate2;
    }

    public int hash(org.omg.CORBA.Object self, int max) {
        return self.hashCode() % max;
    }

    public synchronized org.omg.CORBA.Object set_policy_override(
            org.omg.CORBA.Object self, org.omg.CORBA.Policy[] policies,
            org.omg.CORBA.SetOverrideType set_add) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public short serverPriority = -1; //initial value, not a valid priority, so

    // used to see if this was set

    public int priorityModel;

    public org.omg.CORBA.Policy get_policy(org.omg.CORBA.Object self,
            int policy_type) {

        if (policy_type == PRIORITY_MODEL_POLICY_TYPE.value) { return new PriorityModelPolicyImpl(
                PriorityModel.from_int(priorityModel), serverPriority); }

        return null;
        //throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    ///////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////Request / Reply stuff///////////////////////////
    ///////////////////////////////////////////////////////////////////////////////

    /**
     * Client upcall:
     * <p>
     * <b>Client scope </b> --ex in --&gt; ORB parent scope --&gt; ORB scope
     * --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public org.omg.CORBA.portable.OutputStream request(
            org.omg.CORBA.Object self, String operation,
            boolean responseExpected) {
        ClientRequest cr = ClientRequest.instance();
        cr.init(operation, responseExpected, (byte) 1, (byte) 0, orb, this);
        return cr;
    }

    /**
     * Client upcall:
     * <p>
     * <b>Client scope </b> --ex in --&gt; ORB parent scope --&gt; ORB scope
     * --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public org.omg.CORBA.portable.OutputStream request(
            org.omg.CORBA.Object self, String operation,
            boolean responseExpected, byte majorVersion, byte minorVersion) {
        ClientRequest cr = ClientRequest.instance();
        cr.init(operation, responseExpected, majorVersion, minorVersion, orb, this);
        return cr;
    }

    /**
     * Client upcall:
     * <p>
     * <b>Client scope </b> --ex in --&gt; ORB parent scope --&gt; ORB scope
     * --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public org.omg.CORBA.portable.InputStream invoke(org.omg.CORBA.Object self, org.omg.CORBA.portable.OutputStream os)
            throws org.omg.CORBA.portable.ApplicationException, org.omg.CORBA.portable.RemarshalException {
        try{
            org.omg.CORBA.portable.InputStream ret = ((ClientRequest) os).invoke();
            int replyStatus = ((ClientRequest) os).getReplyStatus();

            switch( replyStatus ){
                case org.omg.GIOP.ReplyStatusType_1_0._NO_EXCEPTION:
                    return ret;
                case org.omg.GIOP.ReplyStatusType_1_0._USER_EXCEPTION:
                    String exceptionID = ((edu.uci.ece.zen.orb.CDRInputStream)ret).peekAtString();
                    throw new org.omg.CORBA.portable.ApplicationException(exceptionID, ret);
                case org.omg.GIOP.ReplyStatusType_1_0._SYSTEM_EXCEPTION:
                    org.omg.CORBA.SystemException sysEx = SystemExceptionHelper.read(ret);
                    throw sysEx;
                default:
                    ZenProperties.logger.log( Logger.WARN , "Unknown status returned" );
                    //TODO: throw some soft of exception here?
                    return ret;
            }

        }catch( NullPointerException npe ){
            npe.printStackTrace();
            return null;
        }finally{
            ((ClientRequest) os).free();
        }
    }

    public void releaseReply(org.omg.CORBA.Object self, org.omg.CORBA.portable.InputStream is) {
        if (is != null) ((CDRInputStream) is).free();
    }

    ///////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////DII // Stuff////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////

    public org.omg.CORBA.Request request(org.omg.CORBA.Object self, String operation) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.InterfaceDef get_interface(org.omg.CORBA.Object self) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object get_interface_def(org.omg.CORBA.Object self) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Request create_request(org.omg.CORBA.Object self,
            org.omg.CORBA.Context ctx, String operation,
            org.omg.CORBA.NVList arg_list, org.omg.CORBA.NamedValue result,
            org.omg.CORBA.ExceptionList exclist,
            org.omg.CORBA.ContextList ctxlist) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Request create_request(org.omg.CORBA.Object self,
            org.omg.CORBA.Context ctx, String operation,
            org.omg.CORBA.NVList arg_list, org.omg.CORBA.NamedValue result) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    ///////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////Local Invocations///////////////////////////
    ///////////////////////////////////////////////////////////////////////////////

    public org.omg.CORBA.portable.ServantObject servant_preinvoke(
            org.omg.CORBA.Object self, String operation, Class expectedType) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void servant_postinvoke(org.omg.CORBA.Object self,
            org.omg.CORBA.portable.ServantObject servant) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
