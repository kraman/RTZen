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
    public  POA                                     self;
    private java.util.Hashtable                     theChildren;
    private org.omg.PortableServer.AdapterActivator adapterActivator;
    private org.omg.PortableServer.ServantManager   theServantManager;
    private org.omg.PortableServer.Servant          theServant = null;
    private POAServerRequestHandler                 serverRequestHandler;
    private POAImplRunnable                         poaImplRunnable;

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
	private Runnable cachedRunnable;

    POAManager manager;
    int tpId;

    public ThreadLocal poaCurrent;

    // --- POA Cached Objects ---
    Queue poaHashMapQueue;
    Queue poaFStringQueue;
    Queue poaIntHolderQueue;

    public POAImpl(){
        poaHashMapQueue = new Queue();
        poaFStringQueue = new Queue();
        poaIntHolderQueue = new Queue();
    }

    public POAHashMap getPOAHashMap(){
        POAHashMap ret = (POAHashMap) poaHashMapQueue.dequeue();
        if( ret == null ){
            ret = new POAHashMap();
        }
        return ret;
    }

    public  void retPOAHashMap( POAHashMap map ){
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
        if(ZenProperties.devDbg) System.out.println( "POAImpl init 1" );
        this.orb = orb;
        this.self = self;
        this.parent = parent;
        this.manager = manager;
        this.tpId = 0;
        this.poaCurrent = new ThreadLocal();
        if(ZenProperties.devDbg) System.out.println( "POAImpl init 2" );

        try{
            serverRequestHandler = (POAServerRequestHandler) ((ORBImpl)((ScopedMemory)orb.orbImplRegion).getPortal()).getServerRequestHandler();
            if( serverRequestHandler == null ){
                serverRequestHandler =  (POAServerRequestHandler) orb.orbImplRegion.newInstance( POAServerRequestHandler.class  );
                ((ORBImpl)((ScopedMemory)orb.orbImplRegion).getPortal()).setServerRequestHandler( serverRequestHandler );
            }
        }catch( Exception e1 ){
            e1.printStackTrace();
        }
        if(ZenProperties.devDbg) System.out.println( "POAImpl init 3" );
        self.poaDemuxIndex = serverRequestHandler.addPOA( self.poaPath , self );
        self.poaDemuxCount = serverRequestHandler.getPOAGenCount( self.poaDemuxIndex );
        if(ZenProperties.devDbg) System.out.println( "POAImpl init 4" );


        //make a local copy of the policies
        if( policies != null )
            this.policyList = new Policy[policies.length];
        else
            this.policyList = new Policy[0];

        for( int i=0;i<policyList.length;i++ )
            this.policyList[i] = policies[i].copy();
        if(ZenProperties.devDbg) System.out.println( "POAImpl init 5" );

        //init the stratergies
        IntHolder ih = getIntHolder();
        this.threadPolicyStrategy = edu.uci.ece.zen.poa.mechanism.ThreadPolicyStrategy.init(policyList,ih);
        if( ih.value != 0 ){ retIntHolder(ih); prun.exception = POARunnable.InvalidPolicyException; return; }
        if(ZenProperties.devDbg) System.out.println( "POAImpl init 6" );

        this.idAssignmentStrategy = edu.uci.ece.zen.poa.mechanism.IdAssignmentStrategy.init(policyList,ih);
        if( ih.value != 0 ){ retIntHolder(ih); prun.exception = POARunnable.InvalidPolicyException; return; }
        if(ZenProperties.devDbg) System.out.println( "POAImpl init 7" );

        this.uniquenessStrategy = edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy.init(policyList,ih);
        if( ih.value != 0 ){ retIntHolder(ih); prun.exception = POARunnable.InvalidPolicyException; return; }
        if(ZenProperties.devDbg) System.out.println( "POAImpl init 8" );

        this.retentionStrategy = edu.uci.ece.zen.poa.mechanism.ServantRetentionStrategy.init(policyList, this.uniquenessStrategy,ih);
        if( ih.value != 0 ){ retIntHolder(ih); prun.exception = POARunnable.InvalidPolicyException; return; }
        if(ZenProperties.devDbg) System.out.println( "POAImpl init 9" );

        this.lifespanStrategy = edu.uci.ece.zen.poa.mechanism.LifespanStrategy.init(this.policyList,ih);
        if( ih.value != 0 ){ retIntHolder(ih); prun.exception = POARunnable.InvalidPolicyException; return; }
        if(ZenProperties.devDbg) System.out.println( "POAImpl init 10" );

        this.activationStrategy = edu.uci.ece.zen.poa.mechanism.ActivationStrategy.init(this.policyList, this.idAssignmentStrategy, this.retentionStrategy,ih);
        if( ih.value != 0 ){ retIntHolder(ih); prun.exception = POARunnable.InvalidPolicyException; return; }
        if(ZenProperties.devDbg) System.out.println( "POAImpl init 11" );

        this.requestProcessingStrategy = edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy.init(this.policyList,
                this.retentionStrategy, this.uniquenessStrategy, this.threadPolicyStrategy, this , ih);
        if( ih.value != 0 ){ retIntHolder(ih); prun.exception = POARunnable.InvalidPolicyException; return; }
        if(ZenProperties.devDbg) System.out.println( "POAImpl init 12" );

        poaImplRunnable = new POAImplRunnable( self.poaMemoryArea );
        self.poaMemoryArea.setPortal( this );
        NoHeapRealtimeThread nhrt = new NoHeapRealtimeThread( null,null,null,self.poaMemoryArea,null,poaImplRunnable );
        if(ZenProperties.devDbg) System.out.println( "======================starting nhrt in poa impl region=====================" );
        nhrt.start();
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
    public void handleRequest( RequestMessage req , MemoryArea requestScope , POARunnable prun ){
        if(ZenProperties.devDbg) System.out.println( "POAImpl.handled 1" );
        IntHolder ih = getIntHolder();

        if(ZenProperties.devDbg) System.out.println( "POAImpl.handled 2" );

        validateProcessingState( ih ); // check for the state of the poa? if it is discarding then throw the transient exception...
        if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; retIntHolder( ih ); return; }
        if(ZenProperties.devDbg) System.out.println( "POAImpl.handled 3" );

        // Check the state of the POAManager. Here the POA is in active state
        prun.exception = POAManager.checkPOAManagerState(self.the_POAManager());
        if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; retIntHolder( ih ); return; }
        if(ZenProperties.devDbg) System.out.println( "POAImpl.handled 4" );

        // check if the POA has the persistent policy/or the transient
        FString objKey = getFString();
        req.getObjectKey( objKey );
        if(ZenProperties.devDbg) System.out.println( "POAImpl.handled 5" );
        this.lifespanStrategy.validate( objKey , ih );
        if(ZenProperties.devDbg) System.out.println( "POAImpl.handled 6" );
        retFString( objKey );
        if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; retIntHolder( ih ); return; }
        if(ZenProperties.devDbg) System.out.println( "POAImpl.handled 7" );

        try {
            ScopedMemory tpRegion = this.orb.getThreadPoolRegion(tpId);
            if(ZenProperties.devDbg) System.out.println( "POAImpl.handled 8" );

<<<<<<< .mine
            //ExecuteInRunnable eir = (ExecuteInRunnable) requestScope.newInstance( ExecuteInRunnable.class );
			ExecuteInPeterRunnable eipr = (ExecuteInPeterRunnable)  getEIPRunnable();
			eipr.init(requestScope, TPRunnable.class, HandleRequestRunnable.class);
            //TPRunnable tpr = (TPRunnable) requestScope.newInstance( TPRunnable.class );
=======
            edu.uci.ece.zen.utils.Logger.printThreadStack();
			
            if (edu.uci.ece.zen.utils.ZenProperties.devDbg) {
				System.out.println("tp region is " + tpRegion);
				System.out.println("requestScope  is " + requestScope);
				System.out.println("req allocated in " + MemoryArea.getMemoryArea(req));
            }
            ExecuteInRunnable eir = (ExecuteInRunnable) requestScope.newInstance( ExecuteInRunnable.class );
            if(ZenProperties.devDbg) System.out.println( "POAImpl.handled 9.5" );
            TPRunnable tpr = (TPRunnable) requestScope.newInstance( TPRunnable.class );
>>>>>>> .r442
            if(ZenProperties.devDbg) System.out.println( "POAImpl.handled 10" );
            //tpr.init( self , (ScopedMemory) requestScope );
            //eir.init( tpr , tpRegion );
            if(ZenProperties.devDbg) System.out.println( "POAImpl.handled 11" );

            //HandleRequestRunnable hrr = (HandleRequestRunnable) requestScope.newInstance( HandleRequestRunnable.class );
            //hrr.init( self , req );
            if(ZenProperties.devDbg) System.out.println( "POAImpl.handled 12" );
            //((ScopedMemory)requestScope).setPo{rtal( hrr );
            if(ZenProperties.devDbg) System.out.println( "POAImpl.handled 13" );
            orb.orbImplRegion.executeInArea( eipr );
            if(ZenProperties.devDbg) System.out.println( "POAImpl.handled 14" );
        } catch (Exception ex) {
            // -- have to send a request not handled to the client here
            // -- Throw a transient exception
            prun.exception = POARunnable.TransientException;
            ex.printStackTrace();
            return;
        }
    }

	public Runnable getEIPRunnable()
	{
		if (cachedRunnable == null)
		{
			cachedRunnable = new ExecuteInPeterRunnable();
			return cachedRunnable;
		}
		return cachedRunnable;
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

                    this.retentionStrategy.add( oid, map , ih);
                    if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; retPOAHashMap( map ); break; }
                    //Dont do this right now. will do it later when IOR is being created
                    //orb.set_delegate ( p_servant );

                    int index = this.retentionStrategy.bindDemuxIndex( map , ih );
                    if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; retPOAHashMap( map ); break; }
                    int genCount = this.retentionStrategy.getGenCount( index , ih );
                    if( ih.value != POARunnable.NoException ){ prun.exception = ih.value; retPOAHashMap( map ); break; }

                    this.lifespanStrategy.create( self.poaPath , oid, self.poaDemuxIndex , self.poaDemuxCount , index , genCount , okey );
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
                this.lifespanStrategy.create(self.poaPath, oid, self.poaDemuxIndex , self.poaDemuxCount , index, count, okey );

                retVal = this.create_reference_with_object_key (okey, p_servant._all_interfaces(self, null)[0] , clientMemoryArea);
            }
        }
        retFString( okey );
        retFString( oid );
        retIntHolder( ih );
        if(ZenProperties.devDbg) System.out.println( "servant_to_reference " + retVal );
        if(ZenProperties.devDbg) System.out.println( "servant_to_reference client area " + clientMemoryArea);
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

    private CreateReferenceWithObjectRunnable crwor;
    public synchronized org.omg.CORBA.Object create_reference_with_object_key( FString ok, final String intf, MemoryArea clientArea ) {
        if(ZenProperties.devDbg) System.out.println( "create_reference_with_object_key 1" );
        if( crwor == null )
            crwor = new CreateReferenceWithObjectRunnable();
        if(ZenProperties.devDbg) System.out.println( "create_reference_with_object_key 2" );
        crwor.init( ok, intf, clientArea, orb);
        if(ZenProperties.devDbg) System.out.println( "create_reference_with_object_key 3" );
        try{
            orb.orbImplRegion.executeInArea(crwor);
            return crwor.retVal;
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
        return self.numberOfCurrentRequests;
    }

    public void finalize(){
        if(ZenProperties.devDbg) System.out.println( "POAImpl GC'd" );
    }

}

