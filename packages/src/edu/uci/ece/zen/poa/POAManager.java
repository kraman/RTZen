package edu.uci.ece.zen.poa;

// --- OMG Imports ---
import org.omg.CORBA.CompletionStatus;
import org.omg.PortableServer.POAManagerPackage.State;

/**
 * This class POAManager is managing the POA operations. This class activates,deactivates the POA.
 * It is also responsible for handelling the requests - queing and discarding of request.
 */
public class POAManager extends org.omg.CORBA.LocalObject
    implements org.omg.PortableServer.POAManager {

    public static POAManager instance(){
        return null;
    }

    /**
     * Creates a new POAManager associated for the POA assocaited with the given ORB.
     * @param orb the orb associated with the POA.
     */
    public POAManager(edu.uci.ece.zen.orb.ORB orb) {
        java.util.ListIterator protocolFactories = ProtocolFactoryList.elements();

        // Logger.debug("POAManager created in the holding state");
        this.state = State.from_int(State._HOLDING);
        this.poaList = new java.util.Vector();
        this.orb = orb;
        this.acceptorRegistry = new AcceptorRegistry();

        while (protocolFactories.hasNext()) {
            ProtocolFactory factory = (ProtocolFactory) protocolFactories.next();

            try {
                acceptorRegistry.add(factory.createAcceptor(orb,
                        (org.omg.PortableServer.POAManager) this, ""));
            } catch (InvalidEndpoint ex) {
                ex.printStackTrace();
            }
        }
    }
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
            orb.getPOAManagerCache().registerManager(this);
            // Logger.debug("The POAManager changed to the actve state");
        }

    }
/**
 * This methos is used to findout the state of the POAManager that is passed in as the arguement.
 * @param manager The POA manager.
 */
    public static int checkPOAManagerState(org.omg.PortableServer.POAManager manager) {
        switch (manager.get_state().value()) {
            case State._HOLDING:
                /*
                 * for now the queue length in the POAManager is 0.
                 * Should later be implemented.
                 */
                return 1;
            case State._DISCARDING:
                return 2;
            case State._INACTIVE:
                return 3;
            default:
                return -1;
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
            throw new org.omg.CORBA.BAD_INV_ORDER(3,
                    CompletionStatus.COMPLETED_NO);
        }

        synchronized (this) {
            this.state = State.HOLDING;
            // Logger.debug("POA Manager State changed to holding");
            if (wait_for_completion) {
                java.util.Enumeration e = poaList.elements();

                // wait for the requests to complete in all the POAs that are
                // registered with this poa manager.
                while (e.hasMoreElements()) {
                    ((edu.uci.ece.zen.poa.POA)
                            e.nextElement()).waitForCompletion();
                }
            }
        }
    }

    /*
     * This operation makes the POAs discard the requests that have not stareted executing
     * and also the new requests.
     * @throws org.omg.PortableServer.POAManagerPackage.AdapterInactive excpetion is thwown if the state of the POAmanager is inactive.
     */

    public void discard_requests(boolean wait_for_completion) throws
                org.omg.PortableServer.POAManagerPackage.AdapterInactive {
        if (this.state == State.INACTIVE) {
            throw new org.omg.PortableServer.POAManagerPackage.AdapterInactive();
        }

        if (wait_for_completion && isInContext()) {
            throw new org.omg.CORBA.BAD_INV_ORDER(3,
                    CompletionStatus.COMPLETED_NO);
        }

        synchronized (this) {
            this.state = State.DISCARDING;
            // Logger.debug("POaManager state changed to discarding");
            java.util.Enumeration e = poaList.elements();

            // wait for the requests to complete in all the POAs that are
            // registered with this poa manager.
            if (wait_for_completion) {
                while (e.hasMoreElements()) {
                    ((edu.uci.ece.zen.poa.POA) e.nextElement()).waitForCompletion();
                }
            }
        }
    }

    /*
     * This operation changes the state of the POA manager to inactive.Entering the inactive state causes the associated POAs to reject requests that          * have not begun to be executed as well as any new requests.
     * @throws org.omg.PortableServer.POAManagerPackage.AdapterInactive excpetion is thwown if the state of the POAmanager is inactive.
     */


    public void deactivate(boolean etherealize_objects,
            boolean wait_for_completion) throws
                org.omg.PortableServer.POAManagerPackage.AdapterInactive {
        if (wait_for_completion && isInContext()) {
            throw new org.omg.CORBA.BAD_INV_ORDER(6,
                    CompletionStatus.COMPLETED_NO);
        }

        // should invoke the etherealize on all the POAs that have a Retain and also
        // a Servant Manager.

        synchronized (this) {
            this.state = State.DISCARDING;
            orb.getPOAManagerCache().unregisterManager(this);
            // Logger.debug("State changed to deactivate");
            java.util.Enumeration e = poaList.elements();
            edu.uci.ece.zen.poa.POA poa;

            if (wait_for_completion) {
                while (e.hasMoreElements()) {
                    poa = (edu.uci.ece.zen.poa.POA) e.nextElement();
                    if (etherealize_objects
                            && Util.useServantManagerPolicy(poa.policy_list())
                            && Util.useRetainPolicy(poa.policy_list())) {// call ethrealize on the servant manager.and then wait for completion
                    } else {
                        poa.waitForCompletion();
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

    protected synchronized void register(org.omg.PortableServer.POA poa) {
        if (!(poaList.contains(poa))) {
            poaList.add(poa);
        }
    }

    protected synchronized void unRegister(org.omg.PortableServer.POA poa) {
        poaList.remove(poa);
    }

    protected boolean isInContext() {
        ThreadSpecificPOACurrent current = POATSS.tss.getCurrent();

        if (current != null) {
            if (((edu.uci.ece.zen.poa.POA) current.getPOA()).getORB()
                    == this.orb) {
                return true;
            }
        }

        return false;

    }
    /**
     * This methor returns trus if the state of the POAManager is DISCARDING.
     * @return True if the state of the POAManager is DISCARDING
     */

    public boolean isDiscarding() {
        return get_state().value()
                == org.omg.PortableServer.POAManagerPackage.State._DISCARDING
                ? true
                : false;
    }
    /**
     * This methor returns trus if the state of the POAManager is INACTIVE
     * @return True if the state of the POAManager is INACTIVE
     */

    public boolean isInActive() {
        return get_state().value()
                == org.omg.PortableServer.POAManagerPackage.State._INACTIVE
                ? true
                : false;

    }
    /**
     * This methor returns trus if the state of the POAManager is HOLDING.
     * @return True if the state of the POAManager is HOLDING
     */

    public boolean isHolding() {
        return get_state().value()
                == org.omg.PortableServer.POAManagerPackage.State._HOLDING
                ? true
                : false;
    }
    /**
     * This methor returns trus if the state of the POAManager is ACTIVE
     * @return True if the state of the POAManager is ACTIVE
     */

    public boolean isActive() {
        return get_state().value()
                == org.omg.PortableServer.POAManagerPackage.State._ACTIVE
                ? true
                : false;
    }
    /**
     * This method returns the associated ORB.
     * @return The associated ORB.
     */
    public edu.uci.ece.zen.orb.ORB orb() {
        return orb;
    }

    // --- Priavate Variables ---
    private State state;
    private edu.uci.ece.zen.orb.ORB orb;
    private java.util.Vector poaList;
    private AcceptorRegistry acceptorRegistry;

    // --- Channel ---
    protected edu.oswego.cs.dl.util.concurrent.Channel channel_ = null;

    public AcceptorRegistry getAcceptorRegistry() {
        return acceptorRegistry;
    }
}
