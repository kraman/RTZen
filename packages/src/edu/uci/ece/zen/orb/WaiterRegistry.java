package edu.uci.ece.zen.orb;

import javax.realtime.*;

/**
 * Based on edu.uci.ece.zen.utils.Hashtable but the keytable is different
 */
public class WaiterRegistry extends edu.uci.ece.zen.utils.Hashtable
{
    public WaiterRegistry(){
    }

    public void registerWaiter( int key , ScopedMemory mem ) throws edu.uci.ece.zen.utils.HashtableOverflowException{
        super.put( key , mem );
    }

    public ScopedMemory getWaiter( int key ){
        return (ScopedMemory) super.get( key );
    }
}
