package edu.uci.ece.zen.poa.mechanism;


import org.omg.CORBA.CompletionStatus;

import edu.uci.ece.zen.orb.ResponseHandler;
import edu.uci.ece.zen.orb.ServerReply;
import edu.uci.ece.zen.orb.ServerRequest;


public class DefaultServantStrategy extends RequestProcessingStrategy {
    /**
     *
     * @param retain ServantRetentionStrategy
     * @param threadStrategy ThreadPolicyStrategy
     * @throws org.omg.PortableServer.POAPackage.InvalidPolicy
     */
    public void initialize(ServantRetentionStrategy retain,
            ThreadPolicyStrategy threadStrategy, IntHolder exceptionValue) 
    {
        exceptionValue.value = POARunnable.NoException;
        if (retain instanceof ServantRetentionStrategy) 
        {
            if(retain instanceof RetainStrategy)
                this.retentionStrategy = (RetainStrategy) retain;
        }
        else 
        {
            this.retentionStrategy = (NonRetainStrategy) retain;
            this.threadPolicyStrategy = threadStrategy;
        }

    }

    /**
     *
     * @param servant java.lang.Object
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public synchronized void setInvokeHandler(java.lang.Object servant, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.NoException;
        if(servant instanceof org.omg.PortableServer.Servant)
        {
            this.servant = (org.omg.PortableServer.Servant) servant;
        } else {
            exceptionValue.value = POARunnable.WrongPolicyException;
            return null;
        }

    }

    /**
     *
     * @param policyName
     * @return boolean
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     */
    public boolean validate(int policyName, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.NoException;
        if (RequestProcessingStrategy.DEFAULT_SERVANT == policyName) {
            return true;
        } else {
            exceptionValue.value = POARunnable.WrongPolicyException;
            return false;
        }
    }

    /**
     *
     * @param name int
     * @return Object
     * @throws org.omg.PortableServer.POAPackage.WrongPolicy
     * @throws org.omg.PortableServer.POAPackage.ObjectNotActive
     */
    public Object getRequestProcessor(int name, IntHolder excpetionValue)
    {
        exceptionValue.value = POARunnable.NoException;
        if (this.validate(name)) {
            if (this.servant != null) {
                return this.servant;
            } else {
                exceptionValue.value = POARunnable.ObjectNotActiveException;
               return null; 
            }
        } else {
            exceptionValue.value = POARunnable.WrongPolicyException;
            return null;
        }

    }

    /**
     *
     * @param request ServerRequest
     * @param poa edu.uci.ece.zen.poa.POA
     * @param requests edu.uci.ece.zen.poa.SynchronizedInt
     * @return int
     */
    public int handleRequest(ServerRequest request, edu.uci.ece.zen.poa.POA poa, edu.uci.ece.zen.poa.SynchronizedInt requests, exceptionValue) {
        exceptionValue.value = POARunnable.NoException;
        if (this.retentionStrategy != null) {
                int tmp = handleIfRetain(request, poa, requests, exceptionValue);
            } // can thorw ObjectNotActive, ClassCastException and WrongPolicy
        if (exceptionValue.value != 0) 
        {
            //something happened here and nothing is done
            exceptionValue.value = POARunnable.NoException;
        }

        edu.uci.ece.zen.poa.ObjectKey ok = request.getObjectKey();
        byte[] oid = ok.getId();

        // handling as if non retain is in place
        if (this.servant == null) {
            exceptionValue.value = POARunnable.ObjAdapterException;
            return null;
        }

        org.omg.PortableServer.Servant myServant = this.servant;
        org.omg.CORBA.portable.InvokeHandler invokeHandler = (org.omg.CORBA.portable.InvokeHandler)this.servant;

        synchronized (mutex) {
            requests.increment();
        }

        edu.uci.ece.zen.poa.ThreadSpecificPOACurrent.putInvocationContext(poa,
                ok, this.servant);

        ResponseHandler responseHandler = new ResponseHandler(poa.getORB(),
                request.message.getRequestId(),
                request.message.getGIOPVersion().major,
                request.message.getGIOPVersion().minor);

        this.threadPolicyStrategy.enter(invokeHandler);

        ServerReply reply;
        if (request.message.getOperation().equals("_is_a") )
        {
        		boolean _result = myServant._is_a(request.message.getIstream().read_string());
                        org.omg.CORBA.portable.OutputStream _output = responseHandler.createReply();
                        _output.write_boolean(_result);
                        reply = (ServerReply) _output;

        }
        else if (request.message.getOperation().equals("_non_existent") )
                 {
                 	boolean _result = myServant._non_existent();
                        org.omg.CORBA.portable.OutputStream _output = responseHandler.createReply();
                        _output.write_boolean(_result);
                        reply = (ServerReply) _output;

                  }
       else


                  {
        		reply = (ServerReply)
                	invokeHandler._invoke(request.message.getOperation(),
                	request.message.getIstream(), responseHandler);
                  }


        this.threadPolicyStrategy.exit(invokeHandler);

        // --Post Invoke
        synchronized (mutex) {
            requests.decrementAndNotifyAll(poa.isDestructionApparent());
        }
        reply.sendUsing(request.getTransport());
        return edu.uci.ece.zen.orb.ServerRequestHandler.REQUEST_HANDLED;
    }

