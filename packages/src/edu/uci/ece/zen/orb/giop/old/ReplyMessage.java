/* --------------------------------------------------------------------------*
 * $Id: ReplyMessage.java,v 1.1 2004/01/15 18:25:19 kraman Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.orb.giop;

/**
 * This object represents a response object.
 *
 * @author Raymond Kelfstad
 * @author Krishna Raman
 * @version $Revision: 1.1 $ $Date: 2004/01/15 18:25:19 $
 */
public interface ReplyMessage extends GIOPMessage
{
	/**
	 * @see GIOPMessage#demarshallMessage
	 */
	void demarshallMessage() throws java.io.IOException;

	/**
	 * @see GIOPMessage#marshallHeader
	 */
	void marshallHeader() throws java.io.IOException;

	/**
	 * Returns the status of this reply message.
	 */
	int getReplyStatus();

	/**
	 * Returns the unique Id of the request message.
	 */
	int getRequestId();

	/**
	 * Return all the service contexts that were sent with this message.
	 */
	org.omg.IOP.ServiceContext[] getServiceContext();

	/**
	 * Set the service context for this message
	 */
	void setServiceContext(org.omg.IOP.ServiceContext[] list);

	/**
	 * @see GIOPMessage#handle
	 */
	void handle(edu.uci.ece.zen.orb.protocols.Transport transport);
}
