package edu.uci.ece.zen.poa;

import javax.realtime.*;
import org.omg.CORBA.CompletionStatus;
import edu.uci.ece.zen.orb.ObjRefDelegate;
import edu.uci.ece.zen.orb.IOR;
import edu.uci.ece.zen.utils.*;
import java.io.*;
import java.util.Properties;

public class POA extends org.omg.CORBA.LocalObject implements org.omg.PortableServer.POA {

    private static String maxNumPOAsProperty = "doc.zen.poa.maxNumPOAs";
    private static String defaultMaxNumPOAs = "1";
    private static int maxNumPOAs


    private static Queue unusedFacades;
    private static ImmortalMemory imm;

    static{
        try{
            imm = ImmortalMemory.instance();
            int numFacades = Integer.parseInt( ZenProperties.getGlobalProperty( maxNumPOAsProperty , defaultMaxNumPOAs ) );
            unusedFacades = (Queue) imm.newInstance( Queue.class );
            for( int i=0;i<maxNumPOAs;i++ )
                unusedFacades.enqueue( imm.newInstance( edu.uci.ece.zen.poa.POA.class ) );
        }catch( Exception e ){
            e.printStackTrace();
            System.exit( -1 );
        }
    }

    public synchronized static org.omg.CORBA.Object instance(final edu.uci.ece.zen.orb.ORB orb) throws org.omg.PortableServer.POAPackage.InvalidPolicy {
            edu.uci.ece.zen.poa.POA retVal;
            retVal = (edu.uci.ece.zen.poa.POA) unusedFacades.dequeue();
            retVal.internalInit(orb, "RootPOA", "RootPOA", null, null, null);
            return retVal;
        }


    public POA(){}

    protected void internalInit(final edu.uci.ece.zen.orb.ORB orb,
            final String poaName,
            final String poaPath,
            org.omg.CORBA.Policy[] policies,
            org.omg.PortableServer.POA parent,
            org.omg.PortableServer.POAManager manager)

        throws org.omg.PortableServer.POAPackage.InvalidPolicy {
            this.orb = orb;
            this.poaName = poaName;
            this.parent = (edu.uci.ece.zen.poa.POA) parent;
            this.poaPath = poaPath;
            currentMemory = MemoryArea.getMemoryArea(this);
            run = new Run();

            if (policies != null) {
                this.policyList = new org.omg.CORBA.Policy[policies.length];
                for (int iterator = 0; iterator < policies.length; iterator++) {
                    this.policyList[iterator] = policies[iterator].copy();
                }
            }

            if (manager == null) {
                this.poaManager = new edu.uci.ece.zen.poa.POAManager(orb);
            } else {
                this.poaManager = manager;
            }

            // register the POA with the POA Manager
            ((edu.uci.ece.zen.poa.POAManager) poaManager).register(this);

            this.serverRequestHandler = (POAServerRequestHandler) orb.requestHandler();
            // Get the index to the Active Demux Index
            this.poaDemuxIndex = serverRequestHandler.addPOA(poaPath, this);

            theChildren = new java.util.Hashtable();
            numberOfCurrentRequests = new SynchronizedInt();

            // --- Strategies ---
            this.createImpl();

            this.poaState = Util.CREATION_COMPLETE;
        }

    private void createImpl()
    {

        try{
            ScopedMemory childMemory = orb.getScopedRegion();
            Object poa =childMemory.newInstance(POAImpl.class);

            childMemory.enter( new Runnable() {
                    public void run()
                    {
                    POAImpl impl  = new POAImpl();
                    ScopedMemory curMem = ((ScopedMemory) RealtimeThread.getCurrentMemoryArea());
                    impl.setParameters(orb,poaName,poaPath,policyList,poaManager,serverRequestHandler,
                        poaDemuxIndex,numberOfCurrentRequests,curMem);
                    curMem.setPortal( impl );
                    }
                    });
        }catch(Exception e) { //System.out.println(e);
        }
    }

    public void handleRequest(final ServerRequest req) {

        //System.out.println("in poa invoke    ");

        run.setType(0);
        run.input[0] = req;
        childMemory.enter(run);
        //return (byte[]) run.getObject();


        //( (POAImpl) (childMemory.getScope()) ).handleRequest(req);

    }

