package edu.uci.ece.zen.poa;

import edu.uci.ece.zen.utils.ZenProperties;

final public class ActiveDemuxServantTable extends edu.uci.ece.zen.utils.Hashtable{

    private static String maxCapacityProperty = "poa.servant_demux_table.capcity";
    public static String defaultMaxCapacity = "100";
    private static int maxCapacity;

    static {
        maxCapacity = Integer.parseInt(ZenProperties.getGlobalProperty( maxCapacityProperty, defaultMaxCapacity));
    }

    public ActiveDemuxServantTable(){
        super();
        init( ActiveDemuxServantTable.maxCapacity );
    }

    /**
     * bind the servant to a location in the table.
     * @param map The POAHashMap thats to be added to the table.
     * @return int The location of the POAHashMap in the table.
     */
    public synchronized int bind(POAHashMap map) throws edu.uci.ece.zen.utils.HashtableOverflowException{
        put( map.objectID() , map );
        return getTableEntry( map.objectID() );
    }

    /**
     * This method returns the location of the object in the POA map if present or else returns -1.
     * @param oid The object_id the object.
     * @return int The location of the object.
     */
    public int find(byte[] oid) {
        return getTableEntry( oid );
    }

    /**
     * remove the object id from the POA map
     * @param oid The objectId
     * @return boolean true if remove was successful
     */
    public synchronized boolean unbind( byte[] oid ) {
        return remove( oid );
    }

    /**
     * Returns the POAHashMap presnt at the given location
     * @param place The location of the POAHashMap in the table.
     * @return POAHashMap
     */
    public POAHashMap mapEntry(int place) {
        return (POAHashMap) getEntryAtIndex( place );
    }

    /**
     * Returns true if the objcect at the given location is active elase false.
     * @param index The location in the table.
     * @return boolean
     */
    public boolean isActive(int index) {
        return ((POAHashMap)getEntryAtIndex(index)).isActive();
    }
}
