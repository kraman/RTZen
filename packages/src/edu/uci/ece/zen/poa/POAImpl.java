package edu.uci.ece.zen.poa;

import org.omg.CORBA.CompletionStatus;
import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.giop.type.*;
import org.omg.CORBA.Policy;
import org.omg.CORBA.IntHolder;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableServer.*;

public class POAImpl{

    //////////////////////////////////////////////////////////////////
    ////                   DATA MEMBERS                         /////
    /////////////////////////////////////////////////////////////////
    // -- ZEN ORB ---
    MemoryArea thisMemory;
    private edu.uci.ece.zen.orb.ORB orb;

    // --- POA Names relative and Complete Path Names ---
    private int poaId;

    // --- POA Specific references ---
    private POA                                     parent;
    private POA                                     self;
    private java.util.Hashtable                     theChildren;
    private org.omg.PortableServer.POAManager       poaManager;
    private org.omg.PortableServer.AdapterActivator adapterActivator;
    private org.omg.PortableServer.ServantManager   theServantManager;
    private org.omg.PortableServer.Servant          theServant = null;
    private POAServerRequestHandler                 serverRequestHandler;
    private int                                     poaDemuxIndex;
    private int                                     poaDemuxCount;

    // ---  Current Number of request executing in the POA ---
    protected SynchronizedInt numberOfCurrentRequests;

    // --- Mutexes POA and varable specific to the create and destroy ops ---
    private Object createDestroyPOAMutex = new byte[0];

    private boolean disableCreatePOA = false;
    private boolean etherealize;

    // -- Policy List for the POA
    private org.omg.CORBA.Policy[] policyList;

    // --- State of the POA ---
    private int poaState;
    private int processingState = POA.ACTIVE; // RequestProcessing state

    // --- POA Specific strategies ----
    private transient edu.uci.ece.zen.poa.mechanism.LifespanStrategy
        lifespanStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.ThreadPolicyStrategy
        threadPolicyStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.IdAssignmentStrategy
        idAssignmentStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.ServantRetentionStrategy
        retentionStrategy;

    public transient edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy
        requestProcessingStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy
        uniquenessStrategy;

    private transient edu.uci.ece.zen.poa.mechanism.ActivationStrategy
        activationStrategy;

    POAManager manager;
    int tpId;

    // --- POA Cached Objects ---
    Queue poaHashMapQueue;
    Queue poaFStringQueue;
    Queue poaIntHolderQueue;

    public POAImpl(){
        poaHashMapQueue = new Queue();
        poaFStringQueue = new Queue();
        poaIntHolderQueue = new Queue();
    }

    protected POAHashMap getPOAHashMap(){
        POAHashMap ret = (POAHashMap) poaHashMapQueue.dequeue();
        if( ret == null ){
            ret = new POAHashMap();
        }
        return ret;
    }

    protected void retPOAHashMap( POAHashMap map ){
        poaHashMapQueue.enqueue( map );
    }

    public FString getFString(){
        FString ret = (FString) poaFStringQueue.dequeue();
        if( ret == null ){
            try{
                ret = new FString();
                ret.init(256);
            }catch( Exception e ){
                e.printStackTrace();
            }
        }
        ret.reset();
        return ret;
    }

    public void retFString( FString str ){
        poaFStringQueue.enqueue( str );
    }

    public IntHolder getIntHolder(){
        IntHolder ret = (IntHolder) poaIntHolderQueue.dequeue();
        if( ret == null ){
            ret = new IntHolder();
        }
        return ret;
    }

    public void retIntHolder( IntHolder ih ){
        poaIntHolderQueue.enqueue( ih );
    }

