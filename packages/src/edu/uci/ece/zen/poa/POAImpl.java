package edu.uci.ece.zen.poa;

import org.omg.CORBA.CompletionStatus;
import javax.realtime.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.giop.type.*;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableServer.*;

public class POAImpl{

    //////////////////////////////////////////////////////////////////
    ////                   DATA MEMBERS                         /////
    /////////////////////////////////////////////////////////////////
    // -- Exceptions--
    private org.omg.PortableServer.POAPackage.NoServant ns = new org.omg.PortableServer.POAPackage.NoServant();
    private org.omg.PortableServer.POAPackage.ServantAlreadyActive saa = new org.omg.PortableServer.POAPackage.ServantAlreadyActive();
    private org.omg.PortableServer.POAPackage.ObjectAlreadyActive oaa = new org.omg.PortableServer.POAPackage.ObjectAlreadyActive();
    private org.omg.PortableServer.POAPackage.ObjectNotActive ona = new org.omg.PortableServer.POAPackage.ObjectNotActive();
    private org.omg.PortableServer.POAPackage.ServantNotActive sna = new org.omg.PortableServer.POAPackage.ServantNotActive();
    private org.omg.PortableServer.POAPackage.WrongAdapter wa = new org.omg.PortableServer.POAPackage.WrongAdapter();

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

    // ---  Current Number of request executing in the POA ---
    private SynchronizedInt numberOfCurrentRequests;

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

