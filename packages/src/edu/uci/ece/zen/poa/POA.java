package edu.uci.ece.zen.poa;

import javax.realtime.*;
import org.omg.CORBA.CompletionStatus;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.utils.*;
import org.omg.PortableServer.POAPackage.*;

public class POA extends org.omg.CORBA.LocalObject implements org.omg.PortableServer.POA {

    private static Queue unusedFacades;
    private static Queue unusedPOARunnables;
    private static ImmortalMemory imm;
    private ORB orb;
    private POA parent;
    private ScopedMemory poaMemoryArea;
    private POAManager poaManager;
    private ServerRequestHandler serverRequestHandler;
    private Hashtable theChildren;
    private SynchronizedInt numberOfCurrentRequests;
    private int poaState;
    private int processingState = POA.ACTIVE;
    private AdapterActivator adapterActivator;

    /* Mutexes POA and varable specific to the create and destroy ops */
    private Object createDestroyPOAMutex;
    private boolean disableCreatePOA = false;
    private boolean etherealize;

    /* Constants for the POA Class */
    private static final int CREATING = 0;
    private static final int CREATION_COMPLETE = 1;
    private static final int DESTRUCTION_IN_PROGRESS = 3;
    private static final int DESTRUCTION_APPARANT = 4;
    private static final int DESTRUCTION_COMPLETE = 5;

    /* Request Processing States */
    private static final int ACTIVE = 6;
    private static final int DISCARDING = 7;
    private static final int INACTIVE = 8;

    private static AdapterAlreadyExists adapterAlreadyExistsException;
    private static AdapterNonExistent adapterNonExistExeption;
    private static OBJ_ADAPTER obj_adapterException_1;
    private static BAD_INV_ORDER badInvOrderException_17;
    private static BAD_INV_ORDER badInvOrderException_3;
    private static OBJECT_NOT_EXIST objNotExistException;
    private static TRANSIENT parentDiscardTransientException;
    private static 

