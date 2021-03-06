/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

/*
 * package edu.uci.ece.zen.orb; /* * ZEN exception handler class to handle an
 * exception that occured during request processing. @author Mayur Deshpande
 * @author Arvind Krishna @author Krishna Raman
 * 
 * @version $Revision: 1.5 $ $Date: 2003/07/14 20:45:51 $ / public class
 *          ExceptionHandler { /* * Function to handle server size exceoptions
 *          @param ex The exception to handle. @param message The ressage that
 *          caused the exception to be thrown. @param transport The transport
 *          object associated with the message. @param orb The ORB associated
 *          with the transport. / public static void
 *          handleException(org.omg.CORBA.SystemException ex, RequestMessage
 *          message, Transport transport, ORB orb) { // Logger.debug("Handling
 *          system exception:" + ex); // print the Exception stack trace at the
 *          Server! ex.printStackTrace(); // create an Exception Reply and
 *          marshall the exception to the client ServerReply reply = new
 *          ServerReply(orb, message.getRequestId(),
 *          message.getGIOPVersion().major, message.getGIOPVersion().minor,
 *          org.omg.GIOP.ReplyStatusType_1_0._SYSTEM_EXCEPTION); // marshall the
 *          exception across to the Client SystemExceptionHelper.write(reply,
 *          ex); // use the transport and send the message across to the client
 *          reply.sendUsing(transport); } }
 */