    private int handleIfRetain(ServerRequest request,
            edu.uci.ece.zen.poa.POA poa,
            edu.uci.ece.zen.poa.SynchronizedInt requests, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.NoException;
        edu.uci.ece.zen.poa.ObjectKey ok = request.getObjectKey();
        byte[] oid = ok.getId();

        edu.uci.ece.zen.poa.POAHashMap okey = this.retentionStrategy.getHashMap(oid);

        org.omg.PortableServer.Servant myServant = okey.getServant();

        org.omg.CORBA.portable.InvokeHandler invokeHandler = (org.omg.CORBA.portable.InvokeHandler) okey.getServant();

        if (okey == null || !okey.isActive()) {
            exceptionValue.value = POARunnable.ObjNotExistException;
            return null;
        }
        // --PRE-INVOKE
        synchronized (mutex) {
            okey.incrementActiveRequests();
            requests.increment();
        }

        edu.uci.ece.zen.poa.ThreadSpecificPOACurrent.putInvocationContext(poa,
                ok, okey.getServant());

        ResponseHandler responseHandler = new ResponseHandler(poa.getORB(),
                request.message.getRequestId(),
                request.message.getGIOPVersion().major,
                request.message.getGIOPVersion().minor);

        this.threadPolicyStrategy.enter(invokeHandler);
        ServerReply reply;
        if (request.message.getOperation().equals("_is_a") )
        {
        		boolean _result = myServant._is_a(request.message.getIstream().read_string());
                        org.omg.CORBA.portable.OutputStream _output = responseHandler.createReply();
                        _output.write_boolean(_result);
                        reply = (ServerReply) _output;

        }
        else if (request.message.getOperation().equals("_non_existent") )
                 {
                 	boolean _result = myServant._non_existent();
                        org.omg.CORBA.portable.OutputStream _output = responseHandler.createReply();
                        _output.write_boolean(_result);
                        reply = (ServerReply) _output;

                  }
       else


                  {
        		reply = (ServerReply)
                	invokeHandler._invoke(request.message.getOperation(),
                	request.message.getIstream(), responseHandler);
                  }

        this.threadPolicyStrategy.exit(invokeHandler);

        // --Post Invoke
        synchronized (mutex) {
            requests.decrementAndNotifyAll(poa.isDestructionApparent());
            okey.decrementActiveRequestsAndDeactivate();
        }
        reply.sendUsing(request.getTransport());
        return edu.uci.ece.zen.orb.ServerRequestHandler.REQUEST_HANDLED;

    }

    private static final int name = RequestProcessingStrategy.DEFAULT_SERVANT;

    protected ServantRetentionStrategy retentionStrategy = null;
    protected org.omg.PortableServer.Servant servant;
    protected ThreadPolicyStrategy threadPolicyStrategy;

    // --- MUTEX ---
    private Object mutex = new byte[0];
}