    public POAImpl(){
        poaHashMapQueue = new Queue();
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
            ret = new FString();
            ret.init(256);
        }
        ret.reset();
        return ret;
    }

    public void retFString( FString str ){
        poaFStringQueue.enqueue( str );
    }   

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
        try{
            this.threadPolicyStrategy = edu.uci.ece.zen.poa.mechanism.ThreadPolicyStrategy.init(policyList);
            this.idAssignmentStrategy = edu.uci.ece.zen.poa.mechanism.IdAssignmentStrategy.init(policyList);
            this.uniquenessStrategy = edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy.init(policyList);
            this.retentionStrategy = edu.uci.ece.zen.poa.mechanism.ServantRetentionStrategy.init(policyList, this.uniquenessStrategy);
            this.lifespanStrategy = edu.uci.ece.zen.poa.mechanism.LifespanStrategy.init(this.policyList);
            this.activationStrategy = edu.uci.ece.zen.poa.mechanism.ActivationStrategy.init(this.policyList, this.idAssignmentStrategy, this.retentionStrategy);
            this.requestProcessingStrategy = edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy.init(this.policyList,
                    this.retentionStrategy, this.uniquenessStrategy, this.threadPolicyStrategy); 
        }
        catch( Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Call scoped region graph:
     * <p>
     *      Transport thread:<br/>
     *      <p>
     *          Transport scope --ex in--&gt; ORBImpl scope --&gt; <b>Message</b> --ex in--&gt; ORBImpl scope --&gt; 
     *              POAImpl region --ex in--&gt; ORBImpl scope --&gt; TP Region 
     *      </p>
     *      TP Thread:<br/>
     *      <p>
     *          <b>TP Region</b> --ex in--&gt; ORBImpl scope --&gt; Message Region --ex in--&gt; ORBImpl region --&gt; Transport Scope
     *      </p>
     * </p>
     */
    public void handleRequest( RequestMessage req , POARunnable prun ){
        prun.exception = validateProcessingState(); // check for the state of the poa? if it is discarding then throw the transient exception...
        if( prun.exception != -1 )
            return;

        // Check the state of the POAManager. Here the POA is in active state
        prun.exception = POAManager.checkPOAManagerState(this.poaManager);
        if( prun.exception != -1 ){
            prun.exception += 2;
            return;
        }

        // check if the POA has the persistent policy/or the transient
        prun.exception = this.lifespanStrategy.validate(req.getObjectKey());
        if( prun.exception != -1 ){
            prun.exception += 5;
            return;
        }

        try {
            ScopedMemory tpRegion = this.orb.getThreadPoolRegion(tpId);
            ScopedMemory requestScope = (ScopedMemory) MemoryArea.getMemoryArea( req );

            ExecuteInRunnable eir = (ExecuteInRunnable) requestScope.newInstance( ExecuteInRunnable.class );
            TPRunnable tpr = (TPRunnable) requestScope.newInstance( TPRunnable.class );
            tpr.init( this , numberOfCurrentRequests , this.requestProcessingStrategy , req );
            eir.init( tpr , tpRegion );
            orb.orbImplRegion.executeInArea( eir );
        } catch (Exception ex) {
            // -- have to send a request not handled to the client here
            // -- Throw a transient exception
            prun.exception = 7;
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
    public org.omg.CORBA.Object servant_to_reference( final org.omg.PortableServer.Servant p_servant , MemoryArea clientMemoryArea )
            throws org.omg.PortableServer.POAPackage.ServantNotActive, org.omg.PortableServer.POAPackage.WrongPolicy {

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

        try{
            this.retentionStrategy.getObjectID( p_servant,oid );
            POAHashMap map = this.retentionStrategy.getHashMap(oid);

            int index = this.retentionStrategy.bindDemuxIndex(map);
            int count = this.retentionStrategy.getGenCount(index);

            // Create the Object Key using the IdHint Strategy
            okey = this.lifespanStrategy.create(this.poaPath, oid, this.poaDemuxIndex, index, count);

            return this.create_reference_with_object_key (okey, p_servant._all_interfaces(this, null)[0]);
        }catch( org.omg.PortableServer.POAPackage.ServantNotActive sna ){
            // Servant is not present and the POA has retain policy: Check if
            // Multiple Id and Implicit Activation are permitted next
            if (this.activationStrategy.validate(edu.uci.ece.zen.poa.mechanism.ActivationStrategy.IMPLICIT_ACTIVATION)
                    || this.uniquenessStrategy.validate(edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy.MULTIPLE_ID)) {

                oid =  ((this.idAssignmentStrategy.nextId()));
                POAHashMap map = POAHashMap.initialize(oid, p_servant);

                this.retentionStrategy.add(oid, map);
                orb.set_delegate (p_servant);

                int index = this.retentionStrategy.bindDemuxIndex(map);
                int genCount = this.retentionStrategy.getGenCount(index);

                // Logger.debug("Servant to ref: serv demux loc: index = " + index
                // + " loc = " + genCount);
                //ActiveDemuxLoc servLoc = new ActiveDemuxLoc(index, genCount);

                okey = this.lifespanStrategy.create(this.poaPath, oid, this.poaDemuxIndex, index,genCount); 
                return this.create_reference_with_object_key(okey, p_servant._all_interfaces(this, null)[0]);
            }
            throw sna;
        }finally{
            retFString( okey );
            retFString( oid );
        }
    }

    public void servant_to_id( Servant servant , MemoryArea mem , POARunnable prun ){
    }

    public void servant_to_reference( Servant servant , MemoryArea mem , POARunnable prun ){
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

    public void get_servant_manager( ServantManager manager , POARunnable prun ){
    }

    public void get_servant( POARunnable prun ){
    }

    public void set_servant( Servant servant , POARunnable prun ){
    }

    public void create_reference( String atr , MemoryArea mem , POARunnable prun ){
    }

    public void create_reference_with_id( byte[] oid , String str , MemoryArea mem , POARunnable prun ){
    }

    public void get_policy_list( MemoryArea mem , POARunnable prun ){
    }
    /**
     * Returns the servant associated with the given object id
     * @param oid The object_is object.
     * @throws org.omg.PortableServer.POAPackage.ObjectNotActive If object indicated by the object_is is not active or the POA has the                      * USE_DEFAULT_SERVANT policy and no default servant has  been registered with the POA this exception is raised.
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy If RETAIN policy nor the USE_DEFAULT_SERVANT policyis present this exception is raised.
     * @return The servant associated with the given object id
     */

    public org.omg.PortableServer.Servant id_to_servant(final byte[] oid)
        throws
        org.omg.PortableServer.POAPackage.ObjectNotActive,
    org.omg.PortableServer.POAPackage.WrongPolicy {
        try {
            byte[] id =  (oid);

            return this.retentionStrategy.getServant(id);
        } catch (Exception ex) {}

        return (org.omg.PortableServer.Servant)
            this.requestProcessingStrategy.getRequestProcessor(edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy.DEFAULT_SERVANT);
    }


    public boolean isDestructionApparent() {
        return (poaState > Util.DESTRUCTION_IN_PROGRESS) ? true : false;
    }

    protected int validateProcessingState() {
        switch (this.processingState) {
            case Util.DISCARDING:
                return 1;
            case Util.INACTIVE:
                return 2;
        }
        return -1;
    }

    org.omg.CORBA.Object synchronized create_reference_with_object_key( final byte[]  ok, int okLength, final String intf, MemoryArea clientArea ) {
        CreateReferenceWithObjectRunnable r = CreateReferenceWithObjectRunnable.instance();       
        r.init( ok, okLength, intf, clientArea, orb);
        orb.orbImplRegion().executeInArea(r)
            try{
                return r.retVal; 
            }catch( Exception e ){
                e.printStackTrace();
                return null;
            }
    }

    private void activate_object_with_id_and_return_contents(
            final org.omg.PortableServer.Servant p_servant,
            final byte[] oid)
        throws org.omg.PortableServer.POAPackage.ServantAlreadyActive,
    org.omg.PortableServer.POAPackage.WrongPolicy {
        if (this.retentionStrategy.servantPresent(p_servant)
                && this.uniquenessStrategy.validate(edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy.UNIQUE_ID)) {

            throw saa;
        }

        byte[] oID =  (oid);

        POAHashMap map = POAHashMap.initialize(oID, p_servant);

        this.retentionStrategy.add(oID, map);
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

    public static _instance;

    public CreateReferenceWithObjectRunnable instance(){
        if( _instance == null )
            _instance = new CreateReferenceWithObjectRunnable();
        return _instance;
    }

    public org.omg.CORBA.Object retVal;
    public byte[] ok;
    public String intf;
    public MemoryArea ma;
    public ORB orb;
    public int okLength;

    public void init( byte[] ok , int objKeyLength, String intf, MemoryArea ma, ORB orb){
        this.ok = ok;
        this.intf = intf;
        this.ma = ma;
        this.orb = orb;
        this.okLength = okLength;
    }

    public void run()
    {
        retVal = edu.uci.ece.zen.poa.IOR.makeCORBAObject(orb, intf, ok, okLength, ma);
    }
}