    static{
        try{
            imm = ImmortalMemory.instance();
            //Set up POA Facades
            int numFacades = Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.poa.maxNumPOAs" , "1" ) );
            unusedFacades = (Queue) imm.newInstance( Queue.class );
            unusedPOARunnables = (Queue) imm.newInstance( Queue.class );
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
        queue.enqueue( poa );
    }

    private static POARunnable getPOARunnable(){
        Object poaRunnable = unusedPOARunnables.dequeue();
        if( poaRunnable == null )
            return (POARunnable) ImmortalMemory.instance().newInstance( POARunnable.class );
        return (POARunnable) poaRunnable;
    }

    private static void returnPOARunnable( POARunnable r ){
        unusedPOARunnables.enqueue( r );
    }

    public void free(){
        POA.release( this );
    }

    private final String rootPoaString = "RootPOA";
    public void initAsRootPOA( final edu.uci.ece.zen.orb.ORB orb ){
        this.init( orb , rootPoaString , rootPoaString , null , null , null , null , null );
    }

    public void init( final edu.uci.ece.zen.orb.ORB orb , String poaName , String poaPath , org.omg.CORBA.Policy[] policies ,
            org.omg.PortableServer.POA parent, org.omg.PortableServer.POAManager manager ){
        poaState = POA.CREATING;
        this.orb = orb;
        this.poaMemoryArea = ORB.getScopedRegion();
        if( manager == null )
            manager = POAManager.instance();
        this.poaManager = manager;
        poaManager.register( this );
        serverRequestHandler = orb.getServerRequestHandler();
        serverRequestHandler.addPOA( poaPath , this );
        theChildren.empty();
        numberOfCurrentRequests.reset();
        
        POARunnable r = POA.getPOARunnable();
        r.init( POARunnable.INIT , orb , this , poaName , poaPath , policies , parent , manager);
        poaMemoryArea.enter( r );
        returnPOARunnable( r );
        poaState = POA.CREATION_COMPLETE;
    }
    
	private POA(){
        theChildren = new Hashtable( Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.poa.maxNumPOAs" , "1" ) ) );
        numberOfCurrentRequests = new SynchronizedInt();
        createDestroyPOAMutex = new Integer(0);
    }

    public void handleRequest(final ServerRequest req) {
        POARunnable r = POA.getPOARunnable();
        r.init( POARunnable.HANDLE_REQUEST , req , null , null , null , null , null , null);
        poaMemoryArea.enter( r );
        returnPOARunnable( r );
    }

    public byte[] servant_to_id(final org.omg.PortableServer.Servant p_servant) throws ServantNotActive, WrongPolicy {
        byte[] retVal;
        POARunnable r = POA.getPOARunnable();
        r.init( POARunnable.SERVANT_TO_ID , p_servant , RealtimeThread.getCurrentMemoryArea(), null , null , null , null , null);
        poaMemoryArea.enter( r );
        Exception ex = r.getException();
        retVal = (byte[]) r.getRetVal();
        returnPOARunnable( r );
        if( ex != null ){
            throw ex;
        return retVal;
    }

    public org.omg.CORBA.Object servant_to_reference( final org.omg.PortableServer.Servant p_servant ) throws ServantNotActive, WrongPolicy {
        org.omg.CORBA.Object retVal;
        POARunnable r = POA.getPOARunnable();
        r.init( POARunnable.SERVANT_TO_REFERENCE , p_servant , RealtimeThread.getCurrentMemoryArea()  , null , null , null , null , null);
        poaMemoryArea.enter( r );
        Exception ex = r.getException();
        retVal = (org.omg.CORBA.Object) r.getRetVal();
        returnPOARunnable( r );
        if( ex != null ){
            throw ex;
        return retVal;
    }

    public org.omg.PortableServer.Servant reference_to_servant( final org.omg.CORBA.Object reference ) throws ObjectNotActive,WrongPolicy,WrongAdapter {
        org.omg.PortableServer.Servant retVal;
        POARunnable r = POA.getPOARunnable();
        r.init( POARunnable.REFERENCE_TO_SERVANT , reference , null , null , null , null , null , null);
        poaMemoryArea.enter( r );
        Exception ex = r.getException();
        retVal = (org.omg.PortableServer.Servant) r.getRetVal();
        returnPOARunnable( r );
        if( ex != null ){
            throw ex;
        return retVal;
    }

    public byte[] reference_to_id(final org.omg.CORBA.Object reference) throws WrongAdapter,WrongPolicy {
        byte[] retVal;
        POARunnable r = POA.getPOARunnable();
        r.init( POARunnable.REFERENCE_TO_ID , reference , RealtimeThread.getCurrentMemoryArea(), null , null , null , null , null);
        poaMemoryArea.enter( r );
        Exception ex = r.getException();
        retVal = (byte[]) r.getRetVal();
        returnPOARunnable( r );
        if( ex != null ){
            throw ex;
        return retVal;
    }

    public org.omg.PortableServer.Servant id_to_servant(final byte[] oid) throws ObjectNotActive,WrongPolicy {
        org.omg.PortableServer.Servant retVal;
        POARunnable r = POA.getPOARunnable();
        r.init( POARunnable.ID_TO_SERVANT , oid , null , null , null , null , null , null);
        poaMemoryArea.enter( r );
        Exception ex = r.getException();
        retVal = (org.omg.PortableServer.Servant) r.getRetVal();
        returnPOARunnable( r );
        if( ex != null ){
            throw ex;
        return retVal;
    }

    public org.omg.CORBA.Object id_to_reference(final byte[] oid) throws ObjectNotActive,WrongPolicy {
        org.omg.CORBA.Object retVal;
        POARunnable r = POA.getPOARunnable();
        r.init( POARunnable.ID_TO_REFERENCE , oid , RealtimeThread.getCurrentMemoryArea()  , null , null , null , null , null);
        poaMemoryArea.enter( r );
        Exception ex = r.getException();
        retVal = (org.omg.CORBA.Object) r.getRetVal();
        returnPOARunnable( r );
        if( ex != null ){
            throw ex;
        return retVal;
   }

   public byte[] activate_object (org.omg.PortableServer.Servant p_servant) throws ServantAlreadyActive,WrongPolicy
   {
        org.omg.CORBA.Object retVal;
        POARunnable r = POA.getPOARunnable();
        r.init( POARunnable.ACTIVATE_OBJECT , p_servant , RealtimeThread.getCurrentMemoryArea()  , null , null , null , null , null);
        poaMemoryArea.enter( r );
        Exception ex = r.getException();
        retVal = (org.omg.CORBA.Object) r.getRetVal();
        returnPOARunnable( r );
        if( ex != null ){
            throw ex;
        return retVal;
    }

    public void activate_object_with_id(final byte[] id, final org.omg.PortableServer.Servant p_servant) throws ServantAlreadyActive,ObjectAlreadyActive,WrongPolicy {
        POARunnable r = POA.getPOARunnable();
        r.init( POARunnable.ACTIVATE_OBJECT_WITH_ID , id , p_servant , null , null , null , null , null);
        poaMemoryArea.enter( r );
        Exception ex = r.getException();
        returnPOARunnable( r );
        if( ex != null ){
            throw ex;
   }

    public void deactivate_object(byte[] oid) throws ObjectNotActive,WrongPolicy {
        POARunnable r = POA.getPOARunnable();
        r.init( POARunnable.DEACTIVATE_OBJECT , oid , null , null , null , null , null , null);
        poaMemoryArea.enter( r );
        Exception ex = r.getException();
        returnPOARunnable( r );
    }

    public org.omg.PortableServer.POA create_POA( java.lang.String adapter_name, POAManager a_POAManager, org.omg.CORBA.Policy[] policies)
            throws AdapterAlreadyExists,InvalidPolicy {
        POA poa = POA.instance();
        ScopedMemory procMemory = ORB.getScopedRegion();
        
        synchronized (createDestroyPOAMutex) {
            edu.uci.ece.zen.poa.POA child = (edu.uci.ece.zen.poa.POA) this.theChildren.get(adapter_name);
            if (child != null) {
                if (!(child.getState() == POA.DESTRUCTION_APPARANT)) {
                    if( POA.adapterAlreadyExistsException == null )
                        POA.adapterAlreadyExistsException = ImmortalMemory.instance().newInstance( AdapterAlreadyExists.class );
                    throw POA.adapterAlreadyExistsException;
                }

                if ( this.disableCreatePOA && !poaState == POA.DESTRUCTION_APPARANT ) {
                    if( POA.badInvOrderException_17 == null ){
                        POA.badInvOrderException_17 = new BAD_INV_ORDER(17, CompletionStatus.COMPLETED_NO);
                        POA.badInvOrderException_17.init( 17 , CompletionStatus.COMPLETED_NO );
                    }
                    throw POA.badInvOrderException_17;
                } else {
                    this.disableCreatePOA = false;
                }
            }
            this.poaState = POA.CREATING;

            POARunnable r = POA.getPOARunnable();
            r.init( POARunnable.CREATE_POA_STEP_1 , orb , poa ,  adapter_name , a_POAManager , policies , null , null);
            poaMemoryArea.enter( r );
            Exception ex = r.getException();
            returnPOARunnable( r );
            if( ex != null )
                throw ex;
            theChildren.put(adapter_name, child);
            poaState = POA.CREATION_COMPLETE;
            return child;
        }
    }

    public org.omg.PortableServer.POA find_POA(final java.lang.String adapter_name,final boolean activate_it) throws AdapterNonExistent {
        Object poa = theChildren.get(adapter_name);
        if( poa != null )
            return (POA) poa;

        if (theChildren.get(adapter_name) !) {
            return (org.omg.PortableServer.POA)
                    this.theChildren.get(adapter_name);
        }

        if (activate_it)
        {   
            boolean temp = false;
		    try{
                temp = the_activator().unknown_adapter(this, adapter_name);
            }
		    catch ( Exception ex ){
                if( obj_adapterException_1 == null ){
                    obj_adapterException_1 = ImmortalMemory.instance().newInstance( OBJ_ADAPTER.class );
                    obj_adapterException_1.init(1);
                }
                throw obj_adapterException_1;
            }
		    if (temp)
		    	return (POA) theChildren.get(adapter_name);
		}

        if( adapterNonExistExeption == null )
            adapterNonExistExeption = ImmortalMemory.instance().newInstance( AdapterNonExistent.class );
        throw adapterNonExistExeption;
    }

    public void destroy(final boolean etherealize_objects, final boolean wait_for_completion) {
        if (wait_for_completion) {
            ThreadSpecificPOACurrent current = POATSS.tss.getCurrent();

            if (current != null && ((edu.uci.ece.zen.poa.POA) current.getPOA()).getORB() == this.orb) {
                if( badInvOrderException_3 == null ){
                    badInvOrderException_3 = ImmortalMemory.instance().newInstance( BAD_INV_ORDER.class );
                    badInvOrderException_3.init(3);
                }
                throw badInvOrderException_3;
                /*
                throw new org.omg.CORBA.BAD_INV_ORDER("The destroy is unsuccessful as the Current"
                        + "thread is in the InvocationContext",
                        3,
                        CompletionStatus.COMPLETED_NO);
                */
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
                POARunnable r = getPOARunnable();
                r.init( POARunnable.REMOVE_FROM_PARENT , poa , null , null , null , null , null , null );
                poaMemoryArea.enter( r );
                returnPOARunnable( r );
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
            
            POARunnable r = getPOARunnable();
            r.init( POARunnable.DESTROY , poa , null , null , null , null , null , null );
            poaMemoryArea.enter( r );
            returnPOARunnable( r );

            // At this point the destruction of the POA has been completed
            // notify any threads that could be waiting for this POA to be
            // destroyed
            this.poaState = POA.DESTRUCTION_COMPLETE;
            ORB.freeScopedRegion( this.poaMemoryArea );
            poaManager.free();
            this.createDestroyPOAMutex.notifyAll();
        }
        free();
    }

    public org.omg.PortableServer.ThreadPolicy create_thread_policy( final org.omg.PortableServer.ThreadPolicyValue value ) {
        return this.serverRequestHandler.create_thread_policy(value);
    }

    public org.omg.PortableServer.LifespanPolicy create_lifespan_policy( final org.omg.PortableServer.LifespanPolicyValue value ) {
        return this.serverRequestHandler.create_lifespan_policy(value);
    }

    public org.omg.PortableServer.IdUniquenessPolicy create_id_uniqueness_policy( final org.omg.PortableServer.IdUniquenessPolicyValue value ) {
        return this.serverRequestHandler.create_id_uniqueness_policy(value);
    }

    public org.omg.PortableServer.IdAssignmentPolicy create_id_assignment_policy( final org.omg.PortableServer.IdAssignmentPolicyValue value ) {
        return this.serverRequestHandler.create_id_assignment_policy(value);
    }

    public org.omg.PortableServer.ImplicitActivationPolicy create_implicit_activation_policy( final org.omg.PortableServer.ImplicitActivationPolicyValue value ) {
        return this.serverRequestHandler.create_implicit_activation_policy(value);
    }

    public org.omg.PortableServer.ServantRetentionPolicy create_servant_retention_policy( final org.omg.PortableServer.ServantRetentionPolicyValue value ) {
        return this.serverRequestHandler.create_servant_retention_policy(value);
    }

    public org.omg.PortableServer.RequestProcessingPolicy create_request_processing_policy( final org.omg.PortableServer.RequestProcessingPolicyValue value ) {
        return this.serverRequestHandler.create_request_processing_policy(value);
    }

    public java.lang.String the_name() {
        POARunnable r = getPOARunnable();
        r.init( POARunnable.GET_NAME , RealtimeThread.getCurrentMemoryArea() , null , null , null , null , null , null );
        poaMemoryArea.enter( r );
        String retVal = (String) r.getRetVal();
        returnPOARunnable( r );
        return retVal;
    }

    public java.lang.String path_name() {
        POARunnable r = getPOARunnable();
        r.init( POARunnable.GET_PATH , RealtimeThread.getCurrentMemoryArea() , null , null , null , null , null , null );
        poaMemoryArea.enter( r );
        String retVal = (String) r.getRetVal();
        returnPOARunnable( r );
        return retVal;
    }

    public org.omg.PortableServer.POA the_parent() {
        return parent;
    }

    public byte[] id() {
        POARunnable r = getPOARunnable();
        r.init( POARunnable.ID , RealtimeThread.getCurrentMemoryArea() , null , null , null , null , null , null );
        poaMemoryArea.enter( r );
        byte[] retVal = (byte[]) r.getRetVal();
        returnPOARunnable( r );
        return retVal;
    }

    public org.omg.PortableServer.POA[] the_children() {
        POARunnable r = getPOARunnable();
        r.init( POARunnable.THE_CHILDREN , poa , null , null , null , null , null , null );
        RealtimeThread.enter(r);
        org.omg.PortableServer.POA[] retVal = (org.omg.PortableServer.POA[]) r.getRetVal();
        returnPOARunnable( r );
        return retVal;
    }

    public org.omg.PortableServer.POAManager the_POAManager() {
        return this.poaManager;
    }

    public org.omg.PortableServer.AdapterActivator the_activator() {
        return this.adapterActivator;
    }

    public void the_activator( final org.omg.PortableServer.AdapterActivator the_activator ) {
        this.adapterActivator = the_activator;
    }

    public org.omg.PortableServer.ServantManager get_servant_manager() throws org.omg.PortableServer.POAPackage.WrongPolicy {
        POARunnable r = getPOARunnable();
        r.init( POARunnable.GET_SERVANT_MANAGER , null , null , null , null , null , null , null );
        poaMemoryArea.enter(r);
        Exception ex = r.getException();
        org.omg.PortableServer.ServantManager retVal = (org.omg.PortableServer.ServantManager) r.getRetVal();
        returnPOARunnable( r );
        if( ex != null )
            throw ex;
        return retVal;
    }

    public void set_servant_manager( final org.omg.PortableServer.ServantManager imgr ) throws org.omg.PortableServer.POAPackage.WrongPolicy {
        POARunnable r = getPOARunnable();
        r.init( POARunnable.SET_SERVANT_MANAGER , imgr , null , null , null , null , null , null );
        poaMemoryArea.enter(r);
        Exception ex = r.getException();
        returnPOARunnable( r );
        if( ex != null )
            throw ex;
    }

    public org.omg.PortableServer.Servant get_servant() throws NoServant,WrongPolicy {
        POARunnable r = getPOARunnable();
        r.init( POARunnable.GET_SERVANT , null , null , null , null , null , null , null );
        poaMemoryArea.getCurrentMemoryArea();
        Exception ex = r.getException();
        org.omg.PortableServer.Servant retVal = (org.omg.PortableServer.Servant) r.getRetVal();
        returnPOARunnable( r );
        if( ex != null )
            throw ex;
        return retVal;
    }

    public void set_servant(final org.omg.PortableServer.Servant p_servant) throws WrongPolicy {
        POARunnable r = getPOARunnable();
        r.init( POARunnable.SET_SERVANT , p_servant , null , null , null , null , null , null );
        poaMemoryArea.getCurrentMemoryArea();
        Exception ex = r.getException();
        returnPOARunnable( r );
        if( ex != null )
            throw ex;
    }

    public org.omg.CORBA.Object create_reference(final String intf) throws org.omg.PortableServer.POAPackage.WrongPolicy {
        org.omg.CORBA.Object retVal;
        POARunnable r = getPOARunnable();
        r.init( POARunnable.CREATE_REFERENCE , intf , null , null , null , null , null , null );
        poaMemoryArea.getCurrentMemoryArea();
        Exception ex = r.getException();
        retVal = r.getRetVal();
        returnPOARunnable( r );
        if( ex != null )
            throw ex;
        return retVal;
    }

    public org.omg.CORBA.Object create_reference_with_id(final byte[] oid,final String intf) {
        org.omg.CORBA.Object retVal;
        POARunnable r = getPOARunnable();
        r.init( POARunnable.CREATE_REFERENCE_WITH_ID , intf , oid , null , null , null , null , null );
        poaMemoryArea.getCurrentMemoryArea();
        Exception ex = r.getException();
        retVal = r.getRetVal();
        returnPOARunnable( r );
        if( ex != null )
            throw ex;
        return retVal;
    }

    public org.omg.CORBA.Policy[] policy_list() {
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
    }

    protected void waitForCompletion() {
        numberOfCurrentRequests.waitForCompletion();
    }

    public final edu.uci.ece.zen.orb.ORB getORB() {
        return this.orb;
    }

    POA getChildPOA(String poaName) {
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
    }

    protected void validateProcessingState() {
        switch (this.processingState) {
            case POA.DISCARDING:
            throw new org.omg.CORBA.TRANSIENT("Destruction of the POA in progress",
                    1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
            case POA.INACTIVE:
            throw new org.omg.CORBA.OBJ_ADAPTER("POA Manager associated with the POA is Inactive",
                    1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }
    }
}