    public byte[] servant_to_id(final org.omg.PortableServer.Servant p_servant)
        throws
        org.omg.PortableServer.POAPackage.ServantNotActive,
    org.omg.PortableServer.POAPackage.WrongPolicy {

        return null;
        /*
           run.setType(1);
           run.input[0] = p_servant;
           childMemory.enter(run);
           if(run.chkException()) {
           Exception e = run.getException();
           if ( e instanceof  org.omg.PortableServer.POAPackage.ServantNotActive)
           throw (org.omg.PortableServer.POAPackage.ServantNotActive) e;
           else throw (org.omg.PortableServer.POAPackage.WrongPolicy) e;
           }
           else
           return (byte[]) run.getObject();

         */             //return ( (POAImpl) (childMemory.getScope()) ).servant_to_id(p_servant);
    }

    public org.omg.CORBA.Object servant_to_reference(
            final org.omg.PortableServer.Servant p_servant)
        throws
        org.omg.PortableServer.POAPackage.ServantNotActive,
    org.omg.PortableServer.POAPackage.WrongPolicy {

        run.setType(2);
        run.input[0] = p_servant;
        childMemory.enter(run);
        //System.out.println("Ok back to poa");
        if(run.chkException()) {
            Exception e = run.getException();
            if ( e instanceof  org.omg.PortableServer.POAPackage.ServantNotActive)
                throw (org.omg.PortableServer.POAPackage.ServantNotActive) e;
            else throw (org.omg.PortableServer.POAPackage.WrongPolicy) e;
        }
        else

            return (org.omg.CORBA.Object) run.getObject();


        //return ( (POAImpl) (childMemory.getScope()) ).servant_to_reference(p_servant);
    }

    public org.omg.PortableServer.Servant reference_to_servant(
            final org.omg.CORBA.Object reference)
        throws org.omg.PortableServer.POAPackage.ObjectNotActive,
    org.omg.PortableServer.POAPackage.WrongPolicy,
    org.omg.PortableServer.POAPackage.WrongAdapter {

        return null;
        /*
           run.setType(3);
           run.input[0] = reference;
           childMemory.enter(run);
           if(run.chkException()) {
           Exception e = run.getException();
           if ( e instanceof  org.omg.PortableServer.POAPackage.ObjectNotActive)
           throw (org.omg.PortableServer.POAPackage.ObjectNotActive) e;
           else if ( e instanceof org.omg.PortableServer.POAPackage.WrongAdapter)
           throw (org.omg.PortableServer.POAPackage.WrongAdapter) e;
           else throw (org.omg.PortableServer.POAPackage.WrongPolicy) e;
           }
           else

           return (org.omg.PortableServer.Servant) run.getObject();

        //return ( (POAImpl) (childMemory.getScope()) ).reference_to_servant(reference);
         */
    }
    public byte[] reference_to_id(final org.omg.CORBA.Object reference)
        throws
        org.omg.PortableServer.POAPackage.WrongAdapter,
    org.omg.PortableServer.POAPackage.WrongPolicy {

        return null;
        /*
           run.setType(4);
           run.input[0] = reference;
           childMemory.enter(run);
           if(run.chkException()) {
           Exception e = run.getException();
           if ( e instanceof org.omg.PortableServer.POAPackage.WrongAdapter)
           throw (org.omg.PortableServer.POAPackage.WrongAdapter) e;
           else throw (org.omg.PortableServer.POAPackage.WrongPolicy) e;
           }
           else

           return (byte[]) run.getObject();

         */
        //return ( (POAImpl) (childMemory.getScope()) ).reference_to_id(reference);
    }
    public org.omg.PortableServer.Servant id_to_servant(final byte[] oid)
        throws
        org.omg.PortableServer.POAPackage.ObjectNotActive,
    org.omg.PortableServer.POAPackage.WrongPolicy {

        run.setType(5);
        run.input[0] = oid;
        childMemory.enter(run);
        if(run.chkException()) {
            Exception e = run.getException();
            //System.out.println("OK error in id_ is " + e);
            if ( e instanceof  org.omg.PortableServer.POAPackage.ObjectNotActive)
                throw (org.omg.PortableServer.POAPackage.ObjectNotActive) e;
            else throw (org.omg.PortableServer.POAPackage.WrongPolicy) e;
        }
        else

            return (org.omg.PortableServer.Servant ) run.getObject();


        //return ( (POAImpl) (childMemory.getScope()) ).Servant id_to_servant(oid);
    }
    public org.omg.CORBA.Object id_to_reference(final byte[] oid)
        throws
        org.omg.PortableServer.POAPackage.ObjectNotActive,
    org.omg.PortableServer.POAPackage.WrongPolicy {
        return null;

        /*
           run.setType(6);
           run.input[0] = oid;
           childMemory.enter(run);
           if(run.chkException()) {
           Exception e = run.getException();
           if ( e instanceof  org.omg.PortableServer.POAPackage.ObjectNotActive)
           throw (org.omg.PortableServer.POAPackage.ObjectNotActive) e;
           else throw (org.omg.PortableServer.POAPackage.WrongPolicy) e;
           }
           else

           return (org.omg.CORBA.Object ) run.getObject();
         */


        //        	return ( (POAImpl) (childMemory.getScope()) ).id_to_reference(oid);
    }
    public byte[] activate_object (org.omg.PortableServer.Servant p_servant)
        throws
        org.omg.PortableServer.POAPackage.ServantAlreadyActive,
    org.omg.PortableServer.POAPackage.WrongPolicy
    {
        return null;
        /*

           run.setType(7);
           run.input[0] = p_servant;
           childMemory.enter(run);
           if(run.chkException()) {
           Exception e = run.getException();
           if ( e instanceof  org.omg.PortableServer.POAPackage.ServantAlreadyActive)
           throw (org.omg.PortableServer.POAPackage.ServantAlreadyActive) e;
           else throw (org.omg.PortableServer.POAPackage.WrongPolicy) e;
           }
           else

           return (byte[]) run.getObject();
         */

        //        	return ( (POAImpl) (childMemory.getScope()) ).activate_object (p_servant);
    }