class CreateReferenceWithObjectRunnable implements Runnable{
    public org.omg.CORBA.Object retVal;
    public FString ok;
    public String intf;
    public MemoryArea ma;
    public ORB orb;

    public CreateReferenceWithObjectRunnable(){}

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

class POAImplRunnable implements Runnable{
    private boolean active;
    private ScopedMemory sm;

    public POAImplRunnable(ScopedMemory sm){
        active = true;
        this.sm = sm;
    }

    public boolean isActive(){
        return this.active;
    }

    public void setActive( boolean val ){
        this.active = val;
    }

    public void run(){
        if(ZenProperties.devDbg) System.out.println("getting portal for: " + sm );
        if(ZenProperties.devDbg) System.out.println("inner thread: " + Thread.currentThread().toString());

        POAImpl poaImpl = (POAImpl) sm.getPortal();
        if(ZenProperties.devDbg) System.out.println( "poa impl is " + poaImpl );
        synchronized( poaImpl ){
            try{
                while( active ){
                    poaImpl.wait();
                }
            }catch( InterruptedException ie ){
                ZenProperties.logger.log(
                        Logger.INFO ,
                        "edu.uci.ece.zen.poa.POAImplRunnable" ,
                        "run()" ,
                        "ORB is shutting down.");
            }
            active=false;
        }
    }

}
