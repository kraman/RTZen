package edu.uci.ece.zen.poa.mechanism;


import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.IntHolder;
import edu.uci.ece.zen.orb.*;
import edu.uci.ece.zen.orb.giop.type.*;
import edu.uci.ece.zen.poa.*;
import edu.uci.ece.zen.utils.*;
import javax.realtime.*;

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
    public Object getRequestProcessor(int name, IntHolder excpetionValue )
    {
        excpetionValue.value = POARunnable.NoException;
        if ( this.validate(name , excpetionValue )) {
            if (this.servant != null) {
                return this.servant;
            } else {
                excpetionValue.value = POARunnable.ObjNotActiveException;
               return null; 
            }
        } else {
            excpetionValue.value = POARunnable.WrongPolicyException;
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
    public void handleRequest( RequestMessage request, edu.uci.ece.zen.poa.POA poa, edu.uci.ece.zen.poa.SynchronizedInt requests, IntHolder exceptionValue ) {
        exceptionValue.value = POARunnable.NoException;
        if (this.retentionStrategy != null) {
                int tmp = handleIfRetain(request, poa, requests, exceptionValue);
            } // can thorw ObjectNotActive, ClassCastException and WrongPolicy
        if (exceptionValue.value != 0) 
        {
            //something happened here and nothing is done
            exceptionValue.value = POARunnable.NoException;
        }
        
        POAImpl pimpl = (POAImpl) ((ScopedMemory)poa.poaMemoryArea).getPortal();

        FString okey = pimpl.getFString();
        FString oid = pimpl.getFString();
        request.getObjectKey( okey );
        ObjectKeyHelper.getId( okey , oid );

        // handling as if non retain is in place
        if (this.servant == null) {
            exceptionValue.value = POARunnable.ObjAdapterException;
            pimpl.retFString( okey );
            pimpl.retFString( oid );
            return;
        }

        org.omg.PortableServer.Servant myServant = this.servant;
        org.omg.CORBA.portable.InvokeHandler invokeHandler = (org.omg.CORBA.portable.InvokeHandler)this.servant;

        synchronized (mutex) {
            requests.increment();
        }

        ((POACurrent)pimpl.poaCurrent.get()).init(poa,okey, this.servant);

        ResponseHandler responseHandler = new ResponseHandler(poa.getORB(),request);

        this.threadPolicyStrategy.enter(invokeHandler);

        CDROutputStream reply;
        if (request.getOperation().equals("_is_a") )
        {
            boolean _result = myServant._is_a(request.getCDRInputStream().read_string());
            org.omg.CORBA.portable.OutputStream _output = responseHandler.createReply();
            _output.write_boolean(_result);
            reply = (CDROutputStream) _output;
        }
        else if (request.getOperation().equals("_non_existent") )
        {
            boolean _result = myServant._non_existent();
            org.omg.CORBA.portable.OutputStream _output = responseHandler.createReply();
            _output.write_boolean(_result);
            reply = (CDROutputStream) _output;
        }
        else
        {
            reply = (CDROutputStream)
                invokeHandler._invoke(request.getOperation(),
                (org.omg.CORBA.portable.InputStream) request.getCDRInputStream(),
                responseHandler);
        }
        pimpl.retFString( okey );
        pimpl.retFString( oid );

        this.threadPolicyStrategy.exit(invokeHandler);

        // --Post Invoke
        synchronized (mutex) {
            requests.decrementAndNotifyAll( poa.poaState == POA.DESTRUCTION_APPARANT);
        }
        //reply.sendUsing(request.getTransport());
        //return edu.uci.ece.zen.orb.ServerRequestHandler.REQUEST_HANDLED;
    }

    private void handleIfRetain( RequestMessage request, edu.uci.ece.zen.poa.POA poa, edu.uci.ece.zen.poa.SynchronizedInt requests, IntHolder exceptionValue)
    {
        exceptionValue.value = POARunnable.NoException;
        POAImpl pimpl = (POAImpl) ((ScopedMemory)poa.poaMemoryArea).getPortal();
        FString ok = pimpl.getFString();
        FString oid = pimpl.getFString();
        request.getObjectKey( ok );
        ObjectKeyHelper.getId( ok , oid );

        edu.uci.ece.zen.poa.POAHashMap okey = this.retentionStrategy.getHashMap( oid , exceptionValue );
        if( exceptionValue.value != POARunnable.NoException ){
            pimpl.retFString( ok );
            pimpl.retFString( oid );
        }

        org.omg.PortableServer.Servant myServant = okey.getServant();

        org.omg.CORBA.portable.InvokeHandler invokeHandler = (org.omg.CORBA.portable.InvokeHandler) okey.getServant();

        if (okey == null || !okey.isActive()) {
            exceptionValue.value = POARunnable.ObjNotExistException;
            pimpl.retFString( ok );
            pimpl.retFString( oid );            
            return;
        }
        
        // --PRE-INVOKE
        synchronized (mutex) {
            okey.incrementActiveRequests();
            requests.increment();
        }

        
        ((POACurrent)pimpl.poaCurrent.get()).init(poa,ok, okey.getServant() );

        ResponseHandler responseHandler = new ResponseHandler(poa.getORB(),request);

        this.threadPolicyStrategy.enter(invokeHandler);
        CDROutputStream reply;
        if (request.getOperation().equals("_is_a") )
        {
            boolean _result = myServant._is_a(request.getCDRInputStream().read_string());
            org.omg.CORBA.portable.OutputStream _output = responseHandler.createReply();
            _output.write_boolean(_result);
            reply = (CDROutputStream) _output;
        }
        else if (request.getOperation().equals("_non_existent") )
        {
            boolean _result = myServant._non_existent();
            org.omg.CORBA.portable.OutputStream _output = responseHandler.createReply();
            _output.write_boolean(_result);
            reply = (CDROutputStream) _output;
        }
        else
        {
            reply = (CDROutputStream)
                invokeHandler._invoke(request.getOperation(),
                (org.omg.CORBA.portable.InputStream) request.getCDRInputStream(),
                responseHandler);
        }
        pimpl.retFString( ok );
        pimpl.retFString( oid );

        this.threadPolicyStrategy.exit(invokeHandler);

        // --Post Invoke
        synchronized (mutex) {
            requests.decrementAndNotifyAll( poa.poaState == POA.DESTRUCTION_APPARANT );
            okey.decrementActiveRequestsAndDeactivate();
        }
        //reply.sendUsing(request.getTransport());
        //return edu.uci.ece.zen.orb.ServerRequestHandler.REQUEST_HANDLED;
    }

    private static final int name = RequestProcessingStrategy.DEFAULT_SERVANT;

    protected ServantRetentionStrategy retentionStrategy = null;
    protected org.omg.PortableServer.Servant servant;
    protected ThreadPolicyStrategy threadPolicyStrategy;

    // --- MUTEX ---
    private Object mutex = new byte[0];
}