    public void activate_object_with_id(final byte[] id,
            final org.omg.PortableServer.Servant p_servant)
        throws
        org.omg.PortableServer.POAPackage.ServantAlreadyActive,
    org.omg.PortableServer.POAPackage.ObjectAlreadyActive,
    org.omg.PortableServer.POAPackage.WrongPolicy {
        /*

           run.setType(8);
           run.input[0] = id;
           run.input[1] = p_servant;
           childMemory.enter(run);
           if(run.chkException()) {
           Exception e = run.getException();
           if ( e instanceof  org.omg.PortableServer.POAPackage.ObjectAlreadyActive)
           throw (org.omg.PortableServer.POAPackage.ObjectAlreadyActive) e;
           else if ( e instanceof org.omg.PortableServer.POAPackage.ServantAlreadyActive)
           throw (org.omg.PortableServer.POAPackage.ServantAlreadyActive) e;
           else throw (org.omg.PortableServer.POAPackage.WrongPolicy) e;
           }


        //                	( (POAImpl) (childMemory.getScope()) ).activate_object_with_id(id,p_servant);
         */
    }

    public void deactivate_object(byte[] oid)
        throws
        org.omg.PortableServer.POAPackage.ObjectNotActive,
    org.omg.PortableServer.POAPackage.WrongPolicy {
        /*

           run.setType(9);
           run.input[0] = oid;
           childMemory.enter(run);
           if(run.chkException()) {
           Exception e = run.getException();
           if ( e instanceof  org.omg.PortableServer.POAPackage.ObjectNotActive)
           throw (org.omg.PortableServer.POAPackage.ObjectNotActive) e;
           else throw (org.omg.PortableServer.POAPackage.WrongPolicy) e;
           }
         */

        // ( (POAImpl) (childMemory.getScope()) ).deactivate_object(oid);
    }

