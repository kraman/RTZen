package edu.uci.ece.zen.poa;

import javax.realtime.*;
import org.omg.CORBA.CompletionStatus;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableServer.*;
import edu.uci.ece.zen.orb.giop.type.*;

public class POA extends org.omg.CORBA.LocalObject implements org.omg.PortableServer.POA {

    private static Queue unusedFacades;
    private static Queue unusedPOARunnables;
    private static ImmortalMemory imm;

    private ORB orb;
    private POA parent;
    private ScopedMemory poaMemoryArea;
    private org.omg.PortableServer.POAManager poaManager;
    private ServerRequestHandler serverRequestHandler;
    private Hashtable theChildren;
    private SynchronizedInt numberOfCurrentRequests;
    private int poaState;
    private int poaDemuxIndex;
    private int processingState = POA.ACTIVE;
    private AdapterActivator adapterActivator;
    
    private FString poaName;
    private FString poaPath;

    /* Mutexes POA and varable specific to the create and destroy ops */
    private Object createDestroyPOAMutex;
    private boolean disableCreatePOA = false;
    private boolean etherealize;

    /* Constants for the POA Class */
    protected final String rootPoaString = "RootPOA";
    protected static final int CREATING = 0;
    protected static final int CREATION_COMPLETE = 1;
    protected static final int DESTRUCTION_IN_PROGRESS = 3;
    protected static final int DESTRUCTION_APPARANT = 4;
    protected static final int DESTRUCTION_COMPLETE = 5;

    /* Request Processing States */
    protected static final int ACTIVE = 6;
    protected static final int DISCARDING = 7;
    protected static final int INACTIVE = 8;

