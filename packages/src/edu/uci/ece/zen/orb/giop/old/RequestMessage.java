/* --------------------------------------------------------------------------*
 * $Id: RequestMessage.java,v 1.1 2004/01/15 18:25:19 kraman Exp $
 *--------------------------------------------------------------------------*/

package edu.uci.ece.zen.orb.giop;

import java.util.Vector;

/**
 * This object represents a request message object.
 *
 * @author Raymond Kelfstad
 * @author Krishna Raman
 * @version $Revision: 1.1 $ $Date: 2004/01/15 18:25:19 $
 */
public interface RequestMessage extends GIOPMessage
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
	 * Returns the unique if for this request.
	 */
	int getRequestId();

	/**
	 * Returns the object key of the servant that the request is for.
	 */
	edu.uci.ece.zen.poa.ObjectKey getObjectKey();

	String getOperation();

	boolean getResponseExpected();

	/**
	 * @see GIOPMessage#handle
	 */
	void handle(edu.uci.ece.zen.orb.protocols.Transport transport);

	/**
	 * Return all the service contexts that were sent with this message.
	 */
	Vector getServiceContext();

	/**
	 * Set the service context for this message
	 */
	void setServiceContext(Vector list);
}