    public org.omg.PortableServer.POA create_POA(
            java.lang.String adapter_name,
            org.omg.PortableServer.POAManager a_POAManager,
            org.omg.CORBA.Policy[] policies)
        throws org.omg.PortableServer.POAPackage.AdapterAlreadyExists,
    org.omg.PortableServer.POAPackage.InvalidPolicy {

        return null;
        /*

        // Get the name of the POA with the separators and also escape characters
        String temp = ObjectKey.objectKeyString(adapter_name);

        synchronized (createDestroyPOAMutex) {
        edu.uci.ece.zen.poa.POA child = (edu.uci.ece.zen.poa.POA)
        this.theChildren.get(adapter_name);

        if (child != null) {
        if (!(child.isDestructionApparent())) {
        throw new org.omg.PortableServer.POAPackage.AdapterAlreadyExists();
        }

        if (this.disableCreatePOA && !this.isDestructionApparent()) {
        throw new org.omg.CORBA.BAD_INV_ORDER(17,
        CompletionStatus.COMPLETED_NO);
        } else {
        this.disableCreatePOA = false;
        }
        }
        this.poaState = Util.CREATING;

        Class[] paramType = new Class[6];
        paramType[0] = edu.uci.ece.zen.orb.ORB.class;
        paramType[1] = java.lang.String.class;
        paramType[2] = java.lang.String.class;
        paramType[3] = org.omg.CORBA.Policy[].class;
        paramType[4] = org.omg.PortableServer.POA.class;
        paramType[5] = org.omg.PortableServer.POAManager.class;


        try{java.lang.reflect.Constructor ctor =(POA.class).getConstructor(paramType);
        Object[] param = new Object[6];
        param[0]= orb;
        param[1]=  adapter_name;
        param[2]= this.path_name() + "/" + temp;
        param[3]=policies;
        param[4]=this;
        param[5]= a_POAManager;

        child  = (POA) currentMemory.newInstance(ctor,param);

        this.theChildren.put(adapter_name, child);
        this.poaState = Util.CREATION_COMPLETE;
        return child;
        }
        catch( Exception e) { throw new RuntimeException();}
        }*/
    }

    public org.omg.PortableServer.POA find_POA(
            final java.lang.String adapter_name,
            final boolean activate_it)
        throws org.omg.PortableServer.POAPackage.AdapterNonExistent {

            return null;
            /*
               if (theChildren.containsKey(adapter_name)) {
               return (org.omg.PortableServer.POA)
               this.theChildren.get(adapter_name);
               }

               if (activate_it)
               {   boolean temp = false;
               try{
               temp = the_activator().unknown_adapter(this, adapter_name);
               }
               catch ( Exception ex){throw new org.omg.CORBA.OBJ_ADAPTER("AdapterActivator failed to activate POA",1,CompletionStatus.COMPLETED_NO);}
               if (temp)
               return (POA) theChildren.get(adapter_name);
               else throw new org.omg.PortableServer.POAPackage.AdapterNonExistent();

               }
               else throw new org.omg.PortableServer.POAPackage.AdapterNonExistent();*/
        }

