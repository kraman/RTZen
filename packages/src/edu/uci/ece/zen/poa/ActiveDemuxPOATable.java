package edu.uci.ece.zen.poa;

import edu.uci.ece.zen.utils.ZenProperties;

final public class ActiveDemuxPOATable extends edu.uci.ece.zen.utils.Hashtable{

    private static String maxCapacityProperty = "poa.poa_demux_table.capcity";
    private static String defaultMaxCapacity = "10";
    private static int maxCapacity;

    static {
        maxCapacity = Integer.parseInt(ZenProperties.getGlobalProperty( maxCapacityProperty, defaultMaxCapacity));
    }

    ActiveDemuxPOATable(){
        super();
        init( ActiveDemuxPOATable.maxCapacity );
    }

    /**
     * Bind the POA to a location in the Active Demultiplexing Table.
     * @param oid The object id of the object.
     * @param poa The POA which hosts the object.
     * @return ActiveDemuxLoc The ActiveDemuxLoc object that indicates the location of the object.
     */
    public synchronized void bind(String oid, POA poa) throws edu.uci.ece.zen.utils.HashtableOverflowException{
        put( oid , poa );
    }

    /**
     * This method returns the location of the object in the POA map if present or else returns -1.
     * @param name The object_id the object.
     * @return int The location of the object.
     */
    public int find(String name) {
        return getTableEntry( name );
    }

    /**
     * Returns the genCountpresent at the given location.
     * @param place The location of the object.
     * @return TableEntry The entry at that particular location.
     */
    public POA mapEntry( int place ) {
        return (POA) getEntryAtIndex( place );
    }
}
