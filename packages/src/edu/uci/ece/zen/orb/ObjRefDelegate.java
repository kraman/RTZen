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


import edu.uci.ece.zen.orb.policies.PriorityModelPolicyImpl;
import edu.uci.ece.zen.utils.Queue;
import edu.uci.ece.zen.utils.WriteBuffer;
import edu.uci.ece.zen.utils.ZenProperties;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.orb.transport.TransportFactory;

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

    protected static ObjRefDelegate instance() {
        if (objRefDelegateCache.isEmpty()) {
            try {
                return (ObjRefDelegate) ImmortalMemory.instance().newInstance(
                        ObjRefDelegate.class);
            } catch (Exception e) {
                ZenProperties.logger.log(Logger.FATAL, ObjRefDelegate.class, "instance", e);
                System.exit(-1);
            }
        } else return (ObjRefDelegate) objRefDelegateCache.dequeue();
        return null;
    }

    private static void release(ObjRefDelegate self) {
        if(!self.released){
            objRefDelegateCache.enqueue(self);
            self.ior.free();
            //FString.free(self.priorityLanes[0].objectKey);
            for(int i = 0; i < self.priorityLanes.length; ++i)
                if(self.priorityLanes[i].objectKey != null)
                    FString.free(self.priorityLanes[i].objectKey);

            //System.out.println("RELEASEEEEEEEEEEEEEEEEEEEEEEEEE");
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

    private ORB orb;

    private LaneInfo priorityLanes[];

    private int numLanes;

    protected void init(org.omg.IOP.IOR ior, ObjectImpl obj, ORB orb, ORBImpl orbImpl) {
        init( ior, obj, orb, orbImpl, false );
    }

    protected void init(org.omg.IOP.IOR ior, ObjectImpl obj, ORB orb, ORBImpl orbImpl , boolean isCollocated ) {
        released = false;
        referenceCount = 1;
        this.orb = orb;

        this.ior = WriteBuffer.instance();
        this.ior.init();

        CDROutputStream out = CDROutputStream.instance();
        out.init(orb);
        org.omg.IOP.IORHelper.write(out, ior);
        out.getBuffer().dumpBuffer(this.ior);
        out.free();
        ZenProperties.logger.log("ObjRefDel init 1");
        System.out.println("++++++++++++++++++++++++++++++++++ObjRefDel no of prof:" + ior.profiles.length);
        //process all the tagged profiles
        for (int i = 0; i < ior.profiles.length; i++) {
            processTaggedProfile(ior.profiles[i], obj, orbImpl, isCollocated);
        }
        ZenProperties.logger.log("ObjRefDel init 2");
        numLanes = 0;
    }

    public synchronized void addLaneData(int min, int max,
            ScopedMemory transport, FString objectKey, Class protocolFactory ) {
        if (ZenProperties.dbg) ZenProperties.logger.log(RealtimeThread
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
        priorityLanes[numLanes++].init(min, max, transport, objectKey, protocolFactory);
    }

    public LaneInfo getLane() {
        int priority = RealtimeThread.currentThread().getPriority();
        for (int i = 0; i < priorityLanes.length; i++) {
            LaneInfo ln = (LaneInfo) priorityLanes[i];
            if (ZenProperties.dbg) ZenProperties.logger.log("Checking if "
                    + priority + " is within " + ln.minPri + " <--> "
                    + ln.maxPri);
            if (ln.minPri <= priority && ln.maxPri >= priority) return ln;
        }
        return null;
    }

    private void processTaggedProfile(TaggedProfile profile, ObjectImpl obj, ORBImpl orbImpl, boolean isCollocated) {
        int tag = profile.tag;
        if (ZenProperties.dbg) ZenProperties.logger.log("processTaggedProfile " + tag);
        ZenProperties.logger.log("ObjRefDel processTaggedProfile 1");
        switch (tag) {
            case TAG_INTERNET_IOP.value: //establish appropriate connections
            case TAG_SERIAL.value:
                ZenProperties.logger.log( "IIOP transport profile found" );
                TransportFactory.connect( tag , profile , obj , this , isCollocated );
                break;
            case TAG_MULTIPLE_COMPONENTS.value: //process the tagged components
                //TODO: Currently ignored because they are of no immediate use
                ZenProperties.logger.log("TAG_MULTIPLE_COMPONENTS ignored");
                break;
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
        /*
         * String[] ids = ((org.omg.CORBA.portable.ObjectImpl)self)._ids(); for(
         * int i=0;i <ids.length;i++ ) if( ids[i].equals( repository_id ) )
         * return true; //send _is_a GIOP message
         * org.omg.CORBA.portable.OutputStream _output = null;
         * org.omg.CORBA.portable.InputStream _input = null; try{ _output =
         * request( self , "_is_a" , true ); _output.write_string( repository_id );
         * _input = invoke( self , _output); boolean ret =
         * _input.read_boolean(); return ret; }catch(
         * org.omg.CORBA.portable.RemarshalException _exception){ }catch(
         * org.omg.CORBA.portable.ApplicationException ae ){ }finally{
         * releaseReply( self , _input); } //if still not found
         */

        return false;
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
        /*
         * org.omg.CORBA.portable.OutputStream _output = null;
         * org.omg.CORBA.portable.InputStream _input = null; try{ _output =
         * request( self , "_non_existent", true ); _input = invoke( self ,
         * _output ); boolean ret = _input.read_boolean(); return ret; }catch(
         * org.omg.CORBA.portable.RemarshalException _exception){ }catch(
         * org.omg.CORBA.portable.ApplicationException ae ){ }finally{
         * releaseReply( self,_input ); }
         */
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
    ///////////////////////////////Request / Reply
    // stuff///////////////////////////
    ///////////////////////////////////////////////////////////////////////////////

    /**
     * Client upcall:
     * <p>
     * <b>Client scope </b> --ex in --&gt; ORB parent scope --&gt; ORB scope
     * --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public org.omg.CORBA.portable.OutputStream request( org.omg.CORBA.Object self, String operation, boolean responseExpected) {
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
    public org.omg.CORBA.portable.OutputStream request( org.omg.CORBA.Object self, String operation, boolean responseExpected, byte majorVersion, byte minorVersion) {
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
    public org.omg.CORBA.portable.InputStream invoke(org.omg.CORBA.Object self,
            org.omg.CORBA.portable.OutputStream os)
            throws org.omg.CORBA.portable.ApplicationException,
            org.omg.CORBA.portable.RemarshalException {
        org.omg.CORBA.portable.InputStream ret = ((ClientRequest) os).invoke();
        ((ClientRequest) os).free();
        return ret;
    }

    public void releaseReply(org.omg.CORBA.Object self,
            org.omg.CORBA.portable.InputStream is) {
        if (is != null) ((CDRInputStream) is).free();
    }

    ///////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////DII Stuff///////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////

    public org.omg.CORBA.Request request(org.omg.CORBA.Object self,
            String operation) {
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