    // --- POA Methods ---
    /**
     * Initializes the POAImpl and all mechanisms and policies.
     * @throws InvalidPolicyException
     */
    public void init( ORB orb , POA self , Policy[] policies , POA parent , POAManager manager , POARunnable prun ){
        this.orb = orb;
        this.self = self;
        this.parent = parent;
        this.manager = manager;
        this.tpId = 0;

        //make a local copy of the policies
        this.policyList = new Policy[policies.length];
        for( int i=0;i<policies.length;i++ )
            this.policyList[i] = policies[i].copy();

        //init the stratergies
        IntHolder ih = getIntHolder();
        this.threadPolicyStrategy = edu.uci.ece.zen.poa.mechanism.ThreadPolicyStrategy.init(policyList,ih);
        if( ih.value != 0 ){ retIntHolder(ih); prun.exception = POARunnable.InvalidPolicyException; return; }

        this.idAssignmentStrategy = edu.uci.ece.zen.poa.mechanism.IdAssignmentStrategy.init(policyList,ih);
        if( ih.value != 0 ){ retIntHolder(ih); prun.exception = POARunnable.InvalidPolicyException; return; }
        
        this.uniquenessStrategy = edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy.init(policyList,ih);
        if( ih.value != 0 ){ retIntHolder(ih); prun.exception = POARunnable.InvalidPolicyException; return; }
        
        this.retentionStrategy = edu.uci.ece.zen.poa.mechanism.ServantRetentionStrategy.init(policyList, this.uniquenessStrategy,ih);
        if( ih.value != 0 ){ retIntHolder(ih); prun.exception = POARunnable.InvalidPolicyException; return; }
        
        this.lifespanStrategy = edu.uci.ece.zen.poa.mechanism.LifespanStrategy.init(this.policyList,ih);
        if( ih.value != 0 ){ retIntHolder(ih); prun.exception = POARunnable.InvalidPolicyException; return; }
        
        this.activationStrategy = edu.uci.ece.zen.poa.mechanism.ActivationStrategy.init(this.policyList, this.idAssignmentStrategy, this.retentionStrategy,ih);
        if( ih.value != 0 ){ retIntHolder(ih); prun.exception = POARunnable.InvalidPolicyException; return; }
        
        this.requestProcessingStrategy = edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy.init(this.policyList,
                this.retentionStrategy, this.uniquenessStrategy, this.threadPolicyStrategy, ih);
        if( ih.value != 0 ){ retIntHolder(ih); prun.exception = POARunnable.InvalidPolicyException; return; }
    }

    /**
     * Call scoped region graph:
     * <p>
     *      Transport thread:<br/>
     *      <p>
     *          Transport scope --ex in--&gt; ORBImpl scope --&gt; Message --ex in--&gt; ORBImpl scope --&gt; 
     *              <b>POAImpl region</b> --ex in--&gt; ORBImpl scope --&gt; TP Region 
     *      </p>
     *      TP Thread:<br/>
     *      <p>
     *          <b>TP Region</b> --ex in--&gt; ORBImpl scope --&gt; Message Region --ex in--&gt; ORBImpl region --&gt; Transport Scope
     *      </p>
     * </p>
     */
    public void handleRequest( RequestMessage req , POARunnable prun ){
        IntHolder ih = getIntHolder();
        validateProcessingState( ih ); // check for the state of the poa? if it is discarding then throw the transient exception...
        if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; retIntHolder( ih ); return; }

