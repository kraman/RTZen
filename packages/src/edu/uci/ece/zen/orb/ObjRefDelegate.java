package edu.uci.ece.zen.orb;

import org.omg.IOP.*;
import javax.realtime.*;
import edu.uci.ece.zen.utils.*;

public final class ObjRefDelegate extends org.omg.CORBA_2_3.portable.Delegate {
    private static Queue objRefDelegateCache;
    static{
        try{
            objRefDelegateCache = (Queue) ImmortalMemory.instance().newInstance( Queue.class );
        }catch( Exception e ){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    protected static ObjRefDelegate instance(){
        if( objRefDelegateCache.isEmpty() ){
            try{
                return (ObjRefDelegate) ImmortalMemory.instance().newInstance( ObjRefDelegate.class );
            }catch( Exception e ){
                e.printStackTrace();
                System.exit(-1);
            }           
        }else
            return (ObjRefDelegate) objRefDelegateCache.dequeue();
        return null;
    }

    private static void release( ObjRefDelegate self ){
        objRefDelegateCache.enqueue( self );
    }

    public ObjRefDelegate(){
        priorityLanes = new LaneInfo[10];
        for( int i=0;i<priorityLanes.length;i++ )
            priorityLanes[i] = new LaneInfo();
        numLanes = 0;
    }

    private WriteBuffer ior;
    private ORB orb;
    
    private LaneInfo priorityLanes[];
    private int numLanes;

    protected void init( org.omg.IOP.IOR ior , ObjectImpl obj , ORB orb , ORBImpl orbImpl ){
        System.out.println( Thread.currentThread() + "ObjRefDelegate.init 1" );
        referenceCount=1;
        System.out.println( Thread.currentThread() + "ObjRefDelegate.init 2" );
        this.orb = orb;
        System.out.println( Thread.currentThread() + "ObjRefDelegate.init 3" );

        this.ior = WriteBuffer.instance();
        System.out.println( Thread.currentThread() + "ObjRefDelegate.init 4" );
        this.ior.init();
        System.out.println( Thread.currentThread() + "ObjRefDelegate.init 5" );

        CDROutputStream out = CDROutputStream.instance();
        System.out.println( Thread.currentThread() + "ObjRefDelegate.init 6" );
        out.init( orb );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.init 7" );
        org.omg.IOP.IORHelper.write( out , ior );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.init 8" );
        out.getBuffer().dumpBuffer( this.ior );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.init 9" );
        out.free();
        System.out.println( Thread.currentThread() + "ObjRefDelegate.init 10" );

        //process all the tagged profiles
        for( int i=0;i<ior.profiles.length;i++ ){   //go through each tagged profile
        System.out.println( Thread.currentThread() + "ObjRefDelegate.init 11" );
            processTaggedProfile( ior.profiles[i] , obj , orbImpl );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.init 12" );
        }
        System.out.println( Thread.currentThread() + "ObjRefDelegate.init 13" );
        numLanes=0;
        System.out.println( Thread.currentThread() + "ObjRefDelegate.init 14" );
    }

    public synchronized void addLaneData( int min , int max , ScopedMemory transport , byte[] objectKey ){
        if( ZenProperties.dbg )
            System.out.println( Thread.currentThread() + "New lane info: " + min + " <--> " + max + "  :  " + transport );
        priorityLanes[numLanes++].init( min , max , transport , objectKey );
    }

    public LaneInfo getLane(){
        int priority = RealtimeThread.currentThread().getPriority();
        for( int i=0;i<priorityLanes.length;i++ ){
            LaneInfo ln = (LaneInfo) priorityLanes[i];
            if( ln.minPri <= priority && ln.maxPri >= priority )
                return ln;
        }
        return null;
    }

    private void processTaggedProfile( TaggedProfile profile , ObjectImpl obj , ORBImpl orbImpl ){
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 01" );
        int tag = profile.tag;
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 02" );
        switch( tag ){
            case TAG_INTERNET_IOP.value:            //establish appropriate connections and register them
                {
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 03" );
                    byte[] data = profile.profile_data;
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 04" );
                    ReadBuffer rb = ReadBuffer.instance();
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 05" );
                    rb.init();
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 06" );
                    rb.writeByteArray( data , 0 , data.length );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 07" );
                    CDRInputStream in = CDRInputStream.instance();
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 08" );
                    in.init( orb , rb );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 09" );
                    in.setEndian( in.read_boolean() );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 10" );
                    byte iiopMinor = data[2];
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 11" );
                    try{
                        switch( iiopMinor ){
                            case 0:{
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 11-0" );
                                org.omg.IIOP.ProfileBody_1_0 profilebody = org.omg.IIOP.ProfileBody_1_0Helper.read( in );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 12" );
                                long connectionKey = ConnectionRegistry.ip2long( profilebody.host , profilebody.port );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 13" );
                                ScopedMemory transportScope = orb.getConnectionRegistry().getConnection( connectionKey );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 14" );
                                if( transportScope == null ){
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 15" );
                                    transportScope = edu.uci.ece.zen.orb.transport.iiop.Connector.instance().connect( profilebody.host , profilebody.port , orb , orbImpl );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 16" );
                                    orb.getConnectionRegistry().putConnection( connectionKey , transportScope );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 17" );
                                }

                                addLaneData( Thread.MIN_PRIORITY , Thread.MAX_PRIORITY , transportScope , profilebody.object_key );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 18" );
                            }break;
                            case 1:{
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 11-1" );
                                org.omg.IIOP.ProfileBody_1_1 profilebody = org.omg.IIOP.ProfileBody_1_1Helper.read( in );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 19" );
                                long connectionKey = ConnectionRegistry.ip2long( profilebody.host , profilebody.port );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 20" );
                                //System.err.println( orb );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 21" );
                                ScopedMemory transportScope = orb.getConnectionRegistry().getConnection( connectionKey );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 22" );
                                if( transportScope == null ){
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 23" );
                                    edu.uci.ece.zen.orb.transport.Connector connector = edu.uci.ece.zen.orb.transport.iiop.Connector.instance();
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 23-1" );
                                    transportScope = connector.connect( profilebody.host , profilebody.port , orb , orbImpl );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 24" );
                                    orb.getConnectionRegistry().putConnection( connectionKey , transportScope );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 25" );
                                }
                            
                                //TODO: process priority policies here and add the appropriate lanes
                                addLaneData( NoHeapRealtimeThread.MIN_PRIORITY , NoHeapRealtimeThread.MAX_PRIORITY , transportScope , profilebody.object_key );
        System.out.println( Thread.currentThread() + "ObjRefDelegate.processTaggedProfile 26" );
                            }break;
                        }
                    }catch( HashtableOverflowException hoe ){
                        ZenProperties.logger.log(
                            Logger.WARN,
                            "edu.uci.ece.zen.orb.ObjRefDelegate",
                            "processTaggedProfile",
                            "Cant make any more connections. Hashtable is full" 
                        );
                    }
                }
                break;
            case TAG_MULTIPLE_COMPONENTS.value:     //process the tagged components
                //TODO: Currently ignored because they are of no immediate use
                break;
        }
    }

    public org.omg.IOP.IOR getIOR(){
        CDRInputStream in = CDRInputStream.instance();
        in.init( orb , ior.getReadBuffer() );
        org.omg.IOP.IOR ret = org.omg.IOP.IORHelper.read( in );
        in.free();
        return null;
    }

    public String toString(){
        return IOR.makeIOR( ior );
    }
    
    /**
     * The logical_type_id is a string denoting a shared type identifier (RepositoryId). The operation
     * returns true if the object is really an instance of that type, including if that type is an
     * ancestor of the  most derived  type of that object. Determining whether an object's type is
     * compatible with the logical_type_id may require contacting a remote ORB or interface repository.
     * Such an attempt may fail at either the local or the remote end. If is_a cannot make a reliable
     * determination of type compatibility due to failure, it raises an exception in the calling
     * application code. This enables the application to distinguish among the TRUE, FALSE, and 
     * indeterminate cases.
     */
    public boolean is_a(org.omg.CORBA.Object self, String repository_id ){
        //TODO: Send _is_a message
        /*
        String[] ids = ((org.omg.CORBA.portable.ObjectImpl)self)._ids();
        for( int i=0;i<ids.length;i++ )
            if( ids[i].equals( repository_id ) )
                return true;
        //send _is_a GIOP message
        org.omg.CORBA.portable.OutputStream _output = null;
        org.omg.CORBA.portable.InputStream  _input  = null;
        try{
            _output = request( self , "_is_a" , true );
            _output.write_string( repository_id );
            _input = invoke( self , _output);
            boolean ret = _input.read_boolean();
            return ret;
        }catch( org.omg.CORBA.portable.RemarshalException _exception){
        }catch( org.omg.CORBA.portable.ApplicationException ae ){
        }finally{
            releaseReply( self , _input);
        }
        
        //if still not found
        */
        return false;
    }

    private int referenceCount;
    private void incermentReferenceCount(){
        referenceCount++;
    }
    private void decrementReferenceCount(){
        referenceCount--;
        if( referenceCount <= 0 ){
            referenceCount=0;
            ObjRefDelegate.release( this );
        }
    }

    public org.omg.CORBA.Object duplicate(org.omg.CORBA.Object self) {
        org.omg.CORBA.portable.ObjectImpl impl = (org.omg.CORBA.portable.ObjectImpl) self;
        ObjRefDelegate delegate = (ObjRefDelegate) impl._get_delegate();
        delegate.incermentReferenceCount();
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
        org.omg.CORBA.portable.OutputStream _output = null;
        org.omg.CORBA.portable.InputStream  _input  = null;
        try{
            _output = request( self , "_non_existent", true );
            _input = invoke( self , _output );
            boolean ret = _input.read_boolean();
            return ret;
        }catch( org.omg.CORBA.portable.RemarshalException _exception){
        }catch( org.omg.CORBA.portable.ApplicationException ae ){
        }finally{
            releaseReply( self,_input );
        }
        */
        return false;
    }

    public boolean is_local(org.omg.CORBA.Object self) {
        return false;
    }

    public boolean is_equivalent(org.omg.CORBA.Object self, org.omg.CORBA.Object rhs ){
        org.omg.CORBA.portable.ObjectImpl impl1 = (org.omg.CORBA.portable.ObjectImpl) self;
        ObjRefDelegate delegate1 = (ObjRefDelegate) impl1._get_delegate();

        org.omg.CORBA.portable.ObjectImpl impl2 = (org.omg.CORBA.portable.ObjectImpl) rhs;
        ObjRefDelegate delegate2 = (ObjRefDelegate) impl2._get_delegate();

        return delegate1 == delegate2;
    }

    public int hash(org.omg.CORBA.Object self, int max) {
        return self.hashCode() % max;
    }

    public synchronized org.omg.CORBA.Object set_policy_override( org.omg.CORBA.Object self, org.omg.CORBA.Policy[] policies, org.omg.CORBA.SetOverrideType set_add) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }


    ///////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////Request / Reply stuff///////////////////////////
    ///////////////////////////////////////////////////////////////////////////////

    /**
     * Client upcall:
     * <p>
     *     <b>Client scope</b> --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public org.omg.CORBA.portable.OutputStream request(org.omg.CORBA.Object self, String operation, boolean responseExpected) {
        return new ClientRequest( operation , responseExpected , (byte)1 , (byte)0 , orb , this );
    }

    /**
     * Client upcall:
     * <p>
     *     <b>Client scope</b> --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public org.omg.CORBA.portable.OutputStream request(org.omg.CORBA.Object self, String operation, boolean responseExpected, byte majorVersion, byte minorVersion) {
        return new ClientRequest( operation , responseExpected , majorVersion , minorVersion , orb , this );
    }

    /**
     * Client upcall:
     * <p>
     *     <b>Client scope</b> --ex in --&gt; ORB parent scope --&gt; ORB scope --&gt; Message scope/Waiter region --&gt; Transport scope
     * </p>
     */
    public org.omg.CORBA.portable.InputStream invoke(org.omg.CORBA.Object self, org.omg.CORBA.portable.OutputStream os)
            throws org.omg.CORBA.portable.ApplicationException, org.omg.CORBA.portable.RemarshalException {
        return ((ClientRequest)os).invoke();
    }

    public void releaseReply(org.omg.CORBA.Object self, org.omg.CORBA.portable.InputStream is) {
        if( is != null )
            ((CDRInputStream)is).free();
    }

    ///////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////DII Stuff///////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////

    public org.omg.CORBA.Request request( org.omg.CORBA.Object self, String operation) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
    public org.omg.CORBA.InterfaceDef get_interface( org.omg.CORBA.Object self ){
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object get_interface_def(org.omg.CORBA.Object self) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Request create_request(org.omg.CORBA.Object self, org.omg.CORBA.Context ctx, String operation, 
            org.omg.CORBA.NVList arg_list, org.omg.CORBA.NamedValue result, org.omg.CORBA.ExceptionList exclist, org.omg.CORBA.ContextList ctxlist) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Request create_request(org.omg.CORBA.Object self, org.omg.CORBA.Context ctx, String operation, 
            org.omg.CORBA.NVList arg_list, org.omg.CORBA.NamedValue result) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    ///////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////Local Invocations///////////////////////////
    ///////////////////////////////////////////////////////////////////////////////

    public org.omg.CORBA.portable.ServantObject servant_preinvoke(org.omg.CORBA.Object self, String operation, Class expectedType) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void servant_postinvoke(org.omg.CORBA.Object self, org.omg.CORBA.portable.ServantObject servant) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
