package edu.uci.ece.zen.orb.portableInterceptor;


import org.omg.CORBA.NO_RESOURCES;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.CompletionStatus;
import org.omg.IOP.ServiceContext;
import java.util.Vector;


public class RequestInfo extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.RequestInfo {
    public int request_id() {
        return requestMessage.getRequestId();
    }

    public java.lang.String operation() {
        return requestMessage.getOperation().toString();
    }

    // these can't be accessed in Java -- according to the spec
    public org.omg.Dynamic.Parameter[] arguments() {
        throw new NO_RESOURCES("Can't access this info", 1,
                CompletionStatus.COMPLETED_MAYBE);
    }

    public org.omg.CORBA.TypeCode[] exceptions() {
        throw new NO_RESOURCES("Can't access this info", 1,
                CompletionStatus.COMPLETED_MAYBE);
    }

    public java.lang.String[] contexts() {
        throw new NO_RESOURCES("Can't access this info", 1,
                CompletionStatus.COMPLETED_MAYBE);
    }

    public java.lang.String[] operation_context() {
        throw new NO_RESOURCES("Can't access this info", 1,
                CompletionStatus.COMPLETED_MAYBE);
    }

    public org.omg.CORBA.Any result() {
        throw new NO_RESOURCES("Can't access this info", 1,
                CompletionStatus.COMPLETED_MAYBE);
    }

    public boolean response_expected() {
        return requestMessage.getResponseExpected() != 0;
    }

    public short sync_scope() {
        return sync_scope;
    }

    public short reply_status() {
        return reply_status;
    }

    public org.omg.CORBA.Object forward_reference() {
        return forward_reference;
    }

    public org.omg.CORBA.Any get_slot(int id)
        throws org.omg.PortableInterceptor.InvalidSlot {
        return current.get_slot(id);
    }

    public ServiceContext get_request_service_context(int id) {
//        Vector contexts = requestMessage.getServiceContext();
//
//        /*
//         if (id >= contexts.context_id || id < 0)
//         throw new BAD_PARAM("No entry for the requested id.", 26, CompletionStatus.COMPLETED_MAYBE);
//         */
//        for (int i = 0; i < contexts.size(); ++i) {
//            if (((ServiceContext) contexts.get(i)).context_id == id) {
//                return (ServiceContext) contexts.get(i);
//            }
//        }

        throw new BAD_PARAM("No entry for the requested id.", 26,
                CompletionStatus.COMPLETED_MAYBE);

    }

    public ServiceContext get_reply_service_context(int id) { /*
         if (id >= reply_service_context_list.length || id < 0)
         throw new BAD_PARAM("No entry for the requested id.", 26, CompletionStatus.COMPLETED_MAYBE);

         return reply_service_context_list[id];  */
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    // public int request_id = 0;
    // public String operation = "";

    // These cannot be made available in Java and throw NO_RESOURCES exception with minor code 1
    public org.omg.Dynamic.Parameter[] arguments;
    public org.omg.CORBA.TypeCode[] exceptions;
    public java.lang.String[] contexts;
    public java.lang.String[] operation_context;
    public org.omg.CORBA.Any result;

    // public boolean response_expected = false;
    public short sync_scope = 0;
    public short reply_status = 0;
    public org.omg.CORBA.Object forward_reference;
    public org.omg.CORBA.Any slot;
    // public org.omg.IOP.ServiceContext[] request_service_context_list = null;
    // public org.omg.IOP.ServiceContext[] reply_service_context_list = null;

    public PICurrent current;

    // this is set in the client and server aspects
    // it contains information that is needed by this class
    public edu.uci.ece.zen.orb.protocol.type.RequestMessage requestMessage;

    public edu.uci.ece.zen.orb.protocol.type.ReplyMessage replyMessage;
}