    public void destroy(final boolean etherealize_objects, final boolean wait_for_completion) {
        /*
           if (wait_for_completion) {
           ThreadSpecificPOACurrent current = POATSS.tss.getCurrent();

           if (current != null
           && ((edu.uci.ece.zen.poa.POA) current.getPOA()).getORB()
           == this.orb) {

           throw new org.omg.CORBA.BAD_INV_ORDER("The destroy is unsuccessful as the Current"
           + "thread is in the InvocationContext",
           3,
           CompletionStatus.COMPLETED_NO);
           }
           }

           synchronized (createDestroyPOAMutex) {
           this.disableCreatePOA = true;

           if (((POAManager) poaManager).isInActive()) {
           this.processingState = Util.INACTIVE;
           } else {
           this.processingState = Util.DISCARDING;
           }

        // if called multiple_times the first time is the etherealize param
        if (poaState < Util.DESTRUCTION_IN_PROGRESS) {
        this.etherealize = etherealize_objects;
        }

        this.poaState = Util.DESTRUCTION_IN_PROGRESS;

        org.omg.PortableServer.POA[] e = this.the_children();

        if (e != null) {
        for (int i = 0; i < e.length; i++) {
        e[i].destroy(etherealize, wait_for_completion);
        }
        }

        // Remove the POA from the POAServerRequestHandler and also from the
        // list of childrenPOA maintained the by Parent
        if (this.parent != null) {
        this.parent.removePOA(this);
        }

        this.serverRequestHandler.remove(this);
        ((edu.uci.ece.zen.poa.POAManager) this.poaManager).unRegister(this);

        // Clear the list of children for this POA
        this.theChildren.clear();

        // Wait for the Apparent Destruction of the POA
        if (this.numberOfCurrentRequests.get() != 0) {
        this.numberOfCurrentRequests.waitForCompletion();
        }

        // At this point the Apparent Destruction of the POA has occured.
        // Ethrealize the servants for each activeObject in the AOM
        this.poaState = Util.DESTRUCTION_APPARANT;

        final edu.uci.ece.zen.poa.POA poa = this;

        if (this.etherealize && ( ( (POAImpl) (childMemory.getPortal()) ).requestProcessingStrategy ) instanceof
        edu.uci.ece.zen.poa.mechanism.ServantActivatorStrategy) {
        Runnable r1 = new Runnable() {
        public void run() {
        ((edu.uci.ece.zen.poa.mechanism.ServantActivatorStrategy)
        ( (POAImpl) (childMemory.getPortal()) ).requestProcessingStrategy).etherealize(poa, true,
        false);
        }
        };

        Thread t = ThreadFactory.createThread(r1);

        if (wait_for_completion) {
            try {
                t.join();
            } catch (InterruptedException ex) {}
        } else {
            t.start();
        }
        r1 = null; // Enable GC
    }

    // At this point the destruction of the POA has been completed
    // notify any threads that could be waiting for this POA to be
    // destroyed
    this.childMemory=null;
    this.poaState = Util.DESTRUCTION_COMPLETE;
    this.createDestroyPOAMutex.notifyAll();
    }*/
    }
    public org.omg.PortableServer.ThreadPolicy create_thread_policy(
            final org.omg.PortableServer.ThreadPolicyValue value) {
        //return this.serverRequestHandler.create_thread_policy(value);
        throw new RuntimeException();
    }

    public org.omg.PortableServer.LifespanPolicy create_lifespan_policy(
            final org.omg.PortableServer.LifespanPolicyValue value) {
        //            return this.serverRequestHandler.create_lifespan_policy(value);
        throw new RuntimeException();
    }

    public org.omg.PortableServer.IdUniquenessPolicy create_id_uniqueness_policy(
            final org.omg.PortableServer.IdUniquenessPolicyValue value) {
        //            return this.serverRequestHandler.create_id_uniqueness_policy(value);

        throw new RuntimeException();
    }

    public org.omg.PortableServer.IdAssignmentPolicy create_id_assignment_policy(
            final org.omg.PortableServer.IdAssignmentPolicyValue value) {
        //            return this.serverRequestHandler.create_id_assignment_policy(value);
        throw new RuntimeException();
    }
    public org.omg.PortableServer.ImplicitActivationPolicy create_implicit_activation_policy(
            final org.omg.PortableServer.ImplicitActivationPolicyValue value) {
        //            return this.serverRequestHandler.create_implicit_activation_policy(value);
        throw new RuntimeException();
    }
    public org.omg.PortableServer.ServantRetentionPolicy create_servant_retention_policy(
            final org.omg.PortableServer.ServantRetentionPolicyValue value) {
        //            return this.serverRequestHandler.create_servant_retention_policy(value);
        throw new RuntimeException();
    }
    public org.omg.PortableServer.RequestProcessingPolicy create_request_processing_policy(
            final org.omg.PortableServer.RequestProcessingPolicyValue value) {
        //            return this.serverRequestHandler.create_request_processing_policy(value);
        throw new RuntimeException();
    }

    public java.lang.String the_name() {
        return poaName;
    }
    public java.lang.String path_name() {
        return poaPath;
    }
    public org.omg.PortableServer.POA the_parent() {
        return parent;
    }
    public byte[] id() {

        run.setType(10);
        childMemory.enter(run);
        return (byte[]) run.getObject();

        //    	return ( (POAImpl) (childMemory.getScope()) ).id();

    }
    public org.omg.PortableServer.POA[] the_children() {
        int i = 0;

        if (this.theChildren.size() == 0) {
            return null;
        }


        java.util.Enumeration e = theChildren.elements();

        //OK we need to find out where to put this array!!!
        org.omg.PortableServer.POA[] array = new org.omg.PortableServer.POA[theChildren.size()];

        while (e.hasMoreElements()) {
            array[i++] = (org.omg.PortableServer.POA) e.nextElement();
        }

        return array;
    }

