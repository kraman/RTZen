/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.orb.portableInterceptor;

import edu.uci.ece.zen.orb.*;
import org.omg.GIOP.ReplyStatusType_1_0;
import java.util.Vector;
import edu.uci.ece.zen.utils.ZenProperties;
/**
  Client side functionality of portable interceptors.

  WARNING: The issues in the CORBA 3.0 spec: "21.3.7.2 Additional Client-side Details"
  have not yet been addressed here yet.

  @author Mark Panahi
*/

public aspect PIClientAspect{

    private static final int SEND_REQUEST = 0;
    private static final int SEND_POLL = 1;
    private static final int RECEIVE_REPLY = 2;
    private static final int RECEIVE_EXCEPTION = 3;
    private static final int RECEIVE_OTHER = 4;

    private ClientRequestInfo clientInfo;

    static Vector clientInterceptors;

    //indicates when the starting int. point have been completed
    private static boolean startPointsComplete;

    //a new exception that may be thrown in an interception point
    private static org.omg.CORBA.SystemException newException;

    /***********BEGIN CLIENT REQUEST INTERCEPTOR********************/

    //We need to get the target object ref first
    pointcut clientGetObj(org.omg.CORBA.portable.Delegate obj, org.omg.CORBA.Object self):
        execution(* *..ObjRefDelegate.request(..)) &&
                args(self,..) //any ObjRefDelegate.request() method where "self" is first arg
                && target(obj);

    before(org.omg.CORBA.portable.Delegate obj, org.omg.CORBA.Object self):
            clientGetObj(obj,self) {

        clientInterceptors = PIAspect.interceptorList.getClientInterceptors();

        if(clientInterceptors != null) {
            clientInfo = new ClientRequestInfo();
            clientInfo.target = self;

            // for later -- can get this from ObjRefDel.init(..)
            //clientInfo.profiles = ((edu.uci.ece.zen.orb.ObjRefDelegate)obj).getIOR().getProfileList();
        }
    }

    //This pointcut defines where to install calls to the client request interceptors
    //We'll do it right before marshalling the message
    pointcut send_request(edu.uci.ece.zen.orb.protocol.type.RequestMessage obj):
        execution(* edu.uci.ece.zen.orb.protocol.type.RequestMessage.init(..)) && target(obj);

    before(edu.uci.ece.zen.orb.protocol.type.RequestMessage obj): send_request(obj) {

        if(clientInterceptors != null) {

            clientInfo.requestMessage = obj;

            //for later
            //clientInfo.current = PIAspect.getCurrent();

            //invoke send_request intercepion point
            iterate(SEND_REQUEST,clientInterceptors,clientInfo,false);
        }
    }

    // this defines behavior that should occur if either a reply is received normally, or an expection is received
    // i.e. RECEIVE_REPLY or RECEIVE_EXCEPTION
    pointcut client(org.omg.CORBA.portable.Delegate obj, org.omg.CORBA.Object self, org.omg.CORBA.portable.OutputStream os):
        execution(* org.omg.CORBA.portable.Delegate.invoke(..)) && args(self,os) && target(obj);

    // retruns normally
    after(org.omg.CORBA.portable.Delegate obj, org.omg.CORBA.Object self, org.omg.CORBA.portable.OutputStream os)
        returning (org.omg.CORBA.portable.InputStream is):client(obj,self,os) {
        if(clientInterceptors != null) {
            iterate(RECEIVE_REPLY,clientInterceptors,clientInfo,false);

        if(newException != null)
            throw newException;
            //clientInfo.result = reply.istream.read_any();
        }
    }

    // receives exception
    after(org.omg.CORBA.portable.Delegate obj, org.omg.CORBA.Object self, org.omg.CORBA.portable.OutputStream os)
        throwing (java.lang.Exception e):client(obj,self,os) {
        if(clientInterceptors != null) {

            boolean userException = false;


            //check for user exception here and set completed status to yes
            if(e instanceof org.omg.CORBA.portable.ApplicationException){
                userException = true;
            } else if(e instanceof org.omg.CORBA.portable.ApplicationException){
               // clientInfo.received_exception = org.omg.CORBA.SystemExceptionHelper.insert();
            }
            iterate(RECEIVE_EXCEPTION,clientInterceptors,clientInfo,userException);

            if(newException != null)
                throw newException;

        }
    }

    //NOTE: I still havn't implemented SEND_POLL or RECEIVE_OTHER

    /***********END CLIENT REQUEST INTERCEPTOR********************/


    /***** ITERATOR ******/

    private static void iterate(int initialType, Vector clientInterceptors,
                                ClientRequestInfo clientInfo, boolean userException) {
        int type = initialType;
        boolean forward = true;  //direction in which we traverse the list
        int count = 0;

        //ending int. points
        if(!(type == SEND_REQUEST || type == SEND_POLL))
        {
            //cannot call ending int. points if staring points not complete...
            if(!startPointsComplete)
            {
                ////Logger.warn("Cannot call ending interception points.");
                return;
            }

            //...otherwise, reverse the direction...
            forward = false;
            //...and start from the end of the list
            count = clientInterceptors.size() - 1;
        }
        //starting int. points
        else
        {
            //make sure these are reset everytime this is called
            startPointsComplete = true;
            newException = null;
        }

        while((forward && count < clientInterceptors.size()) || (!forward && count >= 0)) {
            try {
                switch(type) {
                    case SEND_REQUEST:
                        ((org.omg.PortableInterceptor.ClientRequestInterceptor)
                        (clientInterceptors.get(count))).send_request(clientInfo);
                    break;

                    case SEND_POLL:
                        ((org.omg.PortableInterceptor.ClientRequestInterceptor)
                        (clientInterceptors.get(count))).send_poll(clientInfo);
                    break;

                    case RECEIVE_REPLY:
                        ((org.omg.PortableInterceptor.ClientRequestInterceptor)
                        (clientInterceptors.get(count))).receive_reply(clientInfo);
                    break;

                    case RECEIVE_EXCEPTION:
                        ((org.omg.PortableInterceptor.ClientRequestInterceptor)
                        (clientInterceptors.get(count))).receive_exception(clientInfo);
                    break;

                    case RECEIVE_OTHER:
                        ((org.omg.PortableInterceptor.ClientRequestInterceptor)
                        (clientInterceptors.get(count))).receive_other(clientInfo);
                    break;

                    default:
                        ZenProperties.logger.log("Client interceptor of type " + type + " not found.");

                }
            }
            catch(org.omg.PortableInterceptor.ForwardRequest fr) {
                throw new org.omg.CORBA.NO_IMPLEMENT();
            }
            catch(org.omg.CORBA.SystemException se) {

                //exInfo(se);

                //take care of completion status
                if(type != RECEIVE_EXCEPTION) {   //same as original comp status

                    if(type == RECEIVE_REPLY)
                        se.completed = org.omg.CORBA.CompletionStatus.COMPLETED_YES;
                    else
                        se.completed = org.omg.CORBA.CompletionStatus.COMPLETED_NO;
                }
                else if (userException)
                    se.completed = org.omg.CORBA.CompletionStatus.COMPLETED_YES;

                //go in reverse
                forward = false;
                type = RECEIVE_EXCEPTION;
                startPointsComplete = false;
                newException = se;
                ZenProperties.logger.log("New Completion status: " + se.completed.value());
            }
            finally {
                //update counter appropriately
                if(forward)
                    count++;
                else
                    count--;
            }
        }

    }
//
//    //hook to weave in debug info
//    static void exInfo(org.omg.CORBA.SystemException se){
//    }
}
















