package edu.uci.ece.zen.poa;

// --- OMG Imports ---
import org.omg.CORBA.CompletionStatus;
import org.omg.PortableServer.POAManagerPackage.State;
import edu.uci.ece.zen.utils.*;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.poa.*;
import javax.realtime.*;

/**
 * This class POAManager is managing the POA operations. This class activates,deactivates the POA.
 * It is also responsible for handelling the requests - queing and discarding of request.
 */
public class POAManager extends org.omg.CORBA.LocalObject implements org.omg.PortableServer.POAManager{

    private static Queue unusedFacades;
    private static ImmortalMemory imm;

    private ORB orb;
    private ScopedMemory poaManagerMemoryArea;
    private State state;

    private org.omg.PortableServer.POA registeredPOAs[];
    private int numRegisteredPOAs;

    static{
        try{
            imm = ImmortalMemory.instance();
            //Set up POA Facades
            int numFacades = Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.poa.maxNumPOAManagers" , "1" ) );
            unusedFacades = (Queue) imm.newInstance( Queue.class );
            for( int i=0;i<numFacades;i++ )
                unusedFacades.enqueue( imm.newInstance( edu.uci.ece.zen.poa.POAManager.class ) );
        }catch( Exception e ){
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    public static edu.uci.ece.zen.poa.POAManager instance(){
        edu.uci.ece.zen.poa.POAManager retVal;
        retVal = (edu.uci.ece.zen.poa.POAManager) unusedFacades.dequeue();
        retVal.numRegisteredPOAs = 0;
        retVal.state = State.INACTIVE;
        return retVal;
    }

    private static void release( edu.uci.ece.zen.poa.POAManager poaManager ){
        unusedFacades.enqueue( poaManager );
    }

    public POAManager(){
        state = State.INACTIVE;
        int numPOAs = Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.poa.maxNumPOAManagers" , "1" ) );
        registeredPOAs = new org.omg.PortableServer.POA[numPOAs];
        numRegisteredPOAs = 0;
    }

    ///////////////////////////////////////////////////////////////////////////
    //////////////////////// BASIC POAMANAGER METHODS /////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    /**
     * This operation activates the POAmanager.
     * @throws org.omg.PortableServer.POAManagerPackage.AdapterInactive Exception is thrown if the state of the POA manager is inactive.
     */
    public synchronized void activate() throws
                org.omg.PortableServer.POAManagerPackage.AdapterInactive {

        /*
         * Right now the queue length in the POA Manager is set to 0, so any
         * requests that are invoked on the POA get a Transient Exception and
         * have to be reissued.
         */

        if (state.value() == State._INACTIVE) {
            throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();
        } else {
            this.state = State.ACTIVE;
        }
    }

    /**
     * This method is used to findout the state of the POAManager that is passed in as the arguement.
     * @param manager The POA manager.
     */
    public static int checkPOAManagerState(org.omg.PortableServer.POAManager manager ) {
        switch (manager.get_state().value()) {
            case State._HOLDING:
                /*
                 * for now the queue length in the POAManager is 0.
                 * Should later be implemented.
                 */
                return POARunnable.TransientException;
            case State._DISCARDING:
                return POARunnable.TransientException;
            case State._INACTIVE:
                return POARunnable.ObjAdapterException;
            default:
                return POARunnable.NoException;
        }
    }

    /**
     * This operation changes the state of the POAManager to holding. By this the requests that the POA receives are queued.
     * @throws org.omg.PortableServer.POAManagerPackage.AdapterInactive excpetion is thwown if the state of the POAmanager is inactive.
     */
    public void hold_requests(boolean wait_for_completion) throws
                org.omg.PortableServer.POAManagerPackage.AdapterInactive {
        // Changes the state of the POA Mananger to holding.
        // I: if in the inactive state then the Adapterexists exception is thrown

        if (this.state == State.INACTIVE) {
            throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();
        }

        // so any of the requests coming to the POA is queued or Exception thrown in
        // our case.
        // all requests that are in the queue that are not yet executed would
        // be continued to be queued, but in our case now the length of the queue
        // is 0.

        if (wait_for_completion && isInContext()) {
            throw new org.omg.CORBA.BAD_INV_ORDER(3, CompletionStatus.COMPLETED_NO);
        }

        synchronized (this) {
            this.state = State.HOLDING;
            // Logger.debug("POA Manager State changed to holding");
            if (wait_for_completion) {
                for( int i=0;i<numRegisteredPOAs;i++ ){
                    // wait for the requests to complete in all the POAs that are
                    // registered with this poa manager.
                    ((edu.uci.ece.zen.poa.POA)registeredPOAs[i]).waitForCompletion();
                }
            }
        }
    }

    /*
     * This operation makes the POAs discard the requests that have not stareted executing
     * and also the new requests.
     * @throws org.omg.PortableServer.POAManagerPackage.AdapterInactive excpetion is thwown if the state of the POAmanager is inactive.
     */
    public void discard_requests(boolean wait_for_completion) throws org.omg.PortableServer.POAManagerPackage.AdapterInactive {
        if (this.state == State.INACTIVE) {
            throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();
        }

        if (wait_for_completion && isInContext()) {
            throw new org.omg.CORBA.BAD_INV_ORDER(3, CompletionStatus.COMPLETED_NO);
        }

        synchronized (this) {
            this.state = State.DISCARDING;

            // wait for the requests to complete in all the POAs that are
            // registered with this poa manager.
            if (wait_for_completion) {
                for( int i=0;i<numRegisteredPOAs;i++ ){
                    // wait for the requests to complete in all the POAs that are
                    // registered with this poa manager.
                    ((edu.uci.ece.zen.poa.POA)registeredPOAs[i]).waitForCompletion();
                }
            }
        }
    }

    /*
     * This operation changes the state of the POA manager to inactive.Entering the inactive state causes the associated POAs to reject requests that          * have not begun to be executed as well as any new requests.
     * @throws org.omg.PortableServer.POAManagerPackage.AdapterInactive excpetion is thwown if the state of the POAmanager is inactive.
     */

    public void deactivate(boolean etherealize_objects, boolean wait_for_completion) 
            throws org.omg.PortableServer.POAManagerPackage.AdapterInactive {
        if (wait_for_completion && isInContext()) {
            throw new org.omg.CORBA.BAD_INV_ORDER(6, CompletionStatus.COMPLETED_NO);
        }

        // should invoke the etherealize on all the POAs that have a Retain and also
        // a Servant Manager.

        synchronized (this) {
            this.state = State.DISCARDING;
            //orb.getPOAManagerCache().unregisterManager(this);
            // Logger.debug("State changed to deactivate");
            edu.uci.ece.zen.poa.POA poa;

            if (wait_for_completion) {
                for( int i=0;i<numRegisteredPOAs;i++ ){
                    poa = (edu.uci.ece.zen.poa.POA) registeredPOAs[i];
                    if (etherealize_objects
                            && PolicyUtils.useServantManagerPolicy(poa.policy_list())
                            && PolicyUtils.useRetainPolicy(poa.policy_list())) {
                        // call ethrealize on the servant manager.and then wait for completion
                        //KLUDGE: What to do here?
                    } else {
                        poa.waitForCompletion();
                        if (etherealize_objects
                                && PolicyUtils.useServantManagerPolicy(poa.policy_list())
                                && PolicyUtils.useRetainPolicy(poa.policy_list())) {
                            // call ethrealize on the servant manager.and then wait for completion
                            //KLUDGE: What is supposed to happen here?
                        } else {
                            poa.waitForCompletion();
                        }
                    }
                }
            }
        }
    }

    /**This operation returns the state of the POAManager.
     * @return The state of the POAManager.
     */
    public org.omg.PortableServer.POAManagerPackage.State get_state() {
        return State.from_int(state.value());
    }

    protected void register(org.omg.PortableServer.POA poa) {
        synchronized( this ){
            for( int i=0;i<numRegisteredPOAs;i++ )
                if( registeredPOAs[i].equals( (edu.uci.ece.zen.poa.POA) poa ) )
                    return;
            registeredPOAs[numRegisteredPOAs++] = poa;
        }
    }

    protected void unRegister(org.omg.PortableServer.POA poa) {
        synchronized( this ){
            int foundIdx = -1;
            for( int i=0;i<numRegisteredPOAs;i++ )
                if( registeredPOAs[i].equals( poa ) ){
                    foundIdx=i;
                    break;
                }

            System.arraycopy( registeredPOAs , foundIdx+1 , registeredPOAs , foundIdx , numRegisteredPOAs-foundIdx-1 );
            numRegisteredPOAs--;
        }
    }

    protected boolean isInContext() {
        /* KLUDGE: What to do here?
        ThreadSpecificPOACurrent current = POATSS.tss.getCurrent();

        if (current != null) {
            if (((edu.uci.ece.zen.poa.POA) current.getPOA()).getORB() == this.orb) {
                return true;
            }
        }
        */

        return false;
    }

    /**
     * This methor returns trus if the state of the POAManager is DISCARDING.
     * @return True if the state of the POAManager is DISCARDING
     */
    public boolean isDiscarding() {
        return get_state().value() == org.omg.PortableServer.POAManagerPackage.State._DISCARDING;
    }

    /**
     * This methor returns trus if the state of the POAManager is INACTIVE
     * @return True if the state of the POAManager is INACTIVE
     */
    public boolean isInActive() {
        return get_state().value() == org.omg.PortableServer.POAManagerPackage.State._INACTIVE;
    }

    /**
     * This methor returns trus if the state of the POAManager is HOLDING.
     * @return True if the state of the POAManager is HOLDING
     */
    public boolean isHolding() {
        return get_state().value() == org.omg.PortableServer.POAManagerPackage.State._HOLDING;
    }

    /**
     * This methor returns trus if the state of the POAManager is ACTIVE
     * @return True if the state of the POAManager is ACTIVE
     */
    public boolean isActive() {
        return get_state().value() == org.omg.PortableServer.POAManagerPackage.State._ACTIVE;
    }

    /**
     * This method returns the associated ORB.
     * @return The associated ORB.
     */
    public edu.uci.ece.zen.orb.ORB orb() {
        return orb;
    }
}