    public org.omg.PortableServer.POAManager the_POAManager() {
        return this.poaManager;
    }

    public org.omg.PortableServer.AdapterActivator the_activator() {
        return this.adapterActivator;

    }

    public void the_activator(
            final org.omg.PortableServer.AdapterActivator the_activator) {
        this.adapterActivator = the_activator;
    }

    public org.omg.PortableServer.ServantManager get_servant_manager()
        throws org.omg.PortableServer.POAPackage.WrongPolicy {

            return null;
            /*


               run.setType(11);
               childMemory.enter(run);
               if(run.chkException()) {
               Exception e = run.getException();
               throw (org.omg.PortableServer.POAPackage.WrongPolicy) e;
               }
               else

               return (org.omg.PortableServer.ServantManager ) run.getObject();


            //return ( (POAImpl) (childMemory.getScope()) ).get_servant_manager();
             */
        }
    public void set_servant_manager(
            final org.omg.PortableServer.ServantManager imgr)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
            /*

               run.setType(12);
               run.input[0] = imgr;
               if(run.chkException()) {
               Exception e = run.getException();
               throw (org.omg.PortableServer.POAPackage.WrongPolicy) e;
               }

               childMemory.enter(run);
            //return (org.omg.PortableServer.Servant ) run.getObject();



             */
            //( (POAImpl) (childMemory.getScope()) ).set_servant_manager(imgr);

        }
    public org.omg.PortableServer.Servant get_servant()
        throws org.omg.PortableServer.POAPackage.NoServant,
    org.omg.PortableServer.POAPackage.WrongPolicy {
        return null;
        /*

           run.setType(13);
           childMemory.enter(run);
           if(run.chkException()) {
           Exception e = run.getException();
           if ( e instanceof  org.omg.PortableServer.POAPackage.NoServant)
           throw (org.omg.PortableServer.POAPackage.NoServant) e;
           else throw (org.omg.PortableServer.POAPackage.WrongPolicy) e;
           }

           return (org.omg.PortableServer.Servant ) run.getObject();

         */

        //                return ( (POAImpl) (childMemory.getScope()) )get_servant();

    }
    public void set_servant(final org.omg.PortableServer.Servant p_servant)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
            /*

               run.setType(14);
               run.input[0] = p_servant;
               childMemory.enter(run);
               if(run.chkException()) {
               Exception e = run.getException();
               throw (org.omg.PortableServer.POAPackage.WrongPolicy) e;
               }

            //return (org.omg.PortableServer.Servant ) run.getObject();
             */

            //( (POAImpl) (childMemory.getScope()) ).set_servant(p_servant);
        }

    public org.omg.CORBA.Object create_reference(final String intf)
        throws org.omg.PortableServer.POAPackage.WrongPolicy {
            return null;
            /*
               run.setType(15);
               run.input[0] = intf;
               childMemory.enter(run);
               if(run.chkException()) {
               Exception e = run.getException();
               throw (org.omg.PortableServer.POAPackage.WrongPolicy) e;
               }

               return (org.omg.CORBA.Object ) run.getObject();

            //        return ( (POAImpl) (childMemory.getScope()) ).create_reference(intf);
             */
        }
    public org.omg.CORBA.Object create_reference_with_id(final byte[] oid,
            final String intf) {
        return null;
        /*

           run.setType(16);
           run.input[0] = oid;
           run.input[1] = intf;
           childMemory.enter(run);
           return (org.omg.CORBA.Object ) run.getObject();
         */
        //return ( (POAImpl) (childMemory.getScope()) ).create_reference_with_id(oid,intf);
    }
    public org.omg.CORBA.Policy[] policy_list() {
        return this.policyList;
    }

    protected void waitForCompletion() {
        // Logger.debug("Wait for completion" + this.the_name() + ":"
        // + numberOfCurrentRequests);
        numberOfCurrentRequests.waitForCompletion();

    }

    public boolean isDestructionApparent() {
        return (poaState > Util.DESTRUCTION_IN_PROGRESS) ? true : false;
    }

    protected final void removePOA(edu.uci.ece.zen.poa.POA poa) {
        this.theChildren.remove(poa.the_name());
    }
    public final edu.uci.ece.zen.orb.ORB getORB() {
        return this.orb;
    }

    POA getChildPOA(String poaName) {
        return null;
        /*
           POA child = (POA) this.theChildren.get(poaName.trim());

           if (child == null || child.isDestructionApparent()) {
           if (adapterActivator == null) {
           throw new org.omg.CORBA.OBJECT_NOT_EXIST("No adapter activator exists for "
           + poaName);
           }
           if (((POAManager) poaManager).isDiscarding()) {
           throw new org.omg.CORBA.TRANSIENT("Parent is in discarding state");
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

        return child;*/
    }

    protected void validateProcessingState() {

        switch (this.processingState) {

            case Util.DISCARDING:
                throw new org.omg.CORBA.TRANSIENT("Destruction of the POA in progress",
                        1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);

            case Util.INACTIVE:
                throw new org.omg.CORBA.OBJ_ADAPTER("POA Manager associated with the POA is Inactive",
                        1, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
        }
    }

    org.omg.CORBA.Object create_reference_with_object_key(
            final byte[] ok,
            final String intf) {

        run.setType(17);
        run.input[0] = ok;
        run.input[1] = intf;
        childMemory.enter(run);
        return (org.omg.CORBA.Object ) run.getObject();


        //edu.uci.ece.zen.orb.protocols.ProfileList list = ((edu.uci.ece.zen.poa.POAManager) this.poaManager).getAcceptorRegistry().findMatchingProfiles(ok);
        // this.orb.getAcceptorRegistry().findMatchingProfiles (ok);

        //IOR ior = new IOR(list, intf);

        // return edu.uci.ece.zen.orb.ior.IOR.makeCORBAObject(this.orb, ior);
    }

    /*private void activate_object_with_id_and_return_contents(
      final org.omg.PortableServer.Servant p_servant,
      final ObjectID oid)
      throws org.omg.PortableServer.POAPackage.ServantAlreadyActive,
      org.omg.PortableServer.POAPackage.WrongPolicy {
      if (this.retentionStrategy.servantPresent(p_servant)
      && this.uniquenessStrategy.validate(edu.uci.ece.zen.poa.mechanism.IdUniquenessStrategy.UNIQUE_ID)) {

      throw new org.omg.PortableServer.POAPackage.ServantAlreadyActive();
      }

      POAHashMap map = new POAHashMap(oid, p_servant);

      this.retentionStrategy.add(oid, map);
      }*/

    // //////////////////////////////////////////////////////////////////
    // ////                   DATA MEMBERS                         /////
    // /////////////////////////////////////////////////////////////////

    private Run run;
    public MemoryArea currentMemory;
    private ScopedMemory childMemory;
    // -- ZEN ORB ---
    private edu.uci.ece.zen.orb.ORB orb;
    private org.omg.PortableServer.AdapterActivator adapterActivator;

    // --- POA Names relative and Complete Path Names ---
    private String poaName;
    private String poaPath;
    private org.omg.CORBA.Policy[] policyList;
    private SynchronizedInt numberOfCurrentRequests;
    // --- POA Specific references ---
    private POA                                     parent;
    private java.util.Hashtable                     theChildren;
    private org.omg.PortableServer.POAManager       poaManager;
    private POAServerRequestHandler                 serverRequestHandler;

    // ---  Current Number of request executing in the POA ---


    // --- Mutexes POA and varable specific to the create and destroy ops ---
    private Object createDestroyPOAMutex = new byte[0];

    private boolean disableCreatePOA = false;
    private boolean etherealize;


    // -- Index into the Active Demux Map
    private ActiveDemuxLoc poaDemuxIndex;

    // --- State of the POA ---
    private int poaState;
    private int processingState = Util.ACTIVE; // RequestProcessing state

    // --- POA Specific strategies ----

}

