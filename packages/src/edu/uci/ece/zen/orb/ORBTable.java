package edu.uci.ece.zen.orb;

import javax.realtime.*;
import edu.uci.ece.zen.utils.*;
import java.lang.reflect.*;

/**
 * This class keeps track of all the ORB's that are running. When a user
 * calls ORB.init with a certain name, they should get back the same ORB
 * reference.
 *
 * @author Krishna Raman
 * @version $Revision: 1.3 $ $Date: 2004/02/25 08:15:19 $
 */
public class ORBTable {

    private static int maxNumOrbs;
    private static long orbMemoryRegionSize;

    private static ObjectCache orbFacades;
    private static ObjectCache orbMemoryRegions;
    
    private static Hashtable orbTable;

    static{
        //print startup message
        if( true )
            System.out.println( ZenProperties.zenStartupMessage );

        if( ZenProperties.dbg )
            ZenProperties.logger.log( 
                    edu.uci.ece.zen.utils.Logger.INFO , 
                    "edu.uci.ece.zen.orb.ORBTable" , 
                    "static{}" , 
                    "Now creating and caching ORB Facades and ORB Scoped regions." );
        try{
            //create space of ORBs
            maxNumOrbs = Integer.parseInt( ZenProperties.getGlobalProperty( "edu.uci.ece.zen.orb.maxNum" , "10" ) );
            orbMemoryRegionSize = Long.parseLong( ZenProperties.getGlobalProperty( "edu.uci.ece.zen.orb.memoryRegionSize" , "5242880" ) );

            Class[] memRegionConstrArgs = new Class[]{ Long.class , Long.class };
            Constructor memRegionConstr = LTMemory.class.getConstructor( memRegionConstrArgs ); 
            Object[] memRegionArgs = new Object[]{ new Long(orbMemoryRegionSize) , new Long(orbMemoryRegionSize) };

            Object[] initArgs = new Object[]{ new Integer(maxNumOrbs) };
            Class[] constrArgs = new Class[]{ Integer.class };
            Constructor cacheConstructor = ObjectCache.class.getConstructor( constrArgs );
            orbFacades = (ObjectCache) ImmortalMemory.instance().newInstance( cacheConstructor , initArgs ); 
            orbMemoryRegions = (ObjectCache) ImmortalMemory.instance().newInstance( cacheConstructor , initArgs );

            for( int i=0;i<maxNumOrbs;i++ ){
                orbFacades.put( (ORB) ImmortalMemory.instance().newInstance( edu.uci.ece.zen.orb.ORB.class ) );
                //KLUDGE: have to decide the initial size of memory region later. (KR 10/23/03)
                orbMemoryRegions.put( (LTMemory) ImmortalMemory.instance().newInstance( memRegionConstr , memRegionArgs ) );
            }

            //init orb table
            Constructor orbTableConstructor = Hashtable.class.getConstructor( constrArgs );
            orbTable = (Hashtable) ImmortalMemory.instance().newInstance( orbTableConstructor , initArgs );

            if( ZenProperties.dbg )
                ZenProperties.logger.log( 
                        edu.uci.ece.zen.utils.Logger.INFO , 
                        "edu.uci.ece.zen.orb.ORBTable" , 
                        "static{}" , 
                        "ORB facades, initial memory regions, and orbtable have been init'd" );
        }catch( Exception e ){
            ZenProperties.logger.log( 
                    edu.uci.ece.zen.utils.Logger.FATAL , 
                    "edu.uci.ece.zen.orb.ORBTable" , 
                    "static{}" , 
                    "Fatal exception occured while initializing ZEN. Stack trace follows:" );
            e.printStackTrace();
        }
    }
    public static synchronized ORB find(String name) {
        return (ORB) orbTable.get(name);		
    }

    public static void hash(String orbId, ORB orb) {
        try{
            orbTable.put(orbId, orb);
        }catch( HashtableOverflowException htoe ){
            ZenProperties.logger.log( 
                    edu.uci.ece.zen.utils.Logger.FATAL , 
                    "edu.uci.ece.zen.orb.ORBTable" , 
                    "hash("+orbId+","+orb.toString()+");" , 
                    "Trying to add more orbs to the orb table than # of slots available." );
        }
    }

    public static void remove(ORB orb) {
        orbTable.remove(orb.id());
    }
}
