package edu.uci.ece.zen.orb;

import javax.realtime.*;

/**
 * Based on edu.uci.ece.zen.utils.Hashtable but the keytable is different
 */
public class WaiterRegistry extends edu.uci.ece.zen.utils.ActiveDemuxTable
{
    public WaiterRegistry(){
    }

    public void registerWaiter( int key , ScopedMemory mem ){
        super.bind( (long)key , mem );
    }

    public ScopedMemory getWaiter( int key ){
        return (ScopedMemory) super.mapEntry( super.find((long)key));
    }

    public void remove( int idx ){
        super.unbind( super.find(idx) );
    }
}
