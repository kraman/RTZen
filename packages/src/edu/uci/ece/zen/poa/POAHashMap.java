/* --------------------------------------------------------------------------*
 * $Id: POAHashMap.java,v 1.2 2004/03/11 19:31:34 nshankar Exp $
 *--------------------------------------------------------------------------*/


package edu.uci.ece.zen.poa;

import javax.realtime.*;
import edu.uci.ece.zen.utils.*;
import java.util.Properties;






public class POAHashMap {
	private static Queue unusedFacades;
    	public static void init()
    	{


            //Set up Max No of servants
            int numFacades = Integer.parseInt( ZenProperties.getGlobalProperty( "doc.zen.poa.maxNumServants" , "1" ) );
            unusedFacades = new Queue();;
            for( int i=0;i<numFacades;i++ )
                unusedFacades.enqueue( new POAHashMap() );

    }

    public static POAHashMap initialize(byte[] oid,
            org.omg.PortableServer.Servant servant) {

            POAHashMap retVal =  (POAHashMap) unusedFacades.dequeue();

             if(MemoryArea.getMemoryArea(servant) instanceof ScopedMemory)
			retVal.servantMemory = (ScopedMemory)MemoryArea.getMemoryArea(servant);
              else retVal.servant = servant;
	      retVal.objId = oid;
              return retVal;
    }

    public static void enqueue( POAHashMap ret)
    {
    	unusedFacade.enqueue(ret);
    }

    /**
     *  Increment the number of requests currently serviced by the
     *  servant. Precondition: This method should be called from within
     *  a synchronized block.
     */

    // Called from within a synchronized block
    public void incrementActiveRequests() {
        ++requests;
    }

    /**
     * Decrement the number of active requests serviced by the servant.
     * If the number falls to 0, then notify the threads waiting to
     * deactivate the entry from the AOM.
     */

    // Called from within a synchronized block
    public void decrementActiveRequestsAndDeactivate() {
        if (--this.requests == 0 && !this.active) {
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    /**
     * Check if this entry is active
     * @return boolean true if active, false if not active
     */

    public boolean isActive() {
        return this.active;
    }

    /**
     * Check if there are any active requests being serviced
     * @return boolean
     */

    public boolean servicingRequests() {
        return (isActive() && this.requests != 0) ? true : false;
    }

    /**
     * Deactivate the entry. This method sets the active flag to true and
     * does not remove the entry fromm the AOM. This is done using the
     * decrementActiveRequestsAndDeactivate() method that checks if active
     * falg is set to false and removes the entry from the AOM.
     */

    public void deactivate() {
        this.active = false;
    }


    /**
     * Synchronization point for threads waiting for this servant to be
     * deactivated. These could be activate_object() method waiting for
     * the destruction of the servant inorder to reactivate it again.
     * destroy_POA() method also waits for deactivation of each of the
     * object ids in the corresponding POA.
     *
     */
    public synchronized void waitForDestruction() {
        try {
            wait();
        } catch (InterruptedException ex) {
            edu.uci.ece.zen.orb.Logger.warn("WaitForDestruction for ObjectKey: Interrupted");
        }
    }

    /**
     * Return the servant associated with this entry
     * @return org.omg.PortableServer.Servant
     */

    public org.omg.PortableServer.Servant getServant() {
    		if (servant!= null) return this.servant;
                else
		return (org.omg.PortableServer.Servant) servantMemory.getPortal();

    }


    /**
     * Return ObjectID associated with this entry
     * @return ObjectID
     */

    public byte[] objectID() {
        return this.objId;
    }

    private int requests = 0;

    // --Making this volatile does not need to be synchronized
    private volatile boolean active = true;
    private org.omg.PortableServer.Servant servant;
	private ScopedMemory servantMemory;

    // have the object Id too, done for fast demuxing requests
    private byte[] objId;

}