        // Check the state of the POAManager. Here the POA is in active state
        prun.exception = POAManager.checkPOAManagerState(this.poaManager);
        if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; retIntHolder( ih ); return; }

        // check if the POA has the persistent policy/or the transient
        FString objKey = getFString();
        req.getObjectKey( objKey );
        this.lifespanStrategy.validate( objKey , ih );
        retFString( objKey );
        if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; retIntHolder( ih ); return; }

        try {
            ScopedMemory tpRegion = this.orb.getThreadPoolRegion(tpId);
            ScopedMemory requestScope = (ScopedMemory) MemoryArea.getMemoryArea( req );

            ExecuteInRunnable eir = (ExecuteInRunnable) requestScope.newInstance( ExecuteInRunnable.class );
            TPRunnable tpr = (TPRunnable) requestScope.newInstance( TPRunnable.class );
            tpr.init( self , requestScope );
            eir.init( tpr , tpRegion );

            HandleRequestRunnable hrr = (HandleRequestRunnable) requestScope.newInstance( HandleRequestRunnable.class );
            hrr.init( self , req );
            requestScope.setPortal( hrr );
            
            orb.orbImplRegion.executeInArea( eir );
        } catch (Exception ex) {
            // -- have to send a request not handled to the client here
            // -- Throw a transient exception
            prun.exception = POARunnable.TransientException;
            return;
        }
    }

    /**
     * Generates the object reference for that particular servant.
     * @param p_servant The servant object.
     * @throws org.omg.PortableServer.POAPackage.ServantNotActive  If the servant passed in is not active.
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy If the policies of the POA dont contain RETAIN and UNIQUE_ID/MYULTIPLE_ID policies.
     * @return The object reference for that particular servant.
     */
    public org.omg.CORBA.Object servant_to_reference( final org.omg.PortableServer.Servant p_servant , MemoryArea clientMemoryArea , POARunnable prun ){

        //check if this method is being called as a part of an upcall
        /* KLUDGE: Ignore current for now.
           org.omg.PortableServer.Current current = null;
           try {
           current = (org.omg.PortableServer.Current) Current.currentInit();
           if (p_servant == current.get_servant()) {
           return current.get_reference();
           }
           } catch (org.omg.PortableServer.CurrentPackage.NoContext ex) {
        //KLUDGE:
        //TODO: Check what is supposed to happen here
        //ex.printStackTrace();
        }

        // method was invoked outside the invocation context: Check if the
        // POA has the RETAIN and UNIQUE_ID in place.
        //NOTE: A ServantNotActive exception was being squelched here.
         */

        FString okey = getFString();
        FString oid = getFString();
        IntHolder ih = getIntHolder();
        org.omg.CORBA.Object retVal=null;

        this.retentionStrategy.getObjectID( p_servant,oid,ih );
        switch( ih.value ){
            case POARunnable.ServantNotActiveException:
            {
                if (this.activationStrategy.validate(edu.uci.ece.zen.poa.mechanism.ActivationStrategy.IMPLICIT_ACTIVATION)
                        || this.uniquenessStrategy.validate(edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy.MULTIPLE_ID)) {

                    this.idAssignmentStrategy.nextId( oid , ih );
                    if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; break; }
                        
                    POAHashMap map = getPOAHashMap();
                    map.init( oid, p_servant );

                    this.retentionStrategy.add( oid, map );
                    orb.set_delegate ( p_servant );

                    int index = this.retentionStrategy.bindDemuxIndex( map , ih );
                    if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; retPOAHashMap( map ); break; }
                    int genCount = this.retentionStrategy.getGenCount( index , ih );
                    if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; retPOAHashMap( map ); break; }

                    this.lifespanStrategy.create( self.poaPath , oid, this.poaDemuxIndex , this.poaDemuxCount , index , genCount , okey ); 
                    retVal = this.create_reference_with_object_key(okey, p_servant._all_interfaces(self, null)[0] , clientMemoryArea );
                    break;
                }
                prun.exception = POARunnable.ServantNotActiveException; retIntHolder(ih);
            }break;
            case POARunnable.WrongPolicyException:
                prun.exception = POARunnable.WrongPolicyException; retIntHolder(ih); break;
            case POARunnable.NoException:
            {
                POAHashMap map = this.retentionStrategy.getHashMap(oid , ih);
                if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; break; }

                int index = this.retentionStrategy.bindDemuxIndex( map , ih );
                if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; break; }
                int count = this.retentionStrategy.getGenCount( index , ih);
                if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; break; }

                // Create the Object Key using the IdHint Strategy
                this.lifespanStrategy.create(self.poaPath, oid, this.poaDemuxIndex , this.poaDemuxCount , index, count, okey );
                
                retVal = this.create_reference_with_object_key (okey, p_servant._all_interfaces(self, null)[0] , clientMemoryArea);
            }
        }
        retFString( okey );
        retFString( oid );
        retIntHolder( ih );
        return retVal;
    }

    public void servant_to_id( Servant servant , MemoryArea mem , POARunnable prun ){
    }

    public void reference_to_servant( org.omg.CORBA.Object obj , MemoryArea  mem , POARunnable prun ){
    }

    public void references_to_id( org.omg.CORBA.Object obj , MemoryArea mem , POARunnable prun ){
    }

    public void id_to_servant( byte[] oid , MemoryArea mem , POARunnable prun ){
    }

    public void id_to_reference( byte[] oid , MemoryArea mem , POARunnable prun ){
    }

    public void activate_object( Servant servant , MemoryArea mem , POARunnable prun ){
    }

    public void activate_object_with_id( byte[] oid , Servant servant , MemoryArea mem , POARunnable prun ){
    }

    public void deactivate_object( byte[] oid , MemoryArea mem , POARunnable prun ){
    }

    public void removeFromParent( POA poa ){
    }

    public void destroy( POA thisPoa ){
    }

    public void id( MemoryArea mem , POARunnable prun ){
    }

    public void get_servant_manager( POARunnable prun ){
    }

    public void set_servant_manager( ServantManager manager , POARunnable prun ){
    }

    public void get_servant( POARunnable prun ){
    }

    public void set_servant( Servant servant , POARunnable prun ){
    }

    public void create_reference( String atr , MemoryArea mem , POARunnable prun ){
    }

    public void create_reference_with_id( byte[] oid , String str , MemoryArea mem , POARunnable prun ){
    }

    public void policy_list( MemoryArea mem , POARunnable prun ){
    }

    protected void validateProcessingState( IntHolder ih ) {
        switch (this.processingState) {
            case POA.DISCARDING:
                ih.value = POARunnable.TransientException;
            case POA.INACTIVE:
                ih.value = POARunnable.ObjAdapterException;
        }
        ih.value = POARunnable.NoException;
    }

    public synchronized org.omg.CORBA.Object create_reference_with_object_key( FString ok, final String intf, MemoryArea clientArea ) {
        CreateReferenceWithObjectRunnable r = CreateReferenceWithObjectRunnable.instance();       
        r.init( ok, intf, clientArea, orb);
        try{
            orb.orbImplRegion.executeInArea(r);
            return r.retVal; 
        }catch( Exception e ){
            e.printStackTrace();
            return null;
        }
    }

    public edu.uci.ece.zen.poa.mechanism.ThreadPolicyStrategy getThreadPolicyStrategy()
    {
        return this.threadPolicyStrategy;
    }

    public SynchronizedInt getnumberOfCurrentRequests()
    {
        return numberOfCurrentRequests;
    }

}

class CreateReferenceWithObjectRunnable implements Runnable{

    public static CreateReferenceWithObjectRunnable _instance;

    public static CreateReferenceWithObjectRunnable instance(){
        if( _instance == null )
            _instance = new CreateReferenceWithObjectRunnable();
        return _instance;
    }

    public org.omg.CORBA.Object retVal;
    public FString ok;
    public String intf;
    public MemoryArea ma;
    public ORB orb;

    public void init( FString ok , String intf, MemoryArea ma, ORB orb){
        this.ok = ok;
        this.intf = intf;
        this.ma = ma;
        this.orb = orb;
    }

    public void run()
    {
        try{
            retVal = edu.uci.ece.zen.orb.IOR.makeCORBAObject(orb, intf, ok, ma);
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
}

class HandleRequestRunnable implements Runnable{
    POA poa;
    RequestMessage req;

    public void init( POA poa , RequestMessage req ){
        this.poa = poa;
        this.req = req;
    }
    
    public void run(){
        POAImpl pimpl = ((POAImpl)poa.poaMemoryArea.getPortal());
        try{
            pimpl.requestProcessingStrategy.handleRequest( req , poa , pimpl.numberOfCurrentRequests );
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
}
