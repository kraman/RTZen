package edu.uci.ece.zen.orb.portableInterceptor;

import edu.uci.ece.zen.orb.*;
import org.omg.GIOP.ReplyStatusType_1_0;
import java.util.Vector;
//import org.aspectj.lang.JoinPoint;

/**
  Server side functionality of portable interceptors.

  @author Mark Panahi
*/

public privileged aspect PIServerAspect{
//
//    private ServerRequestInfo serverInfo;
//    private IORInfo iorInfo;
//
//    static java.util.Vector serverInterceptors;
//    private java.util.Vector iorInterceptors;
//
//    private static final int RECEIVE_REQUEST_SERVICE_CONTEXTS = 0;
//    private static final int RECEIVE_REQUEST = 1;
//    private static final int SEND_REPLY = 2;
//    private static final int SEND_EXCEPTION = 3;
//    private static final int SEND_OTHER = 4;
//
//    //indicates when the starting int. point have been completed
//    private static boolean startPointsComplete = true;
//
//    //a new exception that may be thrown in an interception point
//    private static org.omg.CORBA.SystemException newException;
//
//    private static edu.uci.ece.zen.orb.protocols.Transport transport;
//    private static edu.uci.ece.zen.orb.giop.RequestMessage msg;
//    private static edu.uci.ece.zen.orb.giop.ReplyMessage replyMsg;
//
//
//    /***********BEGIN SERVER REQUEST INTERCEPTOR********************/
//
//    //pointcut getTransport(edu.uci.ece.zen.orb.protocols.iiop.ServerTransport obj):
//        //execution(* edu.uci.ece.zen.orb.protocols.Transport.handleEvent(..)) && target(obj);
//
//    // Getting a reference to the transport
//    pointcut getTransport(edu.uci.ece.zen.orb.protocols.Transport obj):
//        (execution(* edu.uci.ece.zen.orb.protocols.iiop.*.*ServerTransport.handleEvent(..)) ) && target(obj);
//
//    before(edu.uci.ece.zen.orb.protocols.Transport obj):
//    //before(edu.uci.ece.zen.orb.protocols.iiop.ServerTransport obj):
//        getTransport(obj) {
//        //Logger.warn("JOINPOINT: " + thisJoinPoint.getSignature().toLongString() );
//        transport = obj;
//    }
//
//    // Location at which we pick out the location of the receive_request_service_contexts
//    // interception point.  The rest of the interception point pointcuts should be self-explanatory.
//    public pointcut receive_request_service_contexts(edu.uci.ece.zen.orb.giop.RequestMessage obj):
//        execution(* edu.uci.ece.zen.orb.giop.RequestMessage.demarshallMessage(..)) && target(obj);
//
//    after(edu.uci.ece.zen.orb.giop.RequestMessage obj):
//        receive_request_service_contexts(obj) {
//
//        serverInterceptors = PIAspect.interceptorList.getServerInterceptors();
//        msg = obj;
//
//        if(serverInterceptors != null) {
//            serverInfo = new ServerRequestInfo();
//            serverInfo.requestMessage = obj;
//            //serverInfo.request_service_context_list = obj.getServiceContext();
//            // I think this is in a different thread than in receive_request, so there will be two currents.
//            //But this probably shouldn't be created here.  I needed it so that I could get an example I have to work.
//            serverInfo.current = PIAspect.getCurrent();
//
//            // These are probably not available at this point
//            //serverInfo.request_id = obj.getRequestId();
//            //serverInfo.operation = obj.getOperation();
//            //serverInfo.response_expected = obj.getResponseExpected();
//
//            iterate(RECEIVE_REQUEST_SERVICE_CONTEXTS, serverInterceptors, serverInfo, false);
//
//           //Logger.warn("server interceptors present.");
//        }
//
//    }
//
//    // please read the spec about "starting interception points"
//    // If these starting interception point have already been executed, then we don't
//    // want "handle to be called.
//    pointcut avoidHandle():execution(* edu.uci.ece.zen.orb.giop.RequestMessage.handle(..));
//
//    void around(): avoidHandle() {
//        if(startPointsComplete)
//        {
//            proceed();
//        }
//    }
//
//    pointcut receive_request( edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy obj,
//                     edu.uci.ece.zen.orb.ServerRequest request, edu.uci.ece.zen.poa.POA poa,
//                                       edu.uci.ece.zen.poa.SynchronizedInt requests):
//        execution(* edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy.handleRequest(..)) &&
//                    args(request,poa,requests) && target(obj);
//
//    before(edu.uci.ece.zen.poa.mechanism.RequestProcessingStrategy obj,
//                     edu.uci.ece.zen.orb.ServerRequest request, edu.uci.ece.zen.poa.POA poa,
//                     edu.uci.ece.zen.poa.SynchronizedInt requests):receive_request(obj,request,poa,requests) {
//
//        if(serverInterceptors != null) {
//
//            //serverInfo.request_id = request.message.getRequestId();
//            //serverInfo.operation = request.message.getOperation();
//            //serverInfo.response_expected = request.message.getResponseExpected();
//
//            serverInfo.current = PIAspect.getCurrent();
//            serverInfo.request = request;
//            serverInfo.poa = poa;
//
//            iterate(RECEIVE_REQUEST, serverInterceptors, serverInfo, false);
//        }
//    }
//
//    // get a reference to the server reply object
//    static edu.uci.ece.zen.orb.ServerReply serverReply = null;
//
//    pointcut getServerReply(edu.uci.ece.zen.orb.ServerReply obj):execution(edu.uci.ece.zen.orb.ServerReply.new(..)) && target(obj);
//
//    before(edu.uci.ece.zen.orb.ServerReply obj):getServerReply(obj) {
//        serverReply = obj;
//    }
//
//
//    // Handle sending the reply and throw an exception if necessary
//    // This pointcut includes handling SEND_REPLY, SEND_EXCEPTION, and SEND_OTHER
//    static boolean needToSendException = false;
//
//    pointcut send_reply(edu.uci.ece.zen.orb.giop.ReplyMessage message):
//        execution(* edu.uci.ece.zen.orb.giop.ReplyMessage.marshallHeader(..)) && target(message);
//
//    before(edu.uci.ece.zen.orb.giop.ReplyMessage message):send_reply(message) {
//
//        replyMsg = message;
//        if(message != null && serverInterceptors != null)
//        {
//            switch(message.getReplyStatus()) {
//                case ReplyStatusType_1_0._NO_EXCEPTION:
//                    //op = ServerInterceptorIterator.SEND_REPLY;
//                    serverInfo.reply_status = PIAspect.SUCCESSFUL;
//                    iterate(SEND_REPLY, serverInterceptors, serverInfo, false);
//                    break;
//
//                case ReplyStatusType_1_0._USER_EXCEPTION :
//                    serverInfo.exception = ORB.init().create_any();
//                    serverInfo.reply_status = PIAspect.USER_EXCEPTION;
//                    iterate(SEND_EXCEPTION, serverInterceptors, serverInfo, true);
//                    serverInfo.exception = null;
//                    break;
//
//                case ReplyStatusType_1_0._SYSTEM_EXCEPTION :
//                    serverInfo.exception = ORB.init().create_any();
//                    serverInfo.reply_status = PIAspect.SYSTEM_EXCEPTION;
//                    iterate(SEND_EXCEPTION, serverInterceptors, serverInfo, false);
//                    serverInfo.exception = null;
//                    break;
//
//                case ReplyStatusType_1_0._LOCATION_FORWARD :
//                    serverInfo.reply_status = PIAspect.LOCATION_FORWARD;
//                    //op = ServerInterceptorIterator.SEND_OTHER;
//
//                    iterate(SEND_OTHER, serverInterceptors, serverInfo, false);
//
//                    break;
//            }
//        }
//    }
//
//    after(edu.uci.ece.zen.orb.giop.ReplyMessage message):send_reply(message) {
//
//        if(needToSendException)
//        {
//            //marshal the exception across to the Client
//            org.omg.CORBA.SystemExceptionHelper.write (serverReply, newException);
//
//            needToSendException = false;
//        }
//    }
//
//    /***********END SERVER REQUEST INTERCEPTOR********************/
//
//    /***********BEGIN IOR INTERCEPTOR********************/
//
//    pointcut establish_components():
//        execution(* edu.uci.ece.zen.orb.protocols.Acceptor.createProfile(..));
//
//    after() returning (edu.uci.ece.zen.orb.protocols.Profile prof):establish_components() {
//        iorInterceptors = PIAspect.interceptorList.getIORInterceptors();
//
//        if(iorInterceptors != null)
//        {
//            iorInfo = new IORInfo();
//
//            iorInfo.profile = prof;
//
//            for(int i = 0; i < iorInterceptors.size(); ++i)
//                ((org.omg.PortableInterceptor.IORInterceptor)(iorInterceptors.get(i))).establish_components(iorInfo);
//
//        }
//    }
//
//    /***********END IOR INTERCEPTOR********************/
//
//
//    /***** ITERATOR ******/
//
//    static boolean forward = true;  //direction in which we traverse the list
//    static int count = 0;
//
//    private static void iterate(int initialType, Vector serverInterceptors,
//                                ServerRequestInfo serverInfo, boolean userException) {
//        int type = initialType;
//
//        if(type == SEND_REPLY || type == SEND_EXCEPTION || type == SEND_OTHER)
//        {
//            //cannot call ending int. points if staring points not complete...
//
//            //...otherwise, reverse the direction...
//            forward = false;
//            if(startPointsComplete)
//            {
//                //...and start from the end of the list
//                count = serverInterceptors.size() - 1;
//                Logger.warn("Resetting count: " + count);
//            }
//        }
//        else if(type == RECEIVE_REQUEST)
//        {
//            //if there was an exception in RECEIVE_REQUEST_SERVICE_CONTEXTS
//
//            forward = true;  //direction in which we traverse the list
//            count = 0;
//        }
//        //starting int. points
//        else //if(type == RECEIVE_REQUEST_SERVICE_CONTEXTS)
//        {
//            //make sure these are reset everytime this is called
//            startPointsComplete = true;
//            newException = null;
//            forward = true;  //direction in which we traverse the list
//            count = 0;
//        }
//
//        while((forward && count < serverInterceptors.size()) || (!forward && count >= 0)) {
//            try {
//                switch(type) {
//                    case RECEIVE_REQUEST_SERVICE_CONTEXTS:
//                        ((org.omg.PortableInterceptor.ServerRequestInterceptor)
//                        (serverInterceptors.get(count))).receive_request_service_contexts(serverInfo);
//                    break;
//
//                    case RECEIVE_REQUEST:
//                        ((org.omg.PortableInterceptor.ServerRequestInterceptor)
//                        (serverInterceptors.get(count))).receive_request(serverInfo);
//                    break;
//
//                    case SEND_REPLY:
//                        ((org.omg.PortableInterceptor.ServerRequestInterceptor)
//                        (serverInterceptors.get(count))).send_reply(serverInfo);
//                    break;
//
//                    case SEND_EXCEPTION:
//
//                        ((org.omg.PortableInterceptor.ServerRequestInterceptor)
//                        (serverInterceptors.get(count))).send_exception(serverInfo);
//
//                    break;
//
//                    case SEND_OTHER:
//                        ((org.omg.PortableInterceptor.ServerRequestInterceptor)
//                        (serverInterceptors.get(count))).send_other(serverInfo);
//                    break;
//
//                    default:
//                        Logger.warn("Server interceptor of type " + type + " not found.");
//
//                }
//            }
//            catch(org.omg.PortableInterceptor.ForwardRequest fr) {
//                throw new org.omg.CORBA.NO_IMPLEMENT();
//
//            }
//            catch(org.omg.CORBA.SystemException se) {
//
//                exInfo(se);
//
//                //take care of completion status
//                if(type != SEND_EXCEPTION) {   //same as original comp status
//
//                    if(type == SEND_REPLY)
//                        se.completed = org.omg.CORBA.CompletionStatus.COMPLETED_YES;
//                    else
//                        se.completed = org.omg.CORBA.CompletionStatus.COMPLETED_NO;
//                }
//                else if (userException)
//                    se.completed = org.omg.CORBA.CompletionStatus.COMPLETED_YES;
//
//                Logger.warn("New Completion status: " + se.completed.value());
//                //all ending int. points must be called in the case where
//                //an exception is thrown in an intermediate int. point
//                //if(type == RECEIVE_REQUEST)
//                //    count = serverInterceptors.size();
//
//                //NOTE: If type is RECEIVE_REQUEST, we can just throw the exception
//                //and wait for the SEND_EXCEPTION int. point. to be called normally
//                if(type == RECEIVE_REQUEST)
//                {
//
//                    throw se;
//                }
//
//                else if(type == RECEIVE_REQUEST_SERVICE_CONTEXTS)
//                {
//                    startPointsComplete = false;
//                    //go in reverse
//                    forward = false;
//                    count--;
//                    newException = se;
//                    Logger.warn("Exception handled in PI");
//                    edu.uci.ece.zen.orb.ExceptionHandler.handleException(newException,
//                                             msg,
//                                             transport,
//                                             transport.requestHandler().orb
//                                             );
//
//                    return;
//                }
//                //thrown from ending int point
//                else //if(type == SEND_EXCEPTION)
//                {
//                    Logger.warn("Marshalling exception");
//
//                    needToSendException = true;
//
//
//
//                    ((edu.uci.ece.zen.orb.giop.ReplyMessage_1_0)replyMsg).replyHeader.reply_status =
//                            org.omg.GIOP.ReplyStatusType_1_0.from_int( org.omg.GIOP.ReplyStatusType_1_0._SYSTEM_EXCEPTION);
//
//
//                }
//
//                type = SEND_EXCEPTION;
//
//                newException = se;
//
//            }
//            finally {
//                //update counter appropriately
//                if(forward)
//                    count++;
//                else
//                    count--;
//            }
//        }
//    }
//
//    //hook to weave in debug info
//    static void exInfo(org.omg.CORBA.SystemException se){
//    }
}