    static{
        try{
            imm = ImmortalMemory.instance();
            //Set up POA Facades
            int numFacades = Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.poa.maxNumPOAs" , "1" ) );
            unusedFacades = (Queue) imm.newInstance( Queue.class );
            for( int i=0;i<numFacades;i++ )
                unusedFacades.enqueue( imm.newInstance( edu.uci.ece.zen.poa.POA.class ) );
        }catch( Exception e ){
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    public static edu.uci.ece.zen.poa.POA instance(){
        edu.uci.ece.zen.poa.POA retVal;
        retVal = (edu.uci.ece.zen.poa.POA) unusedFacades.dequeue();
        return retVal;
    }

    private static void release( edu.uci.ece.zen.poa.POA poa ){
        unusedFacades.enqueue( poa );
    }

    public void free(){
        POA.release( this );
    }

    public void initAsRootPOA( final edu.uci.ece.zen.orb.ORB orb ){
        this.init( orb , rootPoaString , null , null , null );
    }

    ///////////////////////////////////////////////////////////////////////////
    ////////////////////////// BASIC POA METHODS //////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public void init( final edu.uci.ece.zen.orb.ORB orb , String poaName , org.omg.CORBA.Policy[] policies ,
            org.omg.PortableServer.POA _parent, org.omg.PortableServer.POAManager manager ){
        POA parent = (POA) _parent;
        poaState = POA.CREATING;
        this.orb = orb;
        this.poaMemoryArea = ORB.getScopedRegion();

        this.poaName.reset();
        this.poaName.append( poaName );

        this.poaPath.reset();
        if( parent == null ){
            this.poaPath.append( '/' );
        }else{
            this.poaPath.append( parent.poaPath.getData() , 0 , parent.poaPath.length() );
        }
        this.poaPath.append( this.poaName.getData() , 0 , this.poaName.length() );
        this.poaPath.append( '/' );

        if( manager == null )
            manager = POAManager.instance();
        this.poaManager = manager;
        ((POAManager)poaManager).register( (org.omg.PortableServer.POA) this );
        serverRequestHandler = ((ORBImpl)((ScopedMemory)orb.orbImplRegion).getPortal()).getServerRequestHandler();
        poaDemuxIndex = serverRequestHandler.addPOA( poaPath , this );
        theChildren.empty();
        numberOfCurrentRequests.reset();
        
        POARunnable r = new POARunnable(POARunnable.INIT);
        r.addParam( orb );
        r.addParam( this );
        r.addParam( policies );
        r.addParam( parent );
        r.addParam( manager );
        ExecuteInRunnable eir1 = new ExecuteInRunnable();
        eir1.init( r , poaMemoryArea );
        ExecuteInRunnable eir2 = new ExecuteInRunnable();
        eir2.init( eir1 , orb.orbImplRegion );
        try{
            orb.parentMemoryArea.executeInArea( eir2 );
        }catch( Exception e ){
            e.printStackTrace();
        }

        poaState = POA.CREATION_COMPLETE;
    }
    
	private POA(){
        theChildren = new Hashtable();
        theChildren.init( Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.poa.maxNumPOAs" , "1" ) ) );
        numberOfCurrentRequests = new SynchronizedInt();
        createDestroyPOAMutex = new Integer(0);

        poaName = new FString();
        poaPath = new FString();
        try{
            poaName.init( Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.poa.MaxPOANameLen" , "32" ) ) );
            poaPath.init( ( Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.poa.MaxPOAPathLen" , "255" ) ) ) );
        }catch( InstantiationException e1 ){
             ZenProperties.logger.log(
                Logger.FATAL,
                "edu.uci.ece.zen.orb.POA",
                "<init>",
                "Could not initialize POA facade due to exception: " + e1.toString()
                );
             System.exit(-1);
        }catch( IllegalAccessException e2 ){
             ZenProperties.logger.log(
                Logger.FATAL,
                "edu.uci.ece.zen.orb.POA",
                "<init>",
                "Could not initialize POA facade due to exception: " + e2.toString()
                );
             System.exit(-1);
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
    public void handleRequest(final RequestMessage sreq) {
        POARunnable r = new POARunnable( POARunnable.HANDLE_REQUEST );
        r.addParam( sreq );

        ExecuteInRunnable eir1 = new ExecuteInRunnable();
        eir1.init( r , poaMemoryArea );
        try{
            orb.orbImplRegion.executeInArea( eir1 );
        }catch( Exception e ){
            e.printStackTrace();
        }
        switch( r.exception ){
            case -1: //no exception
                break;
            //exceptions from validateProcessingState
            case 1:
                throw new org.omg.CORBA.TRANSIENT("Destruction of the POA in progress", 1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            case 2:
                throw new org.omg.CORBA.OBJ_ADAPTER("POA Manager associated with the POA is Inactive", 1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            //exceptions from POAManager.checkPOAManagerState
            case 3: //holding state
                throw new org.omg.CORBA.TRANSIENT(1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            case 4: //discarding state
                throw new org.omg.CORBA.TRANSIENT(1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            case 5:
                 throw new org.omg.CORBA.OBJ_ADAPTER("POA Manager associated with the POA is Inactive", 1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            case 6: //this.lifespanStrategy.validate
                throw new org.omg.CORBA.OBJECT_NOT_EXIST(2, org.omg.CORBA.CompletionStatus.COMPLETED_NO); 
            case 7: //other exceptions
                throw new org.omg.CORBA.TRANSIENT("Request cancelled:Executor shut down", 3, CompletionStatus.COMPLETED_NO);
        }
    }

    public org.omg.CORBA.Object servant_to_reference( final org.omg.PortableServer.Servant p_servant ) throws ServantNotActive, WrongPolicy {
        POARunnable r = new POARunnable(POARunnable.SERVANT_TO_REFERENCE);
        r.addParam( p_servant );
        r.addParam( RealtimeThread.getCurrentMemoryArea() );
        ExecuteInRunnable eir1 = new ExecuteInRunnable();
        eir1.init( r , poaMemoryArea );
        ExecuteInRunnable eir2 = new ExecuteInRunnable();
        eir2.init( eir1 , orb.orbImplRegion );
        try{
            orb.parentMemoryArea.executeInArea( eir2 );
        }catch( Exception e ){
            e.printStackTrace();
        }
        switch( r.exception ){
            case -1: //no exception
                break;
            case 1:
                throw new ServantNotActive();
            case 2:
                throw new WrongPolicy();
        }
        return (org.omg.CORBA.Object) r.retVal;
    }


    ///////////////////////////////////////////////////////////////////////////
    ////////////////////////// OTHER POA METHODS //////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    public byte[] servant_to_id(final org.omg.PortableServer.Servant p_servant) throws ServantNotActive, WrongPolicy {
        /*
        POARunnable r = new POARunnable(POARunnable.SERVANT_TO_ID);
        r.addParam( p_servant );
        r.addParam( RealtimeThread.getCurrentMemoryArea() );
        ExecuteInRunnable eir1 = new ExecuteInRunnable();
        eir1.init( poaMemoryArea , r );
        ExecuteInRunnable eir2 = new ExecuteInRunnable();
        eir2.init( orb.orbImplRegion , eir1 );
        try{
            orb.parentMemoryArea.executeInArea( eir2 );
        }catch( Exception e ){
            e.printStackTrace();
        }
        switch( r.exception ){
            case -1: //no exception
                break;
            case 1:
                throw new ServantNotActive();
            case 2:
                throw new WrongPolicy();
        }
        return (byte[])r.retVal;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT(); 
    }


    public org.omg.PortableServer.Servant reference_to_servant( final org.omg.CORBA.Object reference ) throws ObjectNotActive,WrongPolicy,WrongAdapter {
        /*
        POARunnable r = new POARunnable(POARunnable.REFERENCE_TO_SERVANT);
        r.addParam( reference );
        r.addParam( RealtimeThread.getCurrentMemoryArea() );
        ExecuteInRunnable eir1 = new ExecuteInRunnable();
        eir1.init( poaMemoryArea , r );
        ExecuteInRunnable eir2 = new ExecuteInRunnable();
        eir2.init( orb.orbImplRegion , eir1 );
        try{
            orb.parentMemoryArea.executeInArea( eir2 );
        }catch( Exception e ){
            e.printStackTrace();
        }
        switch( r.exception ){
            case -1: //no exception
                break;
            case 1:
                throw new ObjectNotActive();
            case 2:
                throw new WrongPolicy();
            case 3:
                throw new WrongAdapter();
        }
        return (Servant)r.retVal;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public byte[] reference_to_id(final org.omg.CORBA.Object reference) throws WrongAdapter,WrongPolicy {
        /*
        POARunnable r = new POARunnable(POARunnable.REFERENCE_TO_ID);
        r.addParam( reference );
        r.addParam( RealtimeThread.getCurrentMemoryArea() );
        ExecuteInRunnable eir1 = new ExecuteInRunnable();
        eir1.init( poaMemoryArea , r );
        ExecuteInRunnable eir2 = new ExecuteInRunnable();
        eir2.init( orb.orbImplRegion , eir1 );
        try{
            orb.parentMemoryArea.executeInArea( eir2 );
        }catch( Exception e ){
            e.printStackTrace();
        }
        switch( r.exception ){
            case -1: //no exception
                break;
            case 1:
                throw new WrongAdapter();
            case 2:
                throw new WrongPolicy();
        }
        return (byte[])r.retVal;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.Servant id_to_servant(final byte[] oid) throws ObjectNotActive,WrongPolicy {
        /*
        POARunnable r = new POARunnable(POARunnable.ID_TO_SERVANT);
        r.addParam( oid );
        r.addParam( RealtimeThread.getCurrentMemoryArea() );
        ExecuteInRunnable eir1 = new ExecuteInRunnable();
        eir1.init( poaMemoryArea , r );
        ExecuteInRunnable eir2 = new ExecuteInRunnable();
        eir2.init( orb.orbImplRegion , eir1 );
        try{
            orb.parentMemoryArea.executeInArea( eir2 );
        }catch( Exception e ){
            e.printStackTrace();
        }
        switch( r.exception ){
            case -1: //no exception
                break;
            case 1:
                throw new ObjectNotActive();
            case 2:
                throw new WrongPolicy();
        }
        return (Servant)r.retVal;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object id_to_reference(final byte[] oid) throws ObjectNotActive,WrongPolicy {
        /*
        POARunnable r = new POARunnable(POARunnable.ID_TO_REFERENCE);
        r.addParam( oid );
        r.addParam( RealtimeThread.getCurrentMemoryArea() );
        ExecuteInRunnable eir1 = new ExecuteInRunnable();
        eir1.init( poaMemoryArea , r );
        ExecuteInRunnable eir2 = new ExecuteInRunnable();
        eir2.init( orb.orbImplRegion , eir1 );
        try{
            orb.parentMemoryArea.executeInArea( eir2 );
        }catch( Exception e ){
            e.printStackTrace();
        }
        switch( r.exception ){
            case -1: //no exception
                break;
            case 1:
                throw new ObjectNotActive();
            case 2:
                throw new WrongPolicy();
        }
        return (org.omg.CORBA.Object)r.retVal;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
   }

   public byte[] activate_object (org.omg.PortableServer.Servant p_servant) throws ServantAlreadyActive,WrongPolicy
   {
       /*
        POARunnable r = new POARunnable(POARunnable.ACTIVATE_OBJECT);
        r.addParam( p_servant );
        r.addParam( RealtimeThread.getCurrentMemoryArea() );
        ExecuteInRunnable eir1 = new ExecuteInRunnable();
        eir1.init( poaMemoryArea , r );
        ExecuteInRunnable eir2 = new ExecuteInRunnable();
        eir2.init( orb.orbImplRegion , eir1 );
        try{
            orb.parentMemoryArea.executeInArea( eir2 );
        }catch( Exception e ){
            e.printStackTrace();
        }
        switch( r.exception ){
            case -1: //no exception
                break;
            case 1:
                throw new ServantAlreadyActive();
            case 2:
                throw new WrongPolicy();
        }
        return (byte[])r.retVal;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void activate_object_with_id(final byte[] id, final org.omg.PortableServer.Servant p_servant) throws ServantAlreadyActive,ObjectAlreadyActive,WrongPolicy {
        /*
        POARunnable r = new POARunnable(POARunnable.ACTIVATE_OBJECT_WITH_ID);
        r.addParam( id );
        r.addParam( p_servant );
        r.addParam( RealtimeThread.getCurrentMemoryArea() );
        ExecuteInRunnable eir1 = new ExecuteInRunnable();
        eir1.init( poaMemoryArea , r );
        ExecuteInRunnable eir2 = new ExecuteInRunnable();
        eir2.init( orb.orbImplRegion , eir1 );
        try{
            orb.parentMemoryArea.executeInArea( eir2 );
        }catch( Exception e ){
            e.printStackTrace();
        }
        switch( r.exception ){
            case -1: //no exception
                break;
            case 1:
                throw new ServantAlreadyActive();
            case 2:
                throw new ObjectAlreadyActive();
            case 3:
                throw new WrongPolicy();
        }
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void deactivate_object(byte[] oid) throws ObjectNotActive,WrongPolicy {
        /*
        POARunnable r = new POARunnable(POARunnable.DEACTIVATE_OBJECT);
        r.addParam( oid );
        r.addParam( RealtimeThread.getCurrentMemoryArea() );
        ExecuteInRunnable eir1 = new ExecuteInRunnable();
        eir1.init( poaMemoryArea , r );
        ExecuteInRunnable eir2 = new ExecuteInRunnable();
        eir2.init( orb.orbImplRegion , eir1 );
        try{
            orb.parentMemoryArea.executeInArea( eir2 );
        }catch( Exception e ){
            e.printStackTrace();
        }
        switch( r.exception ){
            case -1: //no exception
                break;
            case 1:
                throw new ObjectNotActive();
            case 2:
                throw new WrongPolicy();
        }a
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.POA create_POA(String adapter_name, org.omg.PortableServer.POAManager a_POAManager, org.omg.CORBA.Policy[] policies)
            throws org.omg.PortableServer.POAPackage.AdapterAlreadyExists, org.omg.PortableServer.POAPackage.InvalidPolicy{
        /*

        synchronized (createDestroyPOAMutex) {
            edu.uci.ece.zen.poa.POA child = (edu.uci.ece.zen.poa.POA) this.theChildren.get(adapter_name);
            if (child != null) {
                if (!(child.getState() == POA.DESTRUCTION_APPARANT)) {
                    throw new org.omg.PortableServer.POAPackage.AdapterAlreadyExists();
                }

                if ( this.disableCreatePOA && !poaState == POA.DESTRUCTION_APPARANT ) {
                    throw new BAD_INV_ORDER(17, CompletionStatus.COMPLETED_NO);
                } else {
                    this.disableCreatePOA = false;
                }
            }
            this.poaState = POA.CREATING;

            POA newPoa = POA.instance();
            newPoa.init( orb , adapter_name , policies , this , a_POAManager );
            theChildren.put(adapter_name, child);
            poaState = POA.CREATION_COMPLETE;
            return child;
        }
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.POA find_POA(final java.lang.String adapter_name,final boolean activate_it) throws AdapterNonExistent {
        /*
        Object poa = theChildren.get(adapter_name);
        if( poa != null )
            return (POA) poa;

        if (activate_it)
        {   
            boolean temp = false;
		    try{
                temp = the_activator().unknown_adapter(this, adapter_name);
            }
		    catch ( Exception ex ){
                throw new org.omg.CORBA.OBJ_ADAPTER("AdapterActivator failed to activate POA",1,CompletionStatus.COMPLETED_NO);
            }
		    if (temp)
		    	return (POA) theChildren.get(adapter_name);
		}
        throw new org.omg.PortableServer.POAPackage.AdapterNonExistent()
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void destroy(final boolean etherealize_objects, final boolean wait_for_completion) {
        /*
        if (wait_for_completion) {
            ThreadSpecificPOACurrent current = POATSS.tss.getCurrent();

            if (current != null && ((edu.uci.ece.zen.poa.POA) current.getPOA()).getORB() == this.orb) {
                throw new org.omg.CORBA.BAD_INV_ORDER("The destroy is unsuccessful as the Current" + 
                        "thread is in the InvocationContext", 3, CompletionStatus.COMPLETED_NO);
            }
        }

        synchronized (createDestroyPOAMutex) {
            this.disableCreatePOA = true;

            if (((POAManager) poaManager).isInActive()) {
                this.processingState = POA.INACTIVE;
            } else {
                this.processingState = POA.DISCARDING;
            }

            // if called multiple_times the first time is the etherealize param
            if (poaState < POA.DESTRUCTION_IN_PROGRESS) {
                this.etherealize = etherealize_objects;
            }

            this.poaState = POA.DESTRUCTION_IN_PROGRESS;

            Object[] e = this.the_children.getObjects();
            for (int i = 0; i < e.length; i++) {
                if( e[i] != null )
                    ((POA)e[i]).destroy(etherealize, wait_for_completion);
            }

            // Remove the POA from the POAServerRequestHandler and also from the
            // list of childrenPOA maintained the by Parent
            if (this.parent != null) {
                POARunnable r = new POARunnable(POARunnable.REMOVE_FROM_PARENT);
                r.addParam( poa );
                ExecuteInRunnable eir1 = new ExecuteInRunnable();
                eir1.init( poaMemoryArea , r );
                ExecuteInRunnable eir2 = new ExecuteInRunnable();
                eir2.init( orb.orbImplRegion , eir1 );
                try{
                    orb.parentMemoryArea.executeInArea( eir2 );
                }catch( Exception e ){
                    e.printStackTrace();
                }
            }

            this.serverRequestHandler.remove(this);
            ((edu.uci.ece.zen.poa.POAManager) this.poaManager).unRegister(this);

            // Clear the list of children for this POA
            this.theChildren.empty();

            // Wait for the Apparent Destruction of the POA
            if (this.numberOfCurrentRequests.get() != 0) {
                this.numberOfCurrentRequests.waitForCompletion();
            }

            // At this point the Apparent Destruction of the POA has occured.
            // Ethrealize the servants for each activeObject in the AOM
            this.poaState = POA.DESTRUCTION_APPARANT;
 
            POARunnable r = new POARunnable(POARunnable.DESTROY);
            r.addParam( poa );
            ExecuteInRunnable eir1 = new ExecuteInRunnable();
            eir1.init( poaMemoryArea , r );
            ExecuteInRunnable eir2 = new ExecuteInRunnable();
            eir2.init( orb.orbImplRegion , eir1 );
            try{
                orb.parentMemoryArea.executeInArea( eir2 );
            }catch( Exception e ){
                e.printStackTrace();
            }

            // At this point the destruction of the POA has been completed
            // notify any threads that could be waiting for this POA to be
            // destroyed
            this.poaState = POA.DESTRUCTION_COMPLETE;
            ORB.freeScopedRegion( this.poaMemoryArea );
            poaManager.free();
            this.createDestroyPOAMutex.notifyAll();
        }
        free();
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public ThreadPolicy create_thread_policy( final org.omg.PortableServer.ThreadPolicyValue value ) {
        /*
        return this.serverRequestHandler.create_thread_policy(value);
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.LifespanPolicy create_lifespan_policy( final org.omg.PortableServer.LifespanPolicyValue value ) {
        /*
        return this.serverRequestHandler.create_lifespan_policy(value);
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.IdUniquenessPolicy create_id_uniqueness_policy( final org.omg.PortableServer.IdUniquenessPolicyValue value ) {
        /*
        return this.serverRequestHandler.create_id_uniqueness_policy(value);
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.IdAssignmentPolicy create_id_assignment_policy( final org.omg.PortableServer.IdAssignmentPolicyValue value ) {
        /*
        return this.serverRequestHandler.create_id_assignment_policy(value);
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.ImplicitActivationPolicy create_implicit_activation_policy( final org.omg.PortableServer.ImplicitActivationPolicyValue value ) {
        /*return this.serverRequestHandler.create_implicit_activation_policy(value);*/
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.ServantRetentionPolicy create_servant_retention_policy( final org.omg.PortableServer.ServantRetentionPolicyValue value ) {
        /*return this.serverRequestHandler.create_servant_retention_policy(value);*/
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.RequestProcessingPolicy create_request_processing_policy( final org.omg.PortableServer.RequestProcessingPolicyValue value ) {
        /*return this.serverRequestHandler.create_request_processing_policy(value);*/
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public java.lang.String the_name() {
        return new String( poaName.getTrimData() );
    }

    public java.lang.String path_name() {
        return new String( poaPath.getTrimData() );
    }

    public org.omg.PortableServer.POA the_parent() {
        return parent;
    }

    public byte[] id() {
        POARunnable r = new POARunnable(POARunnable.ID);
        r.addParam(RealtimeThread.getCurrentMemoryArea());
        ExecuteInRunnable eir1 = new ExecuteInRunnable();
        eir1.init( r , poaMemoryArea );
        ExecuteInRunnable eir2 = new ExecuteInRunnable();
        eir2.init( eir1 , orb.orbImplRegion );
        try{
            orb.parentMemoryArea.executeInArea( eir2 );
        }catch( Exception e ){
            e.printStackTrace();
        }
        return (byte[]) r.retVal;
    }

    public org.omg.PortableServer.POA[] the_children() {
        /*
        Object[] objs = theChildren.getObjects();
        POA[] poas = new POA[objs.length];
        for( int i=0;i<objs.length;i++ )
            poas[i] = (POA) objs[i];
        return poas;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.POAManager the_POAManager() {
        /*
        return this.poaManager;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.AdapterActivator the_activator() {
        /*
        return this.adapterActivator;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void the_activator( final org.omg.PortableServer.AdapterActivator the_activator ) {
        /*
        this.adapterActivator = the_activator;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public ServantManager get_servant_manager() throws WrongPolicy {
        /*
        POARunnable r = getPOARunnable();
        r.init( POARunnable.GET_SERVANT_MANAGER , null , null , null , null , null , null , null );
        poaMemoryArea.enter(r);
        Exception ex = r.getException();
        org.omg.PortableServer.ServantManager retVal = (org.omg.PortableServer.ServantManager) r.getRetVal();
        returnPOARunnable( r );
        if( ex != null )
            throw ex;
        return retVal;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void set_servant_manager( final org.omg.PortableServer.ServantManager imgr ) throws org.omg.PortableServer.POAPackage.WrongPolicy {
        /*
        POARunnable r = getPOARunnable();
        r.init( POARunnable.SET_SERVANT_MANAGER , imgr , null , null , null , null , null , null );
        poaMemoryArea.enter(r);
        Exception ex = r.getException();
        returnPOARunnable( r );
        if( ex != null )
            throw ex;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.PortableServer.Servant get_servant() throws NoServant,WrongPolicy {
        /*
        POARunnable r = getPOARunnable();
        r.init( POARunnable.GET_SERVANT , null , null , null , null , null , null , null );
        poaMemoryArea.getCurrentMemoryArea();
        Exception ex = r.getException();
        org.omg.PortableServer.Servant retVal = (org.omg.PortableServer.Servant) r.getRetVal();
        returnPOARunnable( r );
        if( ex != null )
            throw ex;
        return retVal;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public void set_servant(final org.omg.PortableServer.Servant p_servant) throws WrongPolicy {
        /*
        POARunnable r = getPOARunnable();
        r.init( POARunnable.SET_SERVANT , p_servant , null , null , null , null , null , null );
        poaMemoryArea.getCurrentMemoryArea();
        Exception ex = r.getException();
        returnPOARunnable( r );
        if( ex != null )
            throw ex;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object create_reference(final String intf) throws org.omg.PortableServer.POAPackage.WrongPolicy {
        /*
        org.omg.CORBA.Object retVal;
        POARunnable r = getPOARunnable();
        r.init( POARunnable.CREATE_REFERENCE , intf , curMemArea , null , null , null , null , null );
        poaMemoryArea.getCurrentMemoryArea();
        Exception ex = r.getException();
        retVal = r.getRetVal();
        returnPOARunnable( r );
        if( ex != null )
            throw ex;
        return retVal;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Object create_reference_with_id(final byte[] oid,final String intf) {
        /*
        org.omg.CORBA.Object retVal;
        POARunnable r = getPOARunnable();
        r.init( POARunnable.CREATE_REFERENCE_WITH_ID , intf , oid , curMemArea , null , null , null , null );
        poaMemoryArea.getCurrentMemoryArea();
        Exception ex = r.getException();
        retVal = r.getRetVal();
        returnPOARunnable( r );
        if( ex != null )
            throw ex;
        return retVal;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public org.omg.CORBA.Policy[] policy_list() {
        /*
        org.omg.CORBA.Policy[] retVal;
        POARunnable r = getPOARunnable();
        r.init( POARunnable.GET_POLICY_LIST , RealtimeThread.getCurrentMemoryArea() , null , null , null , null , null , null );
        poaMemoryArea.getCurrentMemoryArea();
        Exception ex = r.getException();
        retVal = r.getRetVal();
        returnPOARunnable( r );
        if( ex != null )
            throw ex;
        return retVal;
        */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    protected void waitForCompletion() {
        numberOfCurrentRequests.waitForCompletion();
    }

    public final edu.uci.ece.zen.orb.ORB getORB() {
        return this.orb;
    }

    POA getChildPOA(String poaName) {
        /*
        POA child = (POA) this.theChildren.get(poaName);

        if (child == null || child.getStatus() == POA.DESTRUCTION_APPARANT) {
            if (adapterActivator == null) {
                if( POA.objNotExistException == null ){
                    POA.objNotExistException = ImmortalMemory.instance().newInstance( OBJECT_NOT_EXIST.class );
                }
                throw POA.objNotExistException;
            }
            if (((POAManager) poaManager).isDiscarding()) {
                if( POA.parentDiscardTransientException == null ){
                    POA.parentDiscardTransientException = ImmortalMemory.instance().newInstance( ParentDiscardTRANSIENT.class );
                }
                throw parentDiscardTransientException;
            }

            if (((POAManager) poaManager).isInActive()) {
                throw new org.omg.CORBA.OBJ_ADAPTER("Parent POA inactive");
            }

            // one also needs to check for holding state and take action
            if (((POAManager) poaManager).isHolding()) {
                throw new org.omg.CORBA.TRANSIENT("Parent POA in holding state:Cannot Activate Child POA",
                        1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            }

            // The POA is active.. Serailize the calls if Single threaded
            childMemory.enter(new Runnable(){ public void run(){
															(((POAImpl)childMemory.getPortal()).getThreadPolicyStrategy()).enter(); 
					}
				});
            //this.threadPolicyStrategy.enter();
            boolean success = the_activator().unknown_adapter(this, poaName);
            childMemory.enter(new Runnable() { public void run(){
            										( (POAImpl)childMemory.getPortal()).getThreadPolicyStrategy().exit();}
                                                                                      });


            //this.threadPolicyStrategy.exit();

            if (success) {
                child = (POA) this.theChildren.get(poaName);
                if (child == null) {
                    throw new org.omg.CORBA.INTERNAL("unknown_adapter operation",
                            0, CompletionStatus.COMPLETED_NO);
                }
            }
            throw new org.omg.CORBA.OBJECT_NOT_EXIST("POA activation failed");
        }

        return child;
        */throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}

