/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.poa;

import org.omg.PortableServer.Servant;

import edu.uci.ece.zen.utils.FString;
import edu.uci.ece.zen.utils.Logger;
import edu.uci.ece.zen.utils.ZenProperties;

// TODO This is not a map. change the name. 
// Provides information about the state of the servant.  
public class POAHashMap {

    private int requests = 0;
    
    private volatile boolean active = true; // Making this volatile does not need to be synchronized

    private FString key;

    private Servant value;

     /** the priority of the servant */
    private int priority; 

    public POAHashMap() {}

    public void init(FString oid, Servant servant, int priority) 
    {        
        this.value = servant;
        this.key = oid;
        this.priority = priority;
    }

    /**
     * Increment the number of requests currently serviced by the servant.
     * Precondition: This method should be called from within a synchronized
     * block.
     */
    // Called from within a synchronized block
    public void incrementActiveRequests() 
    {
        ++requests;
    }

    /**
     * Decrement the number of active requests serviced by the servant. If the
     * number falls to 0, then notify the threads waiting to deactivate the
     * entry from the AOM.
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
     * 
     * @return boolean true if active, false if not active
     */
    public boolean isActive() 
    {
        return this.active;
    }

    /**
     * Check if there are any active requests being serviced
     * 
     * @return boolean
     */

    public boolean servicingRequests() {
        return (isActive() && this.requests != 0) ? true : false;
    }

    /**
     * Deactivate the entry. This method sets the active flag to true and does
     * not remove the entry fromm the AOM. This is done using the
     * decrementActiveRequestsAndDeactivate() method that checks if active falg
     * is set to false and removes the entry from the AOM.
     */

    public void deactivate() {
        this.active = false;
    }

    /**
     * Synchronization point for threads waiting for this servant to be
     * deactivated. These could be activate_object() method waiting for the
     * destruction of the servant inorder to reactivate it again. destroy_POA()
     * method also waits for deactivation of each of the object ids in the
     * corresponding POA.
     */
    public synchronized void waitForDestruction() {
        try {
            wait();
        } catch (InterruptedException ex) {
            ZenProperties.logger.log(Logger.WARN,
                    getClass(), "waitForDestruction",
                    "WaitForDestruction for ObjectKey: Interrupted", ex);
        }
    }

    /**
     * Return the servant associated with this entry
     * 
     * @return org.omg.PortableServer.Servant
     */

    public org.omg.PortableServer.Servant getServant() {
        return this.value;
    }

    /**
     * Return ObjectID associated with this entry
     * 
     * @return ObjectID
     */
    public FString objectID() {
        return this.key;
    }
    
    /**
     * Returns the priority of the servant.
     * @return the priority.
     */
    public int getPriority() 
    {
        return priority;
    }
